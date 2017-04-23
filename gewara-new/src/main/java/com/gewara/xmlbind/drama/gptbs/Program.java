package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
//项目
public class Program extends BaseObject{
	private static final long serialVersionUID = 4965724876634999303L;
	private Long id;				//ID
	private String code;			//项目编号
	private String cnName;			//项目中文名
	private String enName;			//项目英文名
	private Long stadiumId;			//场馆
	private Long venueId;			//场地
	private Timestamp startTime;	//项目开始时间
	private Timestamp endTime;		//项目结束时间
	private Integer typeId;			//项目类型
	private String available;		//是否有效
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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
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
	public Long getStadiumId() {
		return stadiumId;
	}
	public void setStadiumId(Long stadiumId) {
		this.stadiumId = stadiumId;
	}
	public Long getVenueId() {
		return venueId;
	}
	public void setVenueId(Long venueId) {
		this.venueId = venueId;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
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
	
}
