package com.gewara.web.action.partner;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.support.ErrorCode;
import com.gewara.util.WebUtils;
/**
 * 上海热线
 * @author acerge(acerge@163.com)
 * @since 6:41:43 PM Apr 20, 2010
 */
@Controller
public class PartnerOnlineController extends BasePartnerController{
	public ApiUser getOnline(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_ONLINE);
	}
	//第1步：选择场次
	@RequestMapping("/partner/online/opiList.xhtml")
	public String onlineOpiList(Date fyrq, Long movieid, ModelMap model) {
		ApiUser apiUser = getOnline();
		return opiList(apiUser, movieid, fyrq, "partner/online/step1.vm", model);
	}
	//第二步：选择座位
	@RequestMapping("/partner/online/chooseSeat.xhtml")
	public String onlineChooseSeat(HttpServletResponse response, 
			@CookieValue(value="ukey", required=false)String ukey, String mpid, ModelMap model){
		if(StringUtils.isBlank(mpid) || "null".equals(mpid)){
			return showError(model, "缺少参数！");
		}
		if(StringUtils.isBlank(ukey)){
			ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/online/");
			return showRedirect("partner/online/chooseSeat.shtml?mpid=" + mpid, model);
		}else{
			return onlineChooseSeat(ukey, mpid, model);
		}
	}
	@RequestMapping("/partner/online/chooseSeat.shtml")
	public String onlineChooseSeat(@CookieValue(value="ukey", required=false) String ukey, String mpid, ModelMap model){
		if(StringUtils.isBlank(mpid) || "null".equals(mpid)) return showError(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) return showError(model, "链接来源有错误！");
		ApiUser apiUser = getOnline();
		return chooseSeat(apiUser, new Long(mpid), ukey, "partner/online/step2.vm", model);
	}
	@RequestMapping("/partner/online/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  @CookieValue(value="ukey", required=false) String ukey, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		if(StringUtils.isBlank(ukey)){
			return showJsonError(model, "缺少参数，请刷新重试！");
		}
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/online/addOrder.xhtml")
	public String onlineAddOrder(@CookieValue(value="ukey", required=false)String ukey,
			String captchaId, String captcha, Long mpid,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		String returnUrl = "partner/online/chooseSeat.shtml?mpid=" + mpid + "&mobile=" + mobile + "&r="+ System.currentTimeMillis();
		if(!validCaptcha) return alertMessage(model, "验证码错误！", returnUrl);
		ApiUser partner = getOnline();
		return addOrder(mpid, mobile, seatid, ukey, returnUrl, partner, "", WebUtils.getRemoteIp(request), model);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/online/saveOrder.xhtml")
	public String onlineSaveOrder(ModelMap model, @RequestParam("orderId")long orderId, 
			String mobile, @CookieValue(value="ukey", required=false)String ukey){
		String paymethod = "partnerPay";
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/online/showOrder.xhtml")
	public String sdoShowOrder(Long orderId, ModelMap model, 
			@CookieValue(value="ukey", required=false)String ukey){
		ApiUser partner = getOnline();
		return showOrder(ukey, orderId, partner, "partner/online/step3.vm", model);
	}
}
