package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;

import com.gewara.model.BaseObject;
//场馆
public class Stadium extends BaseObject{
	private static final long serialVersionUID = 7217104536885218337L;
	private	Long id;
	private String cnName;			//中文名称
	private String	enName;			//英文名称
	private String cnAddress;		//中文地址
	private String enAddress;		//英文地址
	private String telephone;		//电话
	private Integer typeId;			//场馆类型
	private String available;		//是否有效
	private String provinceCode;	//省/市ID
	private String cityCode;		//城市ID
	private String cityAreaCode;	//城区ID
	
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
	
	public String getIdString(){
		return String.valueOf(id);
	}
	
	public String getCnName() {
		return cnName;
	}
	public void setCnName(String cnName) {
		this.cnName = cnName;
	}
	public String getEnName() {
		return enName;
	}
	public void setEnName(String enName) {
		this.enName = enName;
	}
	public String getCnAddress() {
		return cnAddress;
	}
	public void setCnAddress(String cnAddress) {
		this.cnAddress = cnAddress;
	}
	public String getEnAddress() {
		return enAddress;
	}
	public void setEnAddress(String enAddress) {
		this.enAddress = enAddress;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public Integer getTypeId() {
		return typeId;
	}
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	public String getAvailable() {
		return available;
	}
	public void setAvailable(String available) {
		this.available = available;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public String getCityAreaCode() {
		return cityAreaCode;
	}
	public void setCityAreaCode(String cityAreaCode) {
		this.cityAreaCode = cityAreaCode;
	}
	
}
