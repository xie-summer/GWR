package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;

import com.gewara.model.BaseObject;
//场次价格
public class SchedulePrice extends BaseObject{
	private static final long serialVersionUID = -3760191427326507862L;
	private Long id;				//ID
	private Long ticketPriceId;		//票价ID
	private Long scheduleAreaId;	//场次区域Id
	private Integer ticketLimit;		//限制数
	private Integer ticketTotal;		//站票总量
	public Serializable realId() {
		return id;
	}
	
	public String getIdString(){
		return String.valueOf(id);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTicketPriceId() {
		return ticketPriceId;
	}
	public void setTicketPriceId(Long ticketPriceId) {
		this.ticketPriceId = ticketPriceId;
	}
	public Long getScheduleAreaId() {
		return scheduleAreaId;
	}
	public void setScheduleAreaId(Long scheduleAreaId) {
		this.scheduleAreaId = scheduleAreaId;
	}
	public Integer getTicketLimit() {
		return ticketLimit;
	}
	public void setTicketLimit(Integer ticketLimit) {
		this.ticketLimit = ticketLimit;
	}
	public Integer getTicketTotal() {
		return ticketTotal;
	}
	public void setTicketTotal(Integer ticketTotal) {
		this.ticketTotal = ticketTotal;
	}
	
}
