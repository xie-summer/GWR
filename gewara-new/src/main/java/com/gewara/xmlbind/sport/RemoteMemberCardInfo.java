package com.gewara.xmlbind.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteMemberCardInfo extends BaseInnerResponse implements Serializable{
	private static final long serialVersionUID = -949592934453514029L;
	private String memberCardCode;
	private String name;
	private String sex;
	private String mobile;
	private Integer overMoney;
	private Timestamp valid;
	private String cardStatus;
	private String fitItem;
	private String belongVenue;
	private String cardTypeUkey;
	
	public String getMemberCardCode() {
		return memberCardCode;
	}
	public void setMemberCardCode(String memberCardCode) {
		this.memberCardCode = memberCardCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getCardStatus() {
		return cardStatus;
	}
	public void setCardStatus(String cardStatus) {
		this.cardStatus = cardStatus;
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
	public Integer getOverMoney() {
		return overMoney;
	}
	public void setOverMoney(Integer overMoney) {
		this.overMoney = overMoney;
	}
	public Timestamp getValid() {
		return valid;
	}
	public void setValid(Timestamp valid) {
		this.valid = valid;
	}
	public String getCardTypeUkey() {
		return cardTypeUkey;
	}
	public void setCardTypeUkey(String cardTypeUkey) {
		this.cardTypeUkey = cardTypeUkey;
	}
}
