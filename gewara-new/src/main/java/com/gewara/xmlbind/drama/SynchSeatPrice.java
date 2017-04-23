package com.gewara.xmlbind.drama;

import java.sql.Timestamp;

public class SynchSeatPrice {

	private Long gewaid;
	private Long busid;
	private Long gewaodiid;
	private String seattype;
	private Integer price;
	private Integer costprice;
	private Integer theatreprice;
	private String status;
	private String remark;
	private String upcard;		//是否支持卡的使用
	private Long gewadpid;
	private Integer quantity;	//剧院拿票数量
	private Integer sales = 0;	//卖出票数量
	private Timestamp updatetime;
	public Long getGewaid() {
		return gewaid;
	}
	public void setGewaid(Long gewaid) {
		this.gewaid = gewaid;
	}
	public Long getBusid(){
		return busid;
	}
	public void setBusid(Long busid){
		this.busid = busid;
	}
	public Long getGewaodiid() {
		return gewaodiid;
	}
	public void setGewaodiid(Long gewaodiid) {
		this.gewaodiid = gewaodiid;
	}
	public String getSeattype() {
		return seattype;
	}
	public void setSeattype(String seattype) {
		this.seattype = seattype;
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
	public Integer getTheatreprice() {
		return theatreprice;
	}
	public void setTheatreprice(Integer theatreprice) {
		this.theatreprice = theatreprice;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getUpcard() {
		return upcard;
	}
	public void setUpcard(String upcard) {
		this.upcard = upcard;
	}
	public Long getGewadpid() {
		return gewadpid;
	}
	public void setGewadpid(Long gewadpid) {
		this.gewadpid = gewadpid;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getSales() {
		return sales;
	}
	public void setSales(Integer sales) {
		this.sales = sales;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
}
