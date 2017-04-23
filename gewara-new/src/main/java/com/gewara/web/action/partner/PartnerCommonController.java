package com.gewara.web.action.partner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
@Controller
public class PartnerCommonController extends BasePartnerController{
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	
	@RequestMapping("/partner/orderResult.xhtml")
	public String orderResult(@RequestParam(required=true) String tradeNo, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(!order.surePartner()) return showError(model, "非法订单，来源不正确！");
		ApiUser partner = daoService.getObject(ApiUser.class, order.getPartnerid());
		if(order instanceof DramaOrder){
			return showDramaOrderResult((DramaOrder)order, partner, model);
		}
		if(order.isAllPaid()){
			model.put("success", true);
			model.put("fee", order.getDue());
		}else{
			model.put("success", false);
			model.put("fee", order.getAlipaid());
		}
		return showOrderResult((TicketOrder)order, partner, model, request, response);
	}
	@RequestMapping("/partner/showOrderResult.xhtml")
	public String showOrderResult(@RequestParam(required=true) String tradeNo, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		if(order == null) return showError(model, "非法订单！");
		if(!order.surePartner()) return showError(model, "非法订单，来源不正确！");
		ApiUser partner = daoService.getObject(ApiUser.class, order.getPartnerid());
		return showOrderResult(order, partner, model, request, response);
	}
	private String showDramaOrderResult(DramaOrder order, ApiUser partner, ModelMap model){
		model.put("order", order);
		model.put("partner", partner);
		if(order.isAllPaid()){
			model.put("success", true);
			model.put("fee", order.getDue());
		}
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
		model.put("odi", odi);
		return "partner/orderDramaResult.vm";
	}
	private String showOrderResult(TicketOrder order, ApiUser partner, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Map<String, String> otherinfo = VmUtils.readJsonToMap(partner.getOtherinfo());
		String view = otherinfo.get(PayConstant.KEY_STEP4);
		if(StringUtils.isBlank(view)) view = "partner/orderResult.vm";
		if(StringUtils.isNotBlank(otherinfo.get(PayConstant.KEY_IFRAME_URL))){
			model.put(PayConstant.KEY_IFRAME_URL, otherinfo.get(PayConstant.KEY_IFRAME_URL));
		}
		model.put("order", order);
		model.put("partner", partner);
		if(order.isAllPaid()){
			model.put("success", true);
			model.put("fee", order.getDue());
		}
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("opi", opi);
		if(opi!=null && opi.getTopicid() != null){
			DiaryBase topic = diaryService.getDiaryBase(opi.getTopicid());
			if(topic!=null){
				String topicBody = blogService.getDiaryBody(topic.getId());
				model.put("topicBody", topicBody);
			}
			model.put("topic", topic);
		}
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, order.getCinemaid());
		if(profile != null){
			model.put("takemethod", profile.getTakemethod());
		}else{
			model.put("takemethod", "U");//未确定
		}
		
