package com.gewara.web.action.gewapay;


import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.pay.Pay12580Util;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.WebUtils;

/**
 * 合作伙伴回调接口
 * 
 * @author acerge(acerge@163.com)
 * @since 6:18:42 PM Mar 16, 2010
 */
@Controller
public class PartnerPayController extends BasePayController {
	@RequestMapping("/pay/partnerPayReturn.xhtml")
	public String partnerPayReturn(String key, String tradeno, String paidAmount, String checkvalue, ModelMap model,HttpServletRequest request) {
		String remoteIp = WebUtils.getRemoteIp(request);
		String params = WebUtils.getParamStr(request, true);
		String headers = WebUtils.getHeaderStr(request);
		dbLogger.error("商家订单调用Param:" + params);
		dbLogger.error("商家订单调用Header:" + headers + ", IP:" + remoteIp);
		
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if (order == null) {
			return showError(model, "订单不存在！");
		}
		ApiUser partner = daoService.getObjectByUkey(ApiUser.class, "partnerkey", key, true);
		if (partner == null) {
			return showError(model, "商户不存在！");
		}
		if (!checkvalue.equalsIgnoreCase(PartnerPayUtil.getCheckValue(partner.getPrivatekey(), tradeno, paidAmount))) {
			return showError(model, "非法的订单信息！");
		}
		model.put("tradeNo", tradeno);
		return "redirect:/partner/orderResult.xhtml";
	}

	// 改变数据库状态
	@RequestMapping("/pay/partnerPayNotify.xhtml")
	@ResponseBody
	public String partnerPayNotify(HttpServletRequest request, String key, String tradeno, 
			String paidAmount, String payseqno, String version, String checkvalue) {
		String remoteIp = WebUtils.getRemoteIp(request);
		String params = WebUtils.getParamStr(request, true);
		String headers = WebUtils.getHeaderStr(request);
		dbLogger.error("商家订单调用Param:" + params);
		dbLogger.error("商家订单调用Header:" + headers + ", IP:" + remoteIp);
		ApiUser partner = daoService.getObjectByUkey(ApiUser.class, "partnerkey", key, true);
		if (partner == null) {
			monitorService.saveSysWarn("商家API错误：商家不存在,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return tradeno + "|" + payseqno + "|partner not exists";
		}
		if(!partner.isRole(ApiUser.ROLE_PAYORDER)){
			monitorService.saveSysWarn("商家API错误：不支持商家支付,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return tradeno + "|" + payseqno + "|pay not supported";
		}
		String valid = PartnerPayUtil.validate(partner, tradeno, payseqno, paidAmount, checkvalue);
		if(StringUtils.equals("success", valid)) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeno);
			orderMonitorService.addOrderPayCallback(tradeno, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_PARTNERPAY, params + ",host=" + Config.getServerIp());
			if(!PartnerPayUtil.isValidIp(remoteIp, partner)){//非法IP调用，报警
				monitorService.saveSysWarn("商家API错误：商家付款非法IP调用,params:", params + "\nheader:" + headers, RoleTag.jishu);
			}
			/* 可以在不同状态下获取订单信息，操作商家数据库使数据同步 */
			int fee = new Double(paidAmount).intValue();
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeno, payseqno, fee, PaymethodConstant.PAYMETHOD_PARTNERPAY, "bk", partner.getBriefname());
				if(result.isSuccess()) processPay(tradeno, partner.getBriefname());
				TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
				if (order == null) {
					monitorService.saveSysWarn("商家API错误：商家订单不存在,params:", params + "\nheader:" + headers, RoleTag.jishu);
					return tradeno + "|" + payseqno + "|order not exists";
				}
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeno, RoleTag.dingpiao);
				return tradeno + "|" + payseqno + "|" + "交易异常";
			}
			if(StringUtils.equals(version, "2.0")) return tradeno + "|" + payseqno + "|success";
			return "success";
		}else{
			monitorService.saveSysWarn("订单验证错误:" + valid + remoteIp, params, RoleTag.jishu);
			return tradeno + "|" + payseqno + "|" + valid;
		}
	}
	/**
	 * 
	 * @param request
	 * @param orderid 12580传入的支付方生成的订单id
	 * @param code 
	 * @param seq 订单流水号
	 * @param platform
	 * @param amount 直接金额
	 * @param msg 支付说明
	 * @param sign 签名校验
	 * @param refer gewara订单号
	 * @return
	 */
	// 改变数据库状态
	@RequestMapping("/pay/partner12580PayNotify.xhtml")
	@ResponseBody
	public String partner12580PayNotify(HttpServletRequest request, String orderid, String code,String seq,
			String platform, String amount,String msg,String sign,String refer) {
		String params = WebUtils.getParamStr(request, true);
		String headers = WebUtils.getHeaderStr(request);
		dbLogger.error("商家订单调用Param:" + params);
		dbLogger.error("商家订单调用Header:" + headers);
		if(!"0000".equals(code) || !"dnapay".equals(platform)){
			dbLogger.error("支付产生订单号为：" + orderid + ".订单:" + refer + "支付情况," + msg );
			monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  refer, RoleTag.dingpiao);
			return "200 OK";
		}

		ApiUser partner = daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_12580);
		String tradeNo = refer;
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if (order == null) {
			monitorService.saveSysWarn("商家API错误：商家订单不存在,params:", params + "\nheader:" + headers, RoleTag.jishu);
			return tradeNo + "|" + seq + "|order not exists";
		}
		String valid = Pay12580Util.validate(partner, sign,orderid,order);
		if(StringUtils.equals("success", valid)) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeNo);
			orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_PARTNERPAY, params + ",host=" + Config.getServerIp());
			String remoteIp = WebUtils.getRemoteIp(request);
			if(!PartnerPayUtil.isValidIp(remoteIp, partner)){//非法IP调用，报警
				monitorService.saveSysWarn("商家API错误：商家付款非法IP调用,params:", params + "\nheader:" + headers, RoleTag.jishu);
			}
			/* 可以在不同状态下获取订单信息，操作商家数据库使数据同步 */
			int fee = new Double(amount).intValue()/100;
			try{
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo, seq, fee, PaymethodConstant.PAYMETHOD_PARTNERPAY, "bk", partner.getBriefname());
				if(result.isSuccess()) processPay(tradeNo, partner.getBriefname());
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
			}
			return "200 OK";
		}else{
			monitorService.saveSysWarn("商家API错误：非法的订单信息,params:", params + "\nheader:" + headers, RoleTag.jishu);
			return "valid is fail" + valid;
		}
	}
	
	//可不反查的商户id
	List<Long> partnerIdList = Arrays.asList(new Long[]{
			PartnerConstant.PARTNER_SHOKW,//百联E城
			PartnerConstant.PARTNER_IPTV,//iptv
			PartnerConstant.PARTNER_ANXIN_TERM,//安欣生活
			PartnerConstant.PARTNER_ANXIN_WEB//安欣生活
	});

	@RequestMapping("/pay/partnerPayCheck.xhtml")
	@ResponseBody
	public String partnerPayCheck(String tradeno){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		if(order == null)return "partnerPayCheck order not find";
		if(partnerIdList.contains(order.getPartnerid())){
			return "success";
		}else{		
			dbLogger.error("商家订单调用反查URL为空，tradeno:" + tradeno);
			return "partnerIdList not contains:"+order.getPartnerid();
		}
	}
	
}
