package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;
//场次
public class Schedule extends BaseObject{
	private static final long serialVersionUID = -4228181861726824560L;
	private Long id;						//ID
	private String code;					//场次编号
	private String cnName;					//场次中文名
	private String enName;					//场次英文名
	private String available;				//是否有效
	private String choseOnline;				//是否支持在线选座
	private Timestamp playTime;				//场次时间
	private Timestamp integerernalTime;		//出票时间
	private Timestamp integerernalEndTime;	//结束时间
	private Long programId;					//演出项目编号
	private Long venueId;					//场地ID
	private Integer logistics;					//快递方式
	private String fixed;					//是否固定时间
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
	public String getAvailable() {
		return available;
	}
	public void setAvailable(String available) {
		this.available = available;
	}
	public String getChoseOnline() {
		return choseOnline;
	}
	public void setChoseOnline(String choseOnline) {
		this.choseOnline = choseOnline;
	}
	public Timestamp getPlayTime() {
		return playTime;
	}
	public void setPlayTime(Timestamp playTime) {
		this.playTime = playTime;
	}
	
	public String getPlaydate(){
		return DateUtil.format(playTime, "yyyy-MM-dd");
	}
	
	public Timestamp getIntegerernalTime() {
		return integerernalTime;
	}
	public void setIntegerernalTime(Timestamp integerernalTime) {
		this.integerernalTime = integerernalTime;
	}
	public Timestamp getIntegerernalEndTime() {
		return integerernalEndTime;
	}
	public void setIntegerernalEndTime(Timestamp integerernalEndTime) {
		this.integerernalEndTime = integerernalEndTime;
	}
	public Long getProgramId() {
		return programId;
	}
	public void setProgramId(Long programId) {
		this.programId = programId;
	}
	public Long getVenueId() {
		return venueId;
	}
	public void setVenueId(Long venueId) {
		this.venueId = venueId;
	}
	
	public Integer getLogistics() {
		return logistics;
	}
	public void setLogistics(Integer logistics) {
		this.logistics = logistics;
	}
	public String getFixed() {
		return fixed;
	}
	public void setFixed(String fixed) {
		this.fixed = fixed;
	}
	public boolean hasAvailable(){
		return StringUtils.equals(this.available, Status.Y);
	}
}