		// 20110215 合作商家订单完成后写cookie, 植入当前订单编号
		String[] cookies = WebUtils.getCookie4ProtectedPage(request, "partnerReg");
		if(cookies == null){
			String refer = order.getMemberid().toString();
			String userID = order.getId().toString();
			Integer far = 60 * 60 * 2 ;	// 设置默认时间
			
			// 保存2次cookie
			String path1 = config.getBasePath() + "home/member/register2.xhtml";
			String path2 = config.getBasePath() + "home/receive5Coupon.xhtml";
			WebUtils.setCookie4ProtectedPage(response, path1, refer, userID, far, "partnerReg");
			WebUtils.setCookie4ProtectedPage(response, path2, refer, userID, far, "partnerReg");
		}
		return view;//购票订单
	}
	private List<String> payList = Arrays.asList(PaymethodConstant.PAYMETHOD_UNIONPAY, PaymethodConstant.PAYMETHOD_UNIONPAY_ACTIVITY, PaymethodConstant.PAYMETHOD_UNIONPAY_JS);
	@RequestMapping("/tmpOrderForm.xhtml")
	public String tmpOrderForm(@RequestParam Long orderId, 
			@RequestParam String charset, String pause, String encQryStr, 
			Long t, String paymethod, String check, String returnUrl, String payserver, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception{
		if(t == null || t < System.currentTimeMillis() - DateUtil.m_minute * 5) return showError(model, "连接已超时，请从“我的订单”处重新支付！");
		String mycheck = StringUtil.md5("paytmp" + paymethod + t, 16);
		if(!StringUtils.equals(mycheck, check)) return showError(model, "验证错误，请从“我的订单”处重新支付！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		String ip = WebUtils.getRemoteIp(request);
		Map<String, String> params = null;
		List<String> paymethodList = paymentService.getPayserverMethodList();
		useCharset(model, charset);//使用页面编码
		if(paymethodList.contains(order.getPaymethod()) || payList.contains(order.getPaymethod()) || StringUtils.equalsIgnoreCase(payserver, "true")){
			dbLogger.warn("the ip changed of the tradeNo " + order.getTradeNo() + ": the ip of client is " + ip);
			ErrorCode<Map<String, String>> result = paymentService.getNetPayParamsV2(order, ip, null);
			if(!result.isSuccess()){
				return forwardMessage(model, "订单信息有异常，请从“我的订单”处重新支付！");
			}
			params = result.getRetval();
			dbLogger.warn("the ip changed of the tradeNo " + order.getTradeNo() + ":" + params);
			ErrorCode code = paymentService.usePayServer(paymethod, params, ip, request, response, model);
			if(!code.isSuccess()){
				return forwardMessage(model, "订单信息有异常，请从“我的订单”处重新支付！");
			}
			model.put("pause", pause);
			return "tempSubmitForm2.vm";
		}else {
			try {
				String checkvalue = null;
				if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWAPAY) && !order.isZeroPay()){
					MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", order.getMemberid());
					if(account!=null){ 
						String prikey = paymentService.getGewaPayPrikey();
						String str = account.getPassword()+account.getMemberid() + orderId + prikey;
						checkvalue = StringUtil.md5(str, "UTF-8");
					}
				}
				params = paymentService.getNetPayParams(order, ip, checkvalue);
			} catch (Exception e) {
				dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_PARTNER, "", e);
				return forwardMessage(model, "支付错误：" + e.getMessage());
			}
			model.put("submitParams", params);
			String method = params.remove("submitMethod");
			if(StringUtils.isBlank(method)) method="post";
			model.put("method", method);
			model.put("payUrl", params.remove("payurl"));
			model.put("pause", pause);
			if(StringUtils.isNotBlank(encQryStr)) params.put("encQryStr", encQryStr);
			if(StringUtils.isNotBlank(returnUrl)) params.put("returnUrl", returnUrl);
			return "tempSubmitForm.vm";
		}
		
	}
	
	@RequestMapping("/tmpChargeForm.xhtml")
	public String tmpChargeForm(HttpServletRequest request, @RequestParam Long chargeId, 
			@RequestParam String charset, String pause, 
			Long t, String paymethod, String check, String payserver, HttpServletResponse response, ModelMap model) throws Exception{
		if(t == null || t < System.currentTimeMillis() - DateUtil.m_minute * 5) return showError(model, "连接已超时，请从“我的充值记录”处重新支付！");
		String mycheck = StringUtil.md5("paytmp" + paymethod + t, 16);
		if(!StringUtils.equals(mycheck, check)) return showError(model, "验证错误，请从“我的充值记录”处重新支付！");
		Charge charge = daoService.getObject(Charge.class, chargeId);
		String ip = WebUtils.getRemoteIp(request);
		Map<String, String> params = null;
		List<String> paymethodList = paymentService.getPayserverMethodList();
		useCharset(model, charset);//使用页面编码
		if(paymethodList.contains(charge.getPaymethod()) || payList.contains(charge.getPaymethod()) || StringUtils.equalsIgnoreCase(payserver, "true")){
			params = paymentService.getNetChargeParamsV2(charge, ip, null);
			ErrorCode code = paymentService.usePayServer(paymethod, params, ip, request, response, model);
			if(!code.isSuccess()) return forwardMessage(model, code.getMsg());
			model.put("pause", pause);
			return "tempSubmitForm2.vm";
		}else {
			return forwardMessage(model, "充值方式错误！");
		}
	}
}
