package com.gewara.web.action.api;

import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;

public class ApiAuth {
	private ApiUser apiUser;
	private ApiUserExtra userExtra;
	private String partnerIp;
	private String msg;
	private String code;
	private boolean checked;
	public ApiUser getApiUser() {
		return apiUser;
	}
	public void setApiUser(ApiUser apiUser) {
		this.apiUser = apiUser;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public ApiAuth(ApiUser apiUser){
		this.apiUser = apiUser;
		this.msg = null;
	}
	public ApiAuth(ApiUser apiUser, ApiUserExtra userExtra){
		this.apiUser = apiUser;
		this.userExtra = userExtra;
		this.msg = null;
	}
	public ApiAuth(ApiUser apiUser, ApiUserExtra userExtra, String partnerIp){
		this.apiUser = apiUser;
		this.userExtra = userExtra;
		this.msg = null;
		this.partnerIp = partnerIp;
	}
	/**
	 * 参数验证是否成功
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public boolean isChecked(){
		return checked;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public ApiUserExtra getUserExtra() {
		return userExtra;
	}
	public void setUserExtra(ApiUserExtra userExtra) {
		this.userExtra = userExtra;
	}
	public String getPartnerIp() {
		return partnerIp;
	}
	public void setPartnerIp(String partnerIp) {
		this.partnerIp = partnerIp;
	}

}
