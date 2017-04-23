package com.gewara.model.user;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class FriendInfo extends BaseObject {

	private static final long serialVersionUID = 8322204814529526693L;
	private Long id;
	private String realname;
	private String email;
	private String mobile;
	private Long memberid;
	private Long addmemberid;
	
	public FriendInfo(){}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Long getAddmemberid() {
		return addmemberid;
	}
	public void setAddmemberid(Long addmemberid) {
		this.addmemberid = addmemberid;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
