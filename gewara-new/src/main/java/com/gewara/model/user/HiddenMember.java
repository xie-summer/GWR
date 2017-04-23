package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class HiddenMember extends BaseObject{
	private static final long serialVersionUID = -6347962427000434933L;
	private Long id;
	private String realname;
	private String mobile;
	private String email;
	private Timestamp addtime;
	private Long inviteid;//—˚«Î»Àid
	
	public HiddenMember() {}
	
	public HiddenMember(String realname){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.realname = realname;
	}
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
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getInviteid() {
		return inviteid;
	}
	public void setInviteid(Long inviteid) {
		this.inviteid = inviteid;
	}
	public String getHeadpicUrl(){
		return "img/default_head.png";
	}
}
