package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class SettleOrder extends BaseObject {
	private static final long serialVersionUID = 9116029141075063480L;
	private Long orderid;
	private Long credentialsId;
	private Timestamp paytime;
	
	public SettleOrder(){}
	
	public SettleOrder(Long orderid, Timestamp paytime, Long credentialsId){
		this.orderid = orderid;
		this.paytime = paytime;
		this.credentialsId = credentialsId;
	}

	public Long getOrderid() {
		return orderid;
	}

	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}

	public Long getCredentialsId() {
		return credentialsId;
	}

	public void setCredentialsId(Long credentialsId) {
		this.credentialsId = credentialsId;
	}

	public Timestamp getPaytime() {
		return paytime;
	}

	public void setPaytime(Timestamp paytime) {
		this.paytime = paytime;
	}

	@Override
	public Serializable realId() {
		return orderid;
	}

}
