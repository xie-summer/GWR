package com.gewara.web.support;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.gewara.constant.MemberConstant;
import com.gewara.model.user.Member;
import com.gewara.model.user.OpenMember;

/**
 * @author acerge(acerge@163.com)
 * @since 5:06:03 PM Mar 7, 2011
 */
public class OpenMemberAuthenticationToken extends AbstractAuthenticationToken {
	public static final String TYPE_OPENMEMBER = "open";
	public static final String TYPE_MOBILE = "mobile";
	private static final long serialVersionUID = 8278827757022204477L;
	private Member member;
	private Long memberid;
	private String source;
	private String identity;
	public OpenMemberAuthenticationToken(OpenMember openMember) {
		super(null);
		this.source = openMember.getSource();
		this.identity = "" + openMember.getId();
		this.memberid = openMember.getMemberid();
	}
	/**
	 * @param mobile ÊÖ»úºÅ
	 * @param dPassword ¶¯Ì¬Âë
	 */
	public OpenMemberAuthenticationToken(Member member, String mobile){
		super(null);
		this.identity = mobile;
		this.memberid = member.getId();
		this.source = MemberConstant.SOURCE_DYNCODE;
	}
	/**
	 * This constructor should only be used by <code>AuthenticationManager</code> or
	 * <code>AuthenticationProvider</code> implementations that are satisfied with producing a trusted (i.e.
	 * {@link #isAuthenticated()} = <code>true</code>) authentication token.
	 * 
	 * @param principal
	 * @param credentials
	 * @param authorities
	 */
	public OpenMemberAuthenticationToken(Member member, String source, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.source = source;
		this.member = member;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return member;
	}
	public Long getMemberid(){
		return memberid;
	}
	public String getSource(){
		return source;
	}

	public String getIdentity() {
		return identity;
	}
	@Override
	public void eraseCredentials() {
	}
}
