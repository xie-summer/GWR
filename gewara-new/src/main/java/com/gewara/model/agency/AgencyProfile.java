package com.gewara.model.agency;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author acerge(acerge@163.com)
 * @since 3:05:09 PM Jan 15, 2010
 */
public class AgencyProfile extends BaseObject{
	
	private static final long serialVersionUID = -2106584019043631599L;
	private Long id;				//与Cinema共用ID
	private String notifymsg1;		//取票短信
	private String notifymsg2;		//商户短信提醒
	private String mobiles;			//商户手机号
	private String status;			//开放状态

	@Override
	public Serializable realId() {
		return id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNotifymsg1() {
		return notifymsg1;
	}
	public void setNotifymsg1(String notifymsg1) {
		this.notifymsg1 = notifymsg1;
	}
	public String getNotifymsg2() {
		return notifymsg2;
	}
	public void setNotifymsg2(String notifymsg2) {
		this.notifymsg2 = notifymsg2;
	}
	public String getMobiles() {
		return mobiles;
	}
	public void setMobiles(String mobiles) {
		this.mobiles = mobiles;
	}
}
