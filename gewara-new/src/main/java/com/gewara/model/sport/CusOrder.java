/**
 * 
 */
package com.gewara.model.sport;

import java.io.Serializable;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.BaseObject;

public class CusOrder extends BaseObject{
	private static final long serialVersionUID = -327229770834459606L;
	private Long id;
	private Long orderid;	//SportOrder id
	private String status;	//״̬
	private String response;
	public CusOrder(){
		this.status = OrderConstant.STATUS_NEW;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
