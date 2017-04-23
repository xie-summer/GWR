/**
 * 
 */
package com.gewara.web.action.gewapay;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.user.Member;
import com.gewara.pay.UmPayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.util.WebUtils;
@Controller
public class UmPhonePayController extends BasePayController{
	@RequestMapping("/pay/cmPhonePayNotify.xhtml")
	public String cmPhonePayNotify(String merId, String goodsId, String orderId,
			String merDate, String payDate, String amount, String amtType,
			String bankType, String mobileId, String transType, String settleDate,
			String merPriv, String retCode, String version, 
			HttpServletRequest request, ModelMap model) throws Exception {
		String param = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, param);
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("merId", merId);
		params.put("goodsId", goodsId);
		params.put("orderId", orderId);
		params.put("merDate", merDate);
		params.put("payDate", payDate);
		params.put("amount", amount);
		params.put("amtType", amtType);
		params.put("bankType", bankType);
		params.put("mobileId", mobileId);
		params.put("transType", transType);
		params.put("settleDate", settleDate);
		params.put("merPriv", merPriv);
		params.put("retCode", retCode);
		params.put("version", version);
		String sign = request.getParameter("sign");
		String p = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, p);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "联动优势话费notifySign:" + sign);
		String plain = UmPayUtil.getSignData(params);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "联动优势话费notifyPlain:" + plain);
		boolean verify = UmPayUtil.verify(plain, sign);
		if(verify){
			String paymethod = PaymethodConstant.PAYMETHOD_UMPAY;
			if(StringUtils.equals(merId, UmPayUtil.getMerIdSH())){
				paymethod = PaymethodConstant.PAYMETHOD_UMPAY_SH;
			}
			orderMonitorService.addOrderPayCallback(orderId, OrderProcessConstant.CALLTYPE_NOTIFY, paymethod, params + ",host=" + Config.getServerIp());
			try{
				int fee = new Double(amount).intValue()/100;
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(orderId, null, fee,paymethod, null, "联动优势话费支付");
				if(result.isSuccess()) processPay(orderId, "联动优势话费支付");
			}catch(Exception e){
			}
			model.put("merId", merId);
			model.put("goodsId", goodsId);
			model.put("orderId", orderId);
			model.put("merDate", merDate);
			model.put("version", version);
			String msg = "格瓦拉订单成功";
			String message = Base64.encodeBase64String(msg.getBytes("GBK"));
			model.put("message", message);
			String orgsign = merId+"|"+goodsId+"|"+orderId+"|"+merDate+"|0000|"+message+"|"+version;
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "联动优势话费CONTENT:" + orgsign +"|" + UmPayUtil.getSign(orgsign));
			model.put("sign", UmPayUtil.getSign(orgsign));
			return "gewapay/umPayResult.vm";
		}else {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "联动优势话费验证签名错误！");
			return forwardMessage(model, "check error!");
		}
	}
	@RequestMapping("/umpay/replyMsg.xhtml")
	public String cmwapPayReturn(String tradeNo, String response, ModelMap model,HttpServletRequest request) {
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		Member member = getLogonMember();
		if(member==null) return forwardMessage(model, "请登录！");
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		model.put("order", order);
		model.put("response", response);
		return "gewapay/umPayMsg.vm";
	}
	@RequestMapping("/umpay/qryOrder.xhtml")
	public String qryOrder(String tradeNo, ModelMap model) {
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order.getStatus().indexOf(OrderConstant.STATUS_PAID)>=0){
			return showJsonSuccess(model);
		}
		return showJsonError(model, "");
	}
}
