package com.gewara.web.action.api2mobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.ui.ModelMap;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.helper.ticket.PartnerPriceHelper;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.json.MovieMpiRemark;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.partner.PartnerService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.untrans.ticket.TicketQueueService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.api.BaseApiController;

public class ApiTicketBaseController extends BaseApiController{
	@Autowired@Qualifier("ticketOperationService")
	protected TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired@Qualifier("ticketQueueService")
	private TicketQueueService ticketQueueService;
	public void setTicketQueueService(TicketQueueService ticketQueueService) {
		this.ticketQueueService = ticketQueueService;
	}
	@Autowired@Qualifier("openPlayService")
	protected OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("partnerService")
	protected PartnerService partnerService;
	public void setPartnerService(PartnerService partnerService) {
		this.partnerService = partnerService;
	}
	@Autowired@Qualifier("goodsOrderService")
	protected GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("mcpService")
	protected MCPService mcpService;
	public void setMCPService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	protected ErrorCode addOpiListData(ApiUser partner, String citycode, Long cinemaid, Long movieid, Date playdate, ModelMap model){
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return ErrorCode.getFailure(ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner, citycode, cinemaid, movieid, playdate);
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.applyFilter(opiList);
		model.put("priceHelper", priceHelper);
		model.put("apiUser", partner);
		model.put("opiList", opiList);
		return ErrorCode.SUCCESS;
	}
	protected ErrorCode addOpmListData(ApiUser partner, String citycode, Long cinemaid, Date playdate, ModelMap model) {
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return ErrorCode.getFailure(ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<Movie> opmList = partnerService.getOpenMovieList(partner, citycode, cinemaid, playdate);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterMovie(opmList);
		mcpService.sortMoviesByMpiCount(citycode, opmList);
		model.put("opmList", opmList);
		model.put("generalmarkMap", getGeneralmarkMap(new HashSet(opmList)));
		return ErrorCode.SUCCESS;
	}

	protected ErrorCode addCinemaListData(ApiUser partner, String citycode, ModelMap model){
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return ErrorCode.getFailure(ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<Cinema> cinemaList = mcpService.getBookingCinemaList(citycode);
		List<Long> idList = BeanUtil.getBeanPropertyList(cinemaList, Long.class, "id", false);
		Map<Long, CinemaProfile> cpMap = daoService.getObjectMap(CinemaProfile.class, idList);
		model.put("cpMap", cpMap);
		model.put("cinemaList", cinemaList);
		return ErrorCode.SUCCESS;
	}
	protected ErrorCode addOpiSeatListData(OpenPlayItem opi, ApiUser partner, ModelMap model){
		if(opi == null) return ErrorCode.getFailure(ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		if(!opi.isOrder()) return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, OpiConstant.getStatusStr(opi));
		if(!opi.isOpenToPartner()) return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		if(opi.isUnShowToGewa()){
			if(apiMobileService.isGewaPartner(partner.getId())){
				return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场暂不对外开放！");
			}
		}
		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, booking.getMsg());
		}

		ErrorCode result = ticketQueueService.isPartnerAllowed(partner.getId(), opi.getCinemaid());
		if(!result.isSuccess()) return result;
		
		model.put("partner", partner);
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		model.put("priceHelper", priceHelper);
		
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(opi.getMpid());
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(opi.getMpid());
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		//采用异步方式调用
		ErrorCode<List<String>> lockSeatList = ticketOperationService.updateLockSeatListAsynch(opi);
		
		if(!lockSeatList.isSuccess()){
			return lockSeatList;
		}
		List<String> remoteLockList = lockSeatList.getRetval();
		model.put("hfhLockList", remoteLockList);
		
		Map<String, OpenSeat> seatMap = BeanUtil.beanListToMap(openSeatList, "position");
		String status = "";
		OpenSeat oseat = null;
		Map<Integer, String> lineMap = new HashMap<Integer, String>();
		Map<Integer, String> lineDataMap = new HashMap<Integer, String>();
		boolean isLoveSeat = false, isMobilePartner = apiMobileService.isGewaPartner(partner.getId());
		List<String> loveSeatList = new ArrayList<String>();
		for(int i=1; i<= room.getLinenum(); i++){
			List<String> seatRankList = new ArrayList<String>();
			List<String> seatRankDataList = new ArrayList<String>();
			for(int j=1; j<= room.getRanknum(); j++){
				String loveseatRank = "";
				oseat = seatMap.get(i + ":" + j);
				String loveInd = "0";
				if(oseat == null){
					status = "ZL"; //走廊
				}else{
					rowMap.put(i, oseat.getSeatline());
					loveInd = StringUtils.isBlank(oseat.getLoveInd())?"0":oseat.getLoveInd();
					if(remoteLockList.contains(oseat.getKey())){ 
						status = "LK"; //锁定
 						if(StringUtils.isNotBlank(oseat.getLoveInd()) 
							&& oseat.getLoveInd().compareTo("0")>0 && isMobilePartner){//若是情侣坐加到情侣坐列表中
							loveseatRank = oseat.getSeatline()+":"+oseat.getSeatrank();
							isLoveSeat=true;
						}
					}else if(StringUtils.isNotBlank(oseat.getLoveInd()) 
							&& oseat.getLoveInd().compareTo("0")>0){//情侣座直接锁定
						if(isMobilePartner){
							if(SeatConstant.STATUS_NEW.equals(seatStatusUtil.getFullStatus(oseat))){
								status = oseat.getSeatrank();
								//如果影院只卖出情侣座的一个座位，则这边展示的时候，都要锁定
								OpenSeat loveoseat2 = null;
								if(StringUtils.equals(oseat.getLoveInd(), "1")){
									if(j<room.getRanknum()) loveoseat2 = seatMap.get(i + ":" + (j+1));
								}else if(StringUtils.equals(oseat.getLoveInd(), "2")){
									if(j>1) loveoseat2 = seatMap.get(i + ":" + (j-1));
								}
								if(loveoseat2!=null){
									if(!SeatConstant.STATUS_NEW.equals(seatStatusUtil.getFullStatus(loveoseat2)) || remoteLockList.contains(loveoseat2.getKey())){
										status = "LK"; 
									}
								}
							}else{
								status = "LK"; //锁定
							}
							loveseatRank = oseat.getSeatline()+":"+oseat.getSeatrank();
							isLoveSeat=true;
						}else {
							status = "LK"; //锁定
						}
					}else if(SeatConstant.STATUS_NEW.equals(seatStatusUtil.getFullStatus(oseat))){
						status = oseat.getSeatrank();
					}else{
						status = "LK"; //锁定
					}
				}
				seatRankList.add(status);
				if(oseat==null){
					seatRankDataList.add(status);
				}else {
					String tmp = status;
					if(!StringUtils.equals(status, "LK")){
						tmp = "A";
					}
					seatRankDataList.add(oseat.getSeatrank()+"@"+tmp+"@"+loveInd);
				}
				if(StringUtils.isNotBlank(loveseatRank)) loveSeatList.add(loveseatRank);
			}
			lineMap.put(i, StringUtils.join(seatRankList, ","));
			lineDataMap.put(i, StringUtils.join(seatRankDataList, ","));
		}
		model.put("lineMap", lineMap);
		model.put("lineDataMap", lineDataMap);
		model.put("isLoveSeat", isLoveSeat);
		model.put("loveSeatList", StringUtils.join(loveSeatList, ","));
		model.put("opi", opi);
		model.put("room", room);
		model.put("rowMap", rowMap);
		List<MovieMpiRemark> remarkList = nosqlService.getMovieMpiRemarkList(opi.getMovieid(), opi.getCitycode(), 5);
		model.put("movieMpiRemark", remarkList);
		return ErrorCode.SUCCESS;
	}
	protected ErrorCode addOpiLockedSeatListData(OpenPlayItem opi, ApiUser partner, ModelMap model){
		if(!opi.isOrder()) return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, OpiConstant.getStatusStr(opi));
		if(!opi.isOpenToPartner()) return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		ErrorCode result = ticketQueueService.isPartnerAllowed(partner.getId(), opi.getCinemaid());
		if(!result.isSuccess()) return result;
		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, booking.getMsg());
		}
		model.put("partner", partner);
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		
		model.put("priceHelper", priceHelper);
		
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(opi.getMpid());
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(opi.getMpid());
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		//火凤凰占用座位
		ErrorCode<List<String>> lockSeatList = ticketOperationService.updateLockSeatListAsynch(opi);
		if(!lockSeatList.isSuccess()){
			return lockSeatList;
		}
		List<String> remoteLockList = lockSeatList.getRetval();
		Map<String/*seatline*/, List<String>> lineMap = new HashMap<String, List<String>>();
		List<String> seatRankList = null;
		for(OpenSeat oseat:openSeatList){
			if(remoteLockList.contains(oseat.getKey()) || 
					!SeatConstant.STATUS_NEW.equals(seatStatusUtil.getFullStatus(oseat)) ||
					(StringUtils.isNotBlank(oseat.getLoveInd()) && oseat.getLoveInd().compareTo("0")>0 )){//情侣座直接锁定
				seatRankList = lineMap.get(oseat.getSeatline());
				if(seatRankList == null) {
					seatRankList = new ArrayList<String>();
					lineMap.put(oseat.getSeatline(), seatRankList);
				}
				seatRankList.add(oseat.getSeatrank());
			}
		}
		List<String> lineStrList = new ArrayList<String>();
		for(String seatline:lineMap.keySet()){
			lineStrList.add(seatline + ":" + StringUtils.join(lineMap.get(seatline), ","));
		}
		model.put("lockStr", StringUtils.join(lineStrList, "@"));
		model.put("opi", opi);
		model.put("room", room);
		return ErrorCode.SUCCESS;
	}
	
	protected ErrorCode addOrder(ApiUser partner, Member member, String ukey, OpenPlayItem opi, String mobile, String seatLabel, ModelMap model){
		ErrorCode<String> vcode = openPlayService.validOpiStatusByPartner(opi, partner, member);
		if(!vcode.isSuccess()){
			return ErrorCode.getFullErrorCode(vcode.getErrcode(), vcode.getMsg(), null);
		}
		try{
			ErrorCode last = ticketOrderService.processLastOrder(partner.getId(), ukey, seatLabel);
			if(!last.isSuccess()){
				return ErrorCode.getFailure(ApiConstant.CODE_REPEAT_OPERATION, last.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFullErrorCode(ApiConstant.CODE_PARAM_ERROR, "手机号有错误！", null);
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()){
			return remoteLockList;
		}
		TicketOrderContainer tc = null;
		try{
			if(member!=null){//GewaSelf
				tc = ticketOrderService.addTicketOrder(opi, seatLabel, member, partner, mobile, randomNum, remoteLockList.getRetval());
				if(tc.getBindGift()!=null){
					model.put("gift", tc.getBindGift());
					model.put("goods", tc.getGoods());
				}
			}else{//outPartner
				tc = ticketOrderService.addPartnerTicketOrder(opi, seatLabel, partner, mobile, randomNum, ukey, ukey, PaymethodConstant.PAYMETHOD_PARTNERPAY, "api", remoteLockList.getRetval());
			}
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return ErrorCode.getFullErrorCode(e.getCode(), e.getMsg(), null);
		} catch (DataIntegrityViolationException e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_SEAT_OCCUPIED, "订单错误：座位被他人占用", null);
		} catch (Exception e){
			dbLogger.error("订单错误：" +StringUtil.getExceptionTrace(e));
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_UNKNOWN_ERROR, "订单错误：座位可能被他人占用", null);
		}
		TicketOrder order = tc.getTicketOrder();
		List<SellSeat> seatList = tc.getSeatList();
		ErrorCode lockResult = ticketOperationService.lockRemoteSeat(opi, order, seatList);
		if(!lockResult.isSuccess()){
			try{
				ticketOrderService.cancelLockFailureOrder(order);
			}catch(HibernateOptimisticLockingFailureException e){//ignore
				dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e, 10));
			}
			return lockResult;
		}
		model.put("partner", partner);
		model.put("order", order);
		model.put("opi", opi);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return ErrorCode.SUCCESS;
	}
	
	protected ErrorCode addTicketOrder(ApiUser partner, Member member, Long memberid, String ukey, OpenPlayItem opi, String mobile, String seatLabel, String paymethod, String paybank, ModelMap model,Long goodsid, Integer quantity){
		//1、检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		ErrorCode<String> vcode = openPlayService.validOpiStatusByPartner(opi, partner, member);
		if(!vcode.isSuccess()){
			return ErrorCode.getFullErrorCode(vcode.getErrcode(), vcode.getMsg(), null);
		}
		try{
			ErrorCode last = ticketOrderService.processLastOrder(memberid, ukey, seatLabel);
			if(!last.isSuccess()){
				return ErrorCode.getFailure(ApiConstant.CODE_REPEAT_OPERATION, last.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}

		String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFullErrorCode(ApiConstant.CODE_PARAM_ERROR, "手机号有错误！", null);
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()){
			return remoteLockList;
		}
		TicketOrderContainer tc = null;
		try{
			if(member!=null){
				tc = ticketOrderService.addTicketOrder(opi, seatLabel, member, partner, mobile, randomNum, remoteLockList.getRetval());
				if(tc.getBindGift()!=null){
					model.put("gift", tc.getBindGift());
					model.put("goods", tc.getGoods());
				}else{
					//增加卖品订单  
					if(null!=goodsid && null!=quantity){
						ErrorCode<GoodsGift> code = ticketOrderService.addOrderGoodsGift(tc.getTicketOrder(), opi, goodsid, quantity);
						if(!code.isSuccess()) return ErrorCode.getFailure(code.getErrcode(), code.getMsg());
						model.put("gift", code.getRetval());
						model.put("goods", daoService.getObject(Goods.class, goodsid));
					}
				}
			}else{//outPartner
				String method = PaymethodConstant.PAYMETHOD_PARTNERPAY;
				if(StringUtils.isNotBlank(paymethod) && PaymethodConstant.isValidPayMethod(paymethod)) method = paymethod;
				tc = ticketOrderService.addPartnerTicketOrder(opi, seatLabel, partner, mobile, randomNum, ukey, ukey, method, paybank, remoteLockList.getRetval());
			}
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return ErrorCode.getFullErrorCode(e.getCode(), e.getMsg(), null);
		} catch (DataIntegrityViolationException e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_SEAT_OCCUPIED, "订单错误：座位被他人占用", null);
		} catch (Exception e){
			dbLogger.error("订单错误：" +StringUtil.getExceptionTrace(e));
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_UNKNOWN_ERROR, "订单错误：座位可能被他人占用", null);
		}
		TicketOrder order = tc.getTicketOrder();
		List<SellSeat> seatList = tc.getSeatList();
		ErrorCode lockResult = ticketOperationService.lockRemoteSeat(opi, order, seatList);
		if(!lockResult.isSuccess()){
			try{
				ticketOrderService.cancelLockFailureOrder(order);
			}catch(HibernateOptimisticLockingFailureException e){//ignore
				dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e, 10));
			}
			return lockResult;
		}
		model.put("partner", partner);
		model.put("order", order);
		model.put("opi", opi);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return ErrorCode.SUCCESS;
	}
}
