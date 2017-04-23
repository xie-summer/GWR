package com.gewara.web.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.user.Member;
import com.gewara.untrans.PersonCenterService;

public class ShAuthenticationSuccessHandler extends GewaAuthenticationSuccessHandler {
	@Autowired@Qualifier("personCenterService")
	private PersonCenterService personCenterService;
	@Override
	protected void successCallback(HttpServletRequest request, GewaraUser user, String ip, String sessid) {
		super.successCallback(request, user, ip, sessid);
		if(user instanceof Member) personCenterService.putMemberId(user.getId());
	}
}
