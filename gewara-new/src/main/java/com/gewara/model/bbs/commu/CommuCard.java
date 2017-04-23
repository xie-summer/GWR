package com.gewara.model.bbs.commu;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class CommuCard extends BaseObject {
	private static final long serialVersionUID = -8076769136860243920L;
	private Long id;
	private Long commuid;
	private Long memberid;
	private String realname;
	private String company;
	private String position;
	private String industry;//ְλ
	private String phone;
	private String remark;
	private String emailset;
	private String messageset;
	private Timestamp addtime;
	
	public CommuCard(){}
	
	public CommuCard(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.emailset = Status.Y;
		this.messageset = Status.Y;
		this.memberid = memberid;
	}
	
	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCommuid() {
		return commuid;
	}

	public void setCommuid(Long commuid) {
		this.commuid = commuid;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getEmailset() {
		return emailset;
	}

	public void setEmailset(String emailset) {
		this.emailset = emailset;
	}

	public String getMessageset() {
		return messageset;
	}

	public void setMessageset(String messageset) {
		this.messageset = messageset;
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

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

}
