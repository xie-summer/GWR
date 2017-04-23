/**
 * 
 */
package com.gewara.model.sport;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author Administrator
 *
 */
public class SportOrder2TimeItem extends BaseObject{
	private static final long serialVersionUID = -8141731734618132712L;
	private Long id;
	private Long orderid;
	private Long otiid;
	
	public Long getOtiid() {
		return otiid;
	}
	public void setOtiid(Long otiid) {
		this.otiid = otiid;
	}
	public SportOrder2TimeItem(){
		
	}
	public SportOrder2TimeItem(Long orderid, Long otiid){
		this.orderid = orderid;
		this.otiid = otiid;
	}
	
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
