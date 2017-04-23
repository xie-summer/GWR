package com.gewara.web.action.sport;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.MemberCardConstant;
import com.gewara.helper.SportSynchHelper;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.sport.MemberCardService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.sport.RemoteMemberCardService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.sport.RemoteMemberCardOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

@Controller
public class MemberCardController extends BaseSportController{
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	@Autowired@Qualifier("remoteMemberCardService")
	private RemoteMemberCardService remoteMemberCardService;
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}
	@RequestMapping("/sport/order/memberCard/showCardType.xhtml")
	public String showCardType(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long mctid, Long placeid, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, mctid);
		if(mct==null || !mct.hasBooking()){
			return showJsonError(model, "该类型的卡暂不接受预定！");
		}
		ErrorCode<RemoteMemberCardType> tcode = remoteMemberCardService.getRemoteMemberCardTypeByKey(mct.getCardTypeUkey());
		if(!tcode.isSuccess()){
			return showJsonError(model, tcode.getMsg());
		}
		RemoteMemberCardType rmct = tcode.getRetval();
		if(rmct==null){
			return showJsonError(model, "该类型已暂停购买！"); 
		}
		SportSynchHelper.copyMemberCardType(mct,rmct);
		daoService.saveObject(mct);
		String fitItem = memberCardService.getFitItem(mct.getFitItem());
		List<Sport> sportList = memberCardService.getFitSportList(mct.getBelongVenue());
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		model.put("mct", mct);
		model.put("fitItem", fitItem);
		model.put("sportList", sportList);
		model.put("member", member);
		model.put("placeid", placeid);
		model.put("sport", daoService.getObject(Sport.class, placeid));
		return "sport/memberCard/showCardType.vm";
	}
	
	
	@RequestMapping("/sport/order/memberCard/step1.xhtml")
	public String step1(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long mctid, ModelMap model) {
		MemberCardType mct = daoService.getObject(MemberCardType.class, mctid);
		if(mct==null || !mct.hasBooking()){
			return forwardMessage(model, "该类型的卡暂不接受预定！");
		}
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		model.put("mct", mct);
		model.put("member", member);
		return "sport/memberCard/wide_choosebyTime.vm";
	}
	
	@RequestMapping("/sport/order/memberCard/step2.xhtml")
	public String step2(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long mctid, Long placeid, String mobile, String captchaId, String captcha, ModelMap model) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		try{
			ErrorCode lastOrder = memberCardService.processLastOrder(member.getId(), member.getId().toString());
			if(!lastOrder.isSuccess()){
				return showJsonError(model, lastOrder.getMsg());
			}
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
		}
		Sport sport = daoService.getObject(Sport.class, placeid);
		if(sport==null){
			return showJsonError(model, "场馆不存在！");
		}
		MemberCardType mct = daoService.getObject(MemberCardType.class, mctid);
		ErrorCode<MemberCardOrder> code = null;
		try {
			code = memberCardService.addMemberCardOrder(mct, placeid, mobile, member);
		} catch (OrderException e) {
			dbLogger.error("订单错误：" + e.getMessage());
			return showJsonError(model, e.getMessage());
		}
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		MemberCardOrder order = code.getRetval();
		ErrorCode<RemoteMemberCardOrder> ocode = remoteMemberCardService.createRemoteMemberCardOrder(order, mct);
		if(!ocode.isSuccess()){
			memberCardService.cancelLockFailureOrder(order);
			return showJsonError(model, ocode.getMsg());
		}else {
			RemoteMemberCardOrder rmco = ocode.getRetval();
			Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
			otherMap.put(MemberCardConstant.CUS_TRADENO, rmco.getTradeNo());
			order.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
			daoService.saveObject(order);
		}
		return showJsonSuccess(model, code.getRetval().getId()+"");
	}
	
	@RequestMapping("/sport/order/memberCard/saveOrderDis.xhtml")
	public String saveOrderInfo(String mobile, ModelMap model){
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机格式不正确");
		return showJsonSuccess(model);
	}
	@RequestMapping("/sport/order/memberCard/sendMobilePass.xhtml")
	public String bindSportMemberCard(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, String captchaId, String captcha, Long orderid){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, ip);
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		SportOrder sorder = daoService.getObject(SportOrder.class, orderid);
		if(!member.getId().equals(sorder.getMemberid())){
			return showJsonError(model, "非法操作！");
		}
		MemberCardInfo memberCard = daoService.getObject(MemberCardInfo.class, sorder.getCardid());
		ErrorCode<String> code = remoteMemberCardService.getMobileCheckpass(memberCard.getMobile(), MemberCardConstant.CHECKPAS_TYPE_CPAY);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/gewapay/memberCard/payOrder.xhtml")
	public String webPayOrder(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		ErrorCode code = orderProcessService.memberCardPayOrderAtServer(orderId, member.getId());
		if(code.isSuccess()) {
			model.put("orderId", orderId);
			return "redirect:/gewapay/orderResult.xhtml";
		}
		return alertMessage(model, code.getMsg(), "gewapay/order.xhtml?orderId=" + orderId);
	}
}
