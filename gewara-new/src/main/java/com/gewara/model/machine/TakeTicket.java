package com.gewara.model.machine;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class TakeTicket extends BaseObject{
	//TODO:É¾³ý
	private static final long serialVersionUID = 9152814299047379603L;
	private Long id;
	private Long placeid;
	private String macid;
	private String status;
	private String tradeno;
	private Timestamp taketime;
	
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
	public String getMacid() {
		return macid;
	}
	public void setMacid(String macid) {
		this.macid = macid;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getTaketime() {
		return taketime;
	}
	public void setTaketime(Timestamp taketime) {
		this.taketime = taketime;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
}
