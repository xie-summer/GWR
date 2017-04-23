/**
 * 
 */
package com.gewara.web.action.partner;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.support.ErrorCode;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class PartnerTaobaoController extends BasePartnerController {
	private ApiUser getTaobao(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_TAOBAO);
	}
	@RequestMapping("/partner/taobao/chooseSeat.xhtml")
	public String chooseSeat(String userId, String nickname, Long extScheduleId, ModelMap model){
		if(StringUtils.isBlank(userId)){
			return forwardMessage(model, "参数校验错误！");
		}
		String encQryStr = StringUtil.md5(userId, 16);
		ApiUser apiUser = getTaobao();
		model.put("scheduleId", extScheduleId);
		model.put("userId", userId);
		model.put("nickname", nickname);
		model.put("encQryStr", encQryStr);
		return chooseSeat(apiUser, extScheduleId, userId, "partner/taobao/step2V1.vm", model);
	}
	@RequestMapping("/partner/taobao/chooseSeatV2.xhtml")
	public String chooseSeatV2(String userId, String nickname, Long extScheduleId, ModelMap model){
		if(StringUtils.isBlank(userId)){
			return forwardMessage(model, "参数校验错误！");
		}
		String encQryStr = StringUtil.md5(userId, 16);
		ApiUser apiUser = getTaobao();
		model.put("scheduleId", extScheduleId);
		model.put("userId", userId);
		model.put("nickname", nickname);
		model.put("encQryStr", encQryStr);
		model.put("iframeUrl", VmUtils.getJsonValueByKey(getTaobao().getOtherinfo(), PayConstant.KEY_IFRAME_URL));
		return chooseSeat(apiUser, extScheduleId, userId, "partner/taobao/step2.vm", model);
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/taobao/addOrder.xhtml")
	public String addOrder(String captchaId, String captcha, Long scheduleId, String encQryStr,String userId,
			@RequestParam("mobile")String mobile, @RequestParam("seatid")String seatid,
			HttpServletRequest request, ModelMap model){
		if(scheduleId==null || StringUtils.isBlank(userId)) return showJsonError(model, "缺少参数！");
		String res = StringUtil.md5(userId, 16);
		if(!StringUtils.equals(encQryStr, res)){
			return showJsonError(model, "校验错误！");
		}
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		ApiUser partner = getTaobao();
		model.put("encQryStr", encQryStr);
		String paymethod = "partnerPay";
		String utbkey = userId+"";
		return addOrderAndPay(scheduleId, mobile, seatid, paymethod, null, userId, partner, utbkey, 0, model);
	}
	@RequestMapping("/partner/taobao/seatPage.xhtml")
	public String seatPage(String userId, Long mpid, Long partnerid, Integer price, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", userId);
		model.put("view", "partner/taobao/seatPageV1.vm");
		ErrorCode<String> code = addSeatData(mpid, partnerid, userId, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/taobao/seatPageV1.vm";
	}
	@RequestMapping("/partner/taobao/seatPageV2.xhtml")
	public String seatPageV2(String userId, Long mpid, Long partnerid, Integer price, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", userId);
		model.put("view", "partner/taobao/seatPage.vm");
		ErrorCode<String> code = addSeatData(mpid, partnerid, userId, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/taobao/seatPage.vm";
	}
}
