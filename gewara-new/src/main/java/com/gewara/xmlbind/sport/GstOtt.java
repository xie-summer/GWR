/**
 * 
 */
package com.gewara.xmlbind.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GstOtt{
	private Long id;
	private Long sportid;
	private Long itemid;
	private String status;
	private Date playdate;
	private Timestamp opentime;
	private Timestamp closetime;
	private String openType;
	private Integer unitMinute;
	private Integer quantity;
	
	private String tkey;
	private List<GstOti> otiList = new ArrayList<GstOti>();
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getPlaydate() {
		return playdate;
	}
	public void setPlaydate(Date playdate) {
		this.playdate = playdate;
	}
	public List<GstOti> getOtiList() {
		return otiList;
	}
	public void setOtiList(List<GstOti> otiList) {
		this.otiList = otiList;
	}
	public void addOti(GstOti oti){
		this.otiList.add(oti);
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
	public String getTkey() {
		return tkey;
	}
	public void setTkey(String tkey) {
		this.tkey = tkey;
	}
	public String getOpenType() {
		return openType;
	}
	public void setOpenType(String openType) {
		this.openType = openType;
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
}
