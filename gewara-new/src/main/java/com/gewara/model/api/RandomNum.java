package com.gewara.model.api;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;


public class RandomNum extends BaseObject {
	private static final long serialVersionUID = -6800394265547863600L;
	private String id;
	private Timestamp validity;
	public RandomNum(){}
	public RandomNum(String id, Timestamp validtime){
		this.id=id;this.validity=validtime;
	}
	public Timestamp getValidity() {
		return validity;
	}

	public void setValidity(Timestamp validity) {
		this.validity = validity;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
