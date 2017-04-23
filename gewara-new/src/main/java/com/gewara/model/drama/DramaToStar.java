package com.gewara.model.drama;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class DramaToStar extends BaseObject {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Long dramaid;
	private Long starid;
	private String tag;
	private Integer numsort;
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
	public Long getDramaid() {
		return dramaid;
	}
	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}
	public Long getStarid() {
		return starid;
	}
	public void setStarid(Long starid) {
		this.starid = starid;
	}
	
	public DramaToStar(){}
	
	public DramaToStar(Long dramaid, Long starid) {
		this.dramaid = dramaid;
		this.starid = starid;
	}

	public Integer getNumsort() {
		return numsort;
	}

	public void setNumsort(Integer numsort) {
		this.numsort = numsort;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
