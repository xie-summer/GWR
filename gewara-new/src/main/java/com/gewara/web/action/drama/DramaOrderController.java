package com.gewara.web.action.drama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.command.TheatrePriceCommand;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.DisQuanHelper;
import com.gewara.helper.GoodsPriceHelper;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.OrderOther;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.drama.TheatreOrderService;
import com.gewara.untrans.drama.impl.DramaControllerService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;

@Controller
public class DramaOrderController extends BaseDramaController {
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService){
		this.elecCardService = elecCardService;
	}
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	public void setDramaOrderService(DramaOrderService dramaOrderService) {
		this.dramaOrderService = dramaOrderService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	@Autowired@Qualifier("theatreOrderService")
	private TheatreOrderService theatreOrderService;
	
	@Autowired@Qualifier("dramaControllerService")
	private DramaControllerService dramaControllerService;
	
	@RequestMapping("/drama/tspDis.xhtml")
	public String tspDis(Long tspid,  ModelMap model){
		String result = "";
		TheatreSeatPrice tsp = daoService.getObject(TheatreSeatPrice.class, tspid);
		List<DisQuantity> disquanList = dramaPlayItemService.getDisQuantityList(tspid);
		DisQuanHelper disHelper = new DisQuanHelper(disquanList);
		if(tsp!=null) result = disHelper.getDisInfo();
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/drama/order/useCardByPass.xhtml")
	public String useCardByPass(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, 
			Long orderId, String cardpass, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(cardpass)) return showJsonError(model, "请输入卡密码！");
		ElecCard card = elecCardService.getElecCardByPass(StringUtils.upperCase(cardpass));
		return useElecCard(orderId, card, member.getId(), model);
	}
	@RequestMapping("/drama/order/useElecCardByNo.xhtml")
	public String useElecCardByNo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String cardno, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡号！");
		ElecCard card = elecCardService.getMemberElecCardByNo(member.getId(), cardno);
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		if(card.getPossessor()==null || !card.getPossessor().equals(member.getId())){
			return showJsonError(model, "卡号有错误，重新输入！");
		}

		return useElecCard(orderId, card, member.getId(), model);
	}
	private String useElecCard(Long orderId, ElecCard card, Long memberid, ModelMap model){
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		DramaOrder order = daoService.getObject(DramaOrder.class, orderId);
		ErrorCode validCode = paymentService.validUse(order);
		if(!validCode.isSuccess()) return showJsonError(model, validCode.getMsg());
		ErrorCode<DramaOrder> code = dramaOrderService.useElecCard(order, card, memberid);
		if(code.isSuccess()) {
			Map jsonMap = new HashMap<String, String>(); 
			jsonMap.put("cardno", card.getCardno());
			jsonMap.put("validtime", DateUtil.format(card.getTimeto(), "yyyy-MM-dd"));
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			for(Discount discount: discountList){
				if(discount.getRelatedid().equals(card.getId())){
					jsonMap.put("description", discount.getDescription());
					jsonMap.put("discountId", discount.getId());
					jsonMap.put("discount", discount.getAmount());
					jsonMap.put("usage", card.gainUsage());
					break;
				}
			}
			jsonMap.put("count", discountList.size());
			jsonMap.put("due", code.getRetval().getDue());
			jsonMap.put("totalDiscount", order.getDiscount());
			return showJsonSuccess(model, jsonMap);
		}
		Map jsonMap = (Map) code.getRetval();
		if(jsonMap != null){
			return showJsonError(model, jsonMap);
		}
		return showJsonError(model, code.getMsg()+"如有疑问请联系客服！");
	}
	@RequestMapping("/drama/order/saveOrderDis.xhtml")
	public String saveOrderInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Long orderId, String discounttype, Integer usepoint, Long addressRadio, String selectTicket, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		DramaOrder order = daoService.getObject(DramaOrder.class, orderId);
		if(order == null) return showJsonError(model, "订单错误!");
		OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(item, buyList);
		Map<Long, OpenDramaItem> odiMap = BeanUtil.beanListToMap(itemList, "dpid");	
		OrderOther orderOther = theatreOrderService.getDramaOrderOtherData(order, buyList, odiMap, model);
		if(!StringUtils.contains(orderOther.getTakemethod(), selectTicket)) return showJsonError(model, "请选择取票方式！");
		if(StringUtils.equals(selectTicket, TheatreProfile.TAKEMETHOD_E)){
			MemberUsefulAddress memberUsefulAddress = daoService.getObject(MemberUsefulAddress.class, addressRadio);
			if(memberUsefulAddress == null) return showJsonError(model, "请填写快递地址！");
			ExpressConfig expressConfig = daoService.getObject(ExpressConfig.class, orderOther.getExpressid());
			ErrorCode<OrderAddress> code = ticketOrderService.createOrderAddress(order, memberUsefulAddress, expressConfig);
			if(!code.isSuccess()){
				return showJsonError(model, code.getMsg());
			}
			ErrorCode<Integer> code2 = ticketOrderService.computeExpressFee(order, expressConfig, code.getRetval().getProvincecode());
			if(!code2.isSuccess()){
				return showJsonError(model, code2.getMsg());
			}
		}else if(orderOther.hasTakemethod(TheatreProfile.TAKEMETHOD_E, TheatreProfile.TAKEMETHOD_A)){
			ErrorCode<Integer> code2 = ticketOrderService.clearExpressFee(order);
			if(!code2.isSuccess()){
				return showJsonError(model, code2.getMsg());
			}
		}
		if(StringUtils.equals(discounttype, "point")){
			if(usepoint == null) return showJsonError(model, "积分不正确!");
			return usePoint(member, orderId, usepoint, model);
		}else if(StringUtils.equals(discounttype, "card")){
			boolean usecard = false;
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			for(Discount discount: discountList){
				if("ABCD".indexOf(discount.getCardtype())>=0) usecard = true;
			}
			if(!usecard) return showJsonError(model, "您选择了话剧票券优惠，但未使用任何票券!");
		}else if(StringUtils.equals(discounttype, "none")){
			if(order.getDiscount() > 0) return showJsonError(model, "您选择了不使用优惠，但订单中使用了其他优惠!");
		}else if(StringUtils.isNotBlank(discounttype)){
			try{
				Long spid = Long.parseLong(discounttype);
				SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
				if(StringUtils.isNotBlank(sd.getValidateUrl())){
					Map jsonMap = new HashMap<String, String>();
					jsonMap.put("url", sd.getValidateUrl() + "?orderId=" + order.getId() + "&spid=" + sd.getId());
					return showJsonSuccess(model, jsonMap);
				}
				ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_DRAMA, order.getId(), sd, WebUtils.getRemoteIp(request));
				if(discount.isSuccess()) return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
				return showJsonError(model, discount.getMsg());
			}catch(Exception e){
				return showJsonError(model, "使用优惠有错误!"); 
			}
		}
		return showJsonSuccess(model);
	}
	private String usePoint(Member member, Long orderId, int pointvalue, ModelMap model){
		ErrorCode code = dramaOrderService.usePoint(orderId, member.getId(), pointvalue);
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping("/drama/order/chooseArea.xhtml")
	public String chooseArea(Long itemid, String spkey, HttpServletRequest request, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(odi==null || item==null) return showMessageAndReturn(model, request, "该场次不存在！");
		if(!odi.isBooking()) return showMessageAndReturn(model, request, "本场次不接受预定！");
		TheatreField field = daoService.getObject(TheatreField.class, item.getRoomid());
		if(field == null) return showMessageAndReturn(model, request, "场地不存在！");
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		if(CollectionUtils.size(seatAreaList) == 1){
			model.put("itemid", itemid);
			model.put("areaid", seatAreaList.get(0).getId());
			model.put("spkey", spkey);
			return showRedirect("drama/order/step1.xhtml", model);
		}
		List<TheatreSeatPrice> priceList = dramaPlayItemService.getTspList(item.getId());
		Map<Long, List<TheatreSeatPrice>> seatPriceMap = BeanUtil.groupBeanList(priceList, "areaid");
		model.put("seatPriceMap", seatPriceMap);
		model.put("odi", odi);
		model.put("item", item);
		model.put("field", field);
		model.put("seatAreaList", seatAreaList);
		Map<Long, String> areaZoneMap = BeanUtil.beanListToMap(seatAreaList, "id", "hotzone", true);
		model.put("areaZoneMap", JsonUtils.writeObjectToJson(areaZoneMap));
		return "drama/ticket/chooseArea.vm";
	}
	
	@RequestMapping("/drama/order/step1.xhtml")
	public String step1(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, HttpServletResponse response, 
			@RequestParam("itemid")Long itemid, Long areaid, String spkey, ModelMap model){
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		ErrorCode code = dramaControllerService.addSeatData(model, odi, areaid, response, request, spkey);
		if(!code.isSuccess()) return showMessageAndReturn(model, request, code.getMsg());
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		return "drama/ticket/wide_chooseSeat.vm";
	}
	
	
	
	@RequestMapping("/ajax/drama/getSeatPage.shtml")
	public String getSeatPage(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long areaid, ModelMap model2){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member==null) return showJsonError(model2, "您还没有登录，请先登录！");
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		
		ErrorCode<Map> code = dramaControllerService.getSeatPage(seatArea);
		if(!code.isSuccess()){
			return showJsonError(model2, code.getMsg());
		}
		Map model = code.getRetval();
		List<OpenTheatreSeat> mySeatList = new ArrayList<OpenTheatreSeat>();
		if(member!=null){
			Long memberid = member.getId();
			DramaOrder order = dramaOrderService.getLastUnpaidDramaOrder(memberid, memberid+"", seatArea.getDpid());
			if(order!=null) {
				List<SellDramaSeat> seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
				for (Iterator<SellDramaSeat> iterator = seatList.iterator(); iterator.hasNext();) {
					SellDramaSeat seat = iterator.next();
					if(!seatArea.getId().equals(seat.getAreaid())) iterator.remove();
				}
				mySeatList.addAll(daoService.getObjectList(OpenTheatreSeat.class, BeanUtil.getBeanPropertyList(seatList, Long.class, "id", true)));
			}
			model.put("logonMember", member);
		}
		Map jsonMap = new HashMap();
		final String template = "drama/wide_seatPage.vm";
		String seatPage = velocityTemplate.parseTemplate(template, model);
		jsonMap.put("seatPage", seatPage);
		jsonMap.put("seatList", mySeatList);
		return showJsonSuccess(model2, jsonMap);
	}
	
	@RequestMapping("/drama/order/choosePrice.xhtml")
	public String choosePrice(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String pricelist, ModelMap model){
		if(StringUtils.isBlank(pricelist)) showJsonError(model, "场次或价格错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(!GoodsPriceHelper.isValidData(pricelist)) return showJsonError(model, "场次或价格错误！");
		List<TheatrePriceCommand> commandList = new ArrayList<TheatrePriceCommand>();
		try{
			commandList = JsonUtils.readJsonToObjectList(TheatrePriceCommand.class, pricelist);
		}catch (Exception e) {
			return showJsonError(model, "场次或价格错误！");
		}
		ErrorCode<Map> code = theatreOrderService.getTheatreSeatPriceInfo(commandList);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		model.putAll(code.getRetval());
		model.put("commandJson", JsonUtils.writeObjectToJson(commandList));
		model.put("logonMember", member);
		return "drama/ticket/chooseConfirmOrder.vm";
	}
	
	@RequestMapping("/drama/order/stepPrice.xhtml")
	public String stepPrice(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,@CookieValue(required=false,value="origin") String origin,
			HttpServletRequest request,	String captchaId, String captcha, String pricelist, String mobile, String spkey, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		//检查该用户有没有未付款的订单，如果有，将所有未付款的订单取消
		try{
			ErrorCode lastOrder = dramaOrderService.processLastOrder(member.getId(), member.getId().toString());
			if(!lastOrder.isSuccess()){
				return showJsonError(model, lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 15));
		}
		ErrorCode<DramaOrder> code = theatreOrderService.addDramaOrder(pricelist, member, mobile, spkey);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		if(StringUtils.isNotBlank(origin)){
			ticketOrderService.addOrderOrigin(code.getRetval(), origin);
		}
		return showJsonSuccess(model, code.getRetval().getId() + "");
	}
	
	@RequestMapping("/drama/order/step2.xhtml")
	public String step2(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,@CookieValue(required=false,value="origin") String origin,
			HttpServletRequest request, @RequestParam("itemid")Long itemid, Long areaid, String captchaId, String captcha, String mobile, String seatid, Long disid, String spkey, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", itemid, false);
		TheatreSeatArea seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		if(seatArea == null) return showJsonError(model, "场次区域数据不存在！");
		List<Long> seatidList = BeanUtil.getIdList(seatid, ",");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model,"手机号有错误！");
		if(seatidList.size()==0) return showJsonError(model, "请选择场座位！");
		if(seatidList.size() > odi.getMaxbuy()) return showJsonError(model,"每次最多选" +odi.getMaxbuy()+"个座位！");
		ErrorCode<DramaOrder> code = theatreOrderService.addDramaOrder(odi, seatArea, seatidList, disid, mobile, member, spkey);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		if(StringUtils.isNotBlank(origin)){
			ticketOrderService.addOrderOrigin(code.getRetval(), origin);
		}
		return showJsonSuccess(model, code.getRetval().getId() + "");
	}
	
	/**
	 * 异步获取座位图信息
	 * */
	@RequestMapping("/ajax/drama/seatView.shtml")
	public String ajaxSeatView(Long roomid, ModelMap model){
		TheatreRoom room = daoService.getObject(TheatreRoom.class, roomid);
		model.put("room", room);
		String viewPage = velocityTemplate.parseTemplate("drama/ajaxSeatPageView.vm", model);
		Map jsonMap = new HashMap();
		jsonMap.put("viewPage", viewPage);
		return showJsonSuccess(model, jsonMap);
	}
	

	@RequestMapping("/ajax/drama/saveGreetings.xhtml")
	public String saveGreetings(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String greeting, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		DramaOrder order = daoService.getObject(DramaOrder.class, orderId);
		if(order == null) return showJsonError(model, "订单错误！");
		if(!member.getId().equals(order.getMemberid())) return showJsonError(model, "不能修改他人订单！");
		ErrorCode code = theatreOrderService.updateOtherInfo(order, greeting, member.getId(), model);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}
}

