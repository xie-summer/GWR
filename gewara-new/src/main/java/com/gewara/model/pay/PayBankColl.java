package com.gewara.model.pay;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class PayBankColl extends BaseObject{
	private static final long serialVersionUID = -358615456027063091L;
	private Long id;
	private String name;
	private Long parentid;
	private String paymethod;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getParentid() {
		return parentid;
	}
	public void setParentid(Long parentid) {
		this.parentid = parentid;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
