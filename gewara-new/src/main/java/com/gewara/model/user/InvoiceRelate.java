package com.gewara.model.user;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class InvoiceRelate extends BaseObject {
	private static final long serialVersionUID = 742764466468793021L;
	private String tradeNo;
	private Long orderid;
	private Long invoiceid;
	private Long memberid;
	
	public InvoiceRelate(){}
	
	public InvoiceRelate(String tradeNo){
		this.tradeNo = tradeNo;
	}
	
	@Override
	public Serializable realId() {
		return tradeNo;
	}
	
	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public Long getOrderid() {
		return orderid;
	}

	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}

	public Long getInvoiceid() {
		return invoiceid;
	}

	public void setInvoiceid(Long invoiceid) {
		this.invoiceid = invoiceid;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

}
