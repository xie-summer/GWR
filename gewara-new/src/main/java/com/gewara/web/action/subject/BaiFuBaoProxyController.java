package com.gewara.web.action.subject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.constant.order.AddressConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.TempMember;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.subject.BaiFuBaoService;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class BaiFuBaoProxyController extends AnnotationController {
	public static final String FLAG_PC = "pc";
	public static final String FLAG_WAP = "wap";
	public static final String FLAG_MAX = "max";
	
	@Autowired@Qualifier("baiFuBaoService")
	private BaiFuBaoService baiFuBaoService;
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@Autowired@Qualifier("config")
	protected Config config;
	@RequestMapping("/baifubao/saveBind.xhtml")
	public String saveBind(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, 
			String mobile, HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		ErrorCode<TempMember> tm = memberService.createTempMemberBind(member, mobile, FLAG_PC, ip);
		if(!tm.isSuccess()){
			return showJsonError(model, tm.getMsg());
		}
		ErrorCode<String> code = baiFuBaoService.getPayUrl(tm.getRetval());
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model, code.getRetval());
	}
	
	@RequestMapping("/baifubao/fastReg.xhtml")
	public String fastReg(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, 
			String mobile, String password, String confirmPassword, String captchaId, String captcha, HttpServletRequest request,
			HttpServletResponse response, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, ip);
		if(!isValidCaptcha) return showJsonError_CAPTCHA_ERROR(model);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member != null) return showJsonError(model, "您是登录账号，不能快速注册！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(password) || !StringUtils.equals(password, confirmPassword)){
			return showJsonError(model, "密码不正确！");
		}
		Map<String, String> otherMap = new HashMap<String, String>();
		otherMap.put("citycode", citycode);
		otherMap.put("regfrom", AddressConstant.ADDRESS_WEB);
		ErrorCode<TempMember> tm = memberService.createTempMember(mobile, password, FLAG_PC, ip, otherMap);
		if(!tm.isSuccess()){
			return showJsonError(model, tm.getMsg());
		}
		ErrorCode<String> code = baiFuBaoService.getPayUrl(tm.getRetval());
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model, code.getRetval());
	}
	
	/**
	 * @param returnParams
	 * @param model
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 */
	@RequestMapping("/baifubao/bfbAcReturn.xhtml")
	public String bfbAcReturn(String tradeNo, /*String paidtime, String deviceType, */String check, String pause,
			HttpServletRequest request, ModelMap model) {
		dbLogger.warn(""+WebUtils.getRequestMap(request));
		String mycheck = StringUtil.md5(tradeNo + "sdlfkjsd23489");
		if(!StringUtils.equals(check, mycheck)){
			return showError(model, "支付错误！");
		}
		String paid = baiFuBaoService.queryOrder(tradeNo);
		if(StringUtils.contains(paid, "success")){
			Long tmid = Long.valueOf(tradeNo);
			TempMember tm = daoService.getObject(TempMember.class, tmid);
			model.put("tm", tm);
			String flag = tm.getFlag();
			if(StringUtils.equals(flag, FLAG_PC)){
				return "gewapay/ticket/orderResult_baidu.vm";
			}else {
				String regfrom = JsonUtils.getJsonValueByKey(tm.getOtherinfo(), "regfrom");
				Map submitMap = new HashMap();
				submitMap.put("success", "true");
				submitMap.put("regfrom", regfrom);
				submitMap.put("ordertype", "baiduwallet");
				model.put("submitParams", submitMap);
				String payResultUrl = config.getPageMap().get("absWap") + "payResult.xhtml";
				model.put("method", "post");
				model.put("pause", pause);
				model.put("submitUrl", payResultUrl);
				return "tempSubmitForm.vm";
			}
		}else{
			return showError(model, "支付失败！");
		}
	}
	
	@RequestMapping("/baifubao/bfbAcNotify.xhtml")
	@ResponseBody
	public String bfbAcNotify(String tradeNo, /*String paidtime, String deviceType, */String check, 
			HttpServletRequest request) {
		//TODO:内网IP验证！
		dbLogger.warn(""+WebUtils.getRequestMap(request));
		String mycheck = StringUtil.md5(tradeNo + "sdlfkjsd23489");
		String ip = WebUtils.getRemoteIp(request);
		if(GewaIpConfig.isInnerIp(ip) || WebUtils.isLocalIp(ip)){
			if(!StringUtils.equals(check, mycheck)){
				return "error";
			}
			String paid = baiFuBaoService.queryOrder(tradeNo);
			if(StringUtils.contains(paid, "success")){
				Long tmid = Long.valueOf(tradeNo);
				baiFuBaoService.processPaySuccess(tmid);
				return "success";
			}
		}
		return "failure";
	}
	@RequestMapping("/baifubao/mockPay.xhtml")
	public String mockPay(ModelMap model, HttpServletRequest request, Long tmid/*, String deviceType*/){
		if(WebUtils.getRemoteIp(request).contains("192.168.2")){
			ErrorCode<TempMember> result = baiFuBaoService.processPaySuccess(tmid);
			TempMember tm = result.getRetval();
			model.put("tm", tm);
			String regfrom = JsonUtils.getJsonValueByKey(tm.getOtherinfo(), "regfrom");
			if(StringUtils.equals(regfrom, AddressConstant.ADDRESS_WEB)){
				return showMessage(model, "成功支付，请稍后登录！");
			}else {
				Map submitMap = new HashMap();
				submitMap.put("success", "true");
				submitMap.put("regfrom", regfrom);
				submitMap.put("ordertype", "baiduwallet");
				model.put("submitParams", submitMap);
				String payResultUrl = config.getPageMap().get("absWap") + "payResult.xhtml";
				model.put("method", "post");
				model.put("submitUrl", payResultUrl);
				return "tempSubmitForm.vm";
			}
		}
		return showMessage(model, "error!");
	}
	@RequestMapping("/baifubao/refresh.xhtml")
	public String refresh(ModelMap model){
		baiFuBaoService.refreshCounter();
		return showJsonSuccess(model, "success:" + new Date());
	}
	
	@RequestMapping("/baifubao/checkStatus.xhtml")
	public String checkStatus(String mobile, String password, ModelMap model){
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码错误！");
		if(StringUtils.isBlank(password)) return showJsonError(model, "密码不能为空！");
		ErrorCode<String> result = baiFuBaoService.checkStatus(mobile, password);
		if(result.isSuccess()){
			return showJsonSuccess(model, "支付成功，登录后查看我的活动码！");
		}else{
			return showJsonError(model, result.getMsg());
		}
	}
	
	//抽奖
	@RequestMapping("/subject/proxy/baifubao/drawClick.xhtml")
	public String drawClick(Long memberid, String ip, ModelMap model){
		ErrorCode<String> drawClickResult = baiFuBaoService.drawClick(memberid, ip);
		if (drawClickResult.isSuccess()) {
			return showJsonSuccess(model, drawClickResult.getRetval());
		} else {
			return showJsonError(model, drawClickResult.getMsg());
		}
	}
	//状态
	@RequestMapping("/subject/proxy/baifubao/getTMStatus.xhtml")
	public String getTMStatus(Long memberid, ModelMap model){
		String result = "false";
		if(memberid != null){
			TempMember tm = daoService.getObjectByUkey(TempMember.class, "memberid", memberid);
			if(tm != null && StringUtils.equals(tm.getStatus(), Status.Y)){
				result = "true";
			}
		}
		return showJsonSuccess(model, result);
	}
	//领码人数
	@RequestMapping("/subject/proxy/baifubao/joinCount.xhtml")
	public String joinCount(ModelMap model){
		long result = baiFuBaoService.joinCount();
		return showJsonSuccess(model, result+"");
	}
}
