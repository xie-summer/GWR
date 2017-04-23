package com.gewara.xmlbind.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApiOpenTimeTable{
	public static final String STATUS_BOOK = "Y"; // 接受预订
	public static final String STATUS_NOBOOK = "N"; // 不接受预订
	public static final String STATUS_DISCARD = "D"; // 废弃
	private Long id;
	private Long sportid; // 场馆ID
	private Long itemid; // 项目ID
	private String sportname; // 场馆名
	private String itemname; // 项目名
	private Date playdate; // 日期
	private String status; // 状态
	private Timestamp opentime;//开放时间
	private Timestamp closetime;//关闭时间
	private List<ApiOpenTimeItem> sportOpenTimeList = new ArrayList<ApiOpenTimeItem>();// 场地图
	public void addSportOpenTime(ApiOpenTimeItem openTimeItem){
		sportOpenTimeList.add(openTimeItem);
	}
	public List<ApiOpenTimeItem> getSportOpenTimeList() {
		return sportOpenTimeList;
	}

	public void setSportOpenTimeList(List<ApiOpenTimeItem> sportOpenTimeList) {
		this.sportOpenTimeList = sportOpenTimeList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSportid() {
		return sportid;
	}

	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}

	public Date getPlaydate() {
		return playdate;
	}

	public void setPlaydate(Date playdate) {
		this.playdate = playdate;
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ApiOpenTimeTable))
			return false;
		final ApiOpenTimeTable temp = (ApiOpenTimeTable) o;
		return !(getId() != null ? !(getId().equals(temp.getId())) : (temp
				.getId() != null));
	}

	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

	public String getSportname() {
		return sportname;
	}

	public void setSportname(String sportname) {
		this.sportname = sportname;
	}

	public String getItemname() {
		return itemname;
	}

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}
	public Timestamp getOpentime() {
		return opentime;
	}
	public void setOpentime(Timestamp opentime) {
		this.opentime = opentime;
	}
	public Timestamp getClosetime() {
		return closetime;
	}
	public void setClosetime(Timestamp closetime) {
		this.closetime = closetime;
	}
}
