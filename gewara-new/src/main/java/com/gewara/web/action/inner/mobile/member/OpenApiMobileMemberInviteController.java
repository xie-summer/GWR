package com.gewara.web.action.inner.mobile.member;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.web.action.inner.mobile.BaseOpenApiController;
@Controller
public class OpenApiMobileMemberInviteController extends BaseOpenApiController{
	/**
	 * 查询邀请活动的可用状态
	 */
	@RequestMapping("/openapi/mobile/member/inviteUsabled.xhtml")
	public String getInviteUsabled(ModelMap model){
		return getSingleResultXmlView(model, "failure");
	}
	
	

	/**
	 * 手机客户端短信邀请用户
	 */
	@RequestMapping("/openapi/mobile/member/sendInvite.xhtml")
	public String sendInvite(ModelMap model) {
		return notSupport(model);
	}
	
	/**
	 * 查询邀请注册活动参与情况
	 */
	@RequestMapping("/openapi/mobile/member/getInivteActInfo.xhtml")
	public String getInivteActInfo(ModelMap model) {
		return notSupport(model);
	}
	
	/**
	 * 查询用户邀请手机号码
	 */
	@RequestMapping("/openapi/mobile/member/getInivteMobile.xhtml")
	public String getInivteMobile(ModelMap model) {
		return notSupport(model);
	}
	
}
