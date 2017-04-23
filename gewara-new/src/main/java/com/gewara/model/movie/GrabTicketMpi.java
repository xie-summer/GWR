package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;


public class GrabTicketMpi extends BaseObject {
	private static final long serialVersionUID = 7365148972996181946L;
	private Long id;
	private Long sid; // 抢票专题id
	private Long mpid;// 场次id
	private String link;
	private Timestamp addtime;
	private String description;
	@Override
	public Serializable realId() {
		return id;
	}

	public GrabTicketMpi(){}

	public GrabTicketMpi(String link) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.link = link;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getSid() {
		return sid;
	}

	public void setSid(Long sid) {
		this.sid = sid;
	}

	public Long getMpid() {
		return mpid;
	}

	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
