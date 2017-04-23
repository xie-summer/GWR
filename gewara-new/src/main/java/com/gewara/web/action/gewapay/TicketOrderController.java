package com.gewara.web.action.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CharacteristicType;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.helper.discount.ElecCardHelper;
import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.helper.ticket.SeatPriceHelper;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.helper.ticket.TicketUtil;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.Picture;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.service.PlaceService;
import com.gewara.service.content.PictureService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.monitor.MemberMonitorService;
import com.gewara.untrans.monitor.MemberMonitorService.CountType;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.untrans.ticket.TicketQueueService;
import com.gewara.untrans.ticket.TicketRollCallService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class TicketOrderController extends AnnotationController {
	@Autowired@Qualifier("config")
	private Config config;
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;

	@Autowired@Qualifier("ticketQueueService")
	private TicketQueueService ticketQueueService;
	public void setTicketQueueService(TicketQueueService ticketQueueService) {
		this.ticketQueueService = ticketQueueService;
	}
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@Autowired@Qualifier("ticketRollCallService")
	private TicketRollCallService ticketRollCallService;
	public void setTicketRollCallService(TicketRollCallService ticketRollCallService) {
		this.ticketRollCallService = ticketRollCallService;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	public void setNosqlService(NosqlService nosqlService) {
		this.nosqlService = nosqlService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Autowired@Qualifier("memberMonitorService")
	private MemberMonitorService memberMonitorService;
	@RequestMapping("/cinema/order/step1.shtml")
	public String oldChooseSeat(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			Long mpid, String mobile, String spkey, String tkn,
			HttpServletRequest request, HttpServletResponse response, ModelMap model){
		if(mpid==null) showMessage(model, "请选择场次！");
		if(WebUtils.isRobot(request.getHeader("User-Agent"))){
			return showMessage(model, "访问超时！！"); 
		}
		if(!TicketUtil.isValidToken(mpid, tkn)){
			dbLogger.warn("INVALID-TOKEN:" + WebUtils.getParamStr(request, true) + WebUtils.getHeaderStr(request));
			//return showMessage(model, "非法访问！");
		}
		
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showMessage(model, "场次不存在！");
		
		if(OpiConstant.OPEN_WD.equals(opi.getOpentype())){
			Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
			if(member==null){
				return gotoLogin("/cinema/order/step1.shtml", request, model);
			}
		}
		
		ErrorCode code = addSeatData(model, opi, response, mobile, request, sessid, spkey);
		if(code.isSuccess()){ 
			if(OpiConstant.OPEN_WD.equals(opi.getOpentype())){
				return "cinema/wide_chooseSeat4Wd.vm";
			}
			return "cinema/chooseSeat.vm";
		}
		return showMessageAndReturn(model, request, code.getMsg());
	}
	private ErrorCode addSeatData(ModelMap model, OpenPlayItem opi, HttpServletResponse response, String mobile, 
			HttpServletRequest request, String sessid, String spkey){
		
		if(!opi.isOrder()) return ErrorCode.getFailure(OpiConstant.getStatusStr(opi));
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
		if(ValidateUtil.isMobile(mobile)){
			model.put("mobile", mobile);
		}else{
			if(member != null){
				if(ValidateUtil.isMobile(member.getMobile())){
					model.put("mobile", member.getMobile());
				}
			}
		}
		String spid = null;
		if(StringUtils.isNotBlank(spkey)){
			spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
		}
		if(StringUtils.isNotBlank(spid)){
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
			PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
			if(sd != null && MovieSpecialDiscountHelper.isEnabled(sd, opi, pvh).isSuccess()){
				model.put("adspdiscount", sd);
				model.put("spkey", spkey);
			}
		}
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
				ErrorCode<List<String>> cachedSeat = ticketOperationService.getCachedRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT);
				model.put("seatCached", cachedSeat.isSuccess());
			}
			if(StringUtils.isNotBlank(cinema.getSubwayTransport())){
				Map<Long,List<Map<String,String>>> subwayTransportMap = JsonUtils.readJsonToObject(new TypeReference<Map<Long,List<Map<String,String>>>>() {},cinema.getSubwayTransport());
				List<Subwaystation> stationList = this.daoService.getObjectList(Subwaystation.class, subwayTransportMap.keySet());
				model.put("subwayTransportMap",subwayTransportMap);
				model.put("stationList", stationList);
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
	/**
	 * 异步获取影厅信息
	 * */
	@RequestMapping("/ajax/roomView.shtml")
	public String ajaxRoomView(Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		Long roomId = null;
		String citycode = "";
		if(opi == null){
			MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
			if(mpi == null){
				return showJsonError(model, "场次信息有误！");
			}
			roomId = mpi.getRoomid();
			citycode = mpi.getCitycode();
		}else{
			roomId = opi.getRoomid();
			citycode = opi.getCitycode();
		}
		if(roomId == null) {
			return showJsonError(model, "影厅信息错误！");
		}
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		if(room == null) {
			return showJsonError(model, "影厅信息错误！");
		}
		List<Picture> picList = pictureService.getPictureListByRelatedid("characterroom",roomId, 0, 1);
		Map jsonMap = new HashMap();
		model.put("room", room);
		model.put("cTypeMap", CharacteristicType.characteristicNameMap);
		if(picList != null && !picList.isEmpty()){
			model.put("pic", picList.get(0));
		}
		model.put("opi", opi);
		model.put("cityPinyin", AdminCityContant.citycode2PinyinMap.get(citycode));
		String viewPage = velocityTemplate.parseTemplate("cinema/ajaxRoomView.vm", model);
		jsonMap.put("viewPage", viewPage);
		return showJsonSuccess(model, jsonMap);
	}
	/**
	 * 异步获取座位图信息
	 * */
	@RequestMapping("/ajax/seatView.shtml")
	public String ajaxSeatView(Long mpid, ModelMap model){
		String[] seatMap = openPlayService.getOpiSeatMap(mpid);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showJsonError(model, "场次信息有误！");
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		if(seatMap==null) {
			model.put("seatMap", room.getSeatmap());
		}else{
			model.put("seatMap", seatMap[0]);
			model.put("updateTime", seatMap[1]);
		}
		model.put("opi", opi);
		model.put("room", room);
		String viewPage = velocityTemplate.parseTemplate("cinema/ajaxSeatPageView.vm", model);
		Map jsonMap = new HashMap();
		jsonMap.put("viewPage", viewPage);
		return showJsonSuccess(model, jsonMap);
	}
	@RequestMapping("/ajax/cinema/getopiListBympid.xhtml")
	public String getNearOpiListBympid(Long mpid, ModelMap model) {
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showMessage(model, "场次不存在！");
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
		return "cinema/ajax_cinemaOpiList.vm";
	}
	
	@RequestMapping("/ajax/ticket/getSeatPage.shtml")
	public String getSeatPage(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long mpid, ModelMap model2){
		
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showJsonError(model2, "场次不存在！");
		if(!opi.isOrder()) return showJsonError(model2, OpiConstant.getStatusStr(opi));
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member==null) return showJsonError(model2, "您还没有登录，请先登录！");
		memberMonitorService.increament(member.getId(), CountType.getSeat, request);
		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return showJsonError(model2, booking.getMsg());
		}
		String ip = WebUtils.getRemoteIp(request);
		//monitorService.addOrderCount(member.getId(), opi.getCinemaid());
		ErrorCode allow = ticketQueueService.isMemberAllowed(member.getId(), opi.getCinemaid(), ip);
		if(!allow.isSuccess()) {
			return showJsonError(model2, allow.getMsg());
		}
		Map model = new HashMap(); 
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(opi.getMpid());
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		model.put("seatStatusUtil", seatStatusUtil);
		
		ErrorCode<List<String>> remoteLockList = ticketOperationService.getCachedRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER);
		if(!remoteLockList.isSuccess()){
			remoteLockList = ticketOperationService.updateRemoteLockSeatFromCache(opi);
		}
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
		List<OpenSeat> mySeatList = new ArrayList<OpenSeat>();
		model.put("member", member);
		List<Map> mySeatMapList = new ArrayList<Map>();
		TicketOrder lastOrder = ticketOrderService.getLastUnpaidTicketOrder(member.getId(), "" + member.getId(), mpid);
		if(lastOrder!=null) {
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(lastOrder.getId());
			for(SellSeat sellSeat : seatList){
				OpenSeat oseat = daoService.getObject(OpenSeat.class, sellSeat.getId()); 
				if(oseat!=null){
					mySeatList.add(oseat);
					Map m = new HashMap();
					m.put("id", oseat.getId());
					m.put("seatLabel", oseat.getSeatLabel());
					m.put("price", sph.getPrice(oseat));
					mySeatMapList.add(m);
				}
			}
		}

		model.put("mySeatList", mySeatList);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("opi", opi);
		model.put("room", room);
		model.put("priceMap", priceMap);
		String cs = StringUtils.reverse(request.getParameter("cs"));
		String[] csArr = StringUtils.split(cs, '0');
		try{
			model.put("hsSeat", csArr[0]);
			model.put("slSeat", csArr[1]);
			model.put("ckSeat", csArr[2]);
		}catch(Exception e){
			dbLogger.error("座位图class", e);
		}
		if(StringUtils.isNotBlank(room.getOtherinfo())){
			if(JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat") != null && 
					StringUtils.equals("true",(String)JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat"))){
				Map<String,String> outerRingseatMap = nosqlService.getOuterRingSeatByRoomId(opi.getRoomid());
		 		model.put("outerRingseatMap", outerRingseatMap);
		 		model.put("outerRingseats", StringUtils.join(outerRingseatMap.keySet(), ","));
			}
		}
		Map jsonMap = new HashMap();
		String template = "cinema/wide_seatPage.vm";
		String seatPage = velocityTemplate.parseTemplate(template, model);
		jsonMap.put("seatPage", seatPage);
		jsonMap.put("seatList", mySeatMapList);
		//model2.put("hsSeat", request.getAttribute("hsSeat"));
		openPlayService.updateOpiSeatMap(mpid, room, openSeatList, remoteLockList.getRetval(), seatStatusUtil);
		return showJsonSuccess(model2, jsonMap);
	}
	/**
	 * @param sessid
	 * @param request
	 * @param captchaId
	 * @param captcha
	 * @param mpid
	 * @param mobile
	 * @param goodsid
	 * @param quantity
	 * @param seatid
	 * @param spkey
	 * @param origin
	 * @param model
	 * @return
	 */
	@RequestMapping("/cinema/order/step2.shtml")
	public String addOrder(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String captchaId, String captcha, Long mpid, String mobile, Long goodsid, Integer quantity, 
			String seatid, String spkey, @CookieValue(required=false,value="origin") String origin, ModelMap model){
		if(mpid==null) return showError(model, "请重新选择场次！");
		//0、检查验证码有没有错误
		Map jsonMap = new HashMap<String, String>();
		jsonMap.put("refreshCaptcha", false);
		jsonMap.put("showLogin", false);
		jsonMap.put("refreshSeat", false);
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) {
			jsonMap.put("msg", "验证码错误！");
			jsonMap.put("refreshCaptcha", true);
			return showJsonError(model, jsonMap);
		}
		if(!ValidateUtil.isMobile(mobile)) {
			jsonMap.put("msg", "手机号有错误！");
			return showJsonError(model, jsonMap);
		}
		List<Long> seatidList = BeanUtil.getIdList(seatid, ",");
		if(seatidList.size()==0) return showJsonError(model, "请选择座位！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) {
			jsonMap.put("msg", "请先登录！");
			jsonMap.put("showLogin", true);
			return showJsonError(model, jsonMap);
		}
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null || opi.isUnOpenToGewa()) return showError(model, "本场次已停止售票！");

		if(ticketRollCallService.isTicketRollCallMember(member.getId(), mobile)){
			jsonMap.put("msg", "你的帐号购票受限，请联系客服：4000-406-506！");
			return showJsonError(model, jsonMap);
		}
		//1、检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			ErrorCode lastOrder = ticketOrderService.processLastOrder(member.getId(), ""+member.getId(), seatid);
			if(!lastOrder.isSuccess()){
				return showJsonError(model, lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 15));
		}
		MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
		Integer point = info.getPointvalue();
		String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()){
			jsonMap.put("msg", remoteLockList.getMsg());
			jsonMap.put("refreshSeat", true);
			return showJsonError(model, jsonMap);
		}
		TicketOrderContainer tc = null;
		try{
			tc = ticketOrderService.addTicketOrder(opi, seatidList, member.getId(), member.getRealname(), mobile, point, randomNum, remoteLockList.getRetval());
			if(tc.getBindGift()==null){
				ticketOrderService.addOrderGoodsGift(tc.getTicketOrder(), opi, goodsid, quantity);
			}
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + e.getMessage());
			jsonMap.put("msg", e.getMsg());
			jsonMap.put("refreshSeat", true);
			return showJsonError(model, jsonMap);
		} catch (DataIntegrityViolationException e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			jsonMap.put("msg", "座位被别人占用，请刷新页面重新订座位！");
			jsonMap.put("refreshSeat", true);
			return showJsonError(model, jsonMap);
		} catch (Exception e){
			monitorService.logException(EXCEPTION_TAG.SERVICE, "/cinema/order/step2.shtml", "下单错误", e, null);
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			jsonMap.put("msg", "订单有错误，可能是座位有被别人占用，请刷新页面重新订座位！");
			jsonMap.put("refreshSeat", true);
			return showJsonError(model, jsonMap);
		}
		List<SellSeat> seatList = tc.getSeatList();
		TicketOrder order = tc.getTicketOrder();
		ErrorCode result = ticketOperationService.lockRemoteSeat(opi, order, seatList);
		if(!result.isSuccess()){
			try{
				ticketOrderService.cancelLockFailureOrder(order);
			}catch(HibernateOptimisticLockingFailureException e){//ignore
				dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e, 20));
			}
			jsonMap.put("msg", result.getMsg());
			jsonMap.put("refreshSeat", true);
			return showJsonError(model, jsonMap);
		}
		if(StringUtils.isNotBlank(origin)){
			ticketOrderService.addOrderOrigin(order, origin);
		}
		
		String spid = null;
		if(StringUtils.isNotBlank(spkey)){
			spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
		}
		if(StringUtils.isNotBlank(spid)){
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
			PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
			if(sd != null && MovieSpecialDiscountHelper.isEnabled(sd, opi, pvh).isSuccess()){
				order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), OpiConstant.FROM_SPID, spid));
				daoService.saveObject(order);
			}
		}
		return showJsonSuccess(model, ""+order.getId());
		
	}
	@RequestMapping("/cinema/order/time.xhtml")
	@ResponseBody
	public String time(Long tid){
		Long valid = orderQueryService.getOrderValidTimeById(tid);
		if(valid!=null){
			Long cur = System.currentTimeMillis();
			Long remain = valid - cur;
			return ""+remain;
		}
		return "";
	}
	
	@RequestMapping("/cinema/order/removeBuyItem.xhtml")
	public String removeBuyItem(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long itemid, ModelMap model) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<TicketOrder> result = ticketOrderService.removeBuyItem(member.getId(), itemid);
		if(result.isSuccess()){
			return showJsonSuccess(model, ""+result.getRetval().getDue());
		}
		return showJsonError(model, result.getMsg());
	}
	//赠品付费的情况
	@RequestMapping("/cinema/order/modOrderMobile.xhtml")
	public String modOrderMobile(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderid, String mobile, ModelMap model) {
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(order==null) return showJsonError_NOT_FOUND(model);
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(!order.getMemberid().equals(member.getId())) return showJsonError(model, "你没有权限!");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机格式不正确");
		if (order.isAllPaid() || order.isCancel()) return showJsonError(model, "不能修改已支付或已（过时）取消的订单！");
		order.setMobile(mobile);
		daoService.saveObject(order);
		if(StringUtils.equals(order.getCategory(), TagConstant.TAG_ACTIVITY) && StringUtils.equals(order.getPricategory(), TagConstant.TAG_ACTIVITY)){
			synchActivityService.updateActiviyOrderMobile(order.getTradeNo(), mobile);
		}
		return showJsonSuccess(model);
	}
	/*	@RequestMapping("/cinema/order/useNewOrder.shtml")
	public String returnToNew(Long mpid, String spkey, String mobile, HttpServletResponse response, ModelMap model){
		long validtime = System.currentTimeMillis()+DateUtil.m_hour*2;
		Cookie cookie = new Cookie("origin", "neworder:" + validtime+":" +StringUtil.md5WithKey("neworder" + validtime, 8));
		cookie.setPath("/cinema/order/");
		cookie.setMaxAge(60 * 60 * 12);//12 hour
		response.addCookie(cookie);
		model.put("mpid", mpid);
		model.put("spkey", spkey);
		model.put("mobile", mobile);
		return showRedirect("/cinema/order/step1.shtml", model);
	}*/
	/*private ErrorCode<Map> getSingDayMpidList(Long mpid){
		int i = 1;
		if(i>0) return ErrorCode.getFailure("不是单身节场次");
		try {
			List<Map> qryMapList = mongoService.getMapList(MongoData.NS_SINGLEDAY);
			for(Map map : qryMapList){
				String manmpid = map.get("manmpid")+"";
				String womenmpid = map.get("womenmpid")+"";
				if(StringUtils.equals(manmpid, mpid+"") || StringUtils.equals(womenmpid, mpid+"")) {
					return ErrorCode.getSuccessReturn(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorCode.getFailure("");
		}
		return ErrorCode.getFailure("不是单身节场次");
	}*/
}
