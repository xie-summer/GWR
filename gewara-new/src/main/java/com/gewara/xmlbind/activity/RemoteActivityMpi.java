package com.gewara.xmlbind.activity;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteActivityMpi extends BaseInnerResponse{
	private Long activityid;
	private Long mpid;
	private String guest;
	public Long getActivityid() {
		return activityid;
	}
	public void setActivityid(Long activityid) {
		this.activityid = activityid;
	}
	public Long getMpid() {
		return mpid;
	}
	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}
	public String getGuest() {
		return guest;
	}
	public void setGuest(String guest) {
		this.guest = guest;
	}
}
