package com.gewara.xmlbind;

import org.apache.commons.lang.StringUtils;

public abstract class BaseInnerResponse {
	
	protected String code;
	protected String error;
	protected String syscode;
	protected String sysmsg;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	public boolean isSuccess(){
		return StringUtils.equals(code, "0000") || StringUtils.isBlank(code) && StringUtils.isBlank(error);
	}
	public String getSyscode() {
		return syscode;
	}
	public void setSyscode(String syscode) {
		this.syscode = syscode;
	}
	public String getSysmsg() {
		return sysmsg;
	}
	public void setSysmsg(String sysmsg) {
		this.sysmsg = sysmsg;
	}
}
