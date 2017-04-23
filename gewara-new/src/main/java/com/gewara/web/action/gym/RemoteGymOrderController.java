package com.gewara.web.action.gym;

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

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.pay.PayUtil;
import com.gewara.service.OpenGymService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.gym.CardItem;
@Controller
public class RemoteGymOrderController extends AnnotationController {
	
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("openGymService")
	private OpenGymService openGymService;
	public void setOpenGymService(OpenGymService openGymService){
		this.openGymService = openGymService;
	}
	
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	
	@RequestMapping("/ajax/gym/saveGymOrderInfo.xhtml")
	public String saveOrderInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String addressRadio, String realname, String IDcard, String telphone, String discounttype, Integer usepoint, ModelMap model){
		GymOrder order = daoService.getObject(GymOrder.class, orderId);
		if(order == null) return showJsonError(model, "该订单不存在！");
		if(order.isTimeout()) return showJsonError(model, "该订单已超时！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(realname)) return showJsonError(model, "姓名不能为空！");
		if(StringUtils.isBlank(telphone)) return showJsonError(model, "电话号码不能为空！");
		if(!ValidateUtil.isMobile(telphone)) return showJsonError(model, "电话号码格式错误！");
		if(StringUtils.isBlank(IDcard)) return showJsonError(model, "身份证号不能为空！");
		if(!ValidateUtil.isIDCard(IDcard)) return showJsonError(model, "请填写有效的身份证号码！");
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		MemberUsefulAddress memberUsefulAddress = null;
		if(StringUtils.equals(addressRadio, "0")){
			memberUsefulAddress = new MemberUsefulAddress();
			memberUsefulAddress.setAddtime(DateUtil.getCurFullTimestamp());
			memberUsefulAddress.setMemberid(order.getMemberid());
			daoService.saveObject(memberUsefulAddress);
		}else{
			if(StringUtils.isBlank(addressRadio) || !ValidateUtil.isNumber(addressRadio)) return showJsonError(model, "获取历史数据错误!");
			memberUsefulAddress = daoService.getObject(MemberUsefulAddress.class, Long.parseLong(addressRadio));
		}
		if(memberUsefulAddress == null || !member.getId().equals(memberUsefulAddress.getMemberid())) return showJsonError(model, "购买信息错误，请核实！");
		memberUsefulAddress.setRealname(realname);
		memberUsefulAddress.setMobile(telphone);
		memberUsefulAddress.setIDcard(IDcard);
		daoService.saveObject(memberUsefulAddress);
		otherInfoMap.put("realname", realname);
		otherInfoMap.put("telphone", telphone);
		otherInfoMap.put("IDcard", IDcard);
		order.setOtherinfo(JsonUtils.writeObjectToJson(otherInfoMap));
		daoService.saveObject(order);
		if(StringUtils.equals(discounttype, "point")){
			if(usepoint == null) return showJsonError(model, "积分不正确!");
			return usePoint(sessid, request, orderId, usepoint, model);
		}else if(StringUtils.equals(discounttype, "card")){
			boolean usecard = false;
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			for(Discount discount: discountList){
				if("ABCD".indexOf(discount.getCardtype())>=0) usecard = true;
			}
			if(!usecard) return showJsonError(model, "您选择了健身票券优惠，但未使用任何票券!");
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
				ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_GYM, orderId, sd, ip);
				if(discount.isSuccess()) return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
				return showJsonError(model, discount.getMsg());
			}catch(Exception e){
				return showJsonError(model, "使用优惠有错误!"); 
			}
		}
		return showJsonSuccess(model, order.getId()+"");
	}
	
	@RequestMapping("/order/gym/useGymPoint.xhtml")
	public String usePoint(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, int pointvalue, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GymOrder order = daoService.getObject(GymOrder.class, orderId);
		if(order == null) return showJsonError(model, "订单不存在！");
		ErrorCode<CardItem> cardCode = synchGymService.getGymCardItem(order.getGci(), true);
		if(!cardCode.isSuccess()) return showJsonError(model, cardCode.getMsg());
		ErrorCode code = openGymService.usePoint(orderId, cardCode.getRetval(), member.getId(), pointvalue);
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/order/gym/useGymCardByPass.xhtml")
	public String useCardByPass(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String cardpass, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(cardpass)) return showJsonError(model, "请输入卡密码！");
		ElecCard card = elecCardService.getElecCardByPass(StringUtils.upperCase(cardpass));
		return useElecCard(orderId, card, member.getId(), model);
	}
	@RequestMapping("/order/gym/useGymElecCardByNo.xhtml")
	public String useElecCardByNo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String password,
			HttpServletRequest request, Long orderId, String cardno, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(password)) return showJsonError(model, "支付密码不能为空！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
		if(memberAccount == null || memberAccount.isNopassword()) return showJsonError(model, "先创建帐号或设置支付密码！"); 
		if(!StringUtils.equals(PayUtil.getPass(password), memberAccount.getPassword())) return showJsonError(model, "支付密码错误！");
		if(StringUtils.isBlank(cardno)) return showJsonError(model, "请输入卡号！");
		ElecCard card = elecCardService.getMemberElecCardByNo(member.getId(), cardno);
		return useElecCard(orderId, card, member.getId(), model);
	}
	private String useElecCard(Long orderId, ElecCard card, Long memberid, ModelMap model){
		if(card==null) return showJsonError(model, "卡密码不存在，重新输入！");
		if(card.needActivation()){
			Map jsonMap = new HashMap();
			jsonMap.put("activation", "true");
			jsonMap.put("msg", card.getCardno());
			return showJsonError(model, jsonMap);
		}
		GymOrder gymOrder = daoService.getObject(GymOrder.class, orderId);
		ErrorCode<CardItem> cardCode = synchGymService.getGymCardItem(gymOrder.getGci(), true);
		if(!cardCode.isSuccess()) return showJsonError(model, cardCode.getMsg());
		ErrorCode<GymOrder> code = openGymService.useElecCard(orderId, cardCode.getRetval(), card, memberid);
		if(code.isSuccess()) {
			Map jsonMap = new HashMap<String, String>(); 
			jsonMap.put("cardno", card.getCardno());
			jsonMap.put("validtime", DateUtil.formatTimestamp(card.getTimeto()));
			GymOrder order = code.getRetval();
			List<Discount> discountList = paymentService.getOrderDiscountList(order);
			for(Discount discount: discountList){
				if(discount.getRelatedid().equals(card.getId())){
					jsonMap.put("description", discount.getDescription());
					jsonMap.put("discountId", discount.getId());
					jsonMap.put("usage", card.gainUsage());
					jsonMap.put("discount", discount.getAmount());
					break;
				}
			}
			jsonMap.put("due", order.getDue());
			jsonMap.put("count", discountList.size());
			jsonMap.put("totalDiscount", code.getRetval().getDiscount());
			jsonMap.put("totalAmount", code.getRetval().getTotalAmount());
			jsonMap.put("type", card.getCardtype());
			jsonMap.put("exchangetype", card.getEbatch().getExchangetype());
			return showJsonSuccess(model, jsonMap);
		}
		return showJsonError(model, code.getMsg()+"如有疑问请联系客服！");
	}
}
