package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class EmailInvite extends BaseObject{

	private static final long serialVersionUID = -1122172971222775219L;
	private Long id;
	private Long memberid;
	private String email;
	private Timestamp addtime;
	private Long registerid;
	
	public Long getRegisterid() {
		return registerid;
	}

	public void setRegisterid(Long registerid) {
		this.registerid = registerid;
	}

	public EmailInvite(){}
	
	public EmailInvite(Long memberid){
		this.memberid = memberid;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	
	public EmailInvite(Long memberid,String email){
		this(memberid);
		this.email = email;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
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
}
