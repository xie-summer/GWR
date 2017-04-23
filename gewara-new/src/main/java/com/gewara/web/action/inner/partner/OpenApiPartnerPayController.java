package com.gewara.web.action.inner.partner;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.WebUtils;
import com.gewara.web.action.gewapay.BasePayController;
import com.gewara.web.filter.OpenApiPartnerAuthenticationFilter;
import com.gewara.web.support.GewaVelocityView;
@Controller
public class OpenApiPartnerPayController extends BasePayController{
	// 改变数据库状态
	@RequestMapping("/openapi/partner/payNotify.xhtml")
	public String partnerPayNotify(HttpServletRequest request, String tradeno, String paidAmount, String payseqno, ModelMap model) {
		String ip = OpenApiPartnerAuthenticationFilter.getApiAuth().getPartnerIp();
		String params = WebUtils.getParamStr(request, true);
		String headers = WebUtils.getHeaderStr(request);
		dbLogger.error("商家订单调用Param:" + params);
		dbLogger.error("商家订单调用Header:" + headers + ", IP:" + ip);
		ApiUser partner = OpenApiPartnerAuthenticationFilter.getApiAuth().getApiUser();
		if (partner == null) {
			monitorService.saveSysWarn("商家API错误：商家不存在,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, tradeno + "|" + payseqno + "|partner not exists");
		}
		String valid = PartnerPayUtil.validateOpenApiPay(partner, tradeno, payseqno, paidAmount);
		if(StringUtils.equals("success", valid)) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeno);
			orderMonitorService.addOrderPayCallback(tradeno, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_PARTNERPAY, params + ",host=" + Config.getServerIp());
			if(!PartnerPayUtil.isValidIp(ip, partner)){//非法IP调用，报警
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
					return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, tradeno + "|" + payseqno + "|order not exists");
				}
			}catch(Exception e){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
				monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeno, RoleTag.dingpiao);
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, tradeno + "|" + payseqno + "|" + "交易异常");
			}
			return getSingleResultXmlView(model, "success");
		}else{
			monitorService.saveSysWarn("订单验证错误:" + valid + ip, params, RoleTag.jishu);
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, tradeno + "|" + payseqno + "|" + valid);
		}
	}
	
	private String getErrorXmlView(ModelMap model, String errorcode, String msg){
		model.put("errmsg", msg);
		model.put("errcode", errorcode);
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		return "api/error.vm";
	}
	private String getSingleResultXmlView(ModelMap model, String result){
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		model.put("result", result);
		return "api/singleResult.vm";
	}
}
