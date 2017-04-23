package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class Notice extends BaseObject {
	private static final long serialVersionUID = 5241842420214112590L;
	public static final String TAG_COMMU = "commu";//È¦×Ó¹«¸æ
	private Long id;
	private Long relatedid;
	private Long memberid;
	private String tag;
	private String body;
	private Timestamp addtime;
	private String status;
	private Integer ordernum;
	
	public Notice(){}
	
	public Notice(Long memberid){
		this.status = Status.Y_NEW;
		this.ordernum = 0;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
	}
	
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

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

}
