package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;


public class JobsUp extends BaseObject{
	private static final long serialVersionUID = -5010141453720441090L;
	
	private Long id;
	private Long memberid;
	private Long jobsid;
	private String position;
	private Timestamp addtime;
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
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
	public Long getJobsid() {
		return jobsid;
	}
	public void setJobsid(Long jobsid) {
		this.jobsid = jobsid;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
