package com.gewara.json;

import java.io.Serializable;

public class Weixin2Wala implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6456064687938911170L;
	private String id;
	private String walaType;
	private String title;
	private String picUrl;
	private String context;
	private String addTime;
	// 区分文档是否删除（new:可用；del：删除）
	private String docType;

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWalaType() {
		return walaType;
	}

	public void setWalaType(String walaType) {
		this.walaType = walaType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

}
