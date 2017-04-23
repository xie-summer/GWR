package com.gewara.web.action.partner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.mongo.MongoService;
import com.gewara.pay.ChinapayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

/**
 * 银联
 * @author acerge(acerge@163.com)
 * @since 6:42:51 PM Apr 20, 2010
 */
@Controller
public class PartnerChinapayController extends BasePartnerController{

	@Autowired@Qualifier("mongoService")
	protected MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	private ApiUser getChinapay(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_UNION);
	}
	//第1步：选择场次
	@RequestMapping("/partner/chinapay/opiTest.xhtml")
	public String unionOpiList(ModelMap model){
		String userId = "gewaTest";
		String mac = ChinapayUtil.getEncryptStr(userId);
		model.put("userId", userId);
		model.put("mac", mac);
		return "redirect:/partner/chinapay/index.xhtml";
	}
	@RequestMapping("/partner/chinapay/index.xhtml")
	public String index(HttpServletRequest request, HttpServletResponse response,
			@CookieValue(required=false,value="ukey") String ukey,
			String userId, String mac, ModelMap model) {
		return unionOpiList(request, response, null, userId, mac, null, null, ukey, model);
	}
	
	@RequestMapping("/partner/chinapay/opiList.xhtml")
	public String unionOpiList(HttpServletRequest request, HttpServletResponse response, 
			Date fyrq, String userId, String mac, Long movieid, String encQryStr,  
			@CookieValue(required=false,value="ukey") String ukey, ModelMap model) {
		if(StringUtils.isNotBlank(userId)){
			String mac2 = ChinapayUtil.getEncryptStr(""+userId);
			if(!StringUtils.equals(mac2,mac)) return showError(model, "用户校验错误！");
			encQryStr = PartnerUtil.getParamStr(request, "userId");
		}else if(StringUtils.isBlank(encQryStr)){
			Map<String, String> reqParamMap = PartnerUtil.getParamMap(encQryStr);
			if(StringUtils.isBlank(reqParamMap.get("userId"))) return showError(model, "用户校验错误！");
		}
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/chinapay/");
		
		ApiUser apiUser = getChinapay();
		model.put("encQryStr", encQryStr);
		model.put("iframeUrl", VmUtils.getJsonValueByKey(apiUser.getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		return opiList(apiUser, movieid, fyrq, "partner/chinapay/step1.vm", model);
	}
	//第二步：选择座位
	@RequestMapping("/partner/chinapay/chooseSeat.xhtml")
	public String chinapayChooseSeat(
			Long mpid, String event, String evmac/*只因event而存在*/, 
			String encQryStr, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		Map<String, String> reqParamMap = PartnerUtil.getParamMap(encQryStr);
		if(StringUtils.isBlank(reqParamMap.get("userId"))) return showError(model, "用户校验错误！");
		if(StringUtils.isBlank(event)) event = reqParamMap.get("event");
		if(StringUtils.isBlank(evmac)) evmac = reqParamMap.get("evmac");
		if(StringUtils.isNotBlank(event)){
			String mymac = ChinapayUtil.getEncryptStr(mpid + event);
			if(!StringUtils.equalsIgnoreCase(mymac, evmac)) return showError(model, "数据校验错误！");
			encQryStr = PartnerUtil.addParamStr(encQryStr, new String[]{"event", "evmac"}, new String[]{event, evmac});

			SpecialDiscount spdiscount = daoService.getObjectByUkey(SpecialDiscount.class, "flag", event, true);
			model.put("spdiscount", spdiscount);
		}
		ApiUser apiUser = getChinapay();
		model.put("encQryStr", encQryStr);
		model.put("iframeUrl", VmUtils.getJsonValueByKey(apiUser.getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		return chooseSeat(apiUser, mpid, reqParamMap.get("userId"), "partner/chinapay/step2.vm", model);
	}
	@RequestMapping("/partner/chinapay/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/chinapay/addOrder.xhtml")
	public String chinapayAddOrder(@CookieValue(required=false,value="ukey") String ukey, 
			String captchaId, String captcha, Long mpid, String encQryStr,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null || StringUtils.isBlank(ukey)) return showError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		String qryStr = StringUtils.isBlank(encQryStr)?"":"&encQryStr=" + encQryStr;
		String returnUrl = "partner/chinapay/chooseSeat.xhtml?mpid=" + mpid + qryStr + "&mobile=" + mobile + "&r="+ System.currentTimeMillis();
		if(!validCaptcha) return alertMessage(model, "验证码错误！", returnUrl);
		Map<String, String> reqParamMap = PartnerUtil.getParamMap(encQryStr);
		String userId = reqParamMap.get("userId");
		if(StringUtils.isBlank(userId)) return showError(model, "用户校验错误！");

		String event = reqParamMap.get("event");
		if(StringUtils.isNotBlank(event)){
			String mymac = ChinapayUtil.getEncryptStr(mpid + event);
			String evmac = reqParamMap.get("evmac");
			if(!StringUtils.equalsIgnoreCase(mymac, evmac)) return showError(model, "数据校验错误！");
		}
		ApiUser partner = getChinapay();
		model.put("encQryStr", encQryStr);
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, userId, null, event, PaymethodConstant.PAYMETHOD_CHINAPAY1, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) return showRedirect("/partner/" + partner.getPartnerpath() + "/showOrder.xhtml", model);
		return alertMessage(model, code.getMsg(), returnUrl);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/chinapay/saveOrder.xhtml")
	public String chinapaySaveOrder(ModelMap model, String mobile,
			@CookieValue(required=false,value="ukey") String ukey, String paybank,
			@RequestParam("orderId")long orderId, String encQryStr){
		if(StringUtils.isBlank(ukey)) return showError(model, "缺少参数！");
		model.put("encQryStr", encQryStr);
		return saveOrder(orderId, mobile, PaymethodConstant.PAYMETHOD_CHINAPAY1, paybank, ukey, model);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/chinapay/showOrder.xhtml")
	public String chinapayShowOrder(Long orderId, String encQryStr, ModelMap model, 
			@CookieValue(value="ukey", required=false)String ukey){
		ApiUser partner = getChinapay();
		model.put("encQryStr", encQryStr);
		model.put("iframeUrl", VmUtils.getJsonValueByKey(partner.getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		/******************/
		Map<String, String> reqParamMap = PartnerUtil.getParamMap(encQryStr);
		String userId = reqParamMap.get("userId");
		if(!isParticipateDiscount(userId)){
			String result = ChinapayUtil.isAllowDiscount(partner, userId);
			model.put("result", result);
			dbLogger.warn("userId:"+userId+",orderid:"+orderId);
		}else{
			model.put("msg", "此活动每个银联账户每周只能优惠一次！");
		}
		/*******************/
		return showOrder(ukey, orderId, partner, "partner/chinapay/step3.vm", model);
	}
	//第五步：显示订单成功
	@RequestMapping("/partner/chinapay/orderResult.xhtml")
	public String orderResult(@RequestParam(required=true) String tradeNo, 
			String fee, String check, ModelMap model){
		model.put("fee", fee);
		model.put("check", check);
		model.put("tradeNo", tradeNo);
		model.put("successUrl", ChinapayUtil.getSuccessUrl());
		//TicketOrder ticketOrder = this.daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		//addParticipate(ticketOrder);
		return "partner/chinapay/redirectToChinapay.vm";
	}
	
	private boolean isParticipateDiscount(String userId){
		Map<String, Object> qparam = new HashMap<String, Object>();
		qparam.put("userId", userId);	
		qparam.put("status", "paid");
		qparam.put("addDate", DateUtil.formatDate(new Date()));
		List<Map> map = mongoService.find(MongoData.NS_CHINAPAY_ACTIVITY, qparam);
		return !map.isEmpty();
	}
	
	/**
	 * 下载订单数据
	 * @param orderdate
	 * @param model
	 * @return
	 */
	@RequestMapping("/partner/chinapay/orderList.xhtml")
	public String orderList(Date orderdate, ModelMap model){
		String hql = "from TicketOrder t where t.partnerid=? and status != ? and t.addtime>=? and t.addtime<=?";
		List<TicketOrder> orderList = daoService.queryByRowsRange(hql,	0, 5000/*已经足够大了！*/, PartnerConstant.PARTNER_UNION, OrderConstant.STATUS_SYS_CANCEL, 
				DateUtil.getBeginningTimeOfDay(orderdate), DateUtil.getLastTimeOfDay(orderdate));
		int amount = 0;
		for(TicketOrder order:orderList) amount += order.getDue();
		String amountStr = StringUtils.leftPad(amount+"00", 12, "0");
		model.put("amount", amountStr);
		model.put("ChinapayUtil", new ChinapayUtil());
		model.put("orderList", orderList);
		return "partner/chinapay/chinapayOrderList.vm";
	}
	
	@RequestMapping("/partner/chinapay/checkUser.xhtml")
	public String checkUser(String userId,ModelMap model, HttpServletRequest request, HttpServletResponse response){
		dbLogger.warn(WebUtils.getParamStr(request, true));
		String result = ChinapayUtil.isAllowDiscount( this.getChinapay(), userId);
		if(StringUtils.equals(result, "success")){
			this.setUkCookie(response, "chinaPayUkey", userId, "/trade/chinapay/");
		}else{
			model.put("sid", "61277562");
			model.put("msg", "此活动需银联账号绑定银行卡，请先绑定！");
			return "redirect:/subject/qiangpiao.xhtml";
		}
		String spkey = VmUtils.getJsonValueByKey(this.getChinapay().getOtherinfo(), "spkey");
		model.put("spkey", spkey);
		return "redirect:/cinema/searchOpi.xhtml";
	}
	
	public String setUkCookie(HttpServletResponse response,String cookieName,String cookieValue, String path){
		Map<String, String> params = new HashMap<String, String>();
		params.put("chinaPay_userId", cookieValue);
		String ukey = PartnerUtil.getParamStr(params);
		Cookie cookie = new Cookie(cookieName, ukey);
		cookie.setPath(path);
		cookie.setMaxAge(60 * 60 * 12);//12 hour
		response.addCookie(cookie);
		return ukey;
	}
	
	@RequestMapping("/partner/chinapay/validateDiscount.xhtml")
	@ResponseBody
	public String chinapayDiscount(Long orderid,HttpServletRequest request){
		String result = this.validateDiscount(orderid, request);
		dbLogger.warn("result:"+result);
		return result;
	}
	
	private String validateDiscount(Long orderid,HttpServletRequest request){
		dbLogger.warn("validateDiscount:"+WebUtils.getRequestMap(request).toString());
		TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
		String userId = order.getMembername().split("@")[0];
		if(isParticipateDiscount(userId)){return "repeat:"+userId;}
		addParticipate(userId, orderid);
		return ChinapayUtil.isAllowDiscount(this.getChinapay(), userId);
	}
	
	private void addParticipate(String userId,Long orderid){
		Map<String, Object> toSave = new HashMap<String, Object>();
		toSave.put(MongoData.SYSTEM_ID, MongoData.buildId());
		toSave.put("userId", userId);
		toSave.put("orderid", orderid);
		toSave.put("status", "paid");
		toSave.put("type", "partner"+DateUtil.formatTimestamp(DateUtil.getMillTimestamp()));
		toSave.put("addDate", DateUtil.formatDate(new Date()));
		mongoService.saveOrUpdateMap(toSave, MongoData.SYSTEM_ID, MongoData.NS_CHINAPAY_ACTIVITY, false, true);
	}

}
