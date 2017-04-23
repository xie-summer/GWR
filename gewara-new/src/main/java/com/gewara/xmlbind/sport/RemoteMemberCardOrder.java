package com.gewara.xmlbind.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteMemberCardOrder extends BaseInnerResponse implements Serializable{
	private static final long serialVersionUID = 2719635886336187907L;
	private String tradeNo;
	private String ptTradeNo;
	private String partner;
	private String userUkey;
	private String mobile;
	private String sumMoney;
	private String settleAccountsSumMoney;
	private Timestamp orderTime;
	private String state;
	private String cardTypeUkey;
	private Integer memberCardNum;
	private String memberCardCode;
	
	private Integer overMoney;
	private Integer valid;
	private String fitItem;
	private String belongVenue;
	
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getPtTradeNo() {
		return ptTradeNo;
	}
	public void setPtTradeNo(String ptTradeNo) {
		this.ptTradeNo = ptTradeNo;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public String getUserUkey() {
		return userUkey;
	}
	public void setUserUkey(String userUkey) {
		this.userUkey = userUkey;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getSumMoney() {
		return sumMoney;
	}
	public void setSumMoney(String sumMoney) {
		this.sumMoney = sumMoney;
	}
	public String getSettleAccountsSumMoney() {
		return settleAccountsSumMoney;
	}
	public void setSettleAccountsSumMoney(String settleAccountsSumMoney) {
		this.settleAccountsSumMoney = settleAccountsSumMoney;
	}
	public Timestamp getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(Timestamp orderTime) {
		this.orderTime = orderTime;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCardTypeUkey() {
		return cardTypeUkey;
	}
	public void setCardTypeUkey(String cardTypeUkey) {
		this.cardTypeUkey = cardTypeUkey;
	}
	public Integer getMemberCardNum() {
		return memberCardNum;
	}
	public void setMemberCardNum(Integer memberCardNum) {
		this.memberCardNum = memberCardNum;
	}
	public String getMemberCardCode() {
		return memberCardCode;
	}
	public void setMemberCardCode(String memberCardCode) {
		this.memberCardCode = memberCardCode;
	}
	public Integer getOverMoney() {
		return overMoney;
	}
	public void setOverMoney(Integer overMoney) {
		this.overMoney = overMoney;
	}
	
	public String getFitItem() {
		return fitItem;
	}
	public void setFitItem(String fitItem) {
		this.fitItem = fitItem;
	}
	public String getBelongVenue() {
		return belongVenue;
	}
	public void setBelongVenue(String belongVenue) {
		this.belongVenue = belongVenue;
	}
	public Integer getValid() {
		return valid;
	}
	public void setValid(Integer valid) {
		this.valid = valid;
	}
}
