package com.gewara.xmlbind.sport;

import java.io.Serializable;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteMemberCardType extends BaseInnerResponse implements Serializable{
	private static final long serialVersionUID = -7884310640149896066L;
	private String cardTypeUkey;
	private String cardTypeCode;
	private String cardType;
	private Integer money;
	private Integer overNum;
	private Integer reserve;
	private String description;
	private Integer validTime;
	private Integer price;
	private Integer discount;
	private String fitItem;
	private String belongVenue;
	private Long businessId;
	
	public RemoteMemberCardType(){
		
	}
	public String getCardTypeUkey() {
		return cardTypeUkey;
	}
	public void setCardTypeUkey(String cardTypeUkey) {
		this.cardTypeUkey = cardTypeUkey;
	}
	public String getCardTypeCode() {
		return cardTypeCode;
	}
	public void setCardTypeCode(String cardTypeCode) {
		this.cardTypeCode = cardTypeCode;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public Integer getMoney() {
		return money;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}
	
	public Integer getOverNum() {
		return overNum;
	}
	public void setOverNum(Integer overNum) {
		this.overNum = overNum;
	}
	public Integer getReserve() {
		return reserve;
	}
	public void setReserve(Integer reserve) {
		this.reserve = reserve;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getValidTime() {
		return validTime;
	}
	public void setValidTime(Integer validTime) {
		this.validTime = validTime;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getDiscount() {
		return discount;
	}
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
	public String getFitItem() {
		return fitItem;
	}
	public void setFitItem(String fitItem) {
		this.fitItem = fitItem;
	}
	public Long getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	public String getBelongVenue() {
		return belongVenue;
	}
	public void setBelongVenue(String belongVenue) {
		this.belongVenue = belongVenue;
	}
}
