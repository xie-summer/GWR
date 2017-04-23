package com.gewara.command;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

public class SearchOdiCommand implements Serializable{
	private static final long serialVersionUID = -9180577191054713346L;
	private final static Map<String, Boolean> orderMap;
	static{
		Map<String, Boolean> tmp = new HashMap<String, Boolean>();
		tmp.put("clickedtimes", false);
		tmp.put("releasedate", true);
		tmp.put("avggeneral", false);
		tmp.put("boughtcount", false);
		orderMap = UnmodifiableMap.decorate(tmp);
	}
	
	public String citycode;
	public String dramatype;
	public String dramaname;
	public String playdate;
	public Long starid;
	private Integer price;
	private String order;
	
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getDramatype() {
		return dramatype;
	}
	public void setDramatype(String dramatype) {
		this.dramatype = dramatype;
	}
	public String getDramaname() {
		return dramaname;
	}
	
	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}
	
	public String getPlaydate() {
		return playdate;
	}
	
	public void setPlaydate(String playdate) {
		this.playdate = playdate;
	}

	public Long getStarid() {
		return starid;
	}
	public void setStarid(Long starid) {
		this.starid = starid;
	}
	public Integer getPrice() {
		if(price != null && (price < 1 || price > 7)){
			this.price = null;
		}
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	
	public String getOrder() {
		if(StringUtils.isBlank(order) || orderMap.get(order) == null){
			this.order = "clickedtimes";
		}
		return this.order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
	public boolean gainAsc(){
		return Boolean.valueOf(orderMap.get(getOrder()));
	}
	
}
