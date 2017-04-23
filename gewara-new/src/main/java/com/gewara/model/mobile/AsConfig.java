package com.gewara.model.mobile;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class AsConfig extends BaseObject{
	private static final long serialVersionUID = -3610595893740418200L;
	private Long id;
	private Long partnerid;
	private String appsource;
	private String appVersion;
	private String paymethod;
	private String specialmethod;
	
	public AsConfig(){
		
	}
	public AsConfig(Long partnerid, String appsource, String paymethod){
		this.partnerid = partnerid;
		this.appsource = appsource;
		this.paymethod = paymethod;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public String getSpecialmethod() {
		return specialmethod;
	}
	public void setSpecialmethod(String specialmethod) {
		this.specialmethod = specialmethod;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getAppsource() {
		return appsource;
	}
	public void setAppsource(String appsource) {
		this.appsource = appsource;
	}
	public String getAllPaymethod(){
		String pm = "";
		if(StringUtils.isNotBlank(paymethod)){
			pm = paymethod;
			if(StringUtils.isNotBlank(specialmethod)) pm = pm + "," + specialmethod;
		}
		return pm;
	}
}
