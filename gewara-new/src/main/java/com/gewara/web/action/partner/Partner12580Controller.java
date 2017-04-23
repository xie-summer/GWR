package com.gewara.web.action.partner;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import com.gewara.constant.CityData;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.pay.MobileTicketUtil;
import com.gewara.pay.Pay12580Util;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

/**
 * 12580移动的电话增值服务中心
 * @author user
 *
 */
@Controller
public class Partner12580Controller extends BasePartnerController {
	private final String PAID_SUCCESS = "0000";//成功

	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	
	private ApiUser get12580(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_12580);
	}
	
	@RequestMapping("/partner/12580/opiList.xhtml")
	public String opiList(HttpServletResponse response, Date fyrq, Long movieid,String usersign,String checkValue,
			@CookieValue(required=false,value="ukey") String ukey,String target, ModelMap model) {
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/12580/");
		ApiUser partner = get12580();
		String check = StringUtil.md5(partner.getPartnerkey() + partner.getPrivatekey() + usersign);
		if(StringUtils.isBlank(usersign) || !check.equals(checkValue)){
			model.put("message", "请提供正确的坐席工号和checkValue(接入加密校验值)");
			return "partner/12580/error.vm";
		}
		if(fyrq == null){
			List<Date> dateList = partnerService.getPlaydateList(partner,  partner.getDefaultCity(), movieid);
			if(dateList != null && dateList.size() > 0){
				fyrq = dateList.get(0);
			}
		}
		if(StringUtils.equals(target, "_blank"))model.put("target", "_blank");
		model.put("usersign", usersign);
		model.put("checkValue", checkValue);
		addOpiListData(partner, movieid, fyrq, null, null, model);
		return "partner/12580/step1.vm";
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/12580/chooseSeat.xhtml")
	public String chooseSeat(Long mpid, @CookieValue(required=false,value="ukey") String ukey, ModelMap model,
			 HttpServletRequest request, HttpServletResponse response, String usersign, String target){
		if(mpid==null) return forwardMessage(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/12580/");
		ApiUser partner = get12580();
		this.getCitycodeByPartner(partner, request, response);
		model.put("usersign", usersign);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		if(StringUtils.equals(target, "_blank"))model.put("target", "_blank");
		return chooseSeat(partner, mpid, ukey, "partner/12580/step2.vm", model);
	}
	
	@RequestMapping("/partner/12580/seatPage.xhtml")
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
	@RequestMapping("/partner/12580/addOrder.xhtml")
	public String addOrder(@CookieValue(required=false,value="ukey") String ukey, String usersign, String target,
			String captchaId, String captcha, Long mpid, @RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null || StringUtils.isBlank(ukey)) return showJsonError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		ApiUser partner = get12580();
		String paymethod = "partnerPay";
		ErrorCode code = addOrder(mpid, mobile, seatid, mobile, partner, usersign, null, null, paymethod, WebUtils.getRemoteIp(request), model);
		if(!code.isSuccess())return showJsonError(model, code.getMsg());
		target = StringUtils.equals(target, "_blank")?",_blank":"";
		return showJsonSuccess(model, model.get("orderId")+","+mobile+target);
	}
	
	//第四步:确认订单
	@RequestMapping("/partner/12580/showOrder.xhtml")
	public String showOrder(String mobilekey, Long orderId, String target, ModelMap model){
		if(StringUtils.isBlank(mobilekey)) return showJsonError(model, "缺少参数！");
		ApiUser partner = get12580();
		model.put("mobilekey", mobilekey);
		if(StringUtils.equals(target, "_blank"))model.put("target", "_blank");
		return showOrder(mobilekey, orderId, partner, "partner/12580/step3.vm", model);
	}
	//第五步:跳转到12580支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/12580/saveOrder.xhtml")
	public String saveOrder(ModelMap model, @RequestParam("orderId")long orderId, 
			String mobile, String mobilekey){
		if(StringUtils.isBlank(mobilekey)) return showJsonError(model, "缺少参数！");
		String paymethod = "partnerPay";
		ErrorCode<TicketOrder> code = saveOrder(orderId, mobile, paymethod, "", mobilekey);
		if(!code.isSuccess()) {
			model.put("message", code.getMsg());
			return  "partner/12580/error.vm";
		}
		TicketOrder order = code.getRetval();
		ApiUser partner = get12580();
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("order", order);
		model.put("opi", opi);
		model.put("movie", daoService.getObject(Movie.class, opi.getMovieid()));
		model.put("cinema", daoService.getObject(Cinema.class, opi.getCinemaid()));
		model.put("partner", partner);
		model.put("profile", daoService.getObject(CinemaProfile.class, opi.getCinemaid()));
		model.put("sign", StringUtil.md5(order.getTradeNo() + order.getMobile() + DateUtil.format(order.getAddtime(),"yyyyMMddHHmmss")));
		return "partner/12580/paymethod.vm";
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/partner/12580/toPayOrder.xhtml")
	public String toPayOrder(ModelMap model,@RequestParam("orderId")long orderId,String sign,String username,String idno,String cardno,
			String bankprovcity,String idtype,String iaddr,String captchaId,String captcha, HttpServletRequest request){
		boolean validate = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validate){
			model.put("message", "验证码错误！");
			return "partner/12580/error.vm";
		}
		if(StringUtils.isBlank(cardno)){
			model.put("message", "银行卡号不能为空！");
			return "partner/12580/error.vm";
		}
		TicketOrder order = daoService.getObject(TicketOrder.class, orderId);
		if(order == null){
			return "partner/12580/error.vm";
		}
		if(order.getValidtime().getTime() < System.currentTimeMillis()){
			model.put("message", "您的订单已经超时，不能进行支付！");
			return "partner/12580/error.vm";
		}
		if(OrderConstant.STATUS_PAID_SUCCESS.equals(order.getStatus())){
			model.put("message", "您的订单已支付成功，不能进行支付！");
			return "partner/12580/error.vm";
		}
		model.put("order", order);
		if(!StringUtil.md5(order.getTradeNo() + order.getMobile() + DateUtil.format(order.getAddtime(),"yyyyMMddHHmmss")).equals(sign)){
			return "partner/12580/error.vm";
		}
		ApiUser partner = this.get12580();
		model.put("partner", partner);
		Map<String,String> paramsPost = Pay12580Util.getDNAPayParams(order, get12580(), username, idno, idtype, iaddr, cardno, bankprovcity);
		HttpResult code = HttpUtils.postUrlAsString(partner.getAddOrderUrl(),paramsPost);
		dbLogger.error("支付返回:" + code.isSuccess() + "---" + code.getResponse() + "---message:" + code.getMsg());
		if(code.isSuccess()){
			Map<String,String> map = VmUtils.readJsonToMap(code.getResponse());
			if(StringUtils.equals(map.get("code"), PAID_SUCCESS)){
				model.put("payResult", map);
			}else{
				model.put("message", "订单在支付过程中出错.[" + map.get("msg") + "]");
			}
		}else{
			model.put("message", "连接支付请求服务器出错");
		}
		return "partner/12580/error.vm";
	}
	
	@RequestMapping("/partner/12580/qryOrder.xhtml")
	public String qryOrder(String partnerOrderid,Long gewaOrderid,ModelMap model){
		TicketOrder order = daoService.getObject(TicketOrder.class, gewaOrderid);
		Map<String,String> paramsPost = Pay12580Util.getQueryParams(partnerOrderid,this.get12580());
		HttpResult code = HttpUtils.postUrlAsString(this.get12580().getQryurl(),paramsPost);
		dbLogger.error("支付返回:" + code.isSuccess() + ":" + code.getResponse() + "---message:" + code.getMsg());
		if(code.isSuccess()){
			Map<String,String> map = VmUtils.readJsonToMap(code.getResponse());
			if(!"00".equals(map.get("status"))){
				model.put("repay", true);
			}
			model.put("qryOrder", map);
		}
		model.put("order", order);
		model.put("partner",this.get12580());
		model.put("sign", StringUtil.md5(order.getTradeNo() + order.getMobile() + DateUtil.format(order.getAddtime(),"yyyyMMddHHmmss")));
		return "partner/12580/qryOrder.vm";
	}
	
	//12580api模式iframe嵌入页面选择座位
	@RequestMapping("/partner/12580/apichooseSeat.xhtml")
	public String apichooseSeat(Long mpid,String token,@CookieValue(required=false,value="ukey") String ukey,
			String basePath,ModelMap model, HttpServletRequest request, HttpServletResponse response){
		if(mpid==null) return forwardMessage(model, "缺少参数！");	
		if(StringUtils.isBlank(ukey))PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/12580/");
		ErrorCode code = addInitParams(token, null,basePath, model);
		if(!code.isSuccess()) {
			return forwardMessage(model, code.getMsg());
		}
		ApiUser partner = daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_MOBILETICKET);
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		getCitycodeByPartner(partner, request, response);
		if(StringUtils.isBlank(basePath))basePath = "http://www.sh.10086.cn/ticket/";
		if(StringUtils.isBlank(ukey)) ukey = setUkCookie(response, "basePath", basePath, "/partner/12580");
		return chooseSeat(partner, mpid, ukey, "partner/12580/apistep2.vm", model);
	}
	private void addBasePath(ModelMap model,String basePath){
		Long orderId = Long.valueOf(model.get("orderId")+"");
		TicketOrder order = this.daoService.getObject(TicketOrder.class, orderId);
		Map<String, String> dataMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		dataMap.put("basePath", basePath);
		order.setOtherinfo(JsonUtils.writeMapToJson(dataMap));
		this.daoService.saveObject(order);
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/12580/apiSaveOrder.xhtml")
	public String cmwifiAddOrder(@CookieValue(required=false,value="ukey") String ukey,
			String captchaId, String captcha, Long mpid, String encQryStr, 
			@CookieValue(required=false,value="basePath") String basePath,
			@RequestParam("mobile") String mobile, @RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		Map<String, String> params = PartnerUtil.getParamMap(basePath);
		basePath = params.get("mobile_basePath");
		if(StringUtils.isBlank(basePath))basePath = "http://www.sh.10086.cn/ticket/";
		if(mpid==null) return showJsonError(model, "缺少参数！");
		if(StringUtils.isBlank(encQryStr)) {
			return forwardMessage(model, "请先登录！");
		}
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		Map<String, String> paramMap = PartnerUtil.getParamMap(encQryStr);
		String userid = paramMap.get("userid"); 
		if(StringUtils.isBlank(userid)) {
			return showJsonError(model, "获取用户标识错误,请重新登录！");
		}
		ApiUser partner = daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_MOBILETICKET);
		model.put("userid", userid);
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, userid, null, null, PaymethodConstant.PAYMETHOD_PARTNERPAY, WebUtils.getRemoteIp(request), model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		this.addBasePath(model, basePath);
		return showJsonSuccess(model, model.get("orderId")+","+mobile);
	}
	
	@RequestMapping(method=RequestMethod.POST,value="/partner/12580/apiSaveOrder.xhtml")
	public String cmwifiSaveOrder(ModelMap model, String mobile,
			@CookieValue(required=false,value="ukey") String ukey, String paybank,
			@RequestParam("orderId")long orderId){
		if(StringUtils.isBlank(ukey)) return forwardMessage(model, "缺少参数！");
		return saveOrder(orderId, mobile, PaymethodConstant.PAYMETHOD_PARTNERPAY, paybank, ukey, model);
	}
	
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/12580/apiShowOrder.xhtml")
	public String cmwifiShowOrder(Long orderId, ModelMap model,@CookieValue(value="ukey", required=false)String ukey, 
			HttpServletRequest request, HttpServletResponse response){
		ApiUser partner = daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_MOBILETICKET);
		List<GewaCommend> gcList = commonService.getGewaCommendListByRelatedid(null, SignName.PARTNER_AD, partner.getId(), null, true, 0, 1);
		if(gcList.size()>0){
			model.put("gewaCommend", gcList.get(0));
		}
		this.getCitycodeByPartner(partner, request, response);
		model.put("partnerCityMap", CityData.getCityNameMap(partner.getCitycode()));
		return showOrder(ukey, orderId, partner, "partner/12580/confirmOrder.vm", model);
	}
	
	//第三步:锁座位，加订单
	@RequestMapping("/partner/12580/apiAddOrder.xhtml")
	public String apiAddOrder(@CookieValue(required=false,value="ukey") String ukey,
			HttpServletRequest request, String captchaId, String captcha, Long mpid, String encQryStr, 
			@RequestParam("mobile") String mobile, @RequestParam("seatid")String seatid, 
			@CookieValue(required=false,value="basePath") String basePath,ModelMap model){
		Map<String, String> params = PartnerUtil.getParamMap(basePath);
		basePath = params.get("mobile_basePath");
		if(StringUtils.isBlank(basePath))basePath = "http://www.sh.10086.cn/ticket/";
		if(mpid==null) return showJsonError(model, "缺少参数！");
		if(StringUtils.isBlank(encQryStr)){
			return forwardMessage(model, "请先登录！");
		}
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		Map<String, String> paramMap = PartnerUtil.getParamMap(encQryStr);
		String userid = paramMap.get("userid"); 
		if(StringUtils.isBlank(userid)){
			return showJsonError(model, "获取用户标识错误,请重新登录！");
		}
		ApiUser partner = daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_MOBILETICKET);
		model.put("userid", userid);
		String paymethod = "partnerPay";
		String result = addOrderAndPay(mpid, mobile, seatid, paymethod, null, ukey, partner, userid, 0, model);
		this.addBasePath(model, basePath);
		return result;
	}
	
	private ErrorCode addInitParams(String token, String encQryStr,String basePath, ModelMap model){
		if(StringUtils.isNotBlank(token)){
			ErrorCode<String> user = getLoginUser(token,basePath);
			if(!user.isSuccess()) return user;
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("userid", user.getRetval());
			String ukey = DateUtil.format(new Date(), "MMss") + StringUtil.getRandomString(4);
			params.put("ukey", ukey);
			encQryStr = PartnerUtil.getParamStr(params);
		}else if(StringUtils.isBlank(encQryStr)){
			return ErrorCode.getFailure("请先登录！");
		}
		model.put("encQryStr", encQryStr);
		return ErrorCode.SUCCESS;
	}
	
	private ErrorCode getLoginUser(String token,String basePath){
		//if(true)return ErrorCode.getSuccessReturn(StringUtil.getRandomString(10));
		Map<String, String> userMap = MobileTicketUtil.getUserLoginStatus(daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_MOBILETICKET),token,basePath);
		if(userMap != null){
			String result = userMap.get("userId");
			return ErrorCode.getSuccessReturn(result);
		}
		return ErrorCode.getFailure("请先登录！");
	}
	
	public String setUkCookie(HttpServletResponse response,String cookieName,String cookieValue, String path){
		Map<String, String> params = new HashMap<String, String>();
		params.put("mobile_basePath", cookieValue);
		String ukey = PartnerUtil.getParamStr(params);
		Cookie cookie = new Cookie(cookieName, ukey);
		cookie.setPath(path);
		cookie.setMaxAge(60 * 60 * 12);//12 hour
		response.addCookie(cookie);
		return ukey;
	}
}
