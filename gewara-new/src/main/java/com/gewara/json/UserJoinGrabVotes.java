package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

import com.gewara.util.DateUtil;

public class UserJoinGrabVotes implements Serializable{
	private static final long serialVersionUID = 3443516786734098746L;
	private String id;
	private Date addtime;			//添加时间
	private Long relatedid;			//关联ID
	private Long memberid;			//用户ID
	private String type;			//支持类型
	
	public UserJoinGrabVotes(){}
	
	public UserJoinGrabVotes(Long relatedid, Long memberid, String type){
		this.relatedid = relatedid;
		this.memberid = memberid;
		this.type = type;
		this.addtime = DateUtil.currentTime();
	}
	
	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

}