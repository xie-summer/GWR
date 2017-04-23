package com.gewara.command;

public class GoodsCommand {
	private Long goodsid;
	private Long gspid;
	private Integer quantity;
	private String tag;
	public Long getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}
	public Long getGspid() {
		return gspid;
	}
	public void setGspid(Long gspid) {
		this.gspid = gspid;
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
