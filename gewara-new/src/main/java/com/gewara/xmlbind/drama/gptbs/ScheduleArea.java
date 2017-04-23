package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;

import com.gewara.model.BaseObject;
//场次区域
public class ScheduleArea extends BaseObject{
	private static final long serialVersionUID = -1932439244314902861L;
	private Long id;					//ID
	private String cnName;				//区域中文名
	private String enName;				//区域英文名
	private String description;			//区域描述
	private String standing;			//是否站票
	private Integer total;				//站票总量
	private Integer limit;				//限制数
	private Long venueId;				//所属场馆编号
	private Integer gridWidth;			//表格宽度
	private Integer gridHeight;			//表格高度
	private Long venueAreaId;			//场馆区域ID
	private Long scheduleId;			//场次ID
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStanding() {
		return standing;
	}
	public void setStanding(String standing) {
		this.standing = standing;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	public Long getVenueId() {
		return venueId;
	}
	public void setVenueId(Long venueId) {
		this.venueId = venueId;
	}
	public Integer getGridWidth() {
		return gridWidth;
	}
	public void setGridWidth(Integer gridWidth) {
		this.gridWidth = gridWidth;
	}
	public Integer getGridHeight() {
		return gridHeight;
	}
	public void setGridHeight(Integer gridHeight) {
		this.gridHeight = gridHeight;
	}
	public Long getVenueAreaId() {
		return venueAreaId;
	}
	public void setVenueAreaId(Long venueAreaId) {
		this.venueAreaId = venueAreaId;
	}
	public Long getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}
	
}
