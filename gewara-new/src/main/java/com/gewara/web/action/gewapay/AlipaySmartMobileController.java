/**
 * 
 */
package com.gewara.web.action.gewapay;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.pay.AlipaySmartMobileUtil;
import com.gewara.pay.PayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.WebUtils;
import com.gewara.util.XmlUtils;

@Controller
public class AlipaySmartMobileController extends BasePayController {
	public static final int RESULT_CHECK_SIGN_FAILED = 1; // 签名成功
	public static final int RESULT_CHECK_SIGN_SUCCEED = 2; // 签名失败
	@Autowired
	@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;

	public void setProcessOrderService(OrderProcessService orderProcessService) {
		this.orderProcessService = orderProcessService;
	}

	@RequestMapping("/pay/alipaySmartMobileNotify.xhtml")
	@ResponseBody
	public String alipaySmartMobileNotify(String sign, String notify_data, HttpServletRequest req) {
		String params = WebUtils.getParamStr(req, true);

		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		int retVal = RESULT_CHECK_SIGN_FAILED;
		Map<String, Object> resMap = XmlUtils.xml2Map(notify_data);
		boolean check = AlipaySmartMobileUtil.doCheck(notify_data, sign);
		if (check) {
			retVal = RESULT_CHECK_SIGN_SUCCEED;
			String trade_status = resMap.get("trade_status") + "";
			String tradeNo = resMap.get("out_trade_no") + "";
			String serialno = resMap.get("trade_no") + "";
			if (StringUtils.equalsIgnoreCase(trade_status, "TRADE_FINISHED") || StringUtils.equalsIgnoreCase(trade_status, "TRADE_SUCCESS")
					|| StringUtils.equalsIgnoreCase(trade_status, "WAIT_SELLER_SEND_GOODS")) { // 交易完成
				orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY, params + ",host=" + Config.getServerIp());
				try {
					String total_fee = resMap.get("total_fee") + "";
					int fee = new Double(total_fee).intValue();
					if (PayUtil.isChargeTrade(tradeNo)) {
						ErrorCode<Charge> code = paymentService.bankPayCharge(tradeNo, true, serialno, fee, PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY, "bk", PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY,"alismart");

						if (code.isSuccess()) {
							processCharge(tradeNo, "支付宝智能手机支付");
							Charge charge = code.getRetval();
							if (ChargeConstant.isBankPay(charge.getChargetype())) {
								GewaOrder order = daoService.getObject(GewaOrder.class, charge.getOutorderid());
								try {
									orderProcessService.gewaPayOrderAtServer(order.getId(), order.getMemberid(), null, true);
								} catch (Exception e) {
									dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理，订单号：" + tradeNo, e);
									monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" + tradeNo, RoleTag.dingpiao);
								}
							}
						}
					} else {
						ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, serialno, fee, PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY, null,
								"支付宝智能手机支付");
						if (result.isSuccess())
							processPay(tradeNo, "支付宝智能手机支付");
					}
					return "success";
				} catch (Exception e) {

				}
			} else {
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "支付宝智能手机支付验证签名成功，但是订单是非支付状态：" + tradeNo + ", trade_status=" + trade_status);
			}
		} else {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "支付宝智能手机支付验证签名失败");
		}
		return retVal + "";
	}
	/*
	 * trade_status : 交易状态 total_fee ：交易金额 subject ：商品名称 out_trade_no
	 * ：外部交易号（商户交易号） notify_reg_time ：通知时间 trade_no ：支付宝交易号
	 */
	/**
	 * TRADE_FINISHED：交易完成 WAIT_BUYER_PAY：等待付款
	 */
}
