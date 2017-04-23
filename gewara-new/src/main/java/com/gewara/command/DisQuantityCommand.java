package com.gewara.command;

import java.io.Serializable;

public class DisQuantityCommand implements Serializable {
	private static final long serialVersionUID = -8419499672509508056L;
	private Long tspid;
	private Integer quantity;
	private Integer price;
	private Integer costprice;
	private Integer theatreprice;
	private Long settleid;
	private Integer allownum;
	private Integer maxbuy;
	private String distype;
	private String retail;
	
	public Long getTspid() {
		return tspid;
	}
	public void setTspid(Long tspid) {
		this.tspid = tspid;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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
	public Long getSettleid() {
		return settleid;
	}
	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}
	public Integer getAllownum() {
		return allownum;
	}
	public void setAllownum(Integer allownum) {
		this.allownum = allownum;
	}
	public Integer getMaxbuy() {
		return maxbuy;
	}
	public void setMaxbuy(Integer maxbuy) {
		this.maxbuy = maxbuy;
	}
	public String getDistype() {
		return distype;
	}
	public void setDistype(String distype) {
		this.distype = distype;
	}
	public String getRetail() {
		return retail;
	}
	public void setRetail(String retail) {
		this.retail = retail;
	}

}
