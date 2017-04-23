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
 *	安欣生活
 * @author acerge(acerge@163.com)
 * @since 6:40:59 PM Apr 20, 2010
 */
@Controller
public class PartnerAnxinController extends BasePartnerController{
	public ApiUser getAnxin(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_ANXIN_WEB);
	}
	//第1步：选择场次
	@RequestMapping("/partner/anxin/opiList.xhtml")
	public String anxinOpiList(Date fyrq, Long movieid, ModelMap model) {
		ApiUser apiUser = getAnxin();
		return opiList(apiUser, movieid, fyrq, "partner/anxin/step1.vm", model);
	}
	//第二步：选择座位
	@RequestMapping("/partner/anxin/chooseSeat.xhtml")
	public String anxinChooseSeat(HttpServletResponse response, 
			@CookieValue(value="ukey", required=false)String ukey, Long mpid, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)){
			ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/anxin/");
			return showRedirect("/partner/anxin/chooseSeat.shtml?mpid=" + mpid, model);
		}else{
			return anxinChooseSeat(ukey, mpid, model);
		}
	}
	@RequestMapping("/partner/anxin/chooseSeat.shtml")
	public String anxinChooseSeat(@CookieValue(value="ukey", required=false) String ukey, Long mpid, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) return showError(model, "链接来源有错误！");
		ApiUser apiUser = getAnxin();
		return chooseSeat(apiUser, mpid, ukey, "partner/anxin/step2.vm", model);
	}
	@RequestMapping("/partner/anxin/seatPage.xhtml")
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
	@RequestMapping("/partner/anxin/addOrder.xhtml")
	public String anxinAddOrder(@CookieValue(value="ukey", required=false)String ukey,
			String captchaId, String captcha, Long mpid,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		String returnUrl = "partner/anxin/chooseSeat.shtml?mpid=" + mpid + "&mobile=" + mobile + "&r="+ System.currentTimeMillis();
		if(!validCaptcha) return alertMessage(model, "验证码错误！", returnUrl);
		ApiUser partner = getAnxin();
		return addOrder(mpid, mobile, seatid, ukey, returnUrl, partner, "", WebUtils.getRemoteIp(request), model);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/anxin/saveOrder.xhtml")
	public String anxinSaveOrder(ModelMap model, @RequestParam("orderId")long orderId, 
			String mobile, @CookieValue(value="ukey", required=false)String ukey){
		String paymethod = "partnerPay";
		return saveOrder(orderId, mobile, paymethod, "", ukey, model);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/anxin/showOrder.xhtml")
	public String sdoShowOrder(Long orderId, ModelMap model, 
			@CookieValue(value="ukey", required=false)String ukey){
		ApiUser partner = getAnxin();
		return showOrder(ukey, orderId, partner, "partner/anxin/step3.vm", model);
	}
}
