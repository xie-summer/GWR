package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class BindMobile extends BaseObject {
	private static final long serialVersionUID = -8930386676391077252L;
	private String ukey;			//tag + mobile
	private String tag;
	private String mobile;
	private String checkpass;
	private String lastip;
	private Timestamp validtime;
	private Integer sendcount;		//发送次数
	private Integer checkcount;		//当前使用次数，重发时复位
	private Integer totalcheck;		//总使用次数
	private Integer version;
	public Timestamp getValidtime() {
		return validtime;
	}

	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}

	public BindMobile(){}
	
	public BindMobile(String tag, String mobile, String checkpass, String lastip) {
		this.ukey = tag+mobile;
		this.tag = tag;
		this.version = 0;
		this.checkcount = 0;
		this.sendcount = 0;
		this.totalcheck = 0;
		this.mobile = mobile;
		this.checkpass = checkpass;
		this.lastip = lastip;
	}

	public String getCheckpass() {
		return checkpass;
	}

	public void setCheckpass(String checkpass) {
		this.checkpass = checkpass;
	}

	@Override
	public Serializable realId() {
		return ukey;
	}
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public String getSmobile() {
		String m = this.mobile;
		return m.substring(0, 3) + "****" + m.substring(7, 11);
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getCheckcount() {
		return checkcount;
	}

	public void setCheckcount(Integer checkcount) {
		this.checkcount = checkcount;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getUkey() {
		return ukey;
	}

	public void setUkey(String ukey) {
		this.ukey = ukey;
	}

	public Integer getSendcount() {
		return sendcount;
	}

	public void setSendcount(Integer sendcount) {
		this.sendcount = sendcount;
	}

	public String getLastip() {
		return lastip;
	}

	public void setLastip(String lastip) {
		this.lastip = lastip;
	}

	public Integer getTotalcheck() {
		return totalcheck;
	}

	public void setTotalcheck(Integer totalcheck) {
		this.totalcheck = totalcheck;
	}
}
