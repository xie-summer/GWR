package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

import com.gewara.util.DateUtil;

public class TicketRollCallMember implements Serializable{

	private static final long serialVersionUID = -8026628160320143813L;
	private String id;
	private String mobile;
	private Date addDate;
	private String status;
	private Long userid;
	private Date upDate;
	private String reason;
	
	public TicketRollCallMember() {}
	
	public TicketRollCallMember(String mobile, String status){
		this.mobile = mobile;
		Date curDate = DateUtil.currentTime();
		this.status = status;
		this.addDate = curDate;
		this.upDate = curDate;
	}
	
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public Date getUpDate() {
		return upDate;
	}

	public void setUpDate(Date upDate) {
		this.upDate = upDate;
	}
}
