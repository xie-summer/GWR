package com.gewara.web.action.partner;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PaymethodConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.support.ErrorCode;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
/**
 * 
 * 百度合作购票 iframe嵌入
 */
@Controller
public class PartnerBaiduMController extends BasePartnerController {
	private static final String iframeUrl = "http://map.baidu.com/static/movie/proxy.html";
	private ApiUser getApiUser(String key){
		return daoService.getObjectByUkey(ApiUser.class, "partnerkey", key, true);
	}
	
	private boolean validPartner(String sign,ApiUser partner){
		if(partner == null){
			return false;
		}
		if(StringUtils.equals(sign, StringUtil.md5("key=" + partner.getPartnerkey() + "&privateKey=" + partner.getPrivatekey()))){
			return true;
		}
		return false;
	}
	
	//第二步：选择座位
	@RequestMapping("/partner/baidu/chooseSeat.xhtml")
	public String chooseSeat(Long mpId,String appkey,String sign,String callBack,@CookieValue(required=false,value="ukey") String ukey,
			HttpServletResponse response,ModelMap model){
		if(mpId==null) return showError(model, "缺少参数！");
		ApiUser partner = getApiUser(appkey);
		if(!validPartner(sign, partner)){
			return forwardMessage(model, "请确保正确的来源渠道！");
		}
		if(StringUtils.isBlank(ukey)) {
			ukey = PartnerUtil.setUkCookie(response, config.getBasePath() + "partner/baidu/");
		}
		model.put("callBack",callBack);
		model.put("key",appkey);
		model.put("sign",sign);
		model.put("iframeUrl", iframeUrl);
		return chooseSeat(partner, mpId, ukey, "partner/baiduMap/step1.vm", model);
	}
	
	@RequestMapping("/partner/baidu/seatPage.xhtml")
	public String seatPage(Long mpid, Long partnerid, Integer price,@CookieValue(value="ukey", required=false) String ukey,
			String jsSeatList,ModelMap model){
		model.put("mpid", mpid);
		model.put("partnerid", partnerid);
		model.put("price", price);
		model.put("ukey", ukey);
		model.put("jsSeatList",jsSeatList);
		if(StringUtils.isBlank(ukey)){
			return showJsonError(model, "缺少参数请刷新重试！");
		}
		ErrorCode<String> code = addSeatData(mpid, partnerid, ukey, model);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return "partner/baiduMap/seatPage.vm";
	}
	
	//第三步:锁座位，加订单
	@RequestMapping("/partner/baidu/addOrder.xhtml")
	public String addOrder(String captchaId, String captcha, Long mpid,
			@CookieValue(value="ukey", required=false) String ukey,
			@RequestParam("mobile")String mobile,
			String key,String sign,
			@RequestParam("seatid")String seatid, HttpServletRequest request, ModelMap model){
		ApiUser partner = getApiUser(key);
		if(!validPartner(sign, partner)){
			return forwardMessage(model, "请确保正确的来源渠道！");
		}
		String event = request.getParameter("event");
		if (StringUtils.isBlank(event)) {
			event = "";
		}
		boolean validCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!validCaptcha) return showJsonError(model, "验证码错误！");
		String paymethod = PaymethodConstant.PAYMETHOD_PARTNERPAY;
		ErrorCode code = addOrder(mpid, mobile, seatid, ukey, partner, partner.getBriefname(), null, event, paymethod, WebUtils.getRemoteIp(request), model);
		if(code.isSuccess()) {
			TicketOrder order = daoService.getObject(TicketOrder.class,(Long)model.get("orderId"));
			Map<String, String> resultMap = (Map<String, String>)model.get("specialDiscountMap");
			Map<String, String> jsonMap = new HashMap<String, String>();
			if (MapUtils.isNotEmpty(resultMap)) {
				if (StringUtils.isNotBlank(resultMap.get("msg"))) {
					jsonMap.put("msg", resultMap.get("msg"));
				} else {
					jsonMap.put("remark", resultMap.get("remark"));
					jsonMap.put("enableRemark", resultMap.get("enableRemark"));
					jsonMap.put("description", resultMap.get("description"));
				}
			}
			jsonMap.put("tradeno", order.getTradeNo());
			return showJsonSuccess(model,jsonMap);
		}
		return showJsonError(model, code.getMsg());
	}

}
