package com.gewara.web.action.gewapay;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.pay.CardpayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.WebUtils;

/**
 * 百联E城回调接口
 * @author acerge(acerge@163.com)
 * @since 6:18:42 PM Mar 16, 2010
 */
@Controller
public class ShokwController extends BasePayController{
	@RequestMapping("/pay/shokwReturn.xhtml")
	public String shokwReturn(HttpServletRequest request, String tradeNo, String payamount, String checkvalue, ModelMap model){
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		String valid = ""+CardpayUtil.validate(tradeNo, payamount, checkvalue);
		if(valid.contains("true")){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeNo);
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_RETURN, PaymethodConstant.PAYMETHOD_OKCARDPAY, params + ",host=" + Config.getServerIp());
			int fee = Integer.parseInt(payamount);
			model.put("tradeNo", tradeNo);
			String payseqno = "xxx";//无号
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, payseqno, fee, PaymethodConstant.PAYMETHOD_OKCARDPAY, "bk", "百联E城");
				if(result.isSuccess()) processPay(tradeNo, "百联E城");
				if(result.getRetval().surePartner()) return "redirect:/partner/orderResult.xhtml";
				return "redirect:/gewapay/orderResult.xhtml";
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理, 订单号：" +  tradeNo, e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
				if(order.sureOutPartner()) return "redirect:/partner/orderResult.xhtml";
				else return "redirect:/gewapay/orderResult.xhtml";
			}
		}
		return showError(model, "付款失败！");
	}
	//改变数据库状态
	@RequestMapping("/pay/shokwNotify.xhtml")
	@ResponseBody
	public String shokwNotify(HttpServletRequest request, String tradeNo, String payamount, String checkvalue){
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		String valid = ""+CardpayUtil.validate(tradeNo, payamount, checkvalue);
		if(valid.contains("true")){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeNo);
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_OKCARDPAY, params + ",host=" + Config.getServerIp());
			/*可以在不同状态下获取订单信息，操作商户数据库使数据同步*/
			int fee = Integer.parseInt(payamount);
			String payseqno = "xxx";//无号
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, payseqno, fee, PaymethodConstant.PAYMETHOD_OKCARDPAY, "bk", "百联E城");
				if(result.isSuccess()) processPay(tradeNo, "百联E城");
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理, 订单号：" +  tradeNo, e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
			}
			return "支付成功" + tradeNo + "success";
		}else{
			return "pay failure(invalidate code)";
		}
	}
}
