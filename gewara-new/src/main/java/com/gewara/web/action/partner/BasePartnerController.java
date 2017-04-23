package com.gewara.web.action.partner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.helper.ticket.PartnerPriceHelper;
import com.gewara.helper.ticket.SdOpiFilter;
import com.gewara.helper.ticket.SeatPriceHelper;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.json.MovieMpiRemark;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.CooperUser;
import com.gewara.model.common.County;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.MessageService;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.partner.PartnerService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.OuterSorter;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

public class BasePartnerController extends AnnotationController{
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("ticketOrderService")
	protected TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}

	@Autowired@Qualifier("orderMonitorService")
	protected OrderMonitorService orderMonitorService;
	@Autowired@Qualifier("ticketDiscountService")
	protected TicketDiscountService ticketDiscountService;

	@Autowired@Qualifier("ticketOperationService")
	protected TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired@Qualifier("spdiscountService")
	protected SpdiscountService spdiscountService;
	public void setSpdiscountService(SpdiscountService spdiscountService) {
		this.spdiscountService = spdiscountService;
	}
	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("config")
	protected Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("controllerService")
	protected ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("partnerService")
	protected PartnerService partnerService;
	public void setPartnerService(PartnerService partnerService) {
		this.partnerService = partnerService;
	}
	
	@Autowired@Qualifier("openPlayService")
	protected OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("mcpService")
	protected MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("messageService")
	protected MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@Autowired@Qualifier("partnerSynchService")
	protected PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	@Autowired@Qualifier("goodsOrderService")
	protected GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("nosqlService")
	protected NosqlService nosqlService;
	
	public CooperUser getLogonCooperUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth!=null && auth.isAuthenticated()){//登录
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			if(user instanceof CooperUser) return (CooperUser)user;
		}
		return null;
	}
	//第一步：选择场次
	protected String opiList(ApiUser partner, Long movieid, Date fyrq, String view, ModelMap model){
		String citycode = partner.getDefaultCity();
		ErrorCode code = addOpiListData(partner, movieid, fyrq, null, null, model, citycode);
		if(code.isSuccess()) return view;
		return showError(model, code.getMsg());
	}
	protected String opiList(ApiUser partner, Long movieid, Date fyrq, String view, String sort, ModelMap model){
		String citycode = partner.getDefaultCity();
		ErrorCode code = addOpiListData(partner, movieid, fyrq, null, sort, model, citycode);
		if(code.isSuccess()) return view;
		return showError(model, code.getMsg());
	}
	protected String opiList(ApiUser partner, Long movieid, Date fyrq, OpiFilter filter, String view, ModelMap model){
		String citycode = partner.getDefaultCity();
		ErrorCode code = addOpiListData(partner, movieid, fyrq, filter, null, model, citycode);
		if(code.isSuccess()) return view;
		return showError(model, code.getMsg());
	}

	protected ErrorCode addOpiListData(ApiUser partner, Long movieid, Date fyrq, OpiFilter filter, String sort, ModelMap model){
		String citycode = partner.getDefaultCity();
		return this.addOpiListData(partner, movieid, fyrq, filter, sort, model, citycode);
	}
	
	protected ErrorCode addOpiListData(ApiUser partner, Long movieid, Date fyrq, OpiFilter filter, String sort, ModelMap model, String citycode){
		if(!partner.isEnabled()) return ErrorCodeConstant.NOT_FOUND;
		model.put("partner", partner);
		Date cur = new Date();
		if(fyrq==null) {
			if(DateUtil.getHour(cur) < 20) fyrq = DateUtil.getBeginningTimeOfDay(cur);
			else fyrq = DateUtil.addDay(DateUtil.getCurDate(), 1);
		}
		model.put("fyrq", DateUtil.formatDate(fyrq));
		List<Movie> movieList = this.getOpenMovieListByDate(partner, citycode, null);
		if(movieList.isEmpty()) return ErrorCode.getFailure("暂无影片可购票！");
		mcpService.sortMoviesByMpiCount(citycode, movieList);
		Movie curmovie = null;
		if(movieid == null){
			curmovie = movieList.get(0);
			movieid = curmovie.getId();
		}else{
			curmovie = daoService.getObject(Movie.class, movieid);
		}
		if(!movieList.contains(curmovie)) movieList.add(0, curmovie);
		model.put("movieList", movieList);

		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner, citycode, null, movieid, fyrq);
		if(filter!=null) filter.applyFilter(opiList);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter closeRule = new CloseRuleOpiFilter(partner, pcrList);
		closeRule.applyFilter(opiList);

		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		model.put("priceHelper", priceHelper);
		List<Date> dateList = partnerService.getPlaydateList(partner, citycode, movieid);

		model.put("dateList", dateList);
		Map<Long/*cinemaid*/, List<OpenPlayItem>> opiMap = BeanUtil.groupBeanList(opiList, "cinemaid");
		model.put("opiMap", opiMap);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, opiMap.keySet());
		if(StringUtils.equals(sort, "discount")){
			cinemaList = sortCinemaListByDiscount(cinemaList, priceHelper, opiMap);
		}else if(StringUtils.equals(sort, "avgprice")){
			cinemaList = sortCinemaListByAvgprice(cinemaList, priceHelper, opiMap);
		}else{
			Collections.sort(cinemaList, new PropertyComparator("clickedtimes", false, false));
		}
		model.put("cinemaList", cinemaList);
		Map<String/*countycode*/, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "countycode");
		List<County> countyList = daoService.getObjectList(County.class, cinemaMap.keySet());
		model.put("cinemaMap", cinemaMap);
		Map<Long, CinemaProfile> cpmap = daoService.getObjectMap(CinemaProfile.class, opiMap.keySet());
		model.put("cpmap", cpmap);
		model.put("countyList", countyList);
		model.put("curmovie", curmovie);
		model.put("movieid", movieid);
		if(StringUtils.equals(VmUtils.getJsonValueByKey(partner.getOtherinfo(), PayConstant.KEY_OPENDISCOUNT),Status.Y)){
			List<SpecialDiscount> sdList = paymentService.getPartnerSpecialDiscountList(PayConstant.APPLY_TAG_MOVIE, partner.getId());
			if(!sdList.isEmpty()){
				for(SpecialDiscount sd : sdList){
					OpiFilter sfilter = new SdOpiFilter(sd, new Timestamp(System.currentTimeMillis()));
					sfilter.applyFilter(opiList);
				}
				model.put("specialDiscountOpiList", opiList);
			}
		}
		return ErrorCode.SUCCESS;
	}
	/**
	 * 按平均折扣率排序
	 * @param cinemaList
	 * @param priceHelper
	 * @param opiMap
	 * @param totalOpiCount
	 */
	protected List<Cinema> sortCinemaListByDiscount(List<Cinema> cinemaList, PartnerPriceHelper priceHelper, Map<Long/*cinemaid*/, List<OpenPlayItem>> opiMap){
		OuterSorter sorter = new OuterSorter<Cinema>(false);
		for(Cinema cinema:cinemaList){
			double discount = 0.0;
			double gewadiscount = 0.0;//相比Gewa价
			int size = opiMap.get(cinema.getId()).size();
			for(OpenPlayItem opi:opiMap.get(cinema.getId())){
				//discount +=Math.pow(1.0 - priceHelper.getPrice(opi)*1.0d/opi.getPrice(), 2);
				int price = priceHelper.getPrice(opi);
				discount += 1.0 - price*1.0d/opi.getPrice();
				gewadiscount += (opi.getGewaprice() - price)/opi.getGewaprice();
			}
			//discount = Math.sqrt(discount)/size + size * 0.01;
			discount = discount/size + size * 0.01 + gewadiscount*5/size;
			sorter.addBean(discount, cinema);
		}
		return sorter.getDescResult();
	}
	/**
	 * 按均价排序
	 * @param cinemaList
	 * @param priceHelper
	 * @param opiMap
	 * @param totalOpiCount
	 */
	protected List<Cinema> sortCinemaListByAvgprice(List<Cinema> cinemaList, PartnerPriceHelper priceHelper, Map<Long/*cinemaid*/, List<OpenPlayItem>> opiMap){
		OuterSorter sorter = new OuterSorter<Cinema>(false);
		for(Cinema cinema:cinemaList){
			double avgprice = 0.0; //均价
			double gewaprice = 0.0;//相比Gewa价
			for(OpenPlayItem opi:opiMap.get(cinema.getId())){
				int price = priceHelper.getPrice(opi);
				avgprice += price;
				gewaprice += opi.getGewaprice()-price;
			}
			int size = opiMap.get(cinema.getId()).size();
			avgprice = avgprice/size - size * 0.25 - gewaprice/size;
			sorter.addBean(avgprice, cinema);
			
		}
		return sorter.getAscResult();
	}
	//第二步：选择座位
	protected String chooseSeat(ApiUser partner, Long mpid, String ukey, String view, ModelMap model){
		ErrorCode code = addChooseSeatData(partner, mpid, ukey, model);
		if(code.isSuccess()) return view;
		return showMessage(model, code.getMsg());
	}
	protected ErrorCode addChooseSeatData(ApiUser partner, Long mpid, String ukey, ModelMap model){
		if(!partner.isEnabled()) return ErrorCodeConstant.NOT_FOUND;
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi==null) return ErrorCode.getFailure("本场次不支持订票");
		if(!opi.isOpenToPartner()) return ErrorCode.getFailure("本场次不支持订票");
		if(!opi.isOrder()) return ErrorCode.getFailure(OpiConstant.getStatusStr(opi));
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		model.put("profile", daoService.getObject(CinemaProfile.class, opi.getCinemaid()));
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		List<OpenSeat> mySeatList = new ArrayList<OpenSeat>();
		TicketOrder lastOrder = ticketOrderService.getLastUnpaidTicketOrder(partner.getId(), ukey, mpid);
		if(lastOrder != null) {
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(lastOrder.getId());
			mySeatList.addAll(daoService.getObjectList(OpenSeat.class, BeanUtil.getBeanPropertyList(seatList, Long.class, "id", true)));
		}
		model.put("mySeatList", mySeatList);
		model.put("partner", partner);
		model.put("priceHelper", priceHelper);
		model.put("cinema", daoService.getObject(Cinema.class, opi.getCinemaid()));
		model.put("movie", daoService.getObject(Movie.class, opi.getMovieid()));
		model.put("opi", opi);
		model.put("room", room);
		return ErrorCode.SUCCESS;
	}
	//第三步:锁座位，加订单
	protected ErrorCode addOrder(ApiUser partner, Long mpid, String mobile, String seatid, Member member, String paymethod, String ip, ModelMap model){
		String ukey = member.getId()+"";
		return addOrder(mpid, mobile, seatid, ukey, partner, null, member, null, paymethod, ip, model);
	}
	protected String addOrder(Long mpid, String mobile, String seatid, 
			String ukey, String returnUrl, ApiUser partner, String userid, String ip, ModelMap model){
		return addOrder(mpid, mobile, seatid, ukey, returnUrl, partner, userid, null, null, ip, model);
		
	}
	protected String addOrder(Long mpid, String mobile, String seatid, 
			String ukey, String returnUrl, ApiUser partner, String userid, 
			String event, String paymethod, String ip, ModelMap model){
		String successPath = "redirect:/partner/" + partner.getPartnerpath() + "/showOrder.xhtml";
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, userid, null, event, paymethod, ip, model);
		if(code.isSuccess()) return successPath;
		return alertMessage(model, code.getMsg(), returnUrl);
	}
	protected ErrorCode addOrder(Long mpid, String mobile, String seatid, String ukey, 
			ApiUser partner, String userid, Member member, String event, String paymethod, String ip, ModelMap model){
		List<Long> seatidList = BeanUtil.getIdList(seatid, ",");
		if(seatidList.size()==0) return ErrorCode.getFailure("请选择座位！"); 
		//0、检查验证码有没有错误
		if(StringUtils.isBlank(ukey)) return ErrorCode.getFailure("非法订单！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号有错误！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		ErrorCode<String> vcode = openPlayService.validOpiStatusByPartner(opi, partner, member);
		if(!vcode.isSuccess()){
			return ErrorCode.getFullErrorCode(vcode.getErrcode(), vcode.getMsg(), null);
		}
		//1、检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			ErrorCode lastOrder = ticketOrderService.processLastOrder(partner.getId(), ukey, seatid);
			if(!lastOrder.isSuccess()){
				return lastOrder;
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		String checkpass = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()) return remoteLockList;
		
		TicketOrderContainer tc = null;
		try{
			if(member!=null){
				tc = ticketOrderService.addTicketOrder(opi, seatidList, member, partner, mobile, checkpass, remoteLockList.getRetval());
			}else{
				tc = ticketOrderService.addPartnerTicketOrder(opi, seatidList, partner, mobile, checkpass, ukey, userid, paymethod, null, remoteLockList.getRetval());
			}
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + e.getMessage());
			return ErrorCode.getFailure(e.getMessage());
		} catch (DataIntegrityViolationException e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return  ErrorCode.getFailure("订单错误：座位被他人占用");
		} catch (Exception e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e, 4));
			return ErrorCode.getFailure("订单有错误，可能是座位有被别人占用，请刷新页面重新订座位！");
		}
		TicketOrder order = tc.getTicketOrder();
		List<SellSeat> seatList = tc.getSeatList();
		ErrorCode lockResult = ticketOperationService.lockRemoteSeat(opi, order, seatList);
		if(!lockResult.isSuccess()) {
			try{
				ticketOrderService.cancelLockFailureOrder(order);
			}catch(HibernateOptimisticLockingFailureException e){//ignore
				dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e, 10));
			}
			return lockResult;
		}
		model.put("orderId", order.getId());
		model.put("profile", daoService.getObject(CinemaProfile.class, order.getCinemaid()));
		if(StringUtils.isNotBlank(event)){
			SpecialDiscount sd = ticketOrderService.getSpdiscountBySpflag(event);
			if (sd != null) {
				ErrorCode<OrderContainer> code = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, order.getId(), sd, ip);
				Map<String, String> resutlMap = new HashMap<String, String>();
				if(!code.isSuccess()) {
					dbLogger.warn(code.getMsg());
					resutlMap.put("msg",code.getMsg());
				} else {
					SpecialDiscount discount = code.getRetval().getSd();
					resutlMap = BeanUtil.getBeanMap(discount);
				}
				model.put("specialDiscountMap", resutlMap);
				return ErrorCode.getSuccessReturn(resutlMap);
			}
		}
		return ErrorCode.SUCCESS;
	}

	//第四步：确认订单去支付
	protected String saveOrder(long orderId, String mobile, String paymethod, String paybank, Member member, ModelMap model){
		return saveOrder(orderId, mobile, paymethod, paybank, member.getId()+"", model);
	}
	/**
	 * @param orderId
	 * @param mobile
	 * @param paymethod
	 * @param paybank
	 * @param ukey
	 * @param model
	 * @return
	 */
	protected String saveOrder(long orderId, String mobile, String paymethod, String paybank, String ukey, ModelMap model){
		ErrorCode<TicketOrder> code = saveOrder(orderId, mobile, paymethod, paybank, ukey);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		Map jsonMap = new HashMap();
		TicketOrder order = code.getRetval();
		jsonMap.put("orderId", ""+order.getId());
		jsonMap.put("url", paymentService.getOrderPayUrl2(order));
		return showJsonSuccess(model, jsonMap);
	}
	protected ErrorCode<TicketOrder> saveOrder(long orderId, String mobile, String paymethod, String paybank, String ukey){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		if(!order.getUkey().equals(ukey)) return ErrorCode.getFailure("不能修改他人的订单！");
		if(order.isAllPaid() || order.isCancel()) return ErrorCode.getFailure("不能保存已支付或已（过时）取消的订单！");
		
		try{
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			ErrorCode code = ticketOrderService.checkOrderSeat(order, seatList);
			if(!code.isSuccess()){
				dbLogger.error("订单有错：" + order.getTradeNo() + code.getMsg());
				return ErrorCode.getFailure(code.getMsg());
			}
		}catch(Exception e){
			return ErrorCode.getFailure(e.getMessage());
		}
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		order.setUpdatetime(curtime);
		order.setModifytime(curtime);
		if(StringUtils.isNotBlank(mobile)) {
			if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号不正确！"); 
			order.setMobile(mobile);
		}
		order.setPaymethod(paymethod);

		if(PaymethodConstant.PAYMETHOD_PNRPAY.equals(paymethod) || 
				PaymethodConstant.PAYMETHOD_SPSDOPAY1.equals(paymethod) || 
				PaymethodConstant.PAYMETHOD_CHINAPAY2.equals(paymethod) ||
				PaymethodConstant.PAYMETHOD_CHINAPAY1.equals(paymethod) ||
				PaymethodConstant.PAYMETHOD_CHINAPAYSRCB.equals(paymethod)){
			order.setPaybank(paybank);
		}else{
			order.setPaybank(null);
		}
		Map jsonReturn = new HashMap();
		jsonReturn.put("orderId", ""+order.getId());
		if(order.isZeroPay()){//可能是用抵用券，直接结账
			order.setPaymethod(PaymethodConstant.PAYMETHOD_GEWAPAY);
			order.setPaybank(null);
		}else if(!PaymethodConstant.isValidPayMethod(paymethod)){
			return ErrorCode.getFailure("支付方式有错误！");
		}
		order.setStatus(OrderConstant.STATUS_NEW_CONFIRM);
		daoService.saveObject(order);
		CallbackOrder corder = partnerSynchService.addCallbackOrder(order, PayConstant.PUSH_FLAG_NEW, false);
		if(corder!=null) partnerSynchService.pushCallbackOrder(corder);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "去支付", order, "商家去支付,host=" + Config.getServerIp());
		return ErrorCode.getSuccessReturn(order);
	}
	
	/**
	 * 第三步、第四步合并:锁座位、加订单、去支付，不可用优惠券
	 * @param mpid
	 * @param mobile
	 * @param seatid
	 * @param paymethod
	 * @param paybank
	 * @param ukey
	 * @param returnUrl
	 * @param submitType
	 * @param partner
	 * @param userid
	 * @param model
	 * @return
	 */
	protected String addOrderAndPay(Long mpid, String mobile, String seatid,
			String paymethod, String paybank, String ukey, ApiUser partner, String userid, int pdiscount, ModelMap model){
		if(!partner.isEnabled()) return showMessage(model, "");
		//0、检查验证码有没有错误
		if(StringUtils.isBlank(ukey)) return showJsonError(model, "非法订单！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号有错误！");

		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		ErrorCode<String> vcode = openPlayService.validOpiStatusByPartner(opi, partner, null);
		if(!vcode.isSuccess()){
			return showJsonError(model, vcode.getMsg());
		}
		//1、检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			ErrorCode last = ticketOrderService.processLastOrder(partner.getId(), ukey, seatid);
			if(!last.isSuccess()){
				return showJsonError(model, last.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		List<Long> seatidList = BeanUtil.getIdList(seatid, ",");
		String randomNum = ticketOrderService.nextRandomNum(DateUtil.addDay(opi.getPlaytime(), 1), 8, "0");
		model.put("mobile", mobile);
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, false);
		if(!remoteLockList.isSuccess()){
			return showJsonError(model, "无法连接影院售票系统，请稍候再试！");
		}
		TicketOrderContainer tc = null;
		try{
			tc = ticketOrderService.addPartnerTicketOrder(opi, seatidList, partner, mobile, randomNum, ukey, userid, 
					paymethod, paybank, remoteLockList.getRetval());
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + e.getMessage());
			return showJsonError(model, e.getMessage());
		} catch (DataIntegrityViolationException e){
			dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
			return showJsonError(model, "订单有错误，座位被别人占用，请刷新页面重新订座位！");
		} catch (Exception e){
			dbLogger.error("订单错误：", e);
			return showJsonError(model, "订单有错误，可能是座位有被别人占用，请刷新页面重新订座位！");
		}
		TicketOrder order = tc.getTicketOrder();
		List<SellSeat> seatList = tc.getSeatList();
		ErrorCode lockResult = ticketOperationService.lockRemoteSeat(opi, order, seatList);
		if(!lockResult.isSuccess()){
			return showJsonError(model, "无法连接影院售票系统，请稍候再试！");
		}
		if(pdiscount > 0) {
			Discount discount = new Discount(order.getId(), PayConstant.DISCOUNT_TAG_PARTNER, partner.getId(), PayConstant.CARDTYPE_PARTNER);
			discount.setAmount(pdiscount*order.getQuantity());
			discount.setDescription("商家优惠" + discount.getAmount());
			daoService.saveObject(discount);
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			GewaOrderHelper.useDiscount(order, discountList, discount);
		}
		order.setPaymethod(paymethod);
		daoService.saveObject(order);
		Map jsonResurn = new HashMap();
		jsonResurn.put("orderId", ""+order.getId());
		//不做Tenpay的IP处理
		if(PaymethodConstant.isValidPayMethod(paymethod)){
			jsonResurn.put("pay", paymethod);
			jsonResurn.put("url", paymentService.getOrderPayUrl2(order));
		}else{
			return showJsonError(model, "支付方式有错误！");
		}
		return showJsonSuccess(model, jsonResurn);
	}
	//第四步：确认订单去支付（重新查看）
	protected ErrorCode<ApiUser> showOrder(Long orderId, Member member, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		ApiUser partner = daoService.getObject(ApiUser.class, order.getPartnerid());
		ErrorCode code = showOrder(member.getId()+"", order, partner, model);
		if(code.isSuccess()) return ErrorCode.getSuccessReturn(partner);
		return code;
	}
	/**
	 * 只针对可使用卡的商家
	 * @param ukey
	 * @param orderId
	 * @param partner
	 * @param view
	 * @param model
	 * @param request 
	 * @param response 
	 * @return
	 */
	protected String showOrder(String ukey, Long orderId, ApiUser partner, String view, ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		if(order==null) return forwardMessage(model, "订单不存在！");
		ErrorCode code = showOrder(ukey, order, partner, model);
		if(code.isSuccess()) return view;
		return showError(model, code.getMsg());
	}
	private ErrorCode showOrder(String ukey, TicketOrder order, ApiUser partner, ModelMap model){
		//0、检查验证码有没有错误
		if(!order.getUkey().equals(ukey)) return ErrorCode.getFailure("不能修改他人的订单！");
		if(order.isAllPaid() || order.isCancel()) return ErrorCode.getFailure("不能修改已支付或已（过时）取消的订单！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("order", order);
		model.put("opi", opi);
		model.put("movie", daoService.getObject(Movie.class, opi.getMovieid()));
		model.put("cinema", daoService.getObject(Cinema.class, opi.getCinemaid()));
		model.put("partner", partner);
		model.put("profile", daoService.getObject(CinemaProfile.class, opi.getCinemaid()));
		addSpdiscount(partner, opi, order, model);
		return ErrorCode.SUCCESS;
	}
	private void addSpdiscount(ApiUser partner, OpenPlayItem opi, TicketOrder order, ModelMap model){
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		boolean discard = false;
		for(Discount discount : discountList){
			if(StringUtils.equals(discount.getTag(), PayConstant.DISCOUNT_TAG_ECARD)){
				discard = true;
				break;
			}
		}
		model.put("discard", discard);
		model.put("discountList", discountList);
		//下面加入特价活动，原则是如果有可以参加的特价活动，则只显示最优惠的一条，否则显示所有不能参加的活动并给出原因
		String spFlag = VmUtils.getJsonValueByKey(partner.getOtherinfo(), PayConstant.KEY_OPENDISCOUNT);
		if(StringUtils.equals(spFlag, Status.Y) && StringUtils.contains(opi.getElecard(), PayConstant.CARDTYPE_PARTNER)){
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
			SpecialDiscountHelper sdh = new MovieSpecialDiscountHelper(opi, order, seatList, discountList);
			Map<String, String> otherInfo = VmUtils.readJsonToMap(opi.getOtherinfo());
			model.put("opiOtherinfo", otherInfo);
			PayValidHelper valHelp = new PayValidHelper();
			model.put("showDiscount", true);
			Map discountData = spdiscountService.getSpecialDiscountData(sdh, valHelp, order, true, opi.getSpflag(), 
					discountList, SpecialDiscount.OPENTYPE_PARTNER, PayConstant.APPLY_TAG_MOVIE);
			model.putAll(discountData);
		}
	}
	
	protected ErrorCode addSeatData(Long mpid, Long partnerid, String ukey, ModelMap model){
		ApiUser partner = daoService.getObject(ApiUser.class, partnerid);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		ErrorCode<String> code = openPlayService.validOpiStatusByPartner(opi, partner, null);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, booking.getMsg());
		}

		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, OpenSeat> seatMap = new HashMap<String, OpenSeat>();
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(mpid);
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		model.put("seatStatusUtil", seatStatusUtil);
		//火凤凰占用座位
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT, false);
		if(!remoteLockList.isSuccess()){
			return ErrorCode.getFailure("影院服务器连接不正常，请稍候再试！");
		}
		model.put("hfhLockList", remoteLockList.getRetval());
		SeatPriceHelper sph = new SeatPriceHelper(opi, partnerid);
		Map<Long, Integer> priceMap = new HashMap<Long, Integer>();
		if(openSeatList.size() > 0){
			for(OpenSeat seat:openSeatList){
				rowMap.put(seat.getLineno(), seat.getSeatline());
				seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
				priceMap.put(seat.getId(), sph.getPrice(seat));
			}
		}
		List<OpenSeat> mySeatList = new ArrayList<OpenSeat>();
		TicketOrder lastOrder = ticketOrderService.getLastUnpaidTicketOrder(partner.getId(), ukey, mpid);
		if(lastOrder != null) {
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(lastOrder.getId());
			mySeatList.addAll(daoService.getObjectList(OpenSeat.class, BeanUtil.getBeanPropertyList(seatList, Long.class, "id", true)));
		}
		model.put("mySeatList", mySeatList);
		model.put("opi", opi);
		model.put("room", room);
		model.put("price", priceHelper.getPrice(opi));
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("priceHelper", priceHelper);
		model.put("priceMap", priceMap);
		model.put("seatList", BeanUtil.getBeanMapList(mySeatList, "id", "seatLabel", "price"));
		
		List<MovieMpiRemark> movieMpiRemark = nosqlService.getMovieMpiRemarkList(opi.getMovieid(), opi.getCitycode(), 5);
		model.put("movieMpiRemark", movieMpiRemark);

		return ErrorCode.SUCCESS;
	}
	
	protected List<Movie> getOpenMovieListByDate(ApiUser partner, String citycode, Date playdate, 
			HttpServletResponse response, HttpServletRequest request,ModelMap model){
		citycode = this.getCitycodeByPartner(citycode, partner, request, response, model);
		List<Movie> movieList = getOpenMovieListByDate(partner, citycode, playdate);
		return movieList;
	}
	
	protected List<Movie> getOpenMovieListByDate(ApiUser partner, String citycode, Date playdate){
		List<Movie> movieList = partnerService.getOpenMovieListByDate(partner, citycode, playdate);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter closeRule = new CloseRuleOpiFilter(partner, pcrList);
		closeRule.filterMovie(movieList);
		mcpService.sortMoviesByMpiCount(citycode, movieList);
		return movieList;
	}
	
	protected String getCitycodeByPartner(String citycode, ApiUser partner, HttpServletRequest request, HttpServletResponse response, HashMap<String, Object> model){
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode))citycode = partner.getDefaultCity();
			WebUtils.setCitycode(request, citycode, response);
		}else{
			citycode = this.getCitycodeByPartner(partner, request, response);
		}
		model.put("cityname", AdminCityContant.getCitycode2CitynameMap().get(citycode));
		model.put("citycode", citycode);
		return citycode;
	}
	
	protected String getCitycodeByPartner(ApiUser partner, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(partner.supportsCity(citycode)) return citycode;
		return partner.getDefaultCity();
	}
	
	protected boolean filterMovieList(String moviename,List<Movie> movieList,ModelMap model){
		if(StringUtils.isNotBlank(moviename)){
			for (Movie movie :movieList) {
				if(StringUtils.contains(movie.getMoviename(),moviename)){
					List list = new ArrayList();
					list.add(movie);
					model.put("movieList", list);
					return true;
				}
			}
		}else{
			model.put("movieList", movieList);
		}
		return false;
	}
	
}
