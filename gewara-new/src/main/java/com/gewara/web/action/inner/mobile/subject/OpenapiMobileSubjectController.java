package com.gewara.web.action.inner.mobile.subject;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.TempMember;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.subject.BaiFuBaoService;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenapiMobileSubjectController extends BaseOpenApiController {
	@Autowired
	@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	
	@Autowired@Qualifier("baiFuBaoService")
	private BaiFuBaoService baiFuBaoService;

	@RequestMapping("/openapi/mobile/subject/origins/getColorBall.xhtml")
	public String getColorBall(String mobile, String msgContent, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "nologin");
		}
		ErrorCode<SMSRecord> smsRecodeCode = drawActivityService.getColorBall(member.getId(), mobile, msgContent);
		if (!smsRecodeCode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, smsRecodeCode.getErrcode());
		}
		daoService.saveObject(smsRecodeCode.getRetval());
		untransService.sendMsgAtServer(smsRecodeCode.getRetval(), false);
		return getSuccessXmlView(model);
	}
	@RequestMapping("/openapi/mobile/subject/origins/getCouponCode.xhtml")
	public String getCouponCode(String mobile, String msgContent, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "nologin");
		}
		ErrorCode<SMSRecord> code = drawActivityService.getCouponCode(member.getId(), mobile, msgContent);
		if (!code.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getErrcode());
		}
		daoService.saveObject(code.getRetval());
		untransService.sendMsgAtServer(code.getRetval(), false);
		return getSuccessXmlView(model);
	}
	
	/**
	 * 百度钱包抽奖
	 */
	@RequestMapping("/openapi/mobile/subject/baiduwallet/drawClick.xhtml")
	public String drawClick(ModelMap model) {
		OpenApiAuth openAuth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		String ip = openAuth.getRemoteIp();
		Member member = openAuth.getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请先登录");
		}
		ErrorCode<String> drawCode = baiFuBaoService.drawClick(member.getId(), ip);
		if (!drawCode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, drawCode.getMsg());
		}
		return getSingleResultXmlView(model, drawCode.getRetval());
	}
	
	/**
	 * 百付宝优惠券个数
	 */
	@RequestMapping("/openapi/mobile/subject/baiduwallet/joinCount.xhtml")
	public String joinCount(ModelMap model) {
		long joinCount = baiFuBaoService.joinCount();
		return getSingleResultXmlView(model, joinCount);
	}
	@RequestMapping("/openapi/mobile/subject/baiduwallet/buySpcode.xhtml")
	public String buySpcode(String mobile, String password, String memberEncode, String citycode, String regfrom, String flag, ModelMap model) {
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		String ip = auth.getRemoteIp();
		TempMember tm = null;
		Map<String, String> otherMap = new HashMap<String, String>();
		if(StringUtils.isNotBlank(citycode)){
			otherMap.put("citycode", citycode);
		}
		if(StringUtils.isNotBlank(regfrom)){
			otherMap.put("regfrom", regfrom);
		}
		if(StringUtils.isNotBlank(memberEncode)){
			Member member = auth.getMember();
			if (member == null) {
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
			}
			ErrorCode<TempMember> tmcode = memberService.createTempMemberBind(member, mobile, flag, ip);
			if(!tmcode.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, tmcode.getMsg());
			}
			tm = tmcode.getRetval();
		}else {
			ErrorCode<TempMember> tmcode = memberService.createTempMember(mobile, password, flag, ip, otherMap);
			if(!tmcode.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, tmcode.getMsg());
			}
			tm = tmcode.getRetval();
		}
		ErrorCode<String> code = baiFuBaoService.getPayUrl(tm);
		if(!code.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		}
		return getSingleResultXmlView(model, code.getRetval());
	}
	@RequestMapping("/openapi/mobile/subject/baiduwallet/activityShow.xhtml")
	public String activityShow(ModelMap model) {
		return getXmlView(model, "inner/mobile/subject/bfbActivity.vm");
	}
	
	@RequestMapping("/openapi/mobile/subject/baiduwallet/checkStatus.xhtml")
	public String checkStatus(String mobile, String password, ModelMap model) {
		ErrorCode<String> statusCode = baiFuBaoService.checkStatus(mobile, password);
		if (!statusCode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, statusCode.getMsg());
		} 
		return getSuccessXmlView(model);
	}
}
