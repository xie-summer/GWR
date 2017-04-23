package com.gewara.model.common;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class GewaCity extends BaseObject {
	public static String SINGLE_SERVICE_TYPE = "single";//单业务模式
	public static String MORE_SERVICE_TYPE = "more";//多业务模式
	
	private static final long serialVersionUID = 8710609514593838526L;
	private String citycode;
	private String cityname;
	private String provincecode;
	private String provincename;
	private String showHot;
	private String showIdx;
	private String showAdm;
	private Integer hotSort;
	private Integer citySort;
	private Integer provinceSort;
	private String pinyin;
	private String py;
	private String manmethod; //auto, manual
	private String bpointx;		//百度坐标x.y轴
	private String bpointy;
	private String serviceType; //single 单业务模式  more 多业务模式
	
	@Override
	public Serializable realId() {
		return citycode;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getCityname() {
		return cityname;
	}
	public void setCityname(String cityname) {
		this.cityname = cityname;
	}
	public String getProvincecode() {
		return provincecode;
	}
	public void setProvincecode(String provincecode) {
		this.provincecode = provincecode;
	}
	public String getProvincename() {
		return provincename;
	}
	public void setProvincename(String provincename) {
		this.provincename = provincename;
	}
	public String getShowHot() {
		return showHot;
	}
	public void setShowHot(String showHot) {
		this.showHot = showHot;
	}
	public String getShowIdx() {
		return showIdx;
	}
	public void setShowIdx(String showIdx) {
		this.showIdx = showIdx;
	}
	public String getShowAdm() {
		return showAdm;
	}
	public void setShowAdm(String showAdm) {
		this.showAdm = showAdm;
	}
	public Integer getCitySort() {
		return citySort;
	}
	public void setCitySort(Integer citySort) {
		this.citySort = citySort;
	}
	public Integer getProvinceSort() {
		return provinceSort;
	}
	public void setProvinceSort(Integer provinceSort) {
		this.provinceSort = provinceSort;
	}
	public Integer getHotSort() {
		return hotSort;
	}
	public void setHotSort(Integer hotSort) {
		this.hotSort = hotSort;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	public String getPy() {
		return py;
	}
	public void setPy(String py) {
		this.py = py;
	}
	public String getManmethod() {
		return manmethod;
	}
	public void setManmethod(String manmethod) {
		this.manmethod = manmethod;
	}
	public boolean hasAuto(){
		return StringUtils.equals(this.showIdx, Status.Y) && !StringUtils.equals(this.manmethod, "manual");
	}
	public String getBpointx() {
		return bpointx;
	}
	public void setBpointx(String bpointx) {
		this.bpointx = bpointx;
	}
	public String getBpointy() {
		return bpointy;
	}
	public void setBpointy(String bpointy) {
		this.bpointy = bpointy;
	}
	public String getFirstInitials(){
		return StringUtils.substring(py, 0, 1);
	}

	public String getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
}
