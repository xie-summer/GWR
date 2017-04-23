package com.gewara.json;

import java.io.Serializable;

public class SubMessageTask implements Serializable{
	private static final long serialVersionUID = 1500858820265308413L;
	private String id;
	private Long parentid;
	private Long memberid;
	private String phone;
	private String status;
	private String addtime;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getParentid() {
		return parentid;
	}

	public void setParentid(Long parentid) {
		this.parentid = parentid;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

}
