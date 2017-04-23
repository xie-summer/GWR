package com.gewara.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
/**
 * 手机客户端升级
 * @author liushusong
 *
 */

public class MobileApp implements Serializable{
	
	private static final long serialVersionUID = 6351955802258321065L;
	private String id;
	private String ostype;
	private String appurl;
	private String name;
	private String appsize;
	private String logo;
	private String appversion;
	private String status;
	private Date addtime;
	private String otherAppName;
	private Integer sortFlag; 
	private String appdesc;//应用说明
	private List<String> coverapp;//覆盖产品
	
	public MobileApp(){}

	public String getAppdesc() {
		return appdesc;
	}

	public void setAppdesc(String appdesc) {
		this.appdesc = appdesc;
	}

	public String getOtherAppName() {
		return otherAppName;
	}

	public void setOtherAppName(String otherAppName) {
		this.otherAppName = otherAppName;
	}

	public Integer getSortFlag() {
		return sortFlag;
	}

	public void setSortFlag(Integer sortFlag) {
		this.sortFlag = sortFlag;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOstype() {
		return ostype;
	}

	public void setOstype(String ostype) {
		this.ostype = ostype;
	}

	public String getAppurl() {
		return appurl;
	}

	public void setAppurl(String appurl) {
		this.appurl = appurl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppsize() {
		return appsize;
	}

	public void setAppsize(String appsize) {
		this.appsize = appsize;
	}

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public List<String> getCoverapp() {
		return coverapp;
	}

	public void setCoverapp(List<String> coverapp) {
		this.coverapp = coverapp;
	}
	
	
	
	
	

}
