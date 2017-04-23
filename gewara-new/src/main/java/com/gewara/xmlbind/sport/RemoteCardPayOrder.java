package com.gewara.xmlbind.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteCardPayOrder extends BaseInnerResponse implements Serializable{
	private static final long serialVersionUID = 8495704696094283127L;
	private String tradeNo;
	private String ptTradeNo;
	private String cardCode;
	private Integer amount;
	private Timestamp addtime;
	private String status;
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
	public String getCardCode() {
		return cardCode;
	}
	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
