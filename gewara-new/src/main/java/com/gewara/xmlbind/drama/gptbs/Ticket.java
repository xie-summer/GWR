package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class Ticket extends BaseObject{
	private static final long serialVersionUID = -6581023678395472709L;
	private Long id;				//ticketPriceId
	private Long ticketLimit;		//限制数
	private Long ticketTotal;		//站票总量
	private Long venueAreaId;		//场馆区域id
	private Double price;			//价格
	private Long color;				//颜色
	private String description;		//描述
	private Long ticketCount;		//票数
	private Long scheduleAreaId;	//场次区域ID
	private Long ticketPriceId;		//票价ID
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
	public Long getTicketLimit() {
		return ticketLimit;
	}
	public void setTicketLimit(Long ticketLimit) {
		this.ticketLimit = ticketLimit;
	}
	public Long getTicketTotal() {
		return ticketTotal;
	}
	public void setTicketTotal(Long ticketTotal) {
		this.ticketTotal = ticketTotal;
	}
	public Long getVenueAreaId() {
		return venueAreaId;
	}
	public void setVenueAreaId(Long venueAreaId) {
		this.venueAreaId = venueAreaId;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Long getColor() {
		return color;
	}
	public void setColor(Long color) {
		this.color = color;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Long getTicketCount() {
		return ticketCount;
	}
	public void setTicketCount(Long ticketCount) {
		this.ticketCount = ticketCount;
	}
	public Long getScheduleAreaId() {
		return scheduleAreaId;
	}
	public void setScheduleAreaId(Long scheduleAreaId) {
		this.scheduleAreaId = scheduleAreaId;
	}
	public Long getTicketPriceId() {
		return ticketPriceId;
	}
	public void setTicketPriceId(Long ticketPriceId) {
		this.ticketPriceId = ticketPriceId;
	}
	
}
