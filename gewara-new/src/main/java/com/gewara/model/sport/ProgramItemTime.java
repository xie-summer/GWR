package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class ProgramItemTime extends BaseObject {

	private static final long serialVersionUID = -8308622119209655641L;
	private Long id;
	private Long sportid;
	private Long itemid;
	private Integer price;
	private Integer costprice;
	private Integer sportprice;
	private Integer unitMinute;
	private Integer quantity;
	private String unitType;
	private String starttime;
	private String endtime;
	private Integer week;
	private String openType;
	private Timestamp addtime;
	private Long fieldid;
	private String citycode; 
	public ProgramItemTime() {}
	public ProgramItemTime(long sportid, long itemid, int week, String citycode) {
		this.sportid = sportid;
		this.itemid = itemid;
		this.week = week;
		this.price = 0;
		this.costprice = 0;
		this.sportprice = 0;
		this.citycode = citycode;
		this.addtime = DateUtil.getCurFullTimestamp();
	}
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
	public Long getFieldid() {
		return fieldid;
	}
	public void setFieldid(Long fieldid) {
		this.fieldid = fieldid;
	}
	public void setWeek(Integer week) {
		this.week = week;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public int getWeek() {
		return week;
	}
	public void setWeek(int week) {
		this.week = week;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Integer getSportprice() {
		return sportprice;
	}
	public void setSportprice(Integer sportprice) {
		this.sportprice = sportprice;
	}
	public Integer getUnitMinute() {
		return unitMinute;
	}
	public void setUnitMinute(Integer unitMinute) {
		this.unitMinute = unitMinute;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getUnitType() {
		return unitType;
	}
	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}
	public String getOpenType() {
		return openType;
	}
	public void setOpenType(String openType) {
		this.openType = openType;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
}
