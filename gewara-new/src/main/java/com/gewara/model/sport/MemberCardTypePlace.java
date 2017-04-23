package com.gewara.model.sport;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class MemberCardTypePlace extends BaseObject{
	private static final long serialVersionUID = 3439897506579548021L;
	private Long id;
	private Long placeid;
	private Long mctid;
	public MemberCardTypePlace(){
		
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}
	public Long getMctid() {
		return mctid;
	}
	public void setMctid(Long mctid) {
		this.mctid = mctid;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
