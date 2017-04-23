package com.gewara.web.action.gewapay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class AgrmtPayController extends BasePayController{
	
	@RequestMapping("/gewapay/agrmt/cashier.xhtml")
	public String agrmtCashier(Long orderId, String checkpass,ModelMap model) throws Exception{
		Member member = getLogonMember();
		if(member == null)  return showError(model, "您还没登录，请返回登录！");
		
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null){
			 return show404(model, "订单不存在！");
		}
		if(!StringUtil.md5(order.getId() + "&paymethod=" + order.getPaymethod()).equals(checkpass)){
			return show404(model, "非法来源，请按正确的操作步骤进行！");
		}
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		
		Map postMap = new HashMap();
		postMap.put("memberId", member.getId().toString());
		String url = config.getString("basePay") + "api/agrmt/getValid.xhtml";
		HttpResult code = HttpUtils.postUrlAsString(url, postMap);
		if(!code.isSuccess()){
			return showError(model, "请求失败！");
		}
		String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
		Map returnMap = JsonUtils.readJsonToMap(res);
		boolean hasAgrmt = (Boolean)returnMap.get("hasAgrmt");
		List agrmtList = (List)returnMap.get("agrmtList");
		
		model.put("order", order);
		model.put("member", member);
		if(hasAgrmt && agrmtList != null && !agrmtList.isEmpty()){
			MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			if (account != null)	model.put("account", account);			
			
			model.put("agrmtList", agrmtList);
			return "gewapay/agrmt/cashier.vm";
		}else{
			model.put("orderId", order.getId());
			return "redirect:/gewapay/agrmt/unSaved.xhtml";
		}
	}
	
	
	@RequestMapping("/gewapay/agrmt/topay.xhtml")
	public String topay(Long orderId,String agrmtNo,String paypass,ModelMap model){
		if(orderId == null) return show404(model, "订单不存在！");
		if(StringUtils.isBlank(agrmtNo))  return show404(model, "请正确选择银行卡！");
		
		Member member = getLogonMember();		
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
		if(account == null || account.isNopassword()) return showJsonError(model, "账户为空或密码过于简单！");
		if(!account.hasRights()){
			return showJsonError(model, "你的账户暂被禁用，如果有疑问请联系客服");
		}
		if(!PayUtil.passEquals(paypass, account.getPassword())) return showJsonError(model, "支付密码不正确！");

		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		
		Map otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		otherinfoMap.put("agrmtPayType", "pay");
		otherinfoMap.put("agrmtNo", agrmtNo);
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		daoService.saveObject(order);
		
		Map jsonReturn = new HashMap();
		jsonReturn.put("url", paymentService.getOrderPayUrl2(order));
		jsonReturn.put("pay", order.getPaymethod());
		return this.showJsonSuccess(model,jsonReturn);
	}
	
	@RequestMapping("/gewapay/agrmt/dopay.xhtml")
	public String dopay(HttpServletRequest request,ModelMap model){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, WebUtils.getParamStr(request, true));
		Map<String,String> params = WebUtils.getRequestMap(request);
		
		Member member = getLogonMember();
		GewaOrder order = this.daoService.getObjectByUkey(GewaOrder.class, "tradeNo", params.get("orderNo"), false);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");		
		model.put("order",order);
		
		String holderMerId = params.get("holderMerId");
		if(!StringUtils.equals(holderMerId, member.getId().toString())){
			dbLogger.error("非法用户:holderMerId is " + holderMerId + ";memberId is " + member.getId());
			return "非法操作！";//非法用户
		}
		
		String url = config.getString("basePay") + "api/agrmt/pay.xhtml";
		HttpResult httpResult = HttpUtils.postUrlAsString(url, params);
		if(!httpResult.isSuccess()){
			model.put("errorMsg", httpResult.getMsg());
			return "gewapay/agrmt/agrmtPayError.vm";
		}
		String response = httpResult.getResponse();
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "tradeNo:" + order.getTradeNo() + " pay result is " + response);
		if(!StringUtils.equals("success|" + params.get("orderNo"), response)){
			model.put("errorMsg", response);
			return "gewapay/agrmt/agrmtPayError.vm";
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		model.put("tradeNo", order.getTradeNo());
		return "redirect:/gewapay/orderResult.xhtml";
	}
	

	@RequestMapping("/gewapay/agrmt/unSaved.xhtml")
	public String unSavedAgrmtPay(Long orderId,ModelMap model){
		Member member = getLogonMember();
		if(member == null)  return showError(model, "您还没登录，请返回登录！");
		
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null){
			 return show404(model, "订单不存在！");
		}
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		model.put("member", member);
		
		return "gewapay/agrmt/unSaved.vm";
	}
	
	@RequestMapping("/gewapay/agrmt/signPay.xhtml")
	public String signPay(Long orderId,ModelMap model){
		if(orderId == null) return show404(model, "订单不存在！");
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		
		Map otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		otherinfoMap.put("agrmtPayType", "signPay");
		otherinfoMap.remove("agrmtNo");
		order.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		daoService.saveObject(order);
		
		Map jsonReturn = new HashMap();
		jsonReturn.put("url", paymentService.getOrderPayUrl2(order));
		jsonReturn.put("pay", order.getPaymethod());
		return this.showJsonSuccess(model,jsonReturn);
	}
	
	@RequestMapping("/gewapay/agrmt/getAgrmt.xhtml")
	public String getAgrmt(ModelMap model) throws Exception{
		Member member = getLogonMember();
		if(member == null)  return showError(model, "您还没登录，请返回登录！");
		
		Map postMap = new HashMap();
		postMap.put("memberId", member.getId().toString());
		String url = config.getString("basePay") + "api/agrmt/getValid.xhtml";
		HttpResult code = HttpUtils.postUrlAsString(url, postMap);
		if(!code.isSuccess()){
			return showError(model, "请求失败！");
		}
		String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");		
		Map returnMap = JsonUtils.readJsonToMap(res);
		boolean hasAgrmt = (Boolean)returnMap.get("hasAgrmt");
		List agrmtList = (List)returnMap.get("agrmtList");
		
		
		model.put("hasAgrmt", hasAgrmt);
		model.put("agrmtList", agrmtList);
		return "gewapay/agrmt/agrmtList.vm";
	}
	
	@RequestMapping("/gewapay/agrmt/toAgrmt.xhtml")
	public String toAgrmt(ModelMap model) throws Exception{
		Member member = getLogonMember();
		if(member == null)  return showError(model, "您还没登录，请返回登录！");
		
		model.put("member", member);
		return "gewapay/agrmt/addAgrmt.vm";
	}
	
	@RequestMapping("/gewapay/agrmt/addAgrmt.xhtml")
	public String addAgrmt(HttpServletRequest request,HttpServletResponse response,ModelMap model) throws Exception{
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, WebUtils.getParamStr(request, true));		
		Member member = getLogonMember();
		if(member == null)  return showError(model, "您还没登录，请返回登录！");
		
		Map<String,String> paramsMap = WebUtils.getRequestMap(request);
		paramsMap.put("holderMerId", member.getId().toString());
		
		String url = config.getString("basePay") + "api/agrmt/getAgrmtParams.xhtml";
		HttpResult code = HttpUtils.postUrlAsString(url, paramsMap);
		if(!code.isSuccess()){
			return showError(model, "请求失败！");
		}
		String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
		Map<String,String> returnMap = VmUtils.readJsonToMap(res);
		
		String method = returnMap.get("httpMethod");
		String encoding = returnMap.get("httpEncoding");
		request.setCharacterEncoding(encoding);
		response.setCharacterEncoding(encoding);
		String submitParams = returnMap.get("submitParams");
		Map<String, String> submitMap = VmUtils.readJsonToMap(submitParams);
		String[] paramNames = returnMap.get("paramNames").split(",");
		String payurl = returnMap.get("payurl");
		model.put("method", method);
		model.put("submitParams", submitMap);
		model.put("paramsNames", paramNames);
		model.put("payUrl", payurl);
		
		return "gewapay/agrmt/agrmtTempSubmit.vm";
	}
	
	@RequestMapping("/gewapay/agrmt/cancelAgrm.xhtml")
	public String cancelAgrm(String agrmtNo,ModelMap model){
		if(StringUtils.isBlank(agrmtNo)){
			return showJsonError(model, "请选择要解约的银行卡！");
		}
		
		Member member = getLogonMember();
		Map params = new HashMap();
		params.put("agrmtNo", agrmtNo);
		params.put("memberId", member.getId().toString());

		String url = config.getString("basePay") + "api/agrmt/cancelAgrm.xhtml";
		HttpResult httpResult = HttpUtils.postUrlAsString(url, params);
		if(!httpResult.isSuccess()){
			return showJsonError(model, httpResult.getMsg());
		}
		String response = httpResult.getResponse();
		if(!StringUtils.equals("success", response)){
			return showJsonError(model, response);
		}
		
		return this.showJsonSuccess(model);
	}
}
