package com.gewara.untrans.ticket.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.commons.sign.Sign;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.DaoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.web.action.inner.util.TicketRemoteUtil;
import com.gewara.xmlbind.ticket.MpiSeat;
import com.gewara.xmlbind.ticket.MpiSeatList;
import com.gewara.xmlbind.ticket.RemoteLockSeat;
import com.gewara.xmlbind.ticket.SynchPlayItem;
import com.gewara.xmlbind.ticket.SynchPlayItemList;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;
import com.gewara.xmlbind.ticket.TicketRoom;
import com.gewara.xmlbind.ticket.TicketRoomList;
import com.gewara.xmlbind.ticket.TicketRoomSeatList;
import com.gewara.xmlbind.ticket.WDOrderList;
import com.gewara.xmlbind.ticket.WdOrder;
import com.gewara.xmlbind.ticket.WdParam;

@Service("remoteTicketService")
public class RemoteTicketServiceImpl extends AbstractSynchBaseService implements RemoteTicketService, InitializingBean {
	private int TICKET_TIMEOUT = 120000;
	private String ticketApiUrl4All;
	private String ticketApiUrl4Pnx;
	@Value("${ticket.appkey}")
	private String appkey;
	@Value("${ticket.secretCode}")
	private String secretCode;
	@Override
	public void afterPropertiesSet() throws Exception {
		ticketApiUrl4All = config.getString("ticketApiUrl");
		ticketApiUrl4Pnx = config.getString("ticketApi4Pnx");
	}
	private String getApiUrl(String opentype){
		if(StringUtils.equals(opentype, OpiConstant.OPEN_PNX)){
			return ticketApiUrl4Pnx;
		}
		return ticketApiUrl4All;
	}
	@Override
	protected HttpResult getRequestResult(String url, Map<String, String> params, int timeount) {
		params.put("appkey", appkey);
		params.put("sign", Sign.signMD5(params, secretCode));
		HttpResult result = super.getRequestResult(url, params, TICKET_TIMEOUT);
		if(Config.isDebugEnabled()){
			dbLogger.warn("url:" + url + ", params:" + params + ", return:" + result.getResponse());
		}
		return result;
	}

	@Override
	public ErrorCode<TicketRemoteOrder> remoteLockSeat(TicketOrder order, String seqno, String mobile, List<String> seatList, List<Integer> priceList, String playtime){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.lockOrderUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", order.getId() + "");
		params.put("seqno", seqno);
		params.put("mobile", mobile);
		params.put("seatLabel", StringUtils.join(seatList, ","));
		params.put("priceLabel", StringUtils.join(priceList, ","));
		params.put("playtime", playtime);
		return getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
	}
	@Override
	public ErrorCode<TicketRemoteOrder> createRemoteOrder(TicketOrder order, String seqno, String mobile, List<String> seatList){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.createRemoteOrderUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", order.getId()+"");
		params.put("seqno", seqno);
		params.put("mobile", mobile);
		params.put("seatLabel", StringUtils.join(seatList, ","));
		ErrorCode<TicketRemoteOrder> code = getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
		if(!code.isSuccess()){
			ErrorCode<TicketRemoteOrder> orderCode = getRemoteOrder(order, false);
			if(!orderCode.isSuccess()) return code;
			return orderCode;
		}
		return code;
	}
	
