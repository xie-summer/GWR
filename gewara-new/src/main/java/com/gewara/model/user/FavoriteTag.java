package com.gewara.model.user;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class FavoriteTag extends BaseObject {
	private static final long serialVersionUID = 1L;

	private String tag;
	private Long clickcount;
	
	public FavoriteTag() {
	}
	
	public FavoriteTag(String tag) {
		this.tag = tag;
		this.clickcount = 0L;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getClickcount() {
		return clickcount;
	}
	public void setClickcount(Long clickcount) {
		this.clickcount = clickcount;
	}
	@Override
	public Serializable realId() {
		return tag;
	}
}
