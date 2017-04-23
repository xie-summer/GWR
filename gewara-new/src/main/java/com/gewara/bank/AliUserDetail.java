package com.gewara.bank;

import com.gewara.xmlbind.BaseInnerResponse;

public class AliUserDetail extends BaseInnerResponse {
	private String userId;
	private String mobile;
	private String realName;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
}
