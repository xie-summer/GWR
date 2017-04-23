package com.gewara.xmlbind.partner;

import java.io.Serializable;

public class IBoxPayResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private PartnerBoxPayUser response;
	
	private String respCode;
	private String errorDesc;
	public PartnerBoxPayUser getResponse() {
		return response;
	}
	public void setResponse(PartnerBoxPayUser response) {
		this.response = response;
	}
	public String getRespCode() {
		return respCode;
	}
	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
	public String getErrorDesc() {
		return errorDesc;
	}
	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}
	
	public void addresponse(PartnerBoxPayUser payResponse){
		this.response = payResponse;
	}

}
