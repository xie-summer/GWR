package com.gewara.xmlbind.activity;

import java.sql.Timestamp;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteTreasure extends BaseInnerResponse {
	private Long memberid;
	private Timestamp addtime;
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	
}
