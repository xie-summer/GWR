package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class Place extends BaseObject {
	public static final String PASSED = "1";
	public static final String DELED = "1";
	private static final long serialVersionUID = -5586080539806873059L;
	private Long id;
	private String tag;
	private String name;
	private String address;
	private String contactphone;
	private String park;
	private String feature;
	private String content;
	private String countycode;
	private String citycode;
	private String lineidlist;// 地铁线路
	private Long stationid;
	private String stationname;
	private String exitnumber;// 出口
	private String transport;
	private Timestamp addtime;
	private Long userid;
	private String ispass;
	private String isdel;
	private String otherinfo;
	@Override
	public Serializable realId() {
		return id;
	}

	public Place(){}
	
	public Place(String tag) {
		this.isdel = "0";
		this.ispass = "0";
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.tag = tag;
	}

	public String getLineidlist() {
		return lineidlist;
	}

	public void setLineidlist(String lineidlist) {
		this.lineidlist = lineidlist;
	}
	public String getExitnumber() {
		return exitnumber;
	}

	public void setExitnumber(String exitnumber) {
		this.exitnumber = exitnumber;
	}

	public String getTransport() {
		return transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactphone() {
		return contactphone;
	}

	public void setContactphone(String contactphone) {
		this.contactphone = contactphone;
	}

	public String getPark() {
		return park;
	}

	public void setPark(String park) {
		this.park = park;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public String getIspass() {
		return ispass;
	}

	public void setIspass(String ispass) {
		this.ispass = ispass;
	}

	public String getIsdel() {
		return isdel;
	}

	public void setIsdel(String isdel) {
		this.isdel = isdel;
	}

	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Long getStationid() {
		return stationid;
	}

	public void setStationid(Long stationid) {
		this.stationid = stationid;
	}

	public String getStationname() {
		return stationname;
	}

	public void setStationname(String stationname) {
		this.stationname = stationname;
	}
	
	public String getOtherinfo(){
		return this.otherinfo;
	}
	
	public void setOtherinfo(String otherinfo){
		this.otherinfo = otherinfo;
	}
}
