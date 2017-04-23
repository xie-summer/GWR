package com.gewara.web.action.gewapay;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.pay.BackConstant;
import com.gewara.pay.ChinapayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;

/**
 * 银联回调接口
 * @author acerge(acerge@163.com)
 * @since 3:19:11 PM Nov 13, 2009
 */
@Controller
public class ChinaPayController extends BasePayController{
	@RequestMapping("/pay/chinapayReturn.xhtml")
	public String chinapayReturn(HttpServletRequest request, ModelMap model, String merid, String orderno, 
			String amount, String transdate, String transtype, String status, String GateId, String checkvalue, String Priv1) {
		if(StringUtils.isBlank(orderno) || StringUtils.isBlank(amount)
				|| StringUtils.isBlank(checkvalue)) return forwardMessage(model, "非法调用！");
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		boolean isvalid = ChinapayUtil.verifyTransResponse(merid, orderno, amount, transdate, transtype, status, checkvalue);
		if(isvalid && "1001".equals(status)){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + orderno);
			String tradeNo = orderno;
			String paymethod = ChinapayUtil.getPaymethod(merid);
			//ChinaPay无payseqno
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_RETURN, paymethod, params + ",host=" + Config.getServerIp());
			int fee = Integer.parseInt(amount)/100;
			model.put("tradeNo", tradeNo);
			try{
				Map<String, String> otherMap = null;
				if(StringUtils.isNotBlank(Priv1) && StringUtils.contains(Priv1, "cardNumber")){
					otherMap = new HashMap<String, String>();
					otherMap.put(BackConstant.cardNumber, getCardNumber(Priv1));
				}
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, null, fee, paymethod, GateId, "银联", null, otherMap,paymethod,null);
				if(result.isSuccess()) processPay(tradeNo, "银联");
				if(result.getRetval().sureOutPartner()) {
					if(result.getRetval().getPartnerid().equals(PartnerConstant.PARTNER_UNION)){
						return "redirect:/partner/chinapay/orderResult.xhtml";
					}
					return "redirect:/partner/orderResult.xhtml";
				}
				return "redirect:/gewapay/orderResult.xhtml";
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理,订单号：" +  tradeNo, e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
				if(order.sureOutPartner()) {
					if(order.getPartnerid().equals(PartnerConstant.PARTNER_UNION)){
						return "redirect:/partner/chinapay/orderResult.xhtml";
					}
					return "redirect:/partner/orderResult.xhtml";
				}
				return "redirect:/gewapay/orderResult.xhtml";
			}
		}
		return showError(model, "付款失败！");
	}
	//异步调用，改变数据库状态
	@RequestMapping("/pay/chinapayNotify.xhtml")
	public String chinapayNotify(HttpServletResponse response, HttpServletRequest request, ModelMap model,
			String merid, String orderno, String amount, String transdate,
			String transtype, String status, String checkvalue, String GateId, String Priv1) throws IOException{
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		boolean isvalid = ChinapayUtil.verifyTransResponse(merid, orderno, amount, transdate, transtype, status, checkvalue);
		if(isvalid && "1001".equals(status)){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + orderno);
			String tradeNo = orderno;
			String paymethod = ChinapayUtil.getPaymethod(merid);
			//ChinaPay无payseqno
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, paymethod, params + ",host=" + Config.getServerIp());
			int fee = Integer.parseInt(amount)/100;
			try{
				Map<String, String> otherMap = null;
				if(StringUtils.isNotBlank(Priv1) && StringUtils.contains(Priv1, "cardNumber")){
					otherMap = new HashMap<String, String>();
					otherMap.put(BackConstant.cardNumber, getCardNumber(Priv1));
				}
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, null, fee, paymethod, GateId, "银联", null, otherMap,paymethod,null);
				if(result.isSuccess()) processPay(tradeNo, "银联");
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "订单付款调用失败，尽快处理,订单号：" +  tradeNo, e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
			}
			model.put("result", "RECV_ORD_ID_" + tradeNo);
			return "gewapay/pnrpayNotify.vm";
		}else{
			dbLogger.error("银联订单签名出错：");
			response.sendError(404);
			return null;
		}
	}
	/**
	 * 接收对账文件
	 * @param request
	 * @return
	 */
	@RequestMapping("/pay/chinapayReceiveFile.xhtml")
	@ResponseBody
	public String chinapayReceiveFile(HttpServletRequest request){
		String params = WebUtils.getParamStr(request, true);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, params);
		return "ok";
	}
	public String getCardNumber(String priv1){
		String[] p = StringUtil.findFirstByRegex(priv1, "cardNumber=\\d+").split("=");
		if(p==null || p.length<2) return "";
		return p[1];
	}
}
