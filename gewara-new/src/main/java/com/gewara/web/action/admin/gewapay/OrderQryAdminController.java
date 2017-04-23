/**
 * 
 */
package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.bank.ChinaOrderQry;
import com.gewara.bank.SpsdoOrderQry;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.CCBPosPayUtil;
import com.gewara.pay.ChinapayUtil;
import com.gewara.pay.NewPayUtil;
import com.gewara.pay.PartnerPayUtil;
import com.gewara.pay.UmPayUtil;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.PartnerWebService;
import com.gewara.util.CAUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class OrderQryAdminController extends BaseAdminController {
	@Autowired@Qualifier("config")
	private Config config;
	@Autowired@Qualifier("partnerWebService")
	private PartnerWebService partnerWebService;
	public void setPartnerWebService(PartnerWebService partnerWebService) {
		this.partnerWebService = partnerWebService;
	}
	@Autowired@Qualifier("paymentService")
	protected PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@RequestMapping("/admin/orderqry/getOrder.xhtml")
	public String getOrder(String tradeNo, ModelMap model){
		String url = "admin/gewapay/qrySingleOrder.vm";
		if(tradeNo==null) return url;
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order==null) return forwardMessage(model, "订单不存在，请核实订单号！");
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		model.put("order", order);
		return url;
	}
	@RequestMapping("/admin/orderqry/confirmOrderList.xhtml")
	public String confirmOrderList(Integer minute, ModelMap model){
		if(minute==null) minute = 10;
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		String url = "admin/gewapay/confirmList.vm";
		String hql = "from TicketOrder t where t.status=? and t.validtime>=? order by t.addtime asc";
		List<TicketOrder> orderList = hibernateTemplate.find(hql, OrderConstant.STATUS_NEW_CONFIRM, DateUtil.addMinute(curtime, -minute));
		model.put("orderList", orderList);
		return url;
	}
	@RequestMapping("/admin/gewapay/checkOrderPayStatus.xhtml")
	@ResponseBody
	public String checkOrderPayStatus(String tradeNo){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order.isAllPaid()) return "已支付";
		ErrorCode<String> result = qryOrder(order, null);
		if(result.isSuccess()) return "已支付";
		return result.getMsg();
	}

	private ErrorCode qryOrder(GewaOrder order, String paymethod){
		if(order==null) return ErrorCode.getFailure("订单不存在！");
		if(StringUtils.isBlank(paymethod)) paymethod = order.getPaymethod();
		boolean isSupport = true;
		String response = "";
		List<String> paymethodList = paymentService.getPayserverMethodList();
		if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_CHINAPAY1) 
				|| StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_CHINAPAY2)){	//银联
			Map<String, String> params = ChinapayUtil.qryOrder(order);
			response = ChinapayUtil.getQryRes(params);
			ChinaOrderQry orderQry = ChinapayUtil.getQryToObject(response);
			if(orderQry!=null && orderQry.isPaid()) { 
				return ErrorCode.getSuccessReturn(response);
			}
		}else if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_SPSDOPAY1)){		//盛大
			response = partnerWebService.qrySpsdoOrder(order);
			SpsdoOrderQry orderQry = partnerWebService.qrySpsdoOrder(response);
			if(orderQry!=null && orderQry.isPaid()) {
				return ErrorCode.getSuccessReturn(response);
			}
		}else if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_PARTNERPAY)){ //合作伙伴
			ApiUser partner = daoService.getObject(ApiUser.class, order.getPartnerid());
			if(StringUtils.isNotBlank(partner.getQryurl())){
				Map<String, String> params = new HashMap<String, String>();
				params.put("tradeno", order.getTradeNo());
				params.put("paidAmount", order.getDue()+"");
				String payseqno = order.getPayseqno();
				if(StringUtils.isNotBlank(payseqno)) params.put("payseqno", payseqno);
				params.put("checkvalue", PartnerPayUtil.getCheckValue(partner.getPartnerkey(), order.getTradeNo(), payseqno, order.getDue()+""));
				HttpResult result = HttpUtils.postUrlAsString(partner.getQryurl(), params);
				response = result.getResponse();
				if(StringUtils.contains(response, "paid")||StringUtils.contains(response, "success")){
					return ErrorCode.getSuccessReturn(response);
				}
			}
		}else if(StringUtils.startsWith(paymethod, PaymethodConstant.PAYMETHOD_UMPAY)){
			HttpResult result = UmPayUtil.qryOrder(order);
			if(result.isSuccess()) {
				response = result.getResponse();
				response += UmPayUtil.parseQryResult(response).toString();
			}else{
				response = result.getMsg();
			}
		}else if(StringUtils.startsWith(paymethod, PaymethodConstant.PAYMETHOD_UNIONPAY) || paymethodList.contains(paymethod)){
			HttpResult result = HttpUtils.getUrlAsString(config.getString("basePay") + "qryOrderPaidRes.xhtml?tradeno=" + order.getTradeNo());
			if(result.isSuccess()){
				response = result.getResponse();
				if(StringUtils.contains(response, "paid")) {
					return ErrorCode.getSuccessReturn(response);
				}
			}else{
				response = result.getMsg();
			}
		}else if(StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_CCBPOSPAY)){
			GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
			String paidqryurl = CCBPosPayUtil.getPaidResultUrl(gconfig);
			Map<String, String> params = new HashMap<String, String>();
			params.put("trade_no", order.getTradeNo());
			HttpResult result = HttpUtils.postUrlAsString(paidqryurl, params);
			if(result.isSuccess()){
				response = result.getResponse();
				if(StringUtils.contains(response, "success")) {
					return ErrorCode.getSuccessReturn(response);
				}
			}else {
				response = result.getMsg();
			}
		}else {
			isSupport = false;
		}
		if(!isSupport) {
			return ErrorCode.getFailure("该支付方式，暂不支持银行接口查询");
		}
		return ErrorCode.getFailure("查询错误！");
	}

	@RequestMapping("/admin/orderqry/getPayProjectOrder.xhtml")
	public String getPayProjectOrder(String tradeNo,String url, ModelMap model){
		if(StringUtils.isBlank(url)){
			url = config.getString("basePay") + "api/getOrder.xhtml";
		}
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("tradeno", tradeNo);
		String paramStr = JsonUtils.writeMapToJson(paramMap);
		String sign = CAUtil.doSign(paramStr, NewPayUtil.getMerprikey(), "utf-8", "SHA1WithRSA");
		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("merid", NewPayUtil.getMerid());
		try {
			postMap.put("params", Base64.encodeBase64String(paramStr.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		postMap.put("sign", sign);
		model.put("submitParams", postMap);
		model.put("submitUrl", url);
		return "admin/gewapay/paypro/tempQry.vm";
	}
}
