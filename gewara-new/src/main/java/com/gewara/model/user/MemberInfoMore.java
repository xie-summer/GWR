package com.gewara.model.user;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class MemberInfoMore extends BaseObject {
	private static final long serialVersionUID = 1L;
	private Long id;
	private Long  memberid;
	private String tag;
	private String schooladdress;
	private String name;
	private String schooltype;	
	private String educomeinyear;	
	private String jobprovince;
	private String jobcity;	
	private String jobdep;
	private String jobcompanyemail;	
	@Override
	public Serializable realId() {
		return id;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getSchooladdress() {
		return schooladdress;
	}

	public void setSchooladdress(String schooladdress) {
		this.schooladdress = schooladdress;
	}

	public String getSchooltype() {
		return schooltype;
	}

	public void setSchooltype(String schooltype) {
		this.schooltype = schooltype;
	}

	public String getEducomeinyear() {
		return educomeinyear;
	}

	public void setEducomeinyear(String educomeinyear) {
		this.educomeinyear = educomeinyear;
	}

	public String getJobprovince() {
		return jobprovince;
	}

	public void setJobprovince(String jobprovince) {
		this.jobprovince = jobprovince;
	}

	public String getJobcity() {
		return jobcity;
	}

	public void setJobcity(String jobcity) {
		this.jobcity = jobcity;
	}

	public String getJobdep() {
		return jobdep;
	}

	public void setJobdep(String jobdep) {
		this.jobdep = jobdep;
	}

	public String getJobcompanyemail() {
		return jobcompanyemail;
	}

	public void setJobcompanyemail(String jobcompanyemail) {
		this.jobcompanyemail = jobcompanyemail;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
