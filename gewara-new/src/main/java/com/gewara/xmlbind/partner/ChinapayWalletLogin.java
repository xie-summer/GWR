package com.gewara.xmlbind.partner;

import java.io.Serializable;

public class ChinapayWalletLogin implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String code;
	private String user;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
}
