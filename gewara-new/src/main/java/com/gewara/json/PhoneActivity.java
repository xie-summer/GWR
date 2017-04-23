package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

public class PhoneActivity implements Serializable{

	private static final long serialVersionUID = 5746433266589043890L;
	public static final String APP_TYPE_CINAME="cinema";
	public static final String APP_TYPE_SPORT="sport";
	public static final String APP_TYPE_BAR="bar";
	
	public static final String OS_TYPE_ANDROID="ANDROID";
	public static final String OS_TYPE_IPHONE="IPHONE";
	public static final String OS_TYPE_ALL="ALL";
	
	public static final String STATUS_DELETE="D";//É¾³ý
	public static final String STATUS_NEW="N";//ÏÔÊ¾
	public static final String STATUS_HIDDEN="H";//Òþ²Ø
	
	private String id;
	private String title;
	private String logo;
	private Date starttime;
	private Date endtime;
	private String address;
	private String type;
	private String content;
	private String opiinfo;
	private String apptype;
	private String ostype;
	private Date addtime;
	private String citycode;
	private String status;
	private String contentLogo;
	private Integer rank;//ÅÅÐò
	
	public String getContentLogo() {
		return contentLogo;
	}

	public void setContentLogo(String contentLogo) {
		this.contentLogo = contentLogo;
	}

	public String getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getEndtime() {
		return endtime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getOpiinfo() {
		return opiinfo;
	}

	public void setOpiinfo(String opiinfo) {
		this.opiinfo = opiinfo;
	}

	public String getApptype() {
		return apptype;
	}

	public void setApptype(String apptype) {
		this.apptype = apptype;
	}

	public String getOstype() {
		return ostype;
	}

	public void setOstype(String ostype) {
		this.ostype = ostype;
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}
	
	
	
}
