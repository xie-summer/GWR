package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.model.pay.BuyItem;

public class TspSaleCount extends BaseObject {
	private static final long serialVersionUID = -3829272208926162427L;
	private Long id;
	private Long priceid;
	private Long dpid;
	private Integer sales;
	private Long orderid;
	private String status;
	private String ptype;
	private Timestamp validtime;
	public TspSaleCount(){
		
	}
	public TspSaleCount(TheatreSeatPrice price, Integer sales){
		this.priceid = price.getId();
		this.ptype = OdiConstant.PTYPE_P;
		this.dpid = price.getDpid();
		this.sales = sales;
		this.status = Status.N;
	}
	public TspSaleCount(DisQuantity dis, Integer sales){
		this.priceid = dis.getId();
		this.ptype = OdiConstant.PTYPE_Q;
		this.dpid = dis.getDpid();
		this.sales = sales;
		this.status = Status.N;
	}
	public TspSaleCount(BuyItem bi, DramaOrder order){
		if(bi.getDisid()!=null){
			this.priceid = bi.getDisid();
			this.ptype = OdiConstant.PTYPE_Q;
		}else {
			this.priceid = bi.getSmallitemid();
			this.ptype = OdiConstant.PTYPE_P;
		}
		this.dpid = bi.getRelatedid();
		this.sales = bi.getQuantity();
		this.status = Status.N;
		this.orderid = order.getId();
		this.validtime = order.getValidtime();
	}
	@Override
	public Serializable realId() {
		return priceid;
	}

	public Long getPriceid() {
		return priceid;
	}

	public void setPriceid(Long priceid) {
		this.priceid = priceid;
	}

	public Long getDpid() {
		return dpid;
	}

	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}

	public Integer getSales() {
		return sales;
	}

	public void setSales(Integer sales) {
		this.sales = sales;
	}

	public Long getOrderid() {
		return orderid;
	}

	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}


	public String getPtype() {
		return ptype;
	}

	public void setPtype(String ptype) {
		this.ptype = ptype;
	}

	public Timestamp getValidtime() {
		return validtime;
	}

	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public boolean hasDisquantity(){
		return StringUtils.equals(ptype, OdiConstant.PTYPE_Q);
	}
}
