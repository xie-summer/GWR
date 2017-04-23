package com.gewara.untrans.ticket.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.json.order.QryResponse;
import com.gewara.model.acl.User;
import com.gewara.model.common.JsonData;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.HotspotCinema;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.DaoService;
import com.gewara.service.OrderException;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.service.ticket.TicketSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.untrans.ticket.RemoteTicketService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.AsyncHttpUtils;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpResultCallback;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.inner.util.TicketRemoteUtil;
import com.gewara.xmlbind.ticket.SynchPlayItem;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

@Service("ticketOperationService")
public class TicketOperationServiceImpl implements TicketOperationService, InitializingBean{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private AtomicInteger hitCount = new AtomicInteger();
	private Long starttime = System.currentTimeMillis();
	
	private ExecutorService executor = null;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired@Qualifier("orderMonitorService")
	private OrderMonitorService orderMonitorService;
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("ticketSynchService")
	private TicketSynchService ticketSynchService;
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@Autowired@Qualifier("remoteTicketService")
	private RemoteTicketService remoteTicketService;
	@Autowired@Qualifier("config")
	private Config config;
	@Override
	public ErrorCode<List<String>> updateLockSeatListAsynch(OpenPlayItem opi){
		List<String> result = new ArrayList<String>();
		if(opi.hasGewara()) {
			return ErrorCode.getSuccessReturn(result);
		}
		ErrorCode<List<String>> lockSeatList = getCachedRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT);
		if(!lockSeatList.isSuccess()){
			asynchUpdateSeatLock(opi, false, null);
			lockSeatList = updateRemoteLockSeatFromCache(opi);
			if(!lockSeatList.isSuccess()){
				lockSeatList = updateRemoteLockSeat(opi);
			}
		}
		return lockSeatList;
	}
	@Override
	public void asynchUpdateSeatLock(OpenPlayItem opi, boolean asynch, HttpResultCallback callback){
		if(opi.hasGewara()) return;
		String url = config.getAbsPath() + config.getString("ticketPath") + TicketRemoteUtil.asynchLockOrderUrl;
		Map<String, String> params = new HashMap<String, String>();
		long cur = System.currentTimeMillis();
		params.put("t", ""+cur);
		params.put("seqno", opi.getSeqNo());
		params.put("cinemaid", "" + opi.getCinemaid());
		params.put("validtime", "" + opi.getPlaytime().getTime());
		params.put("checkStr", StringUtil.md5(opi.getSeqNo() + cur + config.getString("asynchTicketPriKey"), 8));
		if(asynch){
			AsyncHttpUtils.getUrlAsString(url, params, callback);
		}else{
			HttpResult result = HttpUtils.getUrlAsString(url, params);
			if(callback!=null) callback.processResult(result);
		}
	}
	@Override
	public ErrorCode<List<String>> updateRemoteLockSeat(OpenPlayItem opi){
		return updateRemoteLockSeat(opi, OpiConstant.SECONDS_UPDATE_SEAT, true);
	}
	@Override
	public ErrorCode<List<String>> getCachedRemoteLockSeat(OpenPlayItem opi, int expireSeconds){
		QryResponse res = getQryResponse(opi.getMpid());
		if(res==null || res.isExpired(expireSeconds)){
			return ErrorCode.getFailure("座位过期");
		}
		updateSeatCacheHit(opi.getMpid());
		return ErrorCode.getSuccessReturn(getLockSeatFromStr(res.getResponse()));
	}
	@Override
	public ErrorCode<List<String>> updateRemoteLockSeatFromCache(OpenPlayItem opi){
		if(opi.hasGewara()){
			List<String> result = new ArrayList<String>(0);
			return ErrorCode.getSuccessReturn(result);
		}
		ErrorCode<String> code = remoteTicketService.getLockSeatListFromCache(opi);
		if(code.isSuccess()) {
			QryResponse res = getQryResponse(opi.getMpid());
			if(res==null){
				res = new QryResponse(opi.getMpid());
			}
			updateResponse(res, code.getRetval());
			return ErrorCode.getSuccessReturn(getLockSeatFromStr(code.getRetval()));	
		}else{
			return ErrorCodeConstant.NOT_FOUND;
		}
	}
	@Override
	public ErrorCode<List<String>> updateRemoteLockSeat(OpenPlayItem opi, int expireSeconds, boolean refresh) {
		if(!opi.hasGewara()){
			QryResponse res = getQryResponse(opi.getMpid());
			if(res!=null && !refresh && !res.isExpired(expireSeconds)){
				updateSeatCacheHit(opi.getMpid());
				return ErrorCode.getSuccessReturn(getLockSeatFromStr(res.getResponse()));	
			}
			ErrorCode<String> code = remoteTicketService.getRemoteLockSeat(opi);
			if(!code.isSuccess()) {
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			if(res==null){
				res = new QryResponse(opi.getMpid());
			}
			updateResponse(res, code.getRetval());
			return ErrorCode.getSuccessReturn(getLockSeatFromStr(code.getRetval()));	
		}else{
			List<String> result = new ArrayList<String>(0);
			return ErrorCode.getSuccessReturn(result);
		}
	}
	private void updateSeatCacheHit(Long mpid){
		if(Config.isDebugEnabled()){
			dbLogger.warn("HitLockSeatFromCache:" + mpid);
		}
		int count = hitCount.incrementAndGet();
		if(count >= 200){//统计命中率
			hitCount.set(0);
			Map<String, String> entry = new HashMap<String, String>();
			entry.put("start", "" + starttime);
			starttime = System.currentTimeMillis();
			entry.put("endtime", "" + starttime);
			entry.put("hittype", "qryTicket");
			monitorService.addSysLog(SysLogType.hitCache, entry);
		}
	}
	private List<String> getLockSeatFromStr(String seatStr){
		if(StringUtils.isBlank(seatStr)) return new ArrayList<String>(0);
		return Arrays.asList(StringUtils.split(seatStr, ","));
	}
	@Override
	public ErrorCode lockRemoteSeat(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList) {
		if(!opi.hasGewara()){
			List<String> seatStrList  = getSeatId(seatList);
			List<Integer> priceList = remoteTicketService.getPriceList(order, order.getQuantity());
			String playtime = DateUtil.format(opi.getPlaytime(), "HHmm");
			ErrorCode<TicketRemoteOrder> code = remoteTicketService.remoteLockSeat(order, opi.getSeqNo(), order.getMobile(), seatStrList, priceList, playtime);
			if(!code.isSuccess()) {
				if(StringUtils.equals(code.getErrcode(), ApiConstant.CODE_SEAT_OCCUPIED)){
					updateRemoteLockSeat(opi);
				}
				return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
			}
			order.setStatus(OrderConstant.STATUS_NEW);
			daoService.saveObject(order);
			addLockSeatToQryResponse(opi.getMpid(), seatList);
		}
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode createRemoteOrder(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList) {
		if(!opi.hasGewara()){
			List<String> seatStrList  = getSeatId(seatList);
			ErrorCode<TicketRemoteOrder> code = remoteTicketService.createRemoteOrder(order, opi.getSeqNo(), order.getMobile(), seatStrList);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		}
		return ErrorCode.SUCCESS;
	}

	@Override
	public ErrorCode cancelRemoteTicket(OpenPlayItem opi, TicketOrder order, Long userid) {
		if(!opi.hasGewara()){
			ErrorCode<TicketRemoteOrder> result = remoteTicketService.getRemoteOrder(order, false);
			TicketRemoteOrder remoteOrder = result.getRetval();
			Map<String, String> changeMap = BeanUtil.getSimpleStringMap(remoteOrder);
			orderMonitorService.addOrderChangeLog(order.getTradeNo(), "订单退票-1", changeMap, userid);

			ErrorCode<TicketRemoteOrder> code = remoteTicketService.remoteCancelOrder(order, OpiConstant.getParnterText(opi.getOpentype()) + "退票-1");
			ChangeEntry change = new ChangeEntry(remoteOrder);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());

			remoteOrder = code.getRetval();
			changeMap = change.getChangeMap(remoteOrder);
			if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());

			order.setHfhpass(null);

			order.addChangehis("hfhReturn", changeMap.toString());
			changeMap.put("changeUser", ""+userid);
			daoService.saveObject(order);
			orderMonitorService.addOrderChangeLog(order.getTradeNo(), "订单退票", changeMap, userid);
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode releasePaidFailureOrderSeat(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList) {
		try {
			if(!StringUtils.equals(opi.getOpentype(), OpiConstant.OPEN_GEWARA)){
				ErrorCode<TicketRemoteOrder> code = remoteTicketService.remoteUnLockSeat(order);
				if(!code.isSuccess()) throw new OrderException(code.getErrcode(), code.getMsg());
				removeLockSeatFromQryResponse(opi.getMpid(), seatList);
			}
			Timestamp validtime = new Timestamp(System.currentTimeMillis() - 1000);
			for(SellSeat oseat: seatList){
				if(oseat.getOrderid().equals(order.getId())){
					oseat.setValidtime(validtime);
					daoService.saveObject(oseat);
				}
			}
			return ErrorCode.SUCCESS;
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return ErrorCode.getFailure(e.getCode(), e.getMsg());
		}
	}
	@Override
	public ErrorCode<Boolean> unlockRemoteSeat(TicketOrder order, List<SellSeat> seatList) {
		try {
			if(!StringUtils.equals(order.getCategory(), OpiConstant.OPEN_GEWARA)){
				ErrorCode<TicketRemoteOrder> code = remoteTicketService.remoteUnLockSeat(order);
				if(!code.isSuccess()) {
					return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
				}
				if(seatList==null){
					seatList = ticketOrderService.getOrderSeatList(order.getId());
				}
				removeLockSeatFromQryResponse(order.getMpid(), seatList);				
				return ErrorCode.getSuccessReturn(true);
			}else{
				return ErrorCode.getSuccessReturn(false);
			}
		} catch (Exception e) {
			return ErrorCode.getSuccessReturn(false);
		}
	}

	@Override
	public ErrorCode<TicketRemoteOrder> setAndFixRemoteOrder(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList) throws OrderException{
		if(!opi.hasGewara()){
			List<Integer> priceList = remoteTicketService.getPriceList(order, order.getQuantity());
			List<String> seatStrList  = getSeatId(seatList);
			String playtime = DateUtil.format(opi.getPlaytime(), "HHmm");
			ErrorCode<TicketRemoteOrder> code = remoteTicketService.remoteFixOrder(order, opi.getSeqNo(), order.getMobile(), order.gainRealUnitprice(), seatStrList, priceList, playtime);
			if(!code.isSuccess()) throw new OrderException(code.getErrcode(), code.getMsg());
			TicketRemoteOrder ticketRemoteOrder = code.getRetval();
			addLockSeatToQryResponse(opi.getMpid(), seatList);
			return ErrorCode.getSuccessReturn(ticketRemoteOrder);
		}
		return ErrorCode.getFailure("非远程订单！");
	}
	@Override
	public void addLockSeatToQryResponse(Long mpid, List<SellSeat> seatList) {
		QryResponse res = getQryResponse(mpid);
		if(res==null){//为空，则不需要加入，下次会自动更新
			return;
		}
		List<String> seatStrList = new ArrayList<String>();
		if(StringUtils.isNotBlank(res.getResponse())){
			seatStrList = new ArrayList<String>(Arrays.asList(StringUtils.split(res.getResponse(),",")));
		}
		for(SellSeat seat: seatList){
			String seatLabel = seat.getSeatline() + ":" + seat.getSeatrank();
			if(!seatStrList.contains(seatLabel)){
				seatStrList.add(seatLabel);
			}
		}
		updateResponse(res, StringUtils.join(seatStrList, ","));
	}
	@Override
	public ErrorCode<TicketRemoteOrder> getRemoteOrder(TicketOrder order, boolean forceRefresh) {
		ErrorCode<TicketRemoteOrder> code = remoteTicketService.getRemoteOrder(order, forceRefresh);
		return code;
	}
	@Override
	public ErrorCode<TicketRemoteOrder> checkRemoteOrder(TicketOrder order) {
		ErrorCode<TicketRemoteOrder> code = remoteTicketService.checkRemoteOrder(order);
		return code;
	}
	
	private void removeLockSeatFromQryResponse(Long mpid, List<SellSeat> seatList) {
		QryResponse res = getQryResponse(mpid);
		if(res==null){//为空，则不更新，肯定会重新查询
			return;
		}
		List<String> seatStrList = new ArrayList<String>();
		if(StringUtils.isNotBlank(res.getResponse())){
			seatStrList = new ArrayList<String>(Arrays.asList(StringUtils.split(res.getResponse(),",")));
		}
		for(SellSeat seat: seatList){
			seatStrList.remove(seat.getSeatline() + ":" + seat.getSeatrank());
		}
		updateResponse(res, StringUtils.join(seatStrList, ","));
	}
	
	@Override
	public ErrorCode updateCostPrice(String seqNo, Integer costprice){
		ErrorCode<Integer> code = remoteTicketService.featurePrice(seqNo, costprice);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode backRemoteOrder(User user, TicketOrder order, OpenPlayItem opi){
		if(opi.hasGewara()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "非远程订单不需要影院退票！");
		ErrorCode<TicketRemoteOrder> code = remoteTicketService.backRemoteOrder(order.getId());
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
		TicketRemoteOrder remoteOrder = code.getRetval();
		if(!remoteOrder.hasStatus(OrderConstant.REMOTE_STATUS_CANCEL)) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "退票失败:" + remoteOrder.getStatus());
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "订单退票-影院退票", JsonUtils.writeObjectToJson(remoteOrder), user.getId());
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode updateMoviePlayItem(UpdateMpiContainer container, Long cinemaid, List<String> msgList, int notUpdateWithMin){
		if(cinemaid==null) return ErrorCode.getFailure("影院ID不能为空！");
		String jsonKey = JsonDataKey.KEY_SYNCH_MOVIEPLAYITEM + "_" + cinemaid;
		JsonData jsonData = daoService.getObject(JsonData.class, jsonKey);
		Timestamp updatetime = null;
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(jsonData == null){
			jsonData = new JsonData(jsonKey);
			updatetime = cur;
			jsonData.setTag("synch");
			jsonData.setValidtime(DateUtil.parseTimestamp("2032-01-01 00:00:00"));
		}else{
			Map<String, String> dataMap = JsonUtils.readJsonToMap(jsonData.getData());
			updatetime = DateUtil.parseTimestamp(dataMap.get("updatetime"));
		}
		
		if(DateUtil.addMinute(updatetime, notUpdateWithMin).compareTo(cur) > 0){
			return ErrorCode.getFailure("在" + notUpdateWithMin + "内已经更新，本次忽略！");
		}
		ErrorCode<List<SynchPlayItem>> code = remoteTicketService.getRemotePlayItemListByUpdatetime(DateUtil.addMinute(updatetime, -20), cinemaid);//倒退20分
		if(!code.isSuccess()){
			String msg = "更新排片错误：" + code.getMsg() + ", code:" + code.getErrcode() + ", res:" + code.getRetval();
			msgList.add(msg);
			return ErrorCode.getFailure(msg);
		}
		List<SynchPlayItem> synchPlayItemList = code.getRetval();
		List<Long> idList = BeanUtil.getBeanPropertyList(synchPlayItemList, Long.class, "cinemaid", true);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, idList);
		Map<Long, Cinema> cinemaMap = BeanUtil.beanListToMap(cinemaList, "id");
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinemaid);
		Map<String, CinemaRoom> roomMap = BeanUtil.beanListToMap(roomList, "num");
		for (SynchPlayItem synchPlayItem : synchPlayItemList) {
			CinemaRoom room = roomMap.get(synchPlayItem.getRoomnum());
			if(room==null){
				String msg = "影片影厅未设置：cinemaid：" + synchPlayItem.getCinemaid() + ", roomnum:" + synchPlayItem.getRoomnum();
				msgList.add(msg);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, msg);
				continue;
			}
			Cinema cinema = cinemaMap.get(synchPlayItem.getCinemaid());
			MoviePlayItem mpi = mcpService.getMpiBySeqNo(synchPlayItem.getMpiseq());
			ticketSynchService.updateSynchPlayItem(container, synchPlayItem, mpi, cinema, room, msgList);
		}
		Map<String, String> dataMap = JsonUtils.readJsonToMap(jsonData.getData());
		dataMap.put("updatetime", DateUtil.format(cur, "yyyy-MM-dd HH:mm:ss"));
		jsonData.setData(JsonUtils.writeObjectToJson(dataMap));
		daoService.saveObject(jsonData);
		return ErrorCode.SUCCESS;
	}
	

	@Override
	public void updateMoviePlayItem(Long cinemaid, Date playdate, List<String> msgList){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		ErrorCode<List<SynchPlayItem>> code = remoteTicketService.getRemotePlayItemList(cinema, playdate);
		if(!code.isSuccess()){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "更新排片：cinemaid:" + cinemaid + ",msg" + code.getMsg());
			msgList.add("更新排片错误：" + code.getMsg() + ", code:" + code.getErrcode() + ", res:" + code.getRetval());
			return;
		}
		List<SynchPlayItem> synchPlayItemList = code.getRetval();
		String query = "from MoviePlayItem where cinemaid = ? and playdate = ? and seqNo is not null";
		List<MoviePlayItem> oldList = hibernateTemplate.find(query, cinemaid, playdate);
		Map<String, MoviePlayItem> oldMap = BeanUtil.beanListToMap(oldList, "seqNo");
		UpdateMpiContainer container = new UpdateMpiContainer();
		for (SynchPlayItem synchPlayItem : synchPlayItemList) {
			CinemaRoom room = mcpService.getRoomByRoomnum(cinema.getId(), synchPlayItem.getRoomnum());
			if(room==null){
				String msg = "更新排片错误：影厅未关联：" + cinema.getName() + ":" + synchPlayItem.getRoomnum();
				msgList.add(msg);
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, msg);
				continue;
			}
			MoviePlayItem item = oldMap.remove(synchPlayItem.getMpiseq());
			ticketSynchService.updateSynchPlayItem(container, synchPlayItem, item, cinema, room, msgList);
			
		}
		for(MoviePlayItem mpi: oldMap.values()){
			String msg = "删除排片: cinemaid:" + mpi.getCinemaid() + ", mpi:" + JsonUtils.writeObjectToJson(mpi);
			msgList.add(msg);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, msg);
			monitorService.saveDelLog(0L, mpi.getId(), mpi);
			daoService.removeObject(mpi);
		}
	}
	
	@Override
	public void preloadHotspotPmiCache(){
		List<Long> opiList = getHotspotOPI();
		final AtomicInteger count = new AtomicInteger();
		for(Long opid : opiList){
			final OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						updateLockSeatListAsynch(opi);
						int cur = count.incrementAndGet();
						if(cur%100==0){
							dbLogger.warn("同步热门场次座位:" + cur +", ID:" + opi.getMpid());				
						}
					}catch(Exception e){
						dbLogger.error("同步热门场次座位出错,ID:" + opi.getMpid(), e);
					}
				}
			});
			
		}
	}
	private Long lastLoadTime = null;
	private List<Long> hotopiList = null;
	/**
	 * 获取热点场次
	 * @return
	 */
	private List<Long> getHotspotOPI(){
		if(lastLoadTime!=null && lastLoadTime > System.currentTimeMillis() - DateUtil.m_hour){
			return hotopiList;
		}
		
		DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "o");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		query.add(Restrictions.gt("playtime", cur));
		query.setProjection(Projections.id());
		
		DetachedCriteria sub = DetachedCriteria.forClass(HotspotCinema.class, "h");
		sub.setProjection(Projections.property("h.cinemaid"));		
		query.add(Subqueries.propertyIn("o.cinemaid", sub));
		//query.add(Restrictions.or(Restrictions.gt("o.playtime", cur), Subqueries.propertyIn("o.cinemaid", sub)));
		
		List<Long> allOPI = hibernateTemplate.findByCriteria(query);
		hotopiList = new ArrayList<Long>(allOPI.size()/2);
		for(Long opid : allOPI){
			OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
			if(opi != null){
				if((DateUtil.getWeek(opi.getPlaytime())>5 || DateUtil.format(opi.getPlaytime(), "HH:ss").compareTo("18:00") > 0)  
						&& opi.isBooking()){
					hotopiList.add(opid);
				}
			}
		}
		lastLoadTime = System.currentTimeMillis();
		return hotopiList;
	}
	private static List<String> getSeatId(List<SellSeat> seatList){
		List<String> seatStrList = new ArrayList<String>();
		for(SellSeat seat: seatList){
			seatStrList.add(seat.getSeatline() + ":" + seat.getSeatrank());
		}
		return seatStrList;
	}

	private void updateResponse(QryResponse res, String lockSeat) {
		res.setResponse(lockSeat);
		res.setUpdatetime(System.currentTimeMillis());
		cacheService.set(CacheConstant.REGION_ONEDAY, res.getResid(), res);
	}
	
	private QryResponse getQryResponse(Long mpid){
		QryResponse res = (QryResponse) cacheService.get(CacheConstant.REGION_ONEDAY, "qryMpi" + mpid);
		return res;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		executor = Executors.newFixedThreadPool(10);
	}
}
