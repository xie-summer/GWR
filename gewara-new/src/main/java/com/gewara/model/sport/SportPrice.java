package com.gewara.model.sport;

import java.io.Serializable;

import com.gewara.model.BaseObject;


/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class SportPrice extends BaseObject implements Comparable<SportPrice>{
	private static final long serialVersionUID = 4914995483381697551L;
	private Long id;
	private String weektype;
	private String timerange;
	private Integer price;
	private Integer memberprice;
	private String remark;
	private String unit;
	private Long pricetableid;
	private Integer ordernum;
	
	private Integer bookingprice; //预定价格
	
	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	public Integer getMemberprice() {
		return memberprice;
	}

	public void setMemberprice(Integer memberprice) {
		this.memberprice = memberprice;
	}

	public SportPrice() {
	}

	public SportPrice(Long id, Integer price) {
		this.id = id;
		this.price = price;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWeektype() {
		return this.weektype;
	}

	public void setWeektype(String weektype) {
		this.weektype = weektype;
	}

	public String getTimerange() {
		return this.timerange;
	}

	public void setTimerange(String time) {
		this.timerange = time;
	}

	public Integer getPrice() {
		return this.price;
	}

	public void setPrice(Integer price) {
		this.price = price;
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

	@Override
	public int compareTo(SportPrice o) {
		return price.compareTo(o.price);
	}

	public Long getPricetableid() {
		return pricetableid;
	}

	public void setPricetableid(Long pricetableid) {
		this.pricetableid = pricetableid;
	}

	public Integer getBookingprice() {
		return bookingprice;
	}

	public void setBookingprice(Integer bookingprice) {
		this.bookingprice = bookingprice;
	}
}
