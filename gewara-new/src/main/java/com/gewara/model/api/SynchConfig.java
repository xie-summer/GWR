package com.gewara.model.api;

import java.io.Serializable;

import com.gewara.model.BaseObject;


public class SynchConfig extends BaseObject {
	private static final long serialVersionUID = -6800394265547863600L;
	private String ttype;
	private String tvalue;
	
	public SynchConfig(){}
	
	public String getTtype() {
		return ttype;
	}

	public void setTtype(String ttype) {
		this.ttype = ttype;
	}

	public String getTvalue() {
		return tvalue;
	}

	public void setTvalue(String tvalue) {
		this.tvalue = tvalue;
	}
	@Override
	public Serializable realId() {
		return ttype;
	}
}
