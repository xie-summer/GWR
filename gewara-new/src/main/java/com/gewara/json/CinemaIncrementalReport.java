package com.gewara.json;

import java.io.Serializable;

public class CinemaIncrementalReport implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5157978732774422943L;

	private String _id;
	
	private String date;
	
	private Long cinemaId;
	
	private String citycode;
	
	private String proName;
	
	private String cinemaName;
	
	private Integer ticketCount; //购票数
	
	private Integer clickedtimes; //关注数
	
	private Integer collectedtimes;//收藏数
	
	private Integer walaCount;//哇啦数
	
	private Integer buyMarkCount;//评分用户数（购票）
	
	private Integer notBuyMarkCount;//评分用户数（未购票）

	public CinemaIncrementalReport(){}
	
	public CinemaIncrementalReport(String date,Long cinemaId,String citycode,String proName,String cinemaName){
		this.date = date;
		this.cinemaId = cinemaId;
		this.cinemaName = cinemaName;
		this.citycode = citycode;
		this.proName = proName;
	}
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getCinemaId() {
		return cinemaId;
	}

	public void setCinemaId(Long cinemaId) {
		this.cinemaId = cinemaId;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getProName() {
		return proName;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	public String getCinemaName() {
		return cinemaName;
	}

	public void setCinemaName(String cinemaName) {
		this.cinemaName = cinemaName;
	}

	public Integer getTicketCount() {
		return ticketCount;
	}

	public void setTicketCount(Integer ticketCount) {
		this.ticketCount = ticketCount;
	}

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public Integer getCollectedtimes() {
		return collectedtimes;
	}

	public void setCollectedtimes(Integer collectedtimes) {
		this.collectedtimes = collectedtimes;
	}

	public Integer getWalaCount() {
		return walaCount;
	}

	public void setWalaCount(Integer walaCount) {
		this.walaCount = walaCount;
	}

	public Integer getBuyMarkCount() {
		return buyMarkCount;
	}

	public void setBuyMarkCount(Integer buyMarkCount) {
		this.buyMarkCount = buyMarkCount;
	}

	public Integer getNotBuyMarkCount() {
		return notBuyMarkCount;
	}

	public void setNotBuyMarkCount(Integer notBuyMarkCount) {
		this.notBuyMarkCount = notBuyMarkCount;
	}
	
}
