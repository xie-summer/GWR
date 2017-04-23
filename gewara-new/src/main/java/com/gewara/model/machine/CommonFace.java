package com.gewara.model.machine;

import java.io.Serializable;

public class CommonFace implements Serializable{
	private static final long serialVersionUID = -5213178447600454582L;
	protected String venueCnName;	// 场地中文名
	protected String venueEnName;	// 场地英文名
	protected String showPrice;	// 显示价格
	protected String voucherNo;	// 凭证编号
	protected String ticketType;// 出票类型
	protected String programStartTime;// 项目开始日期
	protected String userName;// 用户简称
	protected String programCnName;// 项目中文名
	protected String programEnName;// 项目英文名
	protected String scheduleCnName;// 场次中文名
	protected String scheduleEnName;// 场次英文名
	protected String tempPrientInfo;// 临时打印信息
	protected String playDate;// 场次日期
	protected String playTime;// 场次时间
	protected String stadiumCnName;// 场馆中文名
	protected String stadiumEnName;// 场馆英文名
	protected String cnAddress;// 场馆地址
	protected String enAddress;// 场馆英文地址
	protected String packInfo;			//套票信息
	protected String packInfo2;			//套票信息
	protected String packIntro;
	protected String greetings;		//个性化祝福语;
	public String getVenueCnName() {
		return venueCnName;
	}
	public void setVenueCnName(String venueCnName) {
		this.venueCnName = venueCnName;
	}
	public String getVenueEnName() {
		return venueEnName;
	}
	public void setVenueEnName(String venueEnName) {
		this.venueEnName = venueEnName;
	}
	public String getShowPrice() {
		return showPrice;
	}
	public void setShowPrice(String showPrice) {
		this.showPrice = showPrice;
	}
	public String getVoucherNo() {
		return voucherNo;
	}
	public void setVoucherNo(String voucherNo) {
		this.voucherNo = voucherNo;
	}
	public String getTicketType() {
		return ticketType;
	}
	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}
	public String getProgramStartTime() {
		return programStartTime;
	}
	public void setProgramStartTime(String programStartTime) {
		this.programStartTime = programStartTime;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getProgramCnName() {
		return programCnName;
	}
	public void setProgramCnName(String programCnName) {
		this.programCnName = programCnName;
	}
	public String getProgramEnName() {
		return programEnName;
	}
	public void setProgramEnName(String programEnName) {
		this.programEnName = programEnName;
	}
	public String getScheduleCnName() {
		return scheduleCnName;
	}
	public void setScheduleCnName(String scheduleCnName) {
		this.scheduleCnName = scheduleCnName;
	}
	public String getScheduleEnName() {
		return scheduleEnName;
	}
	public void setScheduleEnName(String scheduleEnName) {
		this.scheduleEnName = scheduleEnName;
	}
	public String getTempPrientInfo() {
		return tempPrientInfo;
	}
	public void setTempPrientInfo(String tempPrientInfo) {
		this.tempPrientInfo = tempPrientInfo;
	}
	public String getPlayDate() {
		return playDate;
	}
	public void setPlayDate(String playDate) {
		this.playDate = playDate;
	}
	public String getPlayTime() {
		return playTime;
	}
	public void setPlayTime(String playTime) {
		this.playTime = playTime;
	}
	public String getStadiumCnName() {
		return stadiumCnName;
	}
	public void setStadiumCnName(String stadiumCnName) {
		this.stadiumCnName = stadiumCnName;
	}
	public String getStadiumEnName() {
		return stadiumEnName;
	}
	public void setStadiumEnName(String stadiumEnName) {
		this.stadiumEnName = stadiumEnName;
	}
	public String getEnAddress() {
		return enAddress;
	}
	public void setEnAddress(String enAddress) {
		this.enAddress = enAddress;
	}
	public String getCnAddress() {
		return cnAddress;
	}
	public void setCnAddress(String cnAddress) {
		this.cnAddress = cnAddress;
	}
	public String getPackInfo() {
		return packInfo;
	}
	public void setPackInfo(String packInfo) {
		this.packInfo = packInfo;
	}
	public String getPackInfo2() {
		return packInfo2;
	}
	public void setPackInfo2(String packInfo2) {
		this.packInfo2 = packInfo2;
	}
	public String getPackIntro() {
		return packIntro;
	}
	public void setPackIntro(String packIntro) {
		this.packIntro = packIntro;
	}
	public String getGreetings() {
		return greetings;
	}
	public void setGreetings(String greetings) {
		this.greetings = greetings;
	}
	
}
