package com.gewara.web.action.gewapay;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.pay.SpSdoUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.WebUtils;

/**
 * 盛大回调接口
 * @author acerge(acerge@163.com)
 * @since 6:18:42 PM Mar 16, 2010
 */
@Controller
public class SpSdoController extends BasePayController{
	@RequestMapping("/pay/spsdoNotify.xhtml")
	@ResponseBody
	public String spsdoNotify(HttpServletRequest request, 
			String Amount,
			String PayAmount,
			String OrderNo,
			String serialno,
			String Status,			//01：成功
			String MerchantNo,
			String PayChannel,	//03:盛大卡支付 04:银行卡支付
			String Discount,
			String SignType,
			String PayTime,
			String CurrencyType,
			String ProductNo,
			String ProductDesc,
			String Remark1,
			String Remark2,
			String ExInfo,
			String MAC,
			ModelMap model){
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		boolean isValid = SpSdoUtil.isValid(MAC, MerchantNo, Amount, PayAmount, OrderNo, serialno, Status, 
				MerchantNo, PayChannel, Discount, SignType, PayTime, CurrencyType, ProductNo,
				ProductDesc, Remark1, Remark2, ExInfo);
		if("01".equals(Status) && isValid){
			String tradeNo = OrderNo;
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeNo);
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, SpSdoUtil.getPaymethod(MerchantNo), params + ",host=" + Config.getServerIp());
			int fee = new Double(PayAmount).intValue();
			String payseqno = serialno;
			model.put("tradeNo", tradeNo);
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, payseqno, fee, SpSdoUtil.getPaymethod(MerchantNo), PayChannel, "盛大帐户");
				if(result.isSuccess()) processPay(tradeNo, "盛大帐户");
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理, 订单号：" +  tradeNo, e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
			}
			return "OK";
		}else{
			dbLogger.error("RequestParams:" + WebUtils.getParamStr(request, true));
			dbLogger.error("RequestHeaders:" + WebUtils.getHeaderStr(request));
		}
		return showError(model, "订单验证错误！");
	}
	@RequestMapping("/pay/spsdoReturn.xhtml")
	public String spsdoReturn(HttpServletRequest request, 
			String Amount,
			String PayAmount,
			String OrderNo,
			String serialno,
			String Status,			//01：成功
			String MerchantNo,
			String PayChannel,	//03:盛大卡支付 04:银行卡支付
			String Discount,
			String SignType,
			String PayTime,
			String CurrencyType,
			String ProductNo,
			String ProductDesc,
			String Remark1,
			String Remark2,
			String ExInfo,
			String MAC,
			ModelMap model){
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		if(StringUtils.isNotBlank(OrderNo)) {
			orderMonitorService.addOrderPayCallback(OrderNo, OrderProcessConstant.CALLTYPE_RETURN, SpSdoUtil.getPaymethod(MerchantNo), params + ",host=" + Config.getServerIp());
		}
		boolean isValid = SpSdoUtil.isValid(MAC, MerchantNo, Amount, PayAmount, OrderNo, serialno, Status, 
				MerchantNo, PayChannel, Discount, SignType, PayTime, CurrencyType, ProductNo,
				ProductDesc, Remark1, Remark2, ExInfo);
		if("01".equals(Status) && isValid){
			String tradeNo = OrderNo;
			int fee = new Double(PayAmount).intValue();
			model.put("tradeNo", tradeNo);
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, serialno, fee, SpSdoUtil.getPaymethod(MerchantNo), PayChannel, "盛大帐户");
				if(result.isSuccess()) processPay(tradeNo, "盛大帐户");
				if(result.getRetval().sureOutPartner()) {
					return "redirect:/partner/orderResult.xhtml";
				}
				return "redirect:/gewapay/orderResult.xhtml";
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理, 订单号：" +  tradeNo, e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
				if(order.sureOutPartner()) return "redirect:/partner/orderResult.xhtml";
				else return "redirect:/gewapay/orderResult.xhtml";
			}
		}else{
			dbLogger.error("RequestParams:" + WebUtils.getParamStr(request, true));
			dbLogger.error("RequestHeaders:" + WebUtils.getHeaderStr(request));
		}
		return showError(model, "付款失败！");
	}
}
