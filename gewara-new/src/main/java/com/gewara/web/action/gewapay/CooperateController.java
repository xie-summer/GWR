/**
 * 
 */
package com.gewara.web.action.gewapay;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.api.pay.request.ActivationQueryRequest;
import com.gewara.api.pay.request.SendSmsRequest;
import com.gewara.api.pay.response.ActivationQueryResponse;
import com.gewara.api.pay.response.SendSmsResponse;
import com.gewara.api.pay.service.UnionPayFastApiService;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.JsonKeyOrderCallback;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.PayMerchant;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.pay.BackConstant;
import com.gewara.pay.NewPayUtil;
import com.gewara.pay.UnionpayFastUtil;
import com.gewara.service.pay.GatewayService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CooperateService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.CAUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class CooperateController extends AnnotationController{
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("cooperateService")
	private CooperateService cooperateService;
	public void setCooperateService(CooperateService cooperateService) {
		this.cooperateService = cooperateService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	@Autowired@Qualifier("gatewayService")
	private GatewayService gatewayService;
	
	@Autowired@Qualifier("unionPayFastApiService")
	private UnionPayFastApiService unionPayFastApiService;
	
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@RequestMapping("/ajax/cooperate/shbankDiscount.xhtml")
	public String shbankDiscount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, String preCardno, String postCardno, ModelMap model){
		if(StringUtils.length(postCardno)!=4 || !StringUtils.isNumeric(postCardno)){
			return showJsonError(model, "输入的卡号不正确");
		}
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<String> retCode = cooperateService.checkShbankCode(orderId, preCardno, postCardno);
		if(!retCode.isSuccess()) return showJsonError(model, retCode.getMsg());
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, new JsonKeyOrderCallback(BackConstant.shbankcardno, preCardno + "~" + postCardno), ip);
		if(discount.isSuccess()) {
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	@RequestMapping("/ajax/cooperate/xybankDiscount.xhtml")
	public String xybankDiscount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, String preCardno, String postCardno, ModelMap model){
		if(StringUtils.length(postCardno)!=4 || !StringUtils.isNumeric(postCardno)){
			return showJsonError(model, "输入的卡号不正确");
		}
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<String> retCode = cooperateService.checkXybankCode(orderId, preCardno, postCardno);
		if(!retCode.isSuccess()) return showJsonError(model, retCode.getMsg());
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, new JsonKeyOrderCallback(BackConstant.xybankcardno, preCardno + "~" + postCardno), ip);
		if(discount.isSuccess()) {
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	@RequestMapping("/ajax/cooperate/hxbankDiscount.xhtml")
	public String hxbankDiscount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, String preCardno, String postCardno, ModelMap model){
		if(StringUtils.length(postCardno)!=4 || !StringUtils.isNumeric(postCardno)){
			return showJsonError(model, "输入的卡号不正确");
		}
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<String> retCode = cooperateService.checkHxbankCode(orderId, preCardno, postCardno);
		if(!retCode.isSuccess()) return showJsonError(model, retCode.getMsg());
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, new JsonKeyOrderCallback(BackConstant.hxbankcardno, preCardno + "~" + postCardno),  ip);
		if(discount.isSuccess()) {
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	
	@RequestMapping("/ajax/cooperate/queryOrderResult.xhtml")
	public String queryOrderResult(String paymethod,String tradeNo, ModelMap model){
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order != null && order.isAllPaid()){
			return showJsonSuccess(model,"00"); 
		}
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("paymethod", paymethod);
		paramMap.put("tradeNo",tradeNo);
		String paramStr = JsonUtils.writeMapToJson(paramMap);
		String sign = CAUtil.doSign(paramStr, NewPayUtil.getMerprikey(), "utf-8", "SHA1WithRSA");
		Map<String, String> postMap = new HashMap<String, String>();
		postMap.put("merid", NewPayUtil.getMerid());
		try {
			postMap.put("params", Base64.encodeBase64String(paramStr.getBytes("UTF-8")));
			postMap.put("sign", sign);
			
			String payurl = NewPayUtil.getPayurl();
			if(gatewayService.isSwitch(paymethod)){
				payurl = NewPayUtil.getNewPayurl();
			}
			HttpResult code = HttpUtils.postUrlAsString(payurl.replaceAll("getPayParams.xhtml", "queryOrderResult.xhtml"), postMap);
			if(code.isSuccess()){
				String res = new String(Base64.decodeBase64(code.getResponse()), "utf-8");
				Map<String, String> returnMap = VmUtils.readJsonToMap(res);
				Map<String,String> result = VmUtils.readJsonToMap(returnMap.get("submitParams"));
				if(StringUtils.equals(result.get("resultCode"), "00")){
					return showJsonSuccess(model,"00");
				}else if(StringUtils.equals(result.get("resultCode"), "0002")){
					return showJsonSuccess(model,result.get("responseMsg"));
				}
				return showJsonError(model,result.get("responseMsg"));
			}else{
				return this.showJsonError(model, "网络异常，请稍后进行查看");
			}
		} catch (Exception e) {
			return showJsonError(model, "网络异常，请稍后进行查看");
		}
	}
	
	//该方法，由于不走特价活动，所以不检查优惠活动，即直接用来检查卡号
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount.xhtml")
	public String unionPay2Discount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, String cardNumber,  ModelMap model){
		if(orderId == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")){
			return showJsonError(model, "非法操作！");
		}
		if(StringUtils.isBlank(cardNumber) || cardNumber.length() > 19 || cardNumber.length() < 13){
			return showJsonError(model, "请输入正确位数的银行卡号！");
		}
		/***/
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		if(discountList != null && !discountList.isEmpty()){
			for(Discount discount: discountList){
				if(StringUtils.equals(PayConstant.DISCOUNT_TAG_PARTNER, discount.getTag())){
					ErrorCode<String> retCode = cooperateService.checkUnionPayFastCode(order,order.getPaybank(), cardNumber,discount.getRelatedid());
					if(!retCode.isSuccess()) {
						return showJsonError(model, retCode.getMsg());
					}
					break;
				}
			}
		}
		
		ErrorCode<Map> errorCode = validateCardNumber(order, order.getPaymethod(), cardNumber);
		if(!errorCode.isSuccess()){
			return showJsonError(model,errorCode.getMsg());
		}
		Map jsonMap = errorCode.getRetval();
		
		Map<String, String> other = JsonUtils.readJsonToMap(order.getOtherinfo());
		other.put("cardNumber", cardNumber);
		other.put("validateCardStatus", (String)jsonMap.get("retval"));
		other.put("phoneNumber", (String)jsonMap.get("phoneNumber"));
		order.setOtherinfo(JsonUtils.writeMapToJson(other));
		daoService.saveObject(order);
		
		return showJsonSuccess(model, jsonMap);
	}
	
	/**
	 * 银联2.0认证支付活动，卡号验证，包括特价活动的一些验证
	 * 
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param cardNumber
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 20, 2013 5:00:31 PM
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity.xhtml")
	public String unionPayFastDiscountActivity(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "ALL");
	}
	
	/**
	 * 苏洲中国银行，银联2.0认证支付活动，卡号验证，包括特价活动的一些验证
	 * 
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param paymethod
	 * @param cardNumber
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 7, 2013 6:11:29 PM
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/sz.xhtml")
	public String unionPayFastDiscountActivitySZ(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "SZ");
	}
	
	
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/nyyh.xhtml")
	public String unionPayFastDiscountActivityNYYH(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "NYYH");
	}
	
	/**
	 * 重庆农商行活动，验证卡bin，每卡只能参加一次活动
	 * 
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param paymethod
	 * @param cardNumber
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 6:00:32 PM
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/cqnsyh.xhtml")
	public String unionPayFastDiscountActivityCqnsyh(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "CQNSYH");
	}
	
	/**
	 * 银联卡友节活动，限制条件为62卡，每卡只能参加一次活动
	 * 
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param paymethod
	 * @param cardNumber
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:32:30 PM
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/youjie.xhtml")
	public String unionPayFastDiscountActivityYoujie(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "YOUJIE");
	}
	
	/**
	 * 温州银行信用卡开出来走上海的商户号，活动用卡的限制为每卡每周限使用一次。
	 * 
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param paymethod
	 * @param cardNumber
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Jun 9, 2013 11:47:59 AM
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/wzyh.xhtml")
	public String unionPayFastDiscountActivitywzcb(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "WZCB");
	}
	
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/zdyh.xhtml")
	public String unionPayFastDiscountActivityzdcb(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "ZDCB");
	}
	
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/srcb.xhtml")
	public String unionPayFastDiscountActivitysrcb(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "SRCB");
	}
	
	// 邮政储蓄
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/psbc.xhtml")
	public String unionPayFastDiscountActivityPsbc(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "PSBC");
	}
	
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/common.xhtml")
	public String unionPayFastDiscountActivityCommon(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		if(orderId == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")){
			return showJsonError(model, "非法操作！");
		}
		if(StringUtils.isBlank(paymethod)){
			return showJsonError(model, "请选择正确的支付方式！");
		}
		if(StringUtils.isBlank(cardNumber) || cardNumber.length() > 19 || cardNumber.length() < 13){
			return showJsonError(model, "请输入正确位数的银行卡号！");
		}
		//活动需要验证卡bin
		ErrorCode<String> retCode = cooperateService.checkCommonCardbinOrCardNumLimit(order, spid, cardNumber);
		if(!retCode.isSuccess()) {
			return showJsonError(model, retCode.getMsg());
		}
		ErrorCode<Map> errorCode = validateCardNumber(order, PaymethodConstant.PAYMETHOD_UNIONPAYFAST, cardNumber);
		if(!errorCode.isSuccess()){
			return showJsonError(model,errorCode.getMsg());
		}
		Map jsonMap = errorCode.getRetval();
		final String validateCardStatus = (String) jsonMap.get("retval");	
		final String phoneNumber = (String) jsonMap.get("phoneNumber");
		String ip = WebUtils.getRemoteIp(request);
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		//specialDiscountService.useSpecialDiscount 这个方法里修改了订单的支付方式，默认所选优惠活动中维护的支付方式的第一个
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(order.getOrdertype(), orderId, sd, new OrderCallback(){
			@Override
			public void processOrder(SpecialDiscount sd2, GewaOrder gewaOrder) {
				Map<String, String> other = JsonUtils.readJsonToMap(gewaOrder.getOtherinfo());
				other.put("cardNumber", cardNumber);
				other.put("validateCardStatus", validateCardStatus);
				other.put("phoneNumber", phoneNumber);
				other.put("hasCardNumber", "Y");
				other.put("discountByPaymethod", paymethod);//在confirmOrder.vm页，将用户没有选择的支付方式过滤掉。
				other.put(BackConstant.unionfastcardno, cardNumber);
				String[] pay = StringUtils.split(paymethod, ":");
				gewaOrder.setOtherinfo(JsonUtils.writeMapToJson(other));
				gewaOrder.setPaymethod(pay[0]);//将支付方式改成用户选择的
				gewaOrder.setPaybank(pay.length > 1 ? pay[1] : null);
			}
			
		},ip);
		if(discount.isSuccess()) {
			jsonMap.put("amount",discount.getRetval().getCurUsedDiscount().getAmount());
			return showJsonSuccess(model, jsonMap);
		}		
		return showJsonError(model, discount.getMsg());
	}
	
	private String unionPayFastDiscountAJ(String sessid,HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model,String activeType){
		if(orderId == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")){
			return showJsonError(model, "非法操作！");
		}
		if(StringUtils.isBlank(paymethod)){
			return showJsonError(model, "请选择正确的支付方式！");
		}

		if(StringUtils.isBlank(cardNumber) || cardNumber.length() > 19 || cardNumber.length() < 13){
			return showJsonError(model, "请输入正确位数的银行卡号！");
		}
		
		String[] prik = StringUtils.split(paymethod,":");
		String paybank = prik.length > 1 ? prik[1] : null;
		
		
		//活动需要验证卡bin
		//TODO order.getPaybank()支付页出现前，该值没有 
		ErrorCode<String> retCode = null;
		if("SZ".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastCodeForSZ(order,paybank, cardNumber,spid);
		}else if("CQNSYH".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastCodeForCqnsyh(order,paybank, cardNumber,spid);
		}else if("YOUJIE".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastCodeForYouJie(order,paybank, cardNumber,spid);
		}else if("NYYH".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastCodeForNyyh(order,paybank, cardNumber,spid);
		}else if("WZCB".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastCodeForWzcb(order,paybank, cardNumber,spid);
		}else if("ZDCB".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastCodeForZdcb(order,paybank, cardNumber,spid);
		}else if("shenzhenPingAn".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastShenZhenCodeForPingAn(order, paybank, cardNumber, spid);
		}else if("guangzhouBocWeekOne".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastGuangzhouCodeForBocByWeekone(order, paybank, cardNumber, spid);
		}else if("guangzhouBocMonthTwo".equals(activeType)){
			retCode = cooperateService.checkUnionPayFastGuangzhouCodeForBocByMonthTwo(order, paybank, cardNumber, spid);
		}else if("SRCB".equals(activeType)){
			retCode = cooperateService.checkCommonCardbinOrCardNumLimit(order, spid, cardNumber);
		}else if("PSBC".equals(activeType)){
			retCode = cooperateService.checkCommonCardbinOrCardNumLimit(order, spid, cardNumber);
		}else{
			retCode = cooperateService.checkUnionPayFastCode(order,paybank, cardNumber,spid);
		}
		
		if(!retCode.isSuccess()) {
			return showJsonError(model, retCode.getMsg());
		}
		
		ErrorCode<Map> errorCode = validateCardNumber(order, PaymethodConstant.PAYMETHOD_UNIONPAYFAST, cardNumber);//此处只能用PaymethodConstant.PAYMETHOD_UNIONPAYFAST
		if(!errorCode.isSuccess()){
			return showJsonError(model,errorCode.getMsg());
		}
		Map jsonMap = errorCode.getRetval();
		final String validateCardStatus = (String) jsonMap.get("retval");	
		final String phoneNumber = (String) jsonMap.get("phoneNumber");
		String ip = WebUtils.getRemoteIp(request);
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		//specialDiscountService.useSpecialDiscount 这个方法里修改了订单的支付方式，默认所选优惠活动中维护的支付方式的第一个
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(order.getOrdertype(), orderId, sd, new OrderCallback(){

			@Override
			public void processOrder(SpecialDiscount sd2, GewaOrder gewaOrder) {
				Map<String, String> other = JsonUtils.readJsonToMap(gewaOrder.getOtherinfo());
				other.put("cardNumber", cardNumber);
				other.put("validateCardStatus", validateCardStatus);
				other.put("phoneNumber", phoneNumber);
				other.put("hasCardNumber", "Y");
				other.put("discountByPaymethod", paymethod);//在confirmOrder.vm页，将用户没有选择的支付方式过滤掉。
				other.put(BackConstant.unionfastcardno, cardNumber);
				String[] pay = StringUtils.split(paymethod, ":");
				gewaOrder.setOtherinfo(JsonUtils.writeMapToJson(other));
				gewaOrder.setPaymethod(pay[0]);//将支付方式改成用户选择的
				gewaOrder.setPaybank(pay.length > 1 ? pay[1] : null);
			}
			
		},ip);
		if(discount.isSuccess()) {
			jsonMap.put("amount",discount.getRetval().getCurUsedDiscount().getAmount());
			return showJsonSuccess(model, jsonMap);
		}		
		return showJsonError(model, discount.getMsg());
	}
	
	/**
	 * 江苏银联2.0认证支付活动，卡号验证，包括特价活动的一些验证
	 * 
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param cardNumber
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 20, 2013 5:08:06 PM
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/ajs.xhtml")
	public String unionPay2DiscountActivity(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid,final String cardNumber,  ModelMap model){
		if(orderId == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")){
			return showJsonError(model, "非法操作！");
		}
		if(StringUtils.isBlank(cardNumber) || cardNumber.length() > 19 || cardNumber.length() < 13){
			return showJsonError(model, "请输入正确位数的银行卡号！");
		}
		//活动需要验证卡bin
		ErrorCode<String> retCode = cooperateService.checkUnionPayFastAJS(order,cardNumber,spid);
		if(!retCode.isSuccess()) {
			return showJsonError(model, retCode.getMsg());
		}
		
		ErrorCode<Map> errorCode = validateCardNumber(order, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS, cardNumber);
		if(!errorCode.isSuccess()){
			return showJsonError(model,errorCode.getMsg());
		}
		
		Map jsonMap = errorCode.getRetval();
		String ip = WebUtils.getRemoteIp(request);
		return unionpayFast2UseSpecialDiscount(ip, spid, order, cardNumber, jsonMap, model);
	}
	/**
	 * 北京银联活动卡bin校验
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param cardNumber
	 * @param model
	 * @return
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/abj.xhtml")
	public String unionPay2DiscountActivityBJ(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid,final String cardNumber,  ModelMap model){
		if(orderId == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showJsonError(model, "请确认您要支付的订单是否正确！");
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")){
			return showJsonError(model, "非法操作！");
		}
		if(StringUtils.isBlank(cardNumber) || cardNumber.length() > 19 || cardNumber.length() < 13){
			return showJsonError(model, "请输入正确位数的银行卡号！");
		}
		//活动需要验证卡bin
		ErrorCode<String> retCode = cooperateService.checkUnionPayFastBJ(order,cardNumber,spid);
		if(!retCode.isSuccess()) {
			return showJsonError(model, retCode.getMsg());
		}
		ErrorCode<Map> errorCode = validateCardNumber(order, PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ, cardNumber);
		if(!errorCode.isSuccess()){
			return showJsonError(model,errorCode.getMsg());
		}
		Map jsonMap = errorCode.getRetval();
		String ip = WebUtils.getRemoteIp(request);
		return this.unionpayFast2UseSpecialDiscount(ip, spid, order, cardNumber, jsonMap, model);
	}
	/**
	 * 银联2.0活动使用特价活动
	 * @param ip
	 * @param spid
	 * @param order
	 * @param cardNumber
	 * @param jsonMap
	 * @param model
	 * @return
	 */
	private String unionpayFast2UseSpecialDiscount(String ip,long spid,GewaOrder order,final String cardNumber,
			Map jsonMap,ModelMap model){
		final String validateCardStatus = (String) jsonMap.get("retval");	
		final String phoneNumber = (String) jsonMap.get("phoneNumber");
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(order.getOrdertype(), order.getId(), sd, new OrderCallback(){
			@Override
			public void processOrder(SpecialDiscount sd2, GewaOrder gewaOrder) {
				Map<String, String> other = JsonUtils.readJsonToMap(gewaOrder.getOtherinfo());
				other.put(BackConstant.unionfastajscardno, cardNumber);
				other.put("cardNumber", cardNumber);
				other.put("validateCardStatus", validateCardStatus);
				other.put("hasCardNumber", "Y");
				other.put("phoneNumber", phoneNumber);
				gewaOrder.setOtherinfo(JsonUtils.writeMapToJson(other));
			}			
		},ip);
		if(discount.isSuccess()) {
			jsonMap.put("amount",discount.getRetval().getCurUsedDiscount().getAmount());
			return showJsonSuccess(model, jsonMap);
		}
		return showJsonError(model, discount.getMsg());
	}
	
	private ErrorCode<Map> validateCardNumber(GewaOrder order,String paymethod,String cardNumber){
		if(gatewayService.isSwitch(PaymethodConstant.PAYMETHOD_UNIONPAYFAST)){
			return validateCardNumberNew(order, cardNumber);
		}else {
			return validateCardNumberOld(order, paymethod, cardNumber);
		}
	}
	
	/**
	 * 
	 * 
	 * @param paymethod   对应商户标识，否则有问题
	 * @param cardNumber
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Nov 7, 2013 2:30:55 PM
	 */
	private ErrorCode<Map> validateCardNumberNew(GewaOrder order,String cardNumber){
		//String merchantCode = paymethod;
		ErrorCode<PayMerchant> code = gatewayService.findMerchant(order.getCitycode(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST);
		if(!code.isSuccess()){
			dbLogger.warn("tradeNo is " + order.getTradeNo() + " " + code.getMsg());
			return ErrorCode.getFailure(code.getMsg());
		}
		String merchantCode = code.getRetval().getMerchantCode();
		ActivationQueryRequest request = new ActivationQueryRequest(merchantCode, cardNumber);
		ActivationQueryResponse response = unionPayFastApiService.activationQuery(request);
		if(!response.isSuccess()){
			return ErrorCode.getFailure(response.getMsg());
		}
		
		if(StringUtils.isBlank(response.getPhoneNumber())){
			return ErrorCode.getFailure("银行卡绑定手机号为空");
		}
		Map jsonMap = new HashMap();
		jsonMap.put("retval", response.getActivateStatus());
		jsonMap.put("phoneNumber",response.getPhoneNumber());//银行卡帮定的手机号
		return ErrorCode.getSuccessReturn(jsonMap);
	}
	
	/**
	 * 为银联认证支付2.0验证卡号
	 * 
	 * @param order
	 * @param paymethod
	 * @param cardNumber
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 20, 2013 4:55:39 PM
	 */
	private ErrorCode<Map> validateCardNumberOld(GewaOrder order,String paymethod,String cardNumber){
		HttpResult result = UnionpayFastUtil.getCardActivateStatus(paymethod,cardNumber);
		if(!result.isSuccess()){
			return ErrorCode.getFailure(result.getMsg());
		}
		Map<String, String> responses = UnionpayFastUtil.parseUnionpayResponse(result.getResponse());
		if(!UnionpayFastUtil.checkSign(paymethod,responses)){
			return ErrorCode.getFailure("签名验证未通过，非法操作!");
		}
		if(!StringUtils.equals("00",responses.get("respCode"))){
			String respMsg = responses.get("respMsg");
			if(StringUtils.equals("60", responses.get("respCode"))){
				respMsg = "您好，您的" + respMsg + ",请您点击右侧链接进行开通！";
			}
			return ErrorCode.getFailure(respMsg);
		}
				
		Map jsonMap = new HashMap();
		String activateStatus = responses.get("activateStatus");
		jsonMap.put("retval", activateStatus);
		Map<String, String> cupReserved = UnionpayFastUtil.parseUnionpayResponse(responses.get("cupReserved").replace("}", "").replace("{", ""));
		/**
		if("1".equals(activateStatus)){
			if(cupReserved.get("expiry") == null || cupReserved.get("transLimit") ==null){
				dbLogger.error(responses.get("cupReserved"));
				return ErrorCode.getFailure("银行卡验证错误");
			}
			
			if(DateUtil.format(DateUtil.currentTime(), "yyyyMMdd").compareTo(cupReserved.get("expiry")) > 0){
				return ErrorCode.getFailure("您好，您开通的小额支付有效期已过，不能进行支付！");
			}
			if(order.getDue() * 100 > Integer.parseInt(cupReserved.get("transLimit"))){
				return ErrorCode.getFailure("您好，您的订单金额超出您开通的小额支付的单笔限额额度，不能进行支付！");
			}
		}
		*/
		
		if(StringUtils.equals("0", activateStatus) || StringUtils.equals("1", activateStatus)){
			if(StringUtils.isBlank(cupReserved.get("phoneNumber"))){
				dbLogger.error(responses.get("cupReserved"));
				return ErrorCode.getFailure("银行卡绑定手机号为空");
			}
			
			jsonMap.put("phoneNumber",cupReserved.get("phoneNumber"));//银行卡帮定的手机号
			return ErrorCode.getSuccessReturn(jsonMap);
		}else{
			dbLogger.error(responses.get("cupReserved"));
			return ErrorCode.getFailure("银行卡验证错误，请确定是否开通银联认证支付。");
		}
		
	}
	
	
	@RequestMapping("/ajax/cooperate/unionPayFastSendSms.xhtml")
	public String unionPay2SendSms(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId ,String cardNumber,String phoneNumber, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(!member.getId().equals(order.getMemberid())){
			return showJsonError(model, "不可对他人订单操作！");
		}
		if(StringUtils.isBlank(phoneNumber)){
			phoneNumber = order.getMobile();
		}
		
		if(gatewayService.isSwitch(PaymethodConstant.PAYMETHOD_UNIONPAYFAST)){
			//String merchantCode = order.getPaymethod();
			ErrorCode<PayMerchant> code = gatewayService.findMerchant(order.getCitycode(), PaymethodConstant.PAYMETHOD_UNIONPAYFAST);
			if(!code.isSuccess()){
				dbLogger.warn("tradeNo is " + order.getTradeNo() + " " + code.getMsg());
				return showJsonError(model, code.getMsg());
			}
			String merchantCode = code.getRetval().getMerchantCode();
			SendSmsRequest sendSmsRequest = new SendSmsRequest(merchantCode, order.getTradeNo(), cardNumber, phoneNumber, order.getDue() * 100);
			SendSmsResponse sendSmsResponse = unionPayFastApiService.sendSms(sendSmsRequest);
			if(!sendSmsResponse.isSuccess()){
				return showJsonError(model, sendSmsResponse.getMsg());
			}			
		}else{
			HttpResult result = UnionpayFastUtil.sendSms(order,cardNumber,phoneNumber);
			if(!result.isSuccess()){			
				return showJsonError(model, result.getMsg());
			}
			Map<String, String> responses = UnionpayFastUtil.parseUnionpayResponse(result.getResponse());
			if(!UnionpayFastUtil.checkSign(order.getPaymethod(),responses)){
				return showJsonError(model, "签名验证未通过，非法操作!");
			}
			if(!StringUtils.equals("00",responses.get("respCode"))){
				return showJsonError(model, responses.get("respMsg"));
			}
		}		
		
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/validate/cooperate/shbank.xhtml")
	@ResponseBody
	public String shbankDiscount(String otherinfo, String tradeno, Long orderid){
		ErrorCode<String> code = cooperateService.checkShbankBack(orderid, otherinfo, "true");
		if(!code.isSuccess()){
			String msg = code.getMsg();
			if(StringUtils.equals(code.getErrcode(),"error")){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "兴业银行验证非法：" + tradeno + ", 验证结果" + msg);
				return "error";
			}
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "上海银行验证非法：" + tradeno + ", 验证结果" + msg);
			return msg;
		}
		return "success";
	}
	
	@RequestMapping("/validate/cooperate/xybank.xhtml")
	@ResponseBody
	public String xybankDiscount(String otherinfo, String tradeno, Long orderid){
		ErrorCode<String> code = cooperateService.checkXybankBack(orderid, otherinfo, "true");
		if(!code.isSuccess()){
			String msg = code.getMsg();
			if(StringUtils.equals(code.getErrcode(),"error")){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "兴业银行验证非法：" + tradeno + ", 验证结果" + msg);
				return "error";
			}
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "兴业银行验证非法：" + tradeno + ", 验证结果" + msg);
			return msg;
		}
		return "success";
	}
	@RequestMapping("/validate/cooperate/gfbank.xhtml")
	@ResponseBody
	public String gfbank(Long orderid){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		Map<String, String> map = new HashMap<String, String>();
		map.put("orderid", orderid+"");
		map.put("tradeno", order.getTradeNo());
		map.put("memberid", order.getMemberid()+"");
		map.put("week", DateUtil.getCnWeek(order.getAddtime()));//周几
		map.put("draw", Status.N);								//是否已经抽奖
		map.put("winnerid", "");								//获奖记录ID
		mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_GFBANK_ORDER);
		return "success";
	}
	
	
	/**
	 * 深圳平安银行，活动用卡的限制为每卡每周限使用一次。
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/shenzhenPingAn.xhtml")
	public String shenzhenPingAn(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "shenzhenPingAn");
	}
	/**
	 * 广州中国银行（立减），活动用卡的限制为每卡每周限使用一次。
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/guangzhouBocWeekOne.xhtml")
	public String guangzhouBocWeekOne(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "guangzhouBocWeekOne");
	}
	/**
	 * 广州中国银行（抢票），每周限1笔订单、每月限2笔订单
	 */
	@RequestMapping("/ajax/cooperate/unionPayFastDiscount/activity/guangzhouBocMonthTwo.xhtml")
	public String guangzhouBocMonthTwo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, final String paymethod,final String cardNumber,  ModelMap model){
		return unionPayFastDiscountAJ(sessid, request, orderId, spid, paymethod, cardNumber, model, "guangzhouBocMonthTwo");
	}
	/**
	 * 特价活动针对特殊号段的手机号码进行验证
	 * @param sessid
	 * @param request
	 * @param orderId
	 * @param spid
	 * @param mobile
	 * @param model
	 * @return
	 */
	@RequestMapping("/ajax/cooperate/spDiscount/activity/valiMobileCommon.xhtml")
	public String valiMobileCommon(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid,final String mobile,  ModelMap model){
		if(orderId == null) {
			return showJsonError(model, "请确认您要支付的订单是否正确！");
		}
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) {
			return showJsonError(model, "请先登录！");
		}
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) {
			return showJsonError(model, "请确认您要支付的订单是否正确！");
		}
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")){
			return showJsonError(model, "非法操作！");
		}
		if(!ValidateUtil.isMobile(mobile)){
			return showJsonError(model, "请输入正确手机号码！");
		}
		//活动需要验证卡bin
		ErrorCode<String> retCode = cooperateService.checkCommonCardbinOrCardNumLimit(order, spid, mobile);
		if(!retCode.isSuccess()) {
			return showJsonError(model, retCode.getMsg());
		}
		String ip = WebUtils.getRemoteIp(request);
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		//specialDiscountService.useSpecialDiscount 这个方法里修改了订单的购票手机号码
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(order.getOrdertype(), orderId, sd, new OrderCallback(){
			@Override
			public void processOrder(SpecialDiscount sd2, GewaOrder gewaOrder) {
				gewaOrder.setMobile(mobile);
			}
			
		},ip);
		if(discount.isSuccess()) {
			Map jsonMap = new HashMap();
			jsonMap.put("amount",discount.getRetval().getCurUsedDiscount().getAmount());
			return showJsonSuccess(model, jsonMap);
		}		
		return showJsonError(model, discount.getMsg());
	}
}
