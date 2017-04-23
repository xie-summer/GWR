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
 *	百联E城网购
 * @author acerge(acerge@163.com)
 * @since 6:40:59 PM Apr 20, 2010
 */
@Controller
public class PartnerShokwController extends BasePartnerController{
	public ApiUser getShokw(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_SHOKW);
	}
	//第1步：选择场次
	@RequestMapping("/partner/shokw/opiList.xhtml")
	public String shokwOpiList(Date fyrq, Long movieid, ModelMap model) {
		ApiUser apiUser = getShokw();
		return opiList(apiUser, movieid, fyrq, "partner/shokw/step1.vm", model);
	}
	//第二步：选择座位
	@RequestMapping("/partner/shokw/chooseSeat.xhtml")
	public String shokwChooseSeat(HttpServletResponse response, 
			@CookieValue(value="ukey", required=false)String ukey, Long mpid, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)){
			ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/shokw/");
			return showRedirect("partner/shokw/chooseSeat.shtml?mpid=" + mpid, model);
		}else{
			return shokwChooseSeat(ukey, mpid, model);
		}
	}
	@RequestMapping("/partner/shokw/chooseSeat.shtml")
	public String shokwChooseSeat(@CookieValue(value="ukey", required=false) String ukey, Long mpid, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		if(StringUtils.isBlank(ukey)) return showError(model, "链接来源有错误！");
		ApiUser apiUser = getShokw();
		return chooseSeat(apiUser, mpid, ukey, "partner/shokw/step2.vm", model);
	}
	@RequestMapping("/partner/shokw/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,  
			@CookieValue(value="ukey", required=false) String ukey, HttpServletResponse response, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		if(StringUtils.isBlank(ukey)) ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/shokw/");
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/shokw/addOrder.xhtml")
	public String shokwAddOrder(@CookieValue(value="ukey", required=false)String ukey,
			String captchaId, String captcha, Long mpid,
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null) return showError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		String returnUrl = "partner/shokw/chooseSeat.shtml?mpid=" + mpid + "&mobile=" + mobile + "&r="+ System.currentTimeMillis();
		if(!validCaptcha) return alertMessage(model, "验证码错误！", returnUrl);
		ApiUser partner = getShokw();
		if(partner == null) return alertMessage(model, "非法订单，来源不正确！", returnUrl);
		return addOrder(mpid, mobile, seatid, ukey, returnUrl, partner, "", WebUtils.getRemoteIp(request), model);
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/shokw/saveOrder.xhtml")
	public String shokwSaveOrder(ModelMap model, String paymethod, String paybank, 
			@RequestParam("orderId")long orderId, String mobile, 
			@CookieValue(value="ukey", required=false)String ukey){
		return saveOrder(orderId, mobile, paymethod, paybank, ukey, model);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/shokw/showOrder.xhtml")
	public String sdoShowOrder(Long orderId, ModelMap model, 
			@CookieValue(value="ukey", required=false)String ukey){
		ApiUser partner = getShokw();
		return showOrder(ukey, orderId, partner, "partner/shokw/step3.vm", model);
	}
}
