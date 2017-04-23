package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class OrderAddress extends BaseObject {

	private static final long serialVersionUID = -8227413676440639244L;
	private String tradeno;
	private Long usefulAddressId;
	private String realname;
	private String postalcode;
	private Timestamp addtime;
	private String mobile;
	private String provincecode;
	private String provincename;
	private String citycode;
	private String cityname;
	private String countycode;
	private String countyname;
	private String expresstype;
	private String address;
	
	public OrderAddress(){}
	
	public OrderAddress(String tradeno){
		this.tradeno = tradeno;
		this.addtime = DateUtil.getCurFullTimestamp();
	}
	
	@Override
	public Serializable realId() {
		return tradeno;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}

	public String getCountyname() {
		return countyname;
	}

	public void setCountyname(String countyname) {
		this.countyname = countyname;
	}

	public String getExpresstype() {
		return expresstype;
	}

	public void setExpresstype(String expresstype) {
		this.expresstype = expresstype;
	}

	public Long getUsefulAddressId() {
		return usefulAddressId;
	}

	public void setUsefulAddressId(Long usefulAddressId) {
		this.usefulAddressId = usefulAddressId;
	}
	
	public String gainAddress(){
		return this.provincename + " " + this.cityname + " " + this.countyname + " " + this.address;
	}
}
