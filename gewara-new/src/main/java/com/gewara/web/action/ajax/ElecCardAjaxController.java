package com.gewara.web.action.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class ElecCardAjaxController extends AnnotationController {
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;

	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;

	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;

	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@RequestMapping("/ajax/trade/registerCard.xhtml")
	public String registerCard(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String cardpass, String captchaId, String captcha, ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(memberAccount == null || memberAccount.isNopassword()){
			Map<String, String> errorMap = new HashMap<String, String>();
			errorMap.put("needPayPass", "true");
			errorMap.put("msg", "为了你的账户安全，请先设置支付密码！");
			return showJsonError(model, errorMap);
		}
		if(StringUtils.startsWith(cardpass, SpCode.PASSPRE)){//特价券
			return showJsonError(model, "非电子券，不能绑定！！");
		}
		ErrorCode<String> code = elecCardService.registerCard(member, cardpass, ip);
		if(code.isSuccess()) {
			return showJsonSuccess(model);
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/ajax/trade/useElecCard.xhtml")
	public String useElecCard(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, 
			Long orderId, String tag, String cardno,String password, String from, ModelMap model) {
		//1、合作商
		if(StringUtils.equals(from, "partner")){
			return partnerUseCard(orderId, request.getParameter("cardpass"), model);
		}
		//2、
		
		//x、Gewara网站
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ElecCard card = null;
		if(StringUtils.equals("cardno", tag)) {
			if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡号！");
			card = elecCardService.getMemberElecCardByNo(member.getId(), cardno);
			if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		}else {//此处可能是密码
			if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡密码！");
			if(StringUtils.startsWith(cardno, SpCode.PASSPRE)){//特价券
				ErrorCode<? extends OrderContainer> useResult = specialDiscountService.useSpecialDiscountBySpCodePass(OrderConstant.ORDER_TYPE_TICKET, orderId, ip, member.getId(), cardno);
				if(useResult.isSuccess()) {
					Map jsonMap = new HashMap();
					jsonMap.put("distype", "discount");
					return showJsonSuccess(model, jsonMap);
				}else{
					return showJsonError(model, useResult.getMsg()+"<br/>如有疑问请联系客服：4000-406-506");
				}
			}
			card = elecCardService.getElecCardByPass(StringUtils.upperCase(cardno));
		}
		if(StringUtils.isNotBlank(password)) {
			MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
			if(!StringUtils.equals(PayUtil.getPass(password), memberAccount.getPassword())) return showJsonError(model, "支付密码错误！");
		}
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		return useElecCard(orderId, card, member.getId(), model);
	}
	private String partnerUseCard(Long orderId, String cardpass, ModelMap model){
		ElecCard card = elecCardService.getElecCardByPass(StringUtils.upperCase(cardpass));
		ErrorCode code = ticketDiscountService.useElecCard(orderId, card, null);
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}

	private String useElecCard(Long orderId, ElecCard card, Long memberid, ModelMap model){
		/*if(card.needActivation()){
			Map jsonMap = new HashMap();
			jsonMap.put("activation", "true");
			jsonMap.put("msg", card.getCardno());
			return showJsonError(model, jsonMap);
		}*/
		ErrorCode<TicketOrderContainer> code = ticketDiscountService.useElecCard(orderId, card, memberid);
		if(code.isSuccess()) {
			Map jsonMap = new HashMap<String, String>(); 
			jsonMap.put("distype", "ecard");
			jsonMap.put("cardno", card.getCardno());
			jsonMap.put("validtime", DateUtil.format(card.getTimeto(), "yyyy-MM-dd"));
			List<Discount> discountList = code.getRetval().getDiscountList();
			Discount curDiscount = code.getRetval().getCurUsedDiscount();
			jsonMap.put("description", curDiscount.getDescription());
			jsonMap.put("discountId", curDiscount.getId());
			jsonMap.put("discount", curDiscount.getAmount());
			jsonMap.put("usage", card.gainUsage());

			TicketOrder order = code.getRetval().getTicketOrder();
			jsonMap.put("count", discountList.size());
			jsonMap.put("due", order.getDue());
			jsonMap.put("totalDiscount", order.getDiscount());
			jsonMap.put("totalAmount", order.getTotalAmount());
			jsonMap.put("type", card.getCardtype());
			jsonMap.put("exchangetype", card.getEbatch().getExchangetype());
			String bindgoods = VmUtils.getJsonValueByKey(order.getOtherinfo(), PayConstant.KEY_BINDGOODS);
			if(StringUtils.isNotBlank(bindgoods)){
				Goods goods = daoService.getObject(Goods.class, new Long(bindgoods));
				if(goods!=null) jsonMap.put("bindGoods", goods.getGoodsname());
			}
			return showJsonSuccess(model, jsonMap);
		}
		if(ApiConstant.CODE_USER_NORIGHTS.equals(code.getErrcode())){
			Map jsonMap = new HashMap();
			jsonMap.put("bindAndDelayCard", "true");
			jsonMap.put("msg", code.getMsg());
			jsonMap.put("curTime", System.currentTimeMillis());
			return showJsonError(model, jsonMap);
		}
		return showJsonError(model, code.getMsg()+"<br/>如有疑问请联系客服：4000-406-506");
	}

}
