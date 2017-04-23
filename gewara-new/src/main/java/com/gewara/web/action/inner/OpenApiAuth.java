package com.gewara.web.action.inner;

import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.user.Member;
import com.gewara.web.action.api.ApiAuth;

public class OpenApiAuth extends ApiAuth{
	private String remoteIp;
	private Member member;
	public OpenApiAuth(ApiUser apiUser, ApiUserExtra userExtra, String remoteIp, Member member){
		super(apiUser, userExtra);
		this.remoteIp = remoteIp;
		this.member = member;
	}
	public String getRemoteIp() {
		return remoteIp;
	}
	public Member getMember() {
		return member;
	}
}
