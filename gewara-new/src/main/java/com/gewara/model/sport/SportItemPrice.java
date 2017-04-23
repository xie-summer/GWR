package com.gewara.model.sport;

import java.io.Serializable;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class SportItemPrice extends BaseObject {
	private static final long serialVersionUID = 2382752423372374921L;
	private Long id;
	private Long sportid;
	private Long itemid;
	private Integer week;
	private Integer minprice;
	private Integer maxprice;
	private String status;
	
	public SportItemPrice(){}
	public SportItemPrice(Long sportid, Long itemid, Integer week){
		this.sportid = sportid;
		this.itemid = itemid;
		this.week = week;
		this.minprice = 0;
		this.maxprice = 0;
		this.status = Status.Y;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public Integer getMinprice() {
		return minprice;
	}

	public void setMinprice(Integer minprice) {
		this.minprice = minprice;
	}

	public Integer getMaxprice() {
		return maxprice;
	}

	public void setMaxprice(Integer maxprice) {
		this.maxprice = maxprice;
	}

	@Override
	public Serializable realId() {
		return id;
	}

}
