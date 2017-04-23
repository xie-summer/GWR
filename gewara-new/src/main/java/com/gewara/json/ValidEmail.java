package com.gewara.json;

import com.gewara.util.ObjectId;

public class ValidEmail {
	public static final String TYPE_PASSWORD = "password";
	public static final String TYPE_ACCOUNT_PASSWORD = "password_account";
	
	private String id;
	private String email;
	private Long validtime;
	private String validcode;
	private String validtype;
	
	public ValidEmail(){}
	
	public ValidEmail(String email, Long validtime, String validcode, String validtype){
		this.id = ObjectId.uuid();
		this.email = email;
		this.validtime = validtime;
		this.validcode = validcode;
		this.validtype = validtype;
	}
	
	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getValidcode() {
		return validcode;
	}
	
	public void setValidcode(String validcode) {
		this.validcode = validcode;
	}

	public String getValidtype() {
		return validtype;
	}

	public void setValidtype(String validtype) {
		this.validtype = validtype;
	}

	public Long getValidtime() {
		return validtime;
	}

	public void setValidtime(Long validtime) {
		this.validtime = validtime;
	}
	
}
