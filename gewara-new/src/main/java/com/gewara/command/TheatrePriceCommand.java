package com.gewara.command;

public class TheatrePriceCommand {
	private Long itemid;
	private Long tspid;
	private Integer quantity;
	private String tag;
	
	public Long getItemid() {
		return itemid;
	}
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
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
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
}
