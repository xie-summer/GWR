package com.gewara.model.sport;

import java.io.Serializable;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class SportField extends BaseObject{
	private static final long serialVersionUID = -6327117631138231180L;
	private Long id;
	private Long sportid;		//³¡¹ÝID
	private Long itemid;			//ÏîÄ¿ID
	private String name;			//Ãû³Æ
	private Integer ordernum;	//ÅÅÐò
	private String description;//ÃèÊö
	private Long remoteid;		//Ô¶³ÌID
	private String status;		//×´Ì¬
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getRemoteid() {
		return remoteid;
	}

	public void setRemoteid(Long remoteid) {
		this.remoteid = remoteid;
	}

	public SportField() {}
	
	public SportField(String name){
		this.ordernum = 1;
		this.name = name;
	}
	public SportField(Long sportid, Long itemid, String name){
		this(name);
		this.sportid = sportid;
		this.itemid = itemid;
		this.status = Status.Y;
	}
	public SportField(SportField remote){
		this.sportid = remote.getSportid();
		this.itemid = remote.getItemid();
		this.name = remote.getName();
		this.ordernum = remote.getOrdernum();
		this.description= remote.getDescription();
		this.remoteid = remote.getId();
		this.status = "Y";
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getSportid() {
		return sportid;
	}
	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}
	public Long getItemid() {
		return itemid;
	}
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
