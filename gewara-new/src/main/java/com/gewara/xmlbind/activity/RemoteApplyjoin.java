package com.gewara.xmlbind.activity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteApplyjoin extends BaseInnerResponse implements Serializable{
	private static final long serialVersionUID = -4502423276846683786L;
	private Long id;
	private Long relatedid;
	private Timestamp addtime;
	private Integer joinnum;
	private String contactway;
	private Integer admin;
	private Long memberid;
	private String realname;//真实姓名
	private String sex; //性别
	private Integer needpay;
	private Date joindate;
	private Integer score;
	private String mark;
	private String status;
	public Integer getScore() {
		return score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getJoindate() {
		return joindate;
	}
	public void setJoindate(Date joindate) {
		this.joindate = joindate;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	
	
	public RemoteApplyjoin() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Integer getJoinnum() {
		return joinnum;
	}

	public void setJoinnum(Integer joinnum) {
		this.joinnum = joinnum;
	}

	public String getContactway() {
		return contactway;
	}

	public void setContactway(String contactway) {
		this.contactway = contactway;
	}
	
	public Integer getAdmin() {
		return admin;
	}
	public void setAdmin(Integer admin) {
		this.admin = admin;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Integer getNeedpay() {
		if(needpay == null) needpay = 0;
		return needpay;
	}
	public void setNeedpay(Integer needpay) {
		this.needpay = needpay;
	}
}
