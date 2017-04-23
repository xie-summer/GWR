package com.gewara.web.action.gewapay;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.pay.PayValidHelper;

@Controller
public class GewaPayJumpController extends BasePayController{
	/**
	 * 特价活动针对特殊号段的手机号码进行验证
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 */
	@RequestMapping("/gewapay/jump/validateSpMobile.xhtml")
	public String validateSpMobile(Long orderId,Long spid, ModelMap model) {
		Member member = getLogonMember();
		if(member == null){
			dbLogger.error("memer is null:orderid=" + orderId + "|spid=" + spid);
		}
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) {
			return show404(model, "不能修改他人的订单！");
		}
		if (order.isAllPaid() || order.isCancel()) {
			return show404(model, "不能保存已支付或已（过时）取消的订单！");
		}
		model.put("order", order);
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		if(sd == null){
			return show404(model, "您选择的优惠活动不正确，请重新选择！");
		}
		return "gewapay/jumpValidateMobile.vm";
	}
	
	/**
	 * 银联认证支付2.0,优惠活动-跳转链接
	 * 
	 * @param orderId
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 19, 2013 5:44:15 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast.xhtml")
	public String unionPayFastJump(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "ALL");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	/**
	 * 苏洲中国银行，银联认证支付2.0,优惠活动-跳转链接
	 * 
	 * 
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 7, 2013 6:10:51 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast/sz.xhtml")
	public String unionPayFastJumpForSZ(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "SZ");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	/**
	 * 农行-格瓦拉联合营销活动 ，银联认证支付2.0,优惠活动-跳转链接
	 * 
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:30:39 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast/nyyh.xhtml")
	public String unionPayFastJumpForNyyh(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "NYYH");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	/**
	 * 重庆农商行活动，银联认证支付2.0,优惠活动-跳转链接
	 * 
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:30:39 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast/cqnsyh.xhtml")
	public String unionPayFastJumpForCqnsyh(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "CQNSYH");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	/**
	 * 银联卡友节活动，银联认证支付2.0,优惠活动-跳转链接
	 * 
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:30:39 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast/youjie.xhtml")
	public String unionPayFastJumpForYoujie(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "YOUJIE");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	/**
	 * 温州银行信用卡开出来走上海的商户号，活动用卡的限制为每卡每周限使用一次。
	 * 
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Jun 9, 2013 11:57:45 AM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast/wzyh.xhtml")
	public String unionPayFastJumpForWzcb(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "WZCB");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	/**
	 * 查打银行活动，用来演示
	 * 
	 * @param orderId
	 * @param spid
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Jul 2, 2013 5:33:04 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFast/zdyh.xhtml")
	public String unionPayFastJumpForZdcb(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "ZDCB");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	private String unionPayFastJumpComm(Long orderId,Long spid, ModelMap model) {
		Member member = getLogonMember();
		if(member == null){
			dbLogger.error("memer is null:orderid=" + orderId + "|spid=" + spid);
		}
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		if(sd == null || StringUtils.isBlank(sd.getPaymethod())){
			return show404(model, "您选择的优惠活动不正确，请重新选择！");
		}
		
		List<String> limitPayList = paymentService.getLimitPayList();
		PayValidHelper valHelp = new PayValidHelper(sd.getPaymethod());
		String[] paymethodArr = StringUtils.split(sd.getPaymethod(), ",");
		for(String t : paymethodArr){
			limitPayList.remove(t);
		}
		valHelp.setLimitPay(limitPayList);
		model.put("valHelp", valHelp);
		
		return "gewapay/jumpunionPayFast.vm";
	}
	
	/**
	 * 江苏银联认证支付2.0,优惠活动-跳转链接
	 * 
	 * @param orderId
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 19, 2013 5:44:15 PM
	 */
	@RequestMapping("/gewapay/jump/unionPayFastAJS.xhtml")
	public String unionPayFastAJSJump(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/jumpunionPayFastAJS.vm";
	}
	
	@RequestMapping("/gewapay/jump/unionPayFastBJ.xhtml")
	public String unionPayFastBJJump(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/jumpunionPayFastBJ.vm";
	}
	
	
	
	@RequestMapping("/gewapay/jump/unionPayFast/shenzhenPingan.xhtml")
	public String shenzhenPingan(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "shenzhenPingAn");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	@RequestMapping("/gewapay/jump/unionPayFast/guangzhouBocWeekOne.xhtml")
	public String guangzhouBocWeekOne(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "guangzhouBocWeekOne");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	@RequestMapping("/gewapay/jump/unionPayFast/guangzhouBocMonthTwo.xhtml")
	public String guangzhouBocMonthTwo(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "guangzhouBocMonthTwo");
		return unionPayFastJumpComm(orderId, spid, model);
	}

	//上海农商业银行
	@RequestMapping("/gewapay/jump/unionPayFast/srcb.xhtml")
	public String srcb(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "SRCB");
		return unionPayFastJumpComm(orderId, spid, model);
	}
	
	//邮政储蓄
	@RequestMapping("/gewapay/jump/unionPayFast/psbc.xhtml")
	public String psbc(Long orderId,Long spid, ModelMap model) {
		model.put("activeType", "PSBC");
		return unionPayFastJumpComm(orderId, spid, model);
	}
}
