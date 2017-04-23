package com.gewara.xmlbind.ticket;

import java.sql.Timestamp;

public class WdOrder {
	private String snid;//万达订单id
	private Timestamp orderTime;//下单时间
	private String cinemaId;//万达影院id
	private String cinemaName;//影院名称
	private String seats;//座位
	private Integer seatNum;//座位数
	private Double ticketMoney;//金额
	private String filmName;//影片名称
	private String showDate;//放映日期
	private String showTime;//放映时间
	private String hallId;//影厅ID
	private String hallName;//影厅名称
	private Long cId;//影院id
	private String payMode;//有值 表示钱在万达付款
	
	public Long getcId() {
		return cId;
	}
	public void setcId(Long cId) {
		this.cId = cId;
	}
	public String getSnid() {
		return snid;
	}
	public void setSnid(String snid) {
		this.snid = snid;
	}
	public Timestamp getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(Timestamp orderTime) {
		this.orderTime = orderTime;
	}
	public String getCinemaId() {
		return cinemaId;
	}
	public void setCinemaId(String cinemaId) {
		this.cinemaId = cinemaId;
	}
	public String getCinemaName() {
		return cinemaName;
	}
	public void setCinemaName(String cinemaName) {
		this.cinemaName = cinemaName;
	}
	public String getSeats() {
		return seats;
	}
	public void setSeats(String seats) {
		this.seats = seats;
	}
	public Integer getSeatNum() {
		return seatNum;
	}
	public void setSeatNum(Integer seatNum) {
		this.seatNum = seatNum;
	}
	public Double getTicketMoney() {
		return ticketMoney;
	}
	public void setTicketMoney(Double ticketMoney) {
		this.ticketMoney = ticketMoney;
	}
	public String getFilmName() {
		return filmName;
	}
	public void setFilmName(String filmName) {
		this.filmName = filmName;
	}
	public String getShowDate() {
		return showDate;
	}
	public void setShowDate(String showDate) {
		this.showDate = showDate;
	}
	public String getShowTime() {
		return showTime;
	}
	public void setShowTime(String showTime) {
		this.showTime = showTime;
	}
	public String getHallId() {
		return hallId;
	}
	public void setHallId(String hallId) {
		this.hallId = hallId;
	}
	public String getHallName() {
		return hallName;
	}
	public void setHallName(String hallName) {
		this.hallName = hallName;
	}
	public String getPayMode() {
		return payMode;
	}
	public void setPayMode(String payMode) {
		this.payMode = payMode;
	}
}
