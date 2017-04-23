package com.gewara.web.action.gewapay;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.pay.Shop10086Util;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.util.XmlUtils;
import com.gewara.web.support.GewaVelocityView;
import com.gewara.xmlbind.partner.Shop10086Order;

@Controller
public class Shop10086PayController extends BasePayController {
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	// 改变数据库状态
	@RequestMapping("/pay/shop10086PayNotify.xhtml")
	public String partnerPayNotify(HttpServletRequest request,ModelMap model) {
		String remoteIp = WebUtils.getRemoteIp(request);
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line = "";
		try {
			br = request.getReader();
			while((line = br.readLine()) != null){
				sb.append(line);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		String params = sb.toString();
		String headers = WebUtils.getHeaderStr(request);
		dbLogger.error("商家订单调用Param:" + params);
		dbLogger.error("商家订单调用Header:" + headers + ", IP:" + remoteIp);
		ApiUser partner = daoService.getObjectByUkey(ApiUser.class, "partnerkey", "shop10086", true);
		if(StringUtils.isBlank(params)){
			return this.returnResult("0009","参数错误", model,"partner/shop10086/payResponse.vm", partner);
		}
		if (partner == null) {
			monitorService.saveSysWarn("商家API错误：商家不存在,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return this.returnResult("0009","商家不存在", model,"partner/shop10086/payResponse.vm", partner);
		}
		if(!partner.isRole(ApiUser.ROLE_PAYORDER)){
			monitorService.saveSysWarn("商家API错误：不支持商家支付,params:",  params + "\nheader:" + headers, RoleTag.jishu);
			return this.returnResult("0009","不支持商家支付", model,"partner/shop10086/payResponse.vm", partner);
		}
		Shop10086Order so = null;
		String tradeNo = "";
		try {
			BeanReader beanReader = ApiUtils.getBeanReader("requestMsg", Shop10086Order.class);
			so = (Shop10086Order)ApiUtils.xml2Object(beanReader,params);
			if(!(so == null || !StringUtils.equals(so.getSign(),StringUtil.md5(Shop10086Util.getCheckSign(partner,new String[]{"funCode","instId","rpId","reqDate","reqTime","orderId","payType","amount"},
					so))))) {
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "签名成功" + tradeNo);
				if(!Shop10086Util.query(partner,so.getOrderId(),so.getReqDate(), so.getReqTime(),so.getInstId())){
					monitorService.saveSysWarn("订单反查错误:"  + remoteIp, params, RoleTag.jishu);
					return returnResult("0002","订单反查失败",model,"partner/shop10086/payResponse.vm",partner);
				}
				tradeNo = so.getRpId();
				orderMonitorService.addOrderPayCallback(tradeNo, OrderProcessConstant.CALLTYPE_NOTIFY, PaymethodConstant.PAYMETHOD_PARTNERPAY, params + ",host=" + Config.getServerIp());
				if(!PartnerPayUtil.isValidIp(remoteIp, partner)){//非法IP调用，报警
					monitorService.saveSysWarn("商家API错误：商家付款非法IP调用,params:", params + "\nheader:" + headers, RoleTag.jishu);
				}
				/* 可以在不同状态下获取订单信息，操作商家数据库使数据同步 */
				int fee = so.getAmount().intValue()/100;
				ErrorCode<GewaOrder> result = paymentService.netPayOrder(tradeNo,so.getOrderId(), fee, PaymethodConstant.PAYMETHOD_PARTNERPAY, "bk", partner.getBriefname());
				if(result.isSuccess()) processPay(tradeNo, partner.getBriefname());
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
				if (order == null) {
					monitorService.saveSysWarn("商家API错误：商家订单不存在,params:", params + "\nheader:" + headers, RoleTag.jishu);
					return returnResult("0005","商家订单不存在",model,"partner/shop10086/payResponse.vm",partner);
				}
				model.put("instId",so.getInstId());
				model.put("orderId",so.getOrderId());
				model.put("rpId",order.getTradeNo());
				model.put("stlDate", DateUtil.format(DateUtil.currentTime(),"yyyy-MM-dd HH:mm:ss"));
				return returnResult("0000","付款成功",model,"partner/shop10086/payResponse.vm",partner);
			}else{
				monitorService.saveSysWarn("订单验证错误:"  + remoteIp, params, RoleTag.jishu);
				return returnResult("0002","签名失败",model,"partner/shop10086/payResponse.vm",partner);
			}
		}catch(Exception e){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
			monitorService.saveSysWarn("订单付款调用失败，尽快处理", "订单号：" +  tradeNo, RoleTag.dingpiao);
			return returnResult("0012","系统内部异常",model,"partner/shop10086/payResponse.vm",partner);
		}
	}
	
	private String returnResult(String retCode,String msg,ModelMap model,String vm,ApiUser partner){
		model.put("retCode",retCode);
		model.put("retMsg",msg);
		model.put("funCode", "V2012011");
		String sign = Shop10086Util.getSign(partner,model);
		model.put("sign", sign);
		return getXmlView(model,vm);
	}
	
	private String getXmlView(ModelMap model, String view){
		String result = velocityTemplate.parseTemplate(view, model);
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		result = XmlUtils.formatXml(XmlUtils.filterInvalid(result), "utf-8");
		model.put("result", result);
		return "api/result.vm";
	}
	
	@RequestMapping("/pay/pushShop10086Order.xhtml")
	@ResponseBody
	public String partnerPayNotify(String tradeno) {
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, false);
		if(order == null){
			return "fail";
		}
		ApiUser partner = daoService.getObjectByUkey(ApiUser.class, "partnerkey", "shop10086", true);
		if(Shop10086Util.pushOrder(partner, order)){
			return "success";
		}
		return "fail";
	}
}
