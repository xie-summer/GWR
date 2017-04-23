package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class AdPosition extends BaseObject {
	private static final long serialVersionUID = -1297822152819030082L;
	private Long id;
	private String pid;
	private String tag;
	private String position;
	private String description;
	private Timestamp addtime;

	public AdPosition(){}
	
	public AdPosition(String tag) {
		this.tag = tag;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
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

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

}
