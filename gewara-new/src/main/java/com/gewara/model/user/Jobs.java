package com.gewara.model.user;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class Jobs extends BaseObject {
	private static final long serialVersionUID = -5010141453720441090L;
	
	private Long id;
	private String position;
	private Integer expvalue;
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
	
	public Integer getExpvalue() {
		return expvalue;
	}
	public void setExpvalue(Integer expvalue) {
		this.expvalue = expvalue;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
