/* Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gewara.web.support;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

import com.gewara.Config;
import com.gewara.constant.MemberConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.DaoService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;

/**
 * @author acerge(acerge@163.com)
 * @since 1:59:07 PM Mar 21, 2011
 */
public class OpenMemberAuthenticationProvider implements AuthenticationProvider {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
	private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Assert.isInstanceOf(OpenMemberAuthenticationToken.class, authentication,
				"OpenMemberAuthenticationProvider only supports OpenMemberAuthenticationToken");
		OpenMemberAuthenticationToken auth = (OpenMemberAuthenticationToken) authentication;
		Member member = daoService.getObject(Member.class, auth.getMemberid());
		if(StringUtils.equals(auth.getSource(), MemberConstant.SOURCE_DYNCODE)){
			if(!StringUtils.equals(member.getMobile(), auth.getIdentity()) || !ValidateUtil.isMobile(auth.getIdentity())){
				dbLogger.warn("非法手机账号登录：" + auth.getIdentity());
				throw new IllegalArgumentException("非法手机账号登录!");
			}
		}else {
			MemberInfo info = daoService.getObject(MemberInfo.class, auth.getMemberid()); 
			String mSource = VmUtils.getJsonValueByKey(info.getOtherinfo(), "openMember");
			if(!StringUtils.contains(mSource, auth.getSource())){
				//TODO:为了兼容部分一个账户对应多个openMember的情况
			//if(!StringUtils.equals(mSource, auth.getSource())){
				dbLogger.warn("非法开放账户登录：" + auth.getSource() + ", memberid=" + auth.getMemberid());
				throw new IllegalArgumentException("非开放账户！");
			}
		}
		Assert.notNull(member, "用户不能空");
		preAuthenticationChecks.check(member);
		postAuthenticationChecks.check(member);
		Assert.notNull(member, "用户不能空");
		return createSuccessAuthentication(auth, member);
	}

	protected Authentication createSuccessAuthentication(OpenMemberAuthenticationToken auth, Member member) {
   	OpenMemberAuthenticationToken result = new OpenMemberAuthenticationToken(
   			member, auth.getSource(), member.getAuthorities());
   	result.setAuthenticated(true);
   	return result;
   }

	@Override
	public boolean supports(Class<? extends Object> authentication) {
		return (OpenMemberAuthenticationToken.class.isAssignableFrom(authentication));
	}

	private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
		public void check(UserDetails user) {
			if (!user.isAccountNonLocked()) {
				dbLogger.warn("User account is locked");
				throw new LockedException("User account is locked");
			}

			if (!user.isEnabled()) {
				dbLogger.warn("User account is locked");
				throw new DisabledException("User is disabled");
			}

			if (!user.isAccountNonExpired()) {
				dbLogger.warn("User account is expired");
				throw new AccountExpiredException("User account has expired");
			}
		}
	}

	private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
		public void check(UserDetails user) {
			if (!user.isCredentialsNonExpired()) {
				dbLogger.warn("User account credentials have expired");
				throw new CredentialsExpiredException("User credentials have expired");
			}
		}
	}

}
