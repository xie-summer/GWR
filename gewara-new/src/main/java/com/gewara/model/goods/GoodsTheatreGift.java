package com.gewara.model.goods;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class GoodsTheatreGift extends BaseObject {
	private static final long serialVersionUID = 1253608089284152473L;
	private Long id;
	private Long goodsid;
	private Long theatreid;
	private Long dramaid;
	private Long dpid;
	private String rateinfo;
	private String week;
	private String dpidlist;
	private Timestamp fromtime;
	private Timestamp totime;
	
	public GoodsTheatreGift(){
		
	}
	public GoodsTheatreGift(Goods goods, String relateinfo){
		this.goodsid = goods.getId();
		this.fromtime = goods.getFromtime();
		this.totime = goods.getTotime();
		this.rateinfo = relateinfo;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getGoodsid() {
		return goodsid;
	}

	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}

	public Long getTheatreid() {
		return theatreid;
	}

	public void setTheatreid(Long theatreid) {
		this.theatreid = theatreid;
	}

	public Long getDramaid() {
		return dramaid;
	}

	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}

	public Long getDpid() {
		return dpid;
	}

	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}

	public String getRateinfo() {
		return rateinfo;
	}

	public void setRateinfo(String rateinfo) {
		this.rateinfo = rateinfo;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getDpidlist() {
		return dpidlist;
	}

	public void setDpidlist(String dpidlist) {
		this.dpidlist = dpidlist;
	}

	public Timestamp getFromtime() {
		return fromtime;
	}

	public void setFromtime(Timestamp fromtime) {
		this.fromtime = fromtime;
	}

	public Timestamp getTotime() {
		return totime;
	}

	public void setTotime(Timestamp totime) {
		this.totime = totime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
