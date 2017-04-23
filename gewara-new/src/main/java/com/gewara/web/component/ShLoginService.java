package com.gewara.web.component;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gewara.model.user.Member;
import com.gewara.model.user.OpenMember;
import com.gewara.support.ErrorCode;

public interface ShLoginService extends LoginService{
	ErrorCode<Map> autoLogin(HttpServletRequest request, HttpServletResponse response, OpenMember openMember);
	ErrorCode<Map> autoLoginByDyncode(HttpServletRequest request, HttpServletResponse response, Member member);
	/**
	 * 根据ip和SessionId或取用户ID
	 * @param ip
	 * @param sessid
	 * @return
	 */
	Member getLogonMemberBySessid(String ip, String sessid);
	void updateMemberAuth(String sessid, Member member);
}
