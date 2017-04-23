package com.gewara.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.api.vo.BaseVo;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;

public class GewaOrderMap extends BaseVo implements Serializable {
	
	private static final long serialVersionUID = -2087716993799697315L;
	
	private Long id;
	private String tradeNo;
	private String ordertype;
	private GewaOrder order;
	private List<BuyItem> buyItemList = new ArrayList<BuyItem>();
	private Object place;											//场馆
	private Object profile;											//场馆扩展
	private Object item;											//项目
	private Object schedule;										//场次，物品，卡
	private Object relate;											//关联活动
	
	public GewaOrderMap(GewaOrder order){
		this.order = order;
		this.id = order.getId();
		this.tradeNo = order.getTradeNo();
		this.ordertype = order.getOrdertype();
	}
	
	@Override
	public Serializable realId() {
		return id;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getTradeNo() {
		return tradeNo;
	}
	
	public String getOrdertype() {
		return ordertype;
	}
	
	public GewaOrder getOrder() {
		return order;
	}
	public void setOrder(GewaOrder order) {
		this.order = order;
	}
	public List<BuyItem> getBuyItemList() {
		return buyItemList;
	}
	public void setBuyItemList(List<BuyItem> buyItemList) {
		this.buyItemList = buyItemList;
	}
	public Object getPlace() {
		return place;
	}
	public void setPlace(Object place) {
		this.place = place;
	}
	public Object getProfile() {
		return profile;
	}

	public void setProfile(Object profile) {
		this.profile = profile;
	}

	public Object getItem() {
		return item;
	}
	public void setItem(Object item) {
		this.item = item;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Object getSchedule() {
		return schedule;
	}

	public void setSchedule(Object schedule) {
		this.schedule = schedule;
	}
	
	public Object getRelate() {
		return relate;
	}

	public void setRelate(Object relate) {
		this.relate = relate;
	}

	public boolean hasOrdertype(String orderType){
		if(StringUtils.isBlank(orderType)){
			return false;
		}
		return StringUtils.equals(orderType, this.ordertype);
	}

	public String getOrdertitle(){
		String title = "";
		return title;
	}
	
}
