package com.gewara.command;

import java.io.Serializable;
import java.sql.Timestamp;

public class OrderParamsCommand implements Serializable {
	private static final long serialVersionUID = 4463096682044152182L;
	
	private Integer pageNo;
	private Long memberid;
	private String mobile;
	private String tradeno;
	private Timestamp starttime;
	private Timestamp endtime;
	private String ordertype;
	private String status;
	private String paymethod;
	private String category;
	private String citycode;
	private Long placeid;				//场馆ID
	private Long itemid;				//项目ID
	private Long relatedid;				//场次ID
	private String express;
	private String expressstatus;		//快递状态
	
	private String order;
	private Boolean asc = false;
	
	private String level;
	
	private String errorMsg;
	private String xls;
	
	public Long getMemberid() {
		return memberid;
	}
	
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	
	public String getMobile() {
		return mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String getTradeno() {
		return tradeno;
	}
	
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public Timestamp getStarttime() {
		return starttime;
	}
	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}
	public Timestamp getEndtime() {
		return endtime;
	}
	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}
	public String getOrdertype() {
		return ordertype;
	}
	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}
	public Integer getPageNo() {
		if(pageNo == null || pageNo < 0) pageNo = 0;
		return pageNo;
	}
	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public Boolean getAsc() {
		if(asc == null) asc = false;
		return asc;
	}

	public void setAsc(Boolean asc) {
		this.asc = asc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Long getPlaceid() {
		return placeid;
	}

	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getExpress() {
		return express;
	}

	public void setExpress(String express) {
		this.express = express;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getXls() {
		return xls;
	}

	public void setXls(String xls) {
		this.xls = xls;
	}

	public String getExpressstatus() {
		return expressstatus;
	}

	public void setExpressstatus(String expressstatus) {
		this.expressstatus = expressstatus;
	}
	
}
