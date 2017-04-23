package com.gewara.web.action.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.BindConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.TokenType;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.helper.order.JsonKeyOrderCallback;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.pay.CCBPosPayUtil;
import com.gewara.service.OperationService;
import com.gewara.service.member.BindMobileService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class FastPayController extends BasePayController {
	public static final String CARD_PEX = "^\\d{16}$";
	public static final String DATE_PEX = "^\\d{4}$";
	
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	public void setBindMobileService(BindMobileService bindMobileService){
		this.bindMobileService = bindMobileService;
	}
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@RequestMapping("/ajax/mobile/ccbcode.xhtml")
	public String ccbMobileCode(@CookieValue(value=LOGIN_COOKIE_NAME,required=false)String sessid, 
			String mobile, String captchaId, String captcha, ModelMap model, HttpServletRequest request){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		//if(StringUtils.isBlank(member.getMobile())) return showJsonError(model, "请先绑定手机号！");
		Map jsonMap = new HashMap();
		Map errorMap = new HashMap();
		jsonMap.put("errorMap", errorMap);
		if(StringUtils.isBlank(mobile)){
			errorMap.put("mobile", "请输入手机号！");
			return showJsonError(model, jsonMap);
		}
		if(!ValidateUtil.isMobile(mobile)){
			errorMap.put("mobile","手机号输入错误！");
			return showJsonError(model, jsonMap);
		}
		boolean iscaptcha = bindMobileService.isNeedToken(TokenType.CCBMobile, ip, 2);
		if(iscaptcha){
			jsonMap.put("refreshCaptcha", "true");
			model.put("iscaptcha", iscaptcha);
			if(StringUtils.isBlank(captcha)){
				errorMap.put("captcha", "请输入验证码！");
				return showJsonError(model, jsonMap);
			}else{
				boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
				errorMap.put("captcha", "验证码错误！");
				if(!isValidCaptcha) return showJsonError(model, jsonMap);
			}
		}
		boolean next = bindMobileService.getAndUpdateToken(TokenType.CCBMobile, ip, 2);
		if(next){
			jsonMap.put("refreshCaptcha", "true");
		}
		ErrorCode<SMSRecord> code = bindMobileService.refreshBindMobile(BindConstant.TAG_CCBANKCODE, mobile, ip);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		untransService.sendMsgAtServer(code.getRetval(), false);
		
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/gewapay/ccbquickpay.xhtml")
	public String fastPayCCBank(@CookieValue(value=LOGIN_COOKIE_NAME,required=false)String sessid, 
			Long orderId, String ccbCardno, String modCard, String mobile, String checkpass, String errorMsg, HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showError(model, "请先登录！");
		model.put("member", member);
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showMessageAndReturn(model, request, "订单不存在！");
		if(StringUtils.isBlank(ccbCardno)){ 
			ccbCardno = VmUtils.getJsonValueByKey(order.getOtherinfo(), "ccbCardno");
			modCard = "N";
		}
		model.put("ccbCardno", ccbCardno);
		model.put("modCard", modCard);
		model.put("order", order);
		model.put("mobile", mobile);
		model.put("checkpass", checkpass);
		model.put("errorMsg", errorMsg);
		return "gewapay/ccbQuickPay.vm";
	}
	private String showPayMsg(ModelMap model, String errorMsg){
		model.put("errorMsg", errorMsg);
		return "redirect:/gewapay/ccbquickpay.xhtml";
	}
	@RequestMapping("/gewapay/saveFastPay.xhtml")
	public String saveFastPay(@CookieValue(value=LOGIN_COOKIE_NAME,required=false)String sessid, Long orderId, 
			String creditCard, String expire_month, String expire_year, String cvv2, String mobile, 
			String checkpass, HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null) return showError(model, "订单不存在！");
		if(!StringUtils.equals(member.getId()+"", order.getMemberid()+"")) return showError(model, "不能查看别人的订单！");
		if (order.isAllPaid() || order.isCancel()) return showError(model, "不能修改已支付或已（过时）取消的订单！");
		model.put("orderId", orderId);
		if(order.isAllPaid()) {
			return "redirect:/gewapay/orderResult.xhtml";
		}
		model.put("mobile", mobile);
		model.put("checkpass", checkpass);
		String modCard = "N";
		String ccbCardno = VmUtils.getJsonValueByKey(order.getOtherinfo(), "ccbCardno");
		if(StringUtils.isBlank(ccbCardno)) { 
			ccbCardno = creditCard;
			modCard = "Y";
		}
		model.put("ccbCardno", ccbCardno);
		model.put("modCard", modCard);
		//1、验证基本信息
		if(StringUtils.isBlank(ccbCardno)){
			return showPayMsg(model, "信用卡号不能为空！");
		}
		if(StringUtils.isBlank(expire_month) || StringUtils.isBlank(expire_year)){
			return showPayMsg(model, "信用卡有效日期不能为空！");
		}
		if(StringUtils.length(expire_month)!=2 || StringUtils.length(expire_year)!=2){
			return showPayMsg(model, "信用卡有效日期格式错误！");
		}
		//if(!isValidCardbin(ccbCardno)) return showPayMsg(model, "非有效的建行信用卡卡号");
		if(StringUtils.isBlank(cvv2) || StringUtils.length(cvv2)!=3){
			return showPayMsg(model, "CVV2码格式错误！");
		}
		if(StringUtils.isBlank(mobile) || !ValidateUtil.isMobile(mobile)){
			return showPayMsg(model, "手机号输入错误！");
		}
		if(StringUtils.isBlank(checkpass)){
			return showPayMsg(model, "手机验证码不能为空！");
		}
		
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(ccbCardno, CARD_PEX))){
			return showPayMsg(model, "信用卡号格式错误！");
		}
		String expire = expire_year + expire_month;
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(expire, DATE_PEX))){
			return showPayMsg(model, "有效期格式错误！");
		}
		if(order instanceof SportOrder){
			if(isParticipateDiscountSport(ccbCardno)) return showPayMsg(model, "系统忙，请稍后再试！");
		}
		
		ErrorCode code = bindMobileService.checkBindMobile(BindConstant.TAG_CCBANKCODE, mobile, checkpass);
		if(!code.isSuccess()) return showPayMsg(model, code.getMsg());
		boolean allow = operationService.updateOperation("pay" + orderId, 30);
		//2、验证优惠
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("trade_no", order.getTradeNo());
		params.put("cardno", ccbCardno);
		params.put("amount", ""+order.getDue());
		params.put("cvv2", cvv2);
		params.put("expire", expire);
		if(!allow) {
			return showPayMsg(model, "系统正在处理付款，请15-20秒后查看我的订单！");
		}
		GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CCBPOSPAY);
		String payurl = CCBPosPayUtil.getPayUrl(gconfig);
		HttpResult result = HttpUtils.postUrlAsString(payurl, params);
		if(result.isSuccess()){
			Map<String, String> returnMap = JsonUtils.readJsonToMap(result.getResponse());
			orderMonitorService.addOrderPayCallback(order.getTradeNo(), OrderProcessConstant.CALLTYPE_RETURN, PaymethodConstant.PAYMETHOD_CCBPOSPAY, result.getResponse() + ",host=" + Config.getServerIp());
			if(StringUtils.equals(returnMap.get("result"), "success")){
				ErrorCode<GewaOrder> payResult = paymentService.netPayOrder(order.getTradeNo(), returnMap.get("payseqno"), order.getDue(), PaymethodConstant.PAYMETHOD_CCBPOSPAY, "ccb", "建行POS");
				if(payResult.isSuccess()) {
					processPay(order.getTradeNo(), "建行POS");
					GewaOrder rorder = payResult.getRetval();
					Map map = new HashMap();
					map.put("tradeno", rorder.getTradeNo());
					map.put("paiddate", DateUtil.format(rorder.getPaidtime(), "yyyyMMdd"));
					map.put("alipaid", rorder.getAlipaid()+"");
					map.put("payseqno", rorder.getPayseqno());
					map.put("settle", "N");
					mongoService.saveOrUpdateMap(map, "tradeno", MongoData.NS_CCBPOS_ORDER);
				}
				model.remove("mobile");
				model.remove("checkpass");
				model.remove("modCard");
				model.remove("ccbCardno");
				return "redirect:/gewapay/orderResult.xhtml";
			}else{
				return showPayMsg(model, returnMap.get("message"));
			}
		}else{
			return showPayMsg(model, "网络请求错误！");
		}
	}
	//建行信用卡支付优惠
	@RequestMapping("/gewapay/ccb.xhtml")
	public String ccb(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/ccbpay.vm";
	}
	
	//广州电影建行信用卡支付优惠
	@RequestMapping("/gewapay/ccbGZ.xhtml")
	public String ccbGZ(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/ccbpayGZ.vm";
	}
	
	@RequestMapping("/ajax/trade/ccbGZDiscount.xhtml")//建行
	public String ccbGZDiscount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, String creditCard, ModelMap model){
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(creditCard, CARD_PEX))){
			return showJsonError(model, "信用卡号格式错误！");
		}
		if(!isValidCardPrefixAndSuffix(creditCard)) return showJsonError(model, "对不起，亲爱的用户，您输入的卡号不在本活动的范围内，具体详情请查看活动规则。");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(isParticipateDiscountTicketOrder(creditCard)) return showJsonError(model, "对不起，亲爱的用户，您已经参加过此活动了。");
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, new JsonKeyOrderCallback("ccbCardno", creditCard), ip);
		if(discount.isSuccess()) {
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	
	@RequestMapping("/ajax/trade/ccbDiscount.xhtml")//建行
	public String ccbDiscount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long orderId, Long spid, String creditCard, ModelMap model){
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(creditCard, CARD_PEX))){
			return showJsonError(model, "信用卡号格式错误！");
		}
		//卡bin校验[前6位校验]
		if(!isValidCardbin(creditCard)) return showJsonError(model, "非有效的建行信用卡卡号");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(isParticipateDiscountSport(creditCard)) return showJsonError(model, "系统忙，请稍后再试！");
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_SPORT, orderId, sd, new JsonKeyOrderCallback("ccbCardno", creditCard), ip);
		if(discount.isSuccess()) {
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	/**
	 * 建行卡后期验证
	 * @param orderid
	 * @param request
	 * @return
	 */
	@RequestMapping("/trade/ccbpos/validateDiscount.xhtml")
	@ResponseBody
	public String chinapayDiscount(Long orderid,HttpServletRequest request){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		String ccbCardno = VmUtils.getJsonValueByKey(order.getOtherinfo(), "ccbCardno");
		if(isParticipateDiscount(ccbCardno)){
			return "repeat:"+ccbCardno;
		}
		addParticipate(ccbCardno, orderid);
		return "success";
	}
	/**
	 * 广州建行卡后期验证链接
	 * @param cardNo
	 * @param request
	 * @return
	 */
	@RequestMapping("/trade/ccbpos/validateGZDiscount.xhtml")
	@ResponseBody
	public String ccbposPayDiscount(Long orderid,HttpServletRequest request){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		String ccbCardno = VmUtils.getJsonValueByKey(order.getOtherinfo(), "ccbCardno");
		if(isParticipateDiscountTicketOrder(ccbCardno)){
			return "repeat:"+ccbCardno;
		}
		Map<String, Object> toSave = new HashMap<String, Object>();
		toSave.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		toSave.put("cardNo", ccbCardno);
		toSave.put("orderid", orderid);
		toSave.put("addtime", DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		mongoService.saveOrUpdateMap(toSave, MongoData.SYSTEM_ID, MongoData.NS_CCBPOS_GZ_ACTIVITY, false, true);
		return "success";
	}
	
	private boolean isParticipateDiscountSport(String cardNo){
		List<Date> weekdateList = DateUtil.getCurWeekDateList(new Date());
		Date weekbegin =  weekdateList.get(0);
		Date weekend = weekdateList.get(6);
		Timestamp weekbegneintime = DateUtil.getBeginTimestamp(weekbegin);
		Timestamp weekendtime = DateUtil.getLastTimeOfDay(new Timestamp(weekend.getTime()));
		
		DBObject query = mongoService.queryAdvancedDBObject("addtime", new String[]{">=", "<="}, 
				new Object[]{DateUtil.formatTimestamp(weekbegneintime), DateUtil.formatTimestamp(weekendtime)});
		query.put("cardNo", cardNo);
		List<Map> qryMapList = mongoService.find(MongoData.NS_CCBPOS_ACTIVITY, query);
		return VmUtils.size(qryMapList)>=1;
	}
	private boolean isParticipateDiscount(String cardNo){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp begneintime = DateUtil.getBeginTimestamp(curtime);
		Timestamp endtime = DateUtil.getLastTimeOfDay(curtime);
		
		DBObject query = mongoService.queryAdvancedDBObject("addtime", new String[]{">=", "<="}, 
				new Object[]{DateUtil.formatTimestamp(begneintime), DateUtil.formatTimestamp(endtime)});
		query.put("cardNo", cardNo);
		List<Map> qryMapList = mongoService.find(MongoData.NS_CCBPOS_ACTIVITY, query);
		return VmUtils.size(qryMapList)>=2;
	}
	
	private boolean isParticipateDiscountTicketOrder(String cardNo){
		DBObject query = new BasicDBObject();
		query.put("cardNo", cardNo);
		List<Map> qryMapList = mongoService.find(MongoData.NS_CCBPOS_GZ_ACTIVITY, query);
		return VmUtils.size(qryMapList)>=1;
	}
	
	private void addParticipate(String cardNo, Long orderid){
		Map<String, Object> toSave = new HashMap<String, Object>();
		toSave.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		toSave.put("cardNo", cardNo);
		toSave.put("orderid", orderid);
		toSave.put("addtime", DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		mongoService.saveOrUpdateMap(toSave, MongoData.SYSTEM_ID, MongoData.NS_CCBPOS_ACTIVITY, false, true);
	}
	private boolean isValidCardbin(String cardno){
		String startbin = StringUtils.substring(cardno, 0, 6);
		List<String> cardbinList = new ArrayList<String>();
		List<Map> qryMapList = mongoService.find(MongoData.NS_CCBPOS_CARDBIN, new HashMap<String, Object>());
		for(Map map : qryMapList){
			cardbinList.add(map.get("cardbin")+"");
		}
		boolean isres = cardbinList.contains(startbin);
		if(!isres){
			dbLogger.warn("建行验证卡bin错误：" + cardno);
		}
		return isres;
	}
	
	private boolean isValidCardPrefixAndSuffix(String cardno){
		String prefix = StringUtils.substring(cardno, 0, 8);
		int cardLength = cardno.length();
		String suffix = StringUtils.substring(cardno, cardLength - 4, cardLength);
		Map tmpMap = mongoService.findOne(MongoData.NS_CCBPOS_CARDBIN_2013, "_id",prefix + suffix);
		if(tmpMap == null || tmpMap.isEmpty() || 
				!StringUtils.equals(prefix,(String)tmpMap.get("prefixbin")) || !StringUtils.equals(suffix,(String)tmpMap.get("suffixbin"))){
			dbLogger.warn("建行验证卡bin错误：" + cardno);
			return false;
		}
		return true;
	}
	
	
	
	
	
	//电影建行信用卡支付优惠填写卡号---- 每天两次
	@RequestMapping("/gewapay/ccbEveryDayTwice.xhtml")
	public String ccbMovieDiscount(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/ccbpayEveryDayTwice.vm";
	}
	//电影建行信用卡支付优惠 ---每天两次
	@RequestMapping("/ajax/trade/ccbEveryDayTwice.xhtml")//建行
	public String ccbMovieDiscount(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
				HttpServletRequest request, Long orderId, Long spid, String creditCard, ModelMap model){
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(creditCard, CARD_PEX))){
			return showJsonError(model, "信用卡号格式错误！");
		}
		if(!isValidCardbin(creditCard)) return showJsonError(model, "非有效的建行信用卡卡号");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		
		if(isEveryDayTwice(creditCard)) return showJsonError(model, "对不起，该优惠活动每天最多参加两次。");
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, ip);
		if(discount.isSuccess()) {
			GewaOrder order = discount.getRetval().getOrder();
			order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), "ccbCardno", creditCard));
			daoService.saveObject(order);
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	/**
	 * 每天两次后期验证链接
	 * @param cardNo
	 * @param request
	 * @return
	 */
	@RequestMapping("/trade/ccbpos/ccbEveryDayTwice.xhtml")
	@ResponseBody
	public String ccbEveryDayTwice(Long orderid){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		String ccbCardno = VmUtils.getJsonValueByKey(order.getOtherinfo(), "ccbCardno");
		if(isEveryDayTwice(ccbCardno)){
			return "repeat:"+ccbCardno;
		}
		Map<String, Object> toSave = new HashMap<String, Object>();
		toSave.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		toSave.put("cardNo", ccbCardno);
		toSave.put("orderid", orderid);
		toSave.put("addtime", DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		mongoService.saveOrUpdateMap(toSave, MongoData.SYSTEM_ID, MongoData.NS_CCBPOS_EVERYDAYTWICE, false, true);
		return "success";
	}
	private boolean isEveryDayTwice(String cardNo){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp begneintime = DateUtil.getBeginTimestamp(curtime);
		Timestamp endtime = DateUtil.getLastTimeOfDay(curtime);
		
		DBObject query = mongoService.queryAdvancedDBObject("addtime", new String[]{">=", "<="}, 
				new Object[]{DateUtil.formatTimestamp(begneintime), DateUtil.formatTimestamp(endtime)});
		query.put("cardNo", cardNo);
		List<Map> qryMapList = mongoService.find(MongoData.NS_CCBPOS_EVERYDAYTWICE, query);
		return VmUtils.size(qryMapList)>=2;
	}
	
	//据数据库记录显示，这个验证连接是为支付方式“平安/深发展银行支付”使用
	@RequestMapping("/trade/unionpayfast/addoperation.xhtml")
	@ResponseBody
	public String addoperation(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = spid + ":" + cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 5, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
		
	
	/**
	 * 江苏银联活动-2.0认证支付，验证连接
	 * 一个卡号，在一个活动内，一个月内只能用一次
	 * 
	 * @param tradeNo      订单号
	 * @param spid         优惠活动ID
	 * @param otherinfo    订单的otherinfo
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 13, 2013 4:37:46 PM
	 */
	@RequestMapping("/trade/unionPayFastAJS/addoperations.xhtml")
	@ResponseBody
	public String addoperationForUnionpayfastAJS(String tradeno,String otherinfo, Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = spid + ":" + cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 30, 1, tradeno);
			if(allow) {
				return "success";
			}else{
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "tradeno " + tradeno + ", only one time in one month！");
			} 
		}
		return "false";
	}
	/**
	 * 银联认证2.0-北京 活动 同一卡号一周使用一次
	 * @param tradeno
	 * @param otherinfo
	 * @param spid
	 * @return
	 */
	@RequestMapping("/trade/unionPayFastBJ/addoperations.xhtml")
	@ResponseBody
	public String addoperationForUnionpayfastBJ(String tradeno,String otherinfo, Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = spid + ":" + cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) {
				return "success";
			}else{
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "tradeno " + tradeno + ", only one time in one week！");
			} 
		}
		return "false";
	}
	
	/**
	 * 江苏银联活动-2.0认证支付（中国银行），验证连接
	 * 一个卡号，在一个活动内，一个月内最多只能用两次，且一个星期内只能用一次
	 * 
	 * @param orderid
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 26, 2013 2:04:50 PM
	 */
	@RequestMapping("/trade/unionpayfast/boc/addoperation.xhtml")
	@ResponseBody
	public String addoperationForUnionpayfastBOC(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = spid + ":"+ cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 7, OperationService.ONE_DAY * 30, 2, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
		
	
	/**
	 * 江苏银联活动-2.0认证支付（中国银行（苏州）），验证连接
	 * 一个卡号，在一个活动内，一个星期内只能用一次
	 * 
	 * @param orderid
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 26, 2013 2:04:50 PM
	 */
	@RequestMapping("/trade/unionpayfast/boc/sz/addoperation.xhtml")
	@ResponseBody
	public String addoperationForUnionpayfastSZBOC(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = spid + ":"+ cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/nyyh/addoperation.xhtml")
	@ResponseBody
	public String addoperationForNyyh(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/cqnsyh/addoperation.xhtml")
	@ResponseBody
	public String addoperationForCqnsyh(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = spid + ":" + cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/youjie/addoperation.xhtml")
	@ResponseBody
	public String addoperationForYoujie(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/paymethod/common/addoperation.xhtml")
	@ResponseBody
	public String addOperationForCommon(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			SpecialDiscount specialDiscount = daoService.getObject(SpecialDiscount.class, spid);
			boolean allow = false;
			if(specialDiscount.getCardNumPeriodSpan() != null){
				allow = operationService.updateOperation(opkey, OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodIntvel(), 
						OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodSpan(), specialDiscount.getCardNumLimitnum(), tradeno);
			}else{
				allow = operationService.updateOperation(opkey, OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodIntvel(), 
						specialDiscount.getCardNumLimitnum(), tradeno);
			}
			if(allow){
				return "success"; 
			}
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/wzyh/addoperation.xhtml")
	@ResponseBody
	public String addoperationForWzcb(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/zdyh/addoperation.xhtml")
	@ResponseBody
	public String addoperationForUnionpayfastZdyh(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, OperationService.ONE_DAY * 30, 2, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	private String getOpkey(Long spid,String cardNumber){
		SpecialDiscount specialDiscount = daoService.getObject(SpecialDiscount.class, spid);
		String opkey = spid + ":" + cardNumber;
		if(specialDiscount != null && StringUtils.isNotBlank(specialDiscount.getCardUkey())){
			opkey = "spd" + specialDiscount.getCardUkey() + ":" + cardNumber;
		}else if(specialDiscount != null && specialDiscount.getSpcounterid() != null){
			opkey = "spd" + specialDiscount.getSpcounterid() + ":" + cardNumber;
		}
		return opkey;
	}
	
	//建行信用卡支付优惠填写卡号---- 每周四次
	@RequestMapping("/gewapay/jump/ccb/130528.xhtml")
	public String ccbActivity130528(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/jumpCCBActivity130528.vm";
	}
	
	//建行信用卡支付优惠 ---每周四次
	@RequestMapping("/ajax/trade/ccb/discount/130528.xhtml")//建行
	public String ccbasDiscount130528(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
				HttpServletRequest request, Long orderId, Long spid, String creditCard, ModelMap model){
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(creditCard, CARD_PEX))){
			return showJsonError(model, "信用卡号格式错误！");
		}
		if(!isValidCardbin(creditCard)) return showJsonError(model, "非有效的建行信用卡卡号");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		
		String opkey = spid + ":"+ creditCard;
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY,4);
		if(!allow){
			return showJsonError(model, "很抱歉！此优惠活动每张银行卡一周内只能使用四次！");
		}
		
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, ip);
		if(discount.isSuccess()) {
			GewaOrder order = discount.getRetval().getOrder();
			order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), "ccbCardno", creditCard));
			daoService.saveObject(order);
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	
	@RequestMapping("/trade/ccb/addoperation/130528.xhtml")
	@ResponseBody
	public String ccbAddoperation130528(String tradeno,String otherinfo,Long spid){		
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("ccbCardno")){
			String cardNumber = map.get("ccbCardno");
			String opkey = spid + ":" + cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY, 4, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/shenzhenPingAn/addoperation.xhtml")
	@ResponseBody
	public String shenzhenPingAn(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/guangzhouBocWeekOne/addoperation.xhtml")
	@ResponseBody
	public String guangzhouBocWeekOne(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow) return "success"; 
		}
		return "false";
	}
	
	@RequestMapping("/trade/unionpayfast/guangzhouBocMonthTwo/addoperation.xhtml")
	@ResponseBody
	public String guangzhouBocMonthTwo(String tradeno,String otherinfo,Long spid){
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			boolean allow = operationService.updateOperation(opkey+"_1", OperationService.ONE_DAY * 6, 1, tradeno);
			if(allow){
				allow = operationService.updateOperation(opkey+"_2", OperationService.ONE_DAY * 30, 2, tradeno);
				if(allow) return "success"; 
			}
		}
		return "false";
	}
	
	
	
	@RequestMapping("/gewapay/ccbWeekone.xhtml")
	public String ccbWeekone(Long orderId, ModelMap model) {
		Member member = getLogonMember();
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		if (order.isAllPaid() || order.isCancel()) return show404(model, "不能保存已支付或已（过时）取消的订单！");
		model.put("order", order);
		return "gewapay/ccbpayWeekOne.vm";
	}
	@RequestMapping("/ajax/trade/ccbWeekOne.xhtml")//建行
	public String ccbWeekOne(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
				HttpServletRequest request, Long orderId, Long spid, String creditCard, ModelMap model){
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(creditCard, CARD_PEX))){
			return showJsonError(model, "信用卡号格式错误！");
		}
		if(!isValidCardbin(creditCard)) return showJsonError(model, "非有效的建行信用卡卡号");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		String opkey = spid + ":"+ creditCard;
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_WEEK, 1);
		if(!allow){
			return showJsonError(model, "对不起，该优惠活动每周最多参加一次。");
		}
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		ErrorCode<OrderContainer> discount = specialDiscountService.useSpecialDiscount(OrderConstant.ORDER_TYPE_TICKET, orderId, sd, ip);
		if(discount.isSuccess()) {
			GewaOrder order = discount.getRetval().getOrder();
			order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), "ccbCardno", creditCard));
			daoService.saveObject(order);
			return showJsonSuccess(model, ""+discount.getRetval().getCurUsedDiscount().getAmount());
		}
		return showJsonError(model, discount.getMsg());
	}
	@RequestMapping("/trade/ccbpos/ccbWeekOne.xhtml")
	@ResponseBody
	public String ccbWeekOne(String tradeno,String otherinfo,Long spid){		
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("ccbCardno")){
			String cardNumber = map.get("ccbCardno");
			String opkey = spid + ":" + cardNumber;
			boolean allow = operationService.updateOperation(opkey, OperationService.ONE_WEEK, 1, tradeno);
			if(allow) return "success"; 
		}
		return "repea card：："+tradeno;
	}
	
}
