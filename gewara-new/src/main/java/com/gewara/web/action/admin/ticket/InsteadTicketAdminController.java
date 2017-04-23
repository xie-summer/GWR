package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.helper.discount.ElecCardHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.ticket.SeatPriceHelper;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.model.acl.User;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.ticket.InsteadTicketOrderService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.untrans.ticket.TicketQueueService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class InsteadTicketAdminController extends BaseAdminController {
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("config")
	private Config config;
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	@Autowired@Qualifier("ticketQueueService")
	private TicketQueueService ticketQueueService;
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	@Autowired@Qualifier("insteadTicketOrderService")
	private InsteadTicketOrderService insteadTicketOrderService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;

	@RequestMapping("/admin/ticket/chooseSeat.shtml")
	public String chooseSeat(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			Long mpid, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		if(mpid==null) return forwardMessage(model, "请选择场次！");
		if(WebUtils.isRobot(request.getHeader("User-Agent"))){
			return forwardMessage(model, "访问超时！！"); 
		}

		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return forwardMessage(model, "场次不存在！");
		
		if(OpiConstant.OPEN_WD.equals(opi.getOpentype())){
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			if(member==null){
				return gotoLogin("/admin/adminConsole.xhtml", request, model);
			}
		}
		
//		ErrorCode code = addSeatData(model, opi, response, mobile, request, sessid, spkey);
		ErrorCode code = addSeatData(model, opi, response, request, sessid);
		if(code.isSuccess()){ 
			if(OpiConstant.OPEN_WD.equals(opi.getOpentype())){
				return "admin/ticket/wide_chooseSeat4Wd.vm";
			}
			return "admin/ticket/chooseSeat.vm";
		}
		return showMessageAndReturn(model, request, code.getMsg());
	}
	
	private ErrorCode addSeatData(ModelMap model, OpenPlayItem opi, HttpServletResponse response,
			HttpServletRequest request, String sessid){
		//过期判断暂时去掉
		//if(!opi.isOrder()) return ErrorCode.getFailure(OpiConstant.getStatusStr(opi));
		if(opi.isUnOpenToGewa()) return ErrorCode.getFailure("本场次未开放售票！");
		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return booking;
		}

		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		model.put("lineNum", room.getLinenum());
		model.put("rankNum", room.getRanknum());
		Cinema cinema = daoService.getObject(Cinema.class, opi.getCinemaid());
		model.put("subwaylineMap", placeService.getSubwaylineMap(cinema.getCitycode()));
		Movie movie = daoService.getObject(Movie.class, opi.getMovieid());
		WebUtils.setCitycode(request, cinema.getCitycode(), response);
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Map<String,String> otherInfo = JsonUtils.readJsonToMap(opi.getOtherinfo());
		String mealoption = otherInfo.get(OpiConstant.MEALOPTION);
		if(!StringUtils.equals(mealoption, "notuse")){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, PartnerConstant.GEWA_SELF);
			if(goodsGift!=null) {
				model.put("goodsGift", goodsGift);
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goods", goods);
			}else {
				List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, opi.getCinemaid(), 0, 5);
				GoodsFilterHelper.goodsFilter(goodsList, PartnerConstant.GEWA_SELF);
				model.put("goodsList", goodsList);
			}
		}
