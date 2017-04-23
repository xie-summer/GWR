package com.gewara.json.mobile;

import java.io.Serializable;
import java.util.Date;

public class MobileUpGradeCount implements Serializable {
	private static final long serialVersionUID = 5822227303557327450L;

	private String id;
	
	private String downloadUrl;
	private String appsource;

	private String apptype;
	private String tag;
	private String name;
	private int clickedtimes;
	private int downtimes;
	private Date addTime;
	
	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public int getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(int clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public int getDowntimes() {
		return downtimes;
	}

	public void setDowntimes(int downtimes) {
		this.downtimes = downtimes;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getAppsource() {
		return appsource;
	}

	public void setAppsource(String appsource) {
		this.appsource = appsource;
	}

	public String getApptype() {
		return apptype;
	}

	public void setApptype(String apptype) {
		this.apptype = apptype;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
