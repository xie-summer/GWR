/**
 * 
 */
package com.gewara.model.drama;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class DramaOrder2Seat extends BaseObject{
	private static final long serialVersionUID = -8603553369400710033L;
	private Long id;
	private Long orderid;
	private Long seatid;
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getSeatid() {
		return seatid;
	}
	public void setSeatid(Long seatid) {
		this.seatid = seatid;
	}
	public DramaOrder2Seat(){}
	public DramaOrder2Seat(Long orderid, Long seatid){
		this.orderid = orderid;
		this.seatid = seatid;
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
}
