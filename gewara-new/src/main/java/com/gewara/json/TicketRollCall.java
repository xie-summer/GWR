package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

import com.gewara.util.DateUtil;

public class TicketRollCall implements Serializable{

	private static final long serialVersionUID = 840605886815128092L;
	private String id;
	private String mobile;
	private String tag;
	private Long relatedid;
	private Integer quantity;
	private Date addDate;
	private String dateStr;
	
	public TicketRollCall() {}
	
	public TicketRollCall(String mobile, String tag, Long relatedid){
		this.mobile = mobile;
		this.tag = tag;
		this.relatedid= relatedid;
		Date curDate = DateUtil.currentTime();
		this.addDate = curDate;
		this.quantity = 0;
		this.dateStr = DateUtil.format(curDate, "yyyy-MM-dd");
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
}