	@Override
	public ErrorCode<TicketRemoteOrder> remoteUnLockSeat(TicketOrder order){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.unLockSeatUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", order.getId() + "");
		ErrorCode<TicketRemoteOrder> code = getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<TicketRemoteOrder> remoteFixOrder(TicketOrder order, String seqno, String mobile, int unitPrice, List<String> seatList, List<Integer> priceList, String playtime){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.fixOrderUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", order.getId()+"");
		params.put("seqno", seqno);
		params.put("mobile", mobile);
		params.put("unitPrice", unitPrice+"");
		params.put("seatLabel", StringUtils.join(seatList, ","));
		params.put("priceLabel", StringUtils.join(priceList, ","));
		params.put("playtime", playtime);
		ErrorCode<TicketRemoteOrder> code = getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<TicketRemoteOrder> remoteCancelOrder(TicketOrder order, String description){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.cancelOrderUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", order.getId() + "");
		params.put("description", description);
		ErrorCode<TicketRemoteOrder> code = getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<TicketRemoteOrder> getRemoteOrder(TicketOrder order, boolean forceRefresh){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.getRemoteOrderUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", order.getId()+"");
		params.put("forceRefresh", String.valueOf(forceRefresh));
		ErrorCode<TicketRemoteOrder> code = getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	@Override
	public ErrorCode<TicketRemoteOrder> checkRemoteOrder(TicketOrder order){
		String url = getApiUrl(order.getCategory()) + TicketRemoteUtil.checkOrderUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("orderid", "" + order.getId());
		params.put("forceRefresh", "false");
		ErrorCode<TicketRemoteOrder> code = getObject(TicketRemoteOrder.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<String> getRemoteLockSeat(OpenPlayItem opi){
		String url = getApiUrl(opi.getOpentype()) + TicketRemoteUtil.lockSeatUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("seqno", opi.getSeqNo());
		ErrorCode<RemoteLockSeat> code = getObject(RemoteLockSeat.class, url, params, TICKET_TIMEOUT);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		RemoteLockSeat response = code.getRetval();
		return ErrorCode.getSuccessReturn(response.getSeatText());
	}
	
	@Override
	public ErrorCode<String> getLockSeatListFromCache(OpenPlayItem opi){
		String url = getApiUrl(opi.getOpentype()) + TicketRemoteUtil.cacheSeatUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("seqno", opi.getSeqNo());
		ErrorCode<RemoteLockSeat> code = getObject(RemoteLockSeat.class, url, params, TICKET_TIMEOUT);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		RemoteLockSeat response = code.getRetval();
		return ErrorCode.getSuccessReturn(response.getSeatText());
	}
	
	@Override
	public ErrorCode<List<SynchPlayItem>> getRemotePlayItemListByUpdatetime(Timestamp updatetime, Long cinemaid){
		String url = ticketApiUrl4All + TicketRemoteUtil.playItemUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("updatetime", DateUtil.format(updatetime, "yyyy-MM-dd HH:mm:ss"));
		if(cinemaid != null) params.put("cinemaid", String.valueOf(cinemaid));
		ErrorCode<List<SynchPlayItem>> code = getObjectList(SynchPlayItemList.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<List<SynchPlayItem>> getRemotePlayItemList(Cinema cinema, Date playdate){
		String url = ticketApiUrl4All + TicketRemoteUtil.cinemaPlayItemUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("cinemaid", ""+cinema.getId());
		params.put("playdate", DateUtil.formatDate(playdate));
		ErrorCode<List<SynchPlayItem>> code = getObjectList(SynchPlayItemList.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<List<TicketRoom>> getRemoteRoomList(Cinema cinema){
		String url = ticketApiUrl4All + TicketRemoteUtil.roomUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("cinemaid", cinema.getId()+"");
		ErrorCode<List<TicketRoom>> code = getObjectList(TicketRoomList.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public ErrorCode<TicketRoomSeatList> getRemoteRoomSeatList(CinemaRoom room){
		String url = ticketApiUrl4All + TicketRemoteUtil.roomSeatUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("cinemaid", room.getCinemaid()+"");
		params.put("roomnum", room.getNum());
		ErrorCode<TicketRoomSeatList> code = getObject(TicketRoomSeatList.class, url, params, TICKET_TIMEOUT);
		return code;
	}
	
	@Override
	public List<Integer> getPriceList(TicketOrder order, int size){
		List<Integer> priceList = new ArrayList<Integer>();
		for(int i=0;i<size;i++){
			priceList.add(order.getCostprice());
		}
		return priceList;
	}

	@Override
	public ErrorCode<Integer> featurePrice(String seqno, Integer costprice) {
		if(costprice == null) return ErrorCode.getFailure("成本价设置错误！");
		String url = ticketApiUrl4All + TicketRemoteUtil.featurePriceUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("seqno", seqno);
		params.put("costprice", String.valueOf(costprice));
		ErrorCode<String> code = getRemoteResult(url, params, TICKET_TIMEOUT);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return ErrorCode.getSuccessReturn(costprice);
	}
	
	@Override
	public ErrorCode<TicketRemoteOrder> backRemoteOrder(Long orderid){
		if(orderid == null) return ErrorCode.getFailure("订单号不能为空！");
		String url = ticketApiUrl4All + TicketRemoteUtil.backRemoteOrderUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("orderid", String.valueOf(orderid));
		return getObject(TicketRemoteOrder.class, url, params, HttpTimeout.LONG_REQUEST);
	}
	
	@Override
	public ErrorCode<Integer> getPlanSiteStatistc(String seqno){
		if(StringUtils.isBlank(seqno)) return ErrorCode.getFailure("场次编号不能为空！");
		String url = ticketApiUrl4All + TicketRemoteUtil.planSiteStatistcUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("seqno", seqno);
		ErrorCode<Integer> code = getRemoteCount(url, params, TICKET_TIMEOUT);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return code;
	}
	@Override
	public ErrorCode<TicketRemoteOrder> getWdRemoteOrder(String wdOrderId, String seqno, Long cinemaId) {
		if(StringUtils.isBlank(wdOrderId)) return ErrorCode.getFailure("订单号不能为空！");
		String url = ticketApiUrl4All + TicketRemoteUtil.getWdRemoteOrderUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("seqno", seqno);
		params.put("wdOrderId", wdOrderId);
		params.put("gcid", ""+cinemaId);
		return getObject(TicketRemoteOrder.class, url, params, HttpTimeout.LONG_REQUEST);
	}
	@Override
	public ErrorCode<TicketRemoteOrder> createWdRemoteOrder(String seqno, String wdOrderId, Long orderid, Long cinemaId) {
		String url = ticketApiUrl4All + TicketRemoteUtil.createWdRemoteOrderUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("seqno", seqno);
		params.put("orderid", String.valueOf(orderid));
		params.put("wdOrderId", wdOrderId);
		params.put("gcid", ""+cinemaId);
		return getObject(TicketRemoteOrder.class, url, params, HttpTimeout.LONG_REQUEST);
	}
	@Override
	public boolean unlockWandaOrder(String wdOrderId, Long cinemaId) {
		String url = ticketApiUrl4All + TicketRemoteUtil.unlockWdOrderUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("wdOrderId", wdOrderId);
		params.put("gcid", ""+cinemaId);
		ErrorCode<String> result = getRemoteResult(url, params, HttpTimeout.LONG_REQUEST);
		return result.isSuccess()? Boolean.valueOf(result.getRetval()):false;
	}
	@Override
	public ErrorCode<String> getWdUserId(String memberUkey){
		String url = ticketApiUrl4All + TicketRemoteUtil.getWdMemberUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberUkey", memberUkey);
		ErrorCode<String> result = getRemoteResult(url, params, HttpTimeout.LONG_REQUEST);
		return result;
	}
	@Override
	public ErrorCode<WdParam> getWdParam(String memberUkey, String seqno) {
		String url = ticketApiUrl4All + TicketRemoteUtil.getWdWapParamUrl;
		Map<String, String> params = new HashMap<String, String>();
		params.put("memberUkey", memberUkey);
		params.put("seqno", seqno);
		return getObject(WdParam.class, url, params, HttpTimeout.LONG_REQUEST);
	}
	
	private Long SHYC_ROOM1 = 334736L;	//TODO:上海影城1号厅hack

	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	@Override
	public ErrorCode<List<MpiSeat>> getMpiSeat(MoviePlayItem mpi){
		if(SHYC_ROOM1.equals(mpi.getRoomid())){
			List<RoomSeat> seatList = daoService.getObjectListByField(RoomSeat.class, "roomid", SHYC_ROOM1);
			List<MpiSeat> result = new ArrayList<MpiSeat>(seatList.size());
			for(RoomSeat seat: seatList){
				MpiSeat mpiSeat = new MpiSeat();
				mpiSeat.setLineno(seat.getLineno());
				mpiSeat.setRankno(seat.getRankno());
				mpiSeat.setSeatline(seat.getSeatline());
				mpiSeat.setSeatrank(seat.getSeatrank());
				result.add(mpiSeat);
			}
			return ErrorCode.getSuccessReturn(result);
		}else{
			String url = ticketApiUrl4Pnx + TicketRemoteUtil.getMpiSeatUrl;
			Map<String,String> params = new HashMap<String, String>();
			params.put("seqno", mpi.getSeqNo());
			return getObjectList(MpiSeatList.class, url, params, TICKET_TIMEOUT);
		}
	}
	
	@Override
	public ErrorCode<List<WdOrder>> getWDOrderList(Date addDate){
		String url =  getApiUrl(OpiConstant.OPEN_WD) + TicketRemoteUtil.getWdOrderList;
		Map<String,String> params = new HashMap<String, String>();
		params.put("adddate", DateUtil.formatDate(addDate));
		return getObjectList(WDOrderList.class, url, params, TICKET_TIMEOUT *2 );
	}
	
	@Override
	public ErrorCode<Integer> getRemoteMtxSellNum(MoviePlayItem mpi){
		String url = getApiUrl(mpi.getOpentype()) + TicketRemoteUtil.getMtxSellNumUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("mpid", String.valueOf(mpi.getId()));
		ErrorCode<Integer> code = getRemoteCount(url, params, TICKET_TIMEOUT);
		if(!code.isSuccess()) {
			return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		}
		return code;
	}
	
}
