/**
 * 
 */
package com.gewara.web.action.partner;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.pay.ChinapayUtil;
import com.gewara.support.ErrorCode;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

@Controller
public class PartnerSrcbShopController extends BasePartnerController{
	public ApiUser getSrcbShop(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_SRCBSHOP);
	}
	@RequestMapping("/partner/srcbshop/chooseSeat.xhtml")
	public String srcbChooseSeat(Long mpid, String memberId, String sign, ModelMap model){
		if(mpid==null) return forwardMessage(model, "缺少参数！");
		if(StringUtils.isBlank(memberId)){
			model.put("mpid", mpid);
			return "redirect:/partner/srcbshop/ajaxLogin.xhtml";
		}
		ApiUser apiUser = getSrcbShop();
		Map<String, String> otherMap = VmUtils.readJsonToMap(apiUser.getOtherinfo());
		String merant_id = otherMap.get("merant_id");
		String primaryKey = otherMap.get("primaryKey");
		if(!StringUtils.endsWithIgnoreCase(sign, StringUtil.md5(memberId+merant_id+primaryKey))){
			return forwardMessage(model, "校验错误！");
		}
		model.put("memberId", memberId);
		model.put("sign", sign);
		return chooseSeat(apiUser, mpid, memberId, "partner/srcbshop/step2.vm", model);
	}
	@RequestMapping("/partner/srcbshop/ajaxLogin.xhtml")
	public String ajaxLogin(Long mpid, ModelMap model){
		ApiUser apiUser = getSrcbShop();
		Map<String, String> otherMap = VmUtils.readJsonToMap(apiUser.getOtherinfo());
		String merant_id = otherMap.get("merant_id");
		String primaryKey = otherMap.get("primaryKey");
		model.put("loginUrl", otherMap.get("login"));
		model.put("sign", StringUtil.md5(merant_id+primaryKey));
		model.put("mpid", mpid);
		model.put("merchantId", merant_id);
		return "partner/srcbshop/login.vm";
	}
	@RequestMapping("/partner/srcbshop/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price, String memberId, ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", memberId);
		ErrorCode<String> code = addSeatData(mpid, partnerid, memberId, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/seatPage.vm";
	}
	//第三步:锁座位，加订单
	@RequestMapping("/partner/srcbshop/addOrder.xhtml")
	public String srcbAddOrder(String memberId, String sign,
			String captchaId, String captcha, Long mpid, 
			@RequestParam("mobile")String mobile,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		if(mpid==null || StringUtils.isBlank(memberId)) return showJsonError(model, "缺少参数！");
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		ApiUser partner = getSrcbShop();
		Map<String, String> otherMap = VmUtils.readJsonToMap(partner.getOtherinfo());
		String merant_id = otherMap.get("merant_id");
		String primaryKey = otherMap.get("primaryKey");
		if(!StringUtils.endsWithIgnoreCase(sign, StringUtil.md5(memberId + merant_id+primaryKey))){
			return showJsonError(model, "校验错误！");
		}
		ErrorCode code = addOrder(mpid, mobile, seatid, memberId, partner, memberId, null, null, PaymethodConstant.PAYMETHOD_CHINAPAYSRCB, WebUtils.getRemoteIp(request), model);
		if(!code.isSuccess())return showJsonError(model, code.getMsg()); 
		return showJsonSuccess(model, model.get("orderId")+"");
	}
	//第四步：确认订单去支付
	@RequestMapping(method=RequestMethod.POST,value="/partner/srcbshop/saveOrder.xhtml")
	public String srcbSaveOrder(ModelMap model, @RequestParam("orderId")long orderId, String paymethod,
			String mobile, String memberId){
		String paybank = null;
		if(StringUtils.equals(paymethod,PaymethodConstant.PAYMETHOD_CHINAPAYSRCB)){
			paybank = ChinapayUtil.BANK_CODE_SRCB;
		}
		return saveOrder(orderId, mobile, paymethod, paybank, memberId, model);
	}
	//第四步：确认订单去支付（重新查看）
	@RequestMapping("/partner/srcbshop/showOrder.xhtml")
	public String srcbShowOrder(Long orderId, ModelMap model, String memberId, String sign){
		ApiUser partner = getSrcbShop();
		model.put("memberId", memberId);
		model.put("sign", sign);
		return showOrder(memberId, orderId, partner, "partner/srcbshop/step3.vm", model);
	}
}
