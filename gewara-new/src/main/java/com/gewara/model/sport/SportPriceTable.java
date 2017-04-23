package com.gewara.model.sport;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;


/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class SportPriceTable extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	private Long id;
	private String tablename;
	private Long sportid;
	private String remark;
	private String unit;
	private Long itemid;
	private Integer ordernum;
	public Integer getOrdernum() {
		return ordernum;
	}
	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}
	public static List<SportPrice> getWeekPriceList(List<SportPrice> priceList, String weektype){
		List<SportPrice> result = new LinkedList<SportPrice>();
		for(SportPrice price: priceList){
			if(StringUtils.contains(price.getWeektype(),weektype)) result.add(price);
		}
		return result;
	}
	public static Set<String> getTimerangeList(List<SportPrice> priceList){
		Set<String> timeCodeSet = new TreeSet<String>();
		String tmp = "";
		for(SportPrice price: priceList){
			tmp = price.getTimerange();
			if(StringUtils.isBlank(tmp)) tmp="È«Ìì";
			timeCodeSet.add(tmp);
		}
		return timeCodeSet;
	}
	public static Set<String> getWeektypeList(List<SportPrice> priceList){
		Set<String> result = new TreeSet<String>();
		for(SportPrice price: priceList){
			result.add(price.getWeektype());
		}
		return result;
	}
	public static Map<String, SportPrice> getPriceMap(List<SportPrice> priceList){
		Map<String, SportPrice> result = new HashMap<String, SportPrice>();
		for(SportPrice price: priceList){
			result.put(price.getWeektype() + price.getTimerange(), price);
		}
		return result;
	}
	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
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
}
