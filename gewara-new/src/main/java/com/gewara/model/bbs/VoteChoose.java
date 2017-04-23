package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class VoteChoose extends BaseObject{
	private static final long serialVersionUID = -5224752838982070278L;
	private Long id;
	private Long optionid;
	private Long diaryid;
	private Long memberid;
	private Timestamp addtime;
	
	public VoteChoose() {}
	
	public VoteChoose(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getOptionid() {
		return optionid;
	}
	public void setOptionid(Long optionid) {
		this.optionid = optionid;
	}
	
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
	public Long getDiaryid() {
		return diaryid;
	}
	public void setDiaryid(Long diaryid) {
		this.diaryid = diaryid;
	}
}