//		if(ValidateUtil.isMobile(mobile)){
//			model.put("mobile", mobile);
//		}else{
//			if(member != null){
//				if(ValidateUtil.isMobile(member.getMobile())){
//					model.put("mobile", member.getMobile());
//				}
//			}
//		}
//		String spid = null;
//		if(StringUtils.isNotBlank(spkey)){
//			spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
//		}
//		if(StringUtils.isNotBlank(spid)){
//			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
//			PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
//			if(sd != null && MovieSpecialDiscountHelper.isEnabled(sd, opi, pvh).isSuccess()){
//				model.put("adspdiscount", sd);
//				model.put("spkey", spkey);
//			}
//		}
		model.put("cinema", cinema);
		model.put("movie", movie);
		model.put("opi", opi);
		model.put("room", room);
		model.put("profile", daoService.getObject(CinemaProfile.class, room.getCinemaid()));
		String[] seatMap = openPlayService.getOpiSeatMap(opi.getMpid());
		if(seatMap==null) {
			model.put("seatMap", room.getSeatmap());
		}else{
			model.put("seatMap", seatMap[0]);
		}
		if(OpiConstant.OPEN_WD.equals(opi.getOpentype())){
			//万达
		}else{//非万达
			if(!opi.hasGewara()){
				long cur = System.currentTimeMillis();
				model.put("cur", cur);
				model.put("checkStr", StringUtil.md5(opi.getSeqNo() + cur + config.getString("asynchTicketPriKey"), 8));
				model.put("validtime", opi.getPlaytime().getTime());
				model.put("seatCached", ticketOperationService.getCachedRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT));
			}
		}
		model.put("logonMember", member);
		model.put("movieMpiRemark", nosqlService.getMovieMpiRemarkList(opi.getMovieid(), opi.getCitycode(), 5));
		model.put("ElecCardHelper", new ElecCardHelper());
		if(StringUtils.isNotBlank(room.getOtherinfo())){
			if(JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat") != null && 
					StringUtils.equals("true",(String)JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat"))){
				Map<String,String> outerRingseatMap = nosqlService.getOuterRingSeatByRoomId(opi.getRoomid());
		 		model.put("outerRingseatMap", outerRingseatMap);
		 		model.put("outerRingseats", StringUtils.join(outerRingseatMap.keySet(), ","));
			}
		}
		model.put("ticketPath", config.getString("ticketPath"));
		if(opi.hasOpentype(OpiConstant.OPEN_PNX)){
			model.put("ticketPath", config.getString("ticketPath4Pnx"));
		}
		return ErrorCode.SUCCESS;
	}
	
	@RequestMapping("/admin/ticket/getSeatPage.shtml")
	public String getSeatPage(HttpServletRequest request, Long mpid, ModelMap model2){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showJsonError(model2, "场次不存在！");
		if(!opi.isOrder()) return showJsonError(model2, OpiConstant.getStatusStr(opi));
//		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
//		if(member==null) return showJsonError(model2, "您还没有登录，请先登录！");
		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return showJsonError(model2, booking.getMsg());
		}
		String ip = WebUtils.getRemoteIp(request);
		//monitorService.addOrderCount(member.getId(), opi.getCinemaid());
		User user = getLogonUser();
		ErrorCode allow = ticketQueueService.isMemberAllowed(user.getId(), opi.getCinemaid(), ip);
		if(!allow.isSuccess()) {
			return showJsonError(model2, allow.getMsg());
		}
		Map model = new HashMap(); 
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(mpid);
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		model.put("seatStatusUtil", seatStatusUtil);
		
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeatFromCache(opi);
		if(!remoteLockList.isSuccess()){
			remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT, false);
		}
		if(!remoteLockList.isSuccess()) {
			return showJsonError(model2, "影院服务器连接不正常，请稍候再试！");
		}
		model.put("hfhLockList", remoteLockList.getRetval());

		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, OpenSeat> seatMap = new HashMap<String, OpenSeat>();
		Map<Long, Integer> priceMap = new HashMap<Long, Integer>();
		SeatPriceHelper sph = new SeatPriceHelper(opi, PartnerConstant.GEWA_SELF);
		model.put("lineNum", room.getLinenum());
		model.put("rankNum", room.getRanknum());

		int maxline = 0, maxrank = 0;
		for(OpenSeat seat:openSeatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
			priceMap.put(seat.getId(), sph.getPrice(seat));
			maxline = Math.max(seat.getLineno(), maxline);
			maxrank = Math.max(seat.getRankno(), maxrank);
		}
		if(StringUtils.equals(opi.getOpentype(), OpiConstant.OPEN_PNX)){
			//东票使用
			model.put("lineNum", maxline);
			model.put("rankNum", maxrank);
		}
//		List<OpenSeat> mySeatList = new ArrayList<OpenSeat>();
//		model.put("member", member);
		List<Map> mySeatMapList = new ArrayList<Map>();
//		TicketOrder lastOrder = ticketOrderService.getLastUnpaidTicketOrder(member.getId(), "" + member.getId(), mpid);
//		if(lastOrder!=null) {
//			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(lastOrder.getId());
//			for(SellSeat sellSeat : seatList){
//				OpenSeat oseat = daoService.getObject(OpenSeat.class, sellSeat.getId()); 
//				if(oseat!=null){
//					mySeatList.add(oseat);
//					Map m = new HashMap();
//					m.put("id", oseat.getId());
//					m.put("seatLabel", oseat.getSeatLabel());
//					m.put("price", sph.getPrice(oseat));
//					mySeatMapList.add(m);
//				}
//			}
//		}

