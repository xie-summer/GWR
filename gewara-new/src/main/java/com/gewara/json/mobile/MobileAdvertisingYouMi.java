package com.gewara.json.mobile;

import java.io.Serializable;
import java.util.Date;

public class MobileAdvertisingYouMi implements Serializable {
	private static final long serialVersionUID = -4774336996322485278L;

	private String id;
	
	private String deviceid;//对应的就是设备的mac地址
	
	private String url;
	
	private Date addTime;
	
	private int clickedtimes;
	
	private String ymRecord;
	
	private String appsource;
	
	private String apptype;
	
	private String openUDID;
	
	private String msource;
	
	private String uinfo;
	
	public String getUinfo() {
		return uinfo;
	}

	public void setUinfo(String uinfo) {
		this.uinfo = uinfo;
	}
	
	public String getMsource() {
		return msource;
	}

	public void setMsource(String msource) {
		this.msource = msource;
	}

	public String getOpenUDID() {
		return openUDID;
	}

	public void setOpenUDID(String openUDID) {
		this.openUDID = openUDID;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}
	
	public String getApptype() {
		return apptype;
	}

	public void setApptype(String apptype) {
		this.apptype = apptype;
	}

	public String getAppsource() {
		return appsource;
	}

	public void setAppsource(String appsource) {
		this.appsource = appsource;
	}

	public String getYmRecord() {
		return ymRecord;
	}

	public void setYmRecord(String ymRecord) {
		this.ymRecord = ymRecord;
	}

	public int getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(int clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
