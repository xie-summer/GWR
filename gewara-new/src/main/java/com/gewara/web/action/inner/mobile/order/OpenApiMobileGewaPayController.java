package com.gewara.web.action.inner.mobile.order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.GewaAppHelper;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileGewaPayController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	/**
	 * 余额支付(用户余额支付方式API)
	 */
	@RequestMapping("/openapi/mobile/order/confirmBalancePay.xhtml")
	public String balancePay(String tradeNo,String payPass, String wbpay, ModelMap model){
		if(StringUtils.isBlank(tradeNo) || StringUtils.isBlank(payPass)) 
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		
		if(StringUtils.isBlank(tradeNo) || StringUtils.isBlank(payPass)) 
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		if(order.isAllPaid()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, tradeNo+"订单，已支付成功！请不要重复支付！");
		if((!StringUtils.equals(PaymethodConstant.PAYMETHOD_GEWAPAY, order.getPaymethod()))){ 
			ErrorCode code = paymentService.isAllowChangePaymethod(order, PaymethodConstant.PAYMETHOD_GEWAPAY, null);
			if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		order.setPaymethod(PaymethodConstant.PAYMETHOD_GEWAPAY);
		order.setStatus(OrderConstant.STATUS_NEW_CONFIRM);
		daoService.saveObject(order);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "API去支付", order, PaymethodConstant.PAYMETHOD_GEWAPAY + ",host=" + Config.getServerIp());
		if(!StringUtils.equals(ChargeConstant.WABIPAY, wbpay) && !StringUtils.equals(ChargeConstant.BANKPAY, wbpay)) {
			wbpay = ChargeConstant.WABIPAY;
		}
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account.isNopassword()){
			return getErrorXmlView(model, ApiConstant.CODE_PAYPASS_ERROR, "支付密码过于简单,请重新设置支付密码!");
		}
		if(account.getBankcharge()==0) wbpay = ChargeConstant.WABIPAY;
		ErrorCode wbcode = paymentService.validateWbPay(member, account, payPass, wbpay);
		if(!wbcode.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, wbcode.getMsg()); 
		int due = order.getDue();
		order.setPaybank(wbpay);
		daoService.saveObject(order);
		if(account.getBanlance()< due){
			model.put("totalAmount", order.getTotalAmount());
			Integer totalfee = order.getDue() - account.getBanlance();
			model.put("chargefee", totalfee);
			model.put("order", order);
			model.put("status", ApiConstant.ORDER_STATUS_MAP.get(order.getStatus()));
			return getXmlView(model, "inner/mobile/wbOrder.vm");
		}else {
			ErrorCode result = orderProcessService.gewaPayOrderAtServer(order.getId(), member.getId(), null, true);
			if(result.isSuccess()){
				getOrderCommon(model, order, true);
				return getXmlView(model, "inner/mobile/confirmBalancePay.vm");//使用余额支付成功！
			}else{
				return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, result.getMsg());
			}
		}
	}
	@RequestMapping("/openapi/mobile/order/showWbPaymethod.xhtml")
	public String showWbPaymethod(String tradeNo, String apptype, String appVersion,  ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		if(order.isAllPaid()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, tradeNo+"订单，已支付成功！请不要重复支付！");
		if((!StringUtils.equals(PaymethodConstant.PAYMETHOD_GEWAPAY, order.getPaymethod()))){ 
			ErrorCode code = paymentService.isAllowChangePaymethod(order, PaymethodConstant.PAYMETHOD_GEWAPAY, null);
			if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null || account.isNopassword()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "账户为空或密码过于简单！");
		}
		PayValidHelper valHelp = new PayValidHelper();
		Map<String, String> orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		String bindpay = paymentService.getBindPay(discountList, orderOtherinfo, order);
		List<String> limitPayList = paymentService.getLimitPayList();
		if(StringUtils.isNotBlank(bindpay)){
			valHelp = new PayValidHelper(bindpay);
			model.put("flag", "true");
			String[] bindpayArr = StringUtils.split(bindpay, ",");
			for(String t : bindpayArr){
				limitPayList.remove(t);
			}
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		ApiUserExtra extra = auth.getUserExtra();
		model.put("valHelp", valHelp);
		model.put("tradeno", order.getTradeNo());
		model.put("discountAmount", order.getDiscount());
		model.put("totalAmount", order.getTotalAmount());
		model.put("due", order.getDue()-account.getBanlance());
		model.put("payMethodMap", GewaAppHelper.getChargeMap(extra, order, apptype, appVersion));
		model.put("alimember", isAliMember(memberInfo));
		return getXmlView(model, "inner/mobile/showPayMethodList.vm");
	}
	@RequestMapping("/openapi/mobile/order/selectWbPaymethod.xhtml")
	public String selectWbPaymethod(String tradeNo, String payMethod, String apptype, String appVersion, HttpServletRequest request, ModelMap model){
		if(StringUtils.isBlank(tradeNo)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数有误！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "订单不存在！");
		if (order.isAllPaid() || order.isCancel()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作已支付或已（过时）取消的订单！");
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = auth.getMember();
		if(!order.getMemberid().equals(member.getId())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能操作他人订单！");
		Map paramsData = new LinkedHashMap();
		String[] paypair = StringUtils.split(payMethod, ":");
		if(!PaymethodConstant.isValidPayMethod(paypair[0])) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "支付方式错误！");
		List<String> paymethodList = new ArrayList<String>();
		paymethodList.add(PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY);
		paymethodList.add(PaymethodConstant.PAYMETHOD_CHINASMARTMOBILEPAY);
		paymethodList.add(PaymethodConstant.PAYMETHOD_CHINASMARTJSPAY);
		paymethodList.add(PaymethodConstant.PAYMETHOD_CMSMARTPAY);
		if(!paymethodList.contains(payMethod)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "支付方式错误！");
		}
		String mainPaymethod = paypair[0];
		String paybank = paypair.length>1?paypair[1]:"";
		if((!StringUtils.equals(mainPaymethod, order.getPaymethod()) || !StringUtils.equals(paybank, order.getPaybank()))){ 
			ErrorCode code = paymentService.isAllowChangePaymethod(order, mainPaymethod, paybank);
			if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		Charge charge = paymentService.addChargeByOrder(member, account, order, payMethod);
		order.setPaybank(paybank);
		order.setPaymethod(PaymethodConstant.PAYMETHOD_GEWAPAY);
		order.setStatus(OrderConstant.STATUS_NEW_CONFIRM);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "API去支付", order, payMethod + ",host=" + Config.getServerIp());
		daoService.saveObject(order);
		String version = GewaAppHelper.getChinaSmartPayVersion(order, apptype, order.getPaymethod(), appVersion);
		paymentService.usePayServer(charge, auth.getRemoteIp(), paramsData, version, request, model);
		model.put("method", "post");
		model.put("paramsData", paramsData);
		model.put("tradeno", order.getTradeNo());
		model.put("discountAmount", order.getDiscount());
		model.put("totalAmount", order.getTotalAmount());
		model.put("due", charge.getTotalfee());
		order.setStatus(OrderConstant.STATUS_NEW_CONFIRM);
		orderMonitorService.addOrderChangeLog(order.getTradeNo(), "API去充值", order, payMethod + ",host=" + Config.getServerIp());
		daoService.saveObject(order);
		return getXmlView(model, "inner/mobile/selectPayMethod.vm");
	}
}