//		model.put("mySeatList", mySeatList);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("opi", opi);
		model.put("room", room);
		model.put("priceMap", priceMap);
		if(StringUtils.isNotBlank(room.getOtherinfo())){
			if(JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat") != null && 
					StringUtils.equals("true",(String)JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat"))){
				Map<String,String> outerRingseatMap = nosqlService.getOuterRingSeatByRoomId(opi.getRoomid());
		 		model.put("outerRingseatMap", outerRingseatMap);
		 		model.put("outerRingseats", StringUtils.join(outerRingseatMap.keySet(), ","));
			}
		}
		Map jsonMap = new HashMap();
		String template = "admin/cinema/wide_seatPage.vm";
		/*ErrorCode<Map> code = getSingDayMpidList(mpid);
		if(code.isSuccess()){
			Map singleMap = code.getRetval();
			model.put("singleMap", singleMap);
			template = "subject/singlesDay/seatPage.vm";
		}*/
		String seatPage = velocityTemplate.parseTemplate(template, model);
		jsonMap.put("seatPage", seatPage);
		jsonMap.put("seatList", mySeatMapList);
		openPlayService.updateOpiSeatMap(mpid, room, openSeatList, remoteLockList.getRetval(), seatStatusUtil);
		return showJsonSuccess(model2, jsonMap);
	}
	
	@RequestMapping("/admin/ticket/getOpiListByMpid.xhtml")
	public String getNearOpiListBympid(Long mpid, ModelMap model) {
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return forwardMessage(model, "场次不存在！");
		if(!opi.isOrder()) return showMessage(model, OpiConstant.getStatusStr(opi));
		Timestamp curTime = DateUtil.getCurFullTimestamp();
		Timestamp curTimestamp = DateUtil.getBeginTimestamp(curTime);
		Timestamp playtime = DateUtil.getBeginTimestamp(opi.getPlaytime());
		Timestamp starttime = null;
		Timestamp endtime = null;
		if(playtime.after(curTimestamp)){
			starttime = playtime;
			endtime = DateUtil.getLastTimeOfDay(playtime);
		}else {
			starttime = curTime;
			endtime = DateUtil.getLastTimeOfDay(curTime);
		}
		List<OpenPlayItem> opiList = openPlayService.getOpiList(opi.getCitycode(), opi.getCinemaid(), opi.getMovieid(), starttime, endtime, true);
		model.put("opi", opi);
		model.put("opiList", opiList);
		return "admin/ticket/ajax_cinemaOpiList.vm";
	}
	
	@RequestMapping("/admin/ticket/saveTicketOrder.xhtml")
	public String saveTicketOrder(Long mpid, String tradeno, String seatid, ModelMap model) {
		User user = getLogonUser();
		List<Long> seatIdList = BeanUtil.getIdList(seatid, ",");
		if (CollectionUtils.isEmpty(seatIdList)){
			return showJsonError(model, "请选座位！");
		}
		TicketOrder oldTicketOrder = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno);
		if (oldTicketOrder == null) {
			return showJsonError(model, "订单不存在！");
		}
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null){
			return showJsonError(model, "场次不存在！");
		}
		try {
			TicketOrder newTicketOrder = insteadTicketOrderService.addTicketOrder(opi, oldTicketOrder, seatIdList, user);
			return showJsonSuccess(model, String.valueOf(newTicketOrder.getId()));
		} catch (OrderException e) {
			return showJsonError(model, e.getMsg());
		}
	}
	
	@RequestMapping("/admin/ticket/confirmOrder.xhtml")
	public String confirmOrder(Long orderId, HttpServletRequest request, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		if(order == null) return showMessageAndReturn(model, request, "订单不存在！");
		addConfirmOrderData(order, model);
		return "admin/ticket/wide_confirmOrder.vm";
	}
	private void addConfirmOrderData(TicketOrder order, ModelMap model){
		model.put("order", order);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		Cinema cinema = daoService.getObject(Cinema.class, opi.getCinemaid());
		CinemaProfile cinemaProfile = daoService.getObject(CinemaProfile.class, opi.getCinemaid());
		Movie movie = daoService.getObject(Movie.class, opi.getMovieid());
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		model.put("cinema", cinema);
		model.put("cinemaProfile", cinemaProfile);
		model.put("movie", movie);
		model.put("opi", opi);
		model.put("itemList", itemList);
		model.put("GewaOrderHelper", new GewaOrderHelper());
		model.put("room", daoService.getObject(CinemaRoom.class, opi.getRoomid()));
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		model.put("discountList", discountList);
		Map<String, String> orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		model.put("orderOtherinfo", orderOtherinfo);
	}

}
