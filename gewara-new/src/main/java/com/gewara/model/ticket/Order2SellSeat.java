package com.gewara.model.ticket;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class Order2SellSeat extends BaseObject{
	private static final long serialVersionUID = -8716475355885387423L;
	private Long id;
	private Long orderid;
	private Long seatid;
	public Order2SellSeat(){}
	public Order2SellSeat(Long orderid, Long seatid) {
		this.orderid = orderid;
		this.seatid = seatid;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public Long getSeatid() {
		return seatid;
	}
	public void setSeatid(Long seatid) {
		this.seatid = seatid;
	}

}
