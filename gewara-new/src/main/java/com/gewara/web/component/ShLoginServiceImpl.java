package com.gewara.web.component;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.user.Member;
import com.gewara.model.user.OpenMember;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.web.support.AclService;
import com.gewara.web.support.CachedAuthentication;
import com.gewara.web.support.OpenMemberAuthenticationToken;
import com.gewara.web.util.LoginUtils;
public class ShLoginServiceImpl extends LoginServiceImpl implements ShLoginService{
	@Autowired
	private AclService aclService;
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Override
	public ErrorCode<Map> autoLogin(HttpServletRequest request, HttpServletResponse response, String username, String password){
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) return ErrorCode.getFailure("用户名密码必填！");
		Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
		aclService.setLogonType("member");
		return autoLogin(request, response, auth);
	}
	@Override
	public ErrorCode<Map> autoLogin(HttpServletRequest request, HttpServletResponse response, OpenMember openMember){
		Authentication auth = new OpenMemberAuthenticationToken(openMember);
		aclService.setLogonType("member");
		return autoLogin(request, response, auth);
	}
	
	@Override
	public ErrorCode<Map> autoLoginByDyncode(HttpServletRequest request, HttpServletResponse response, Member member){
		Authentication auth = new OpenMemberAuthenticationToken(member, member.getMobile());
		aclService.setLogonType("member");
		return autoLogin(request, response, auth);
	}
	@Override
	public Member getLogonMemberBySessid(String ip, String sessid){
		GewaraUser user = getLogonGewaraUserBySessid(ip, sessid);
		if(user instanceof Member) {
			return (Member) user;
		}
		return null;
	}
	@Override
	public void updateMemberAuth(String sessid, Member member) {
		CachedAuthentication ca =  (CachedAuthentication) cacheService.get(CacheService.REGION_LOGINAUTH, sessid);
		Authentication auth = ca.getAuthentication();
		if(auth instanceof UsernamePasswordAuthenticationToken || auth instanceof OpenMemberAuthenticationToken){
			Member old = (Member) auth.getPrincipal();
			old.setMobile(member.getMobile());
			old.setPassword(member.getPassword());
			old.setEmail(member.getEmail());
			old.setNickname(member.getNickname());
			old.setBindStatus(member.getBindStatus());
			cacheService.set(CacheService.REGION_LOGINAUTH, sessid, ca);
		}
	}
	
	@Override
	public boolean isGewaraUserLogon(String sessid) {
		return cacheService.get(CacheService.REGION_LOGINKEY, LoginUtils.getTimeoutKey(sessid)) != null;
	}
}
