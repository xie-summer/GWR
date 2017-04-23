package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
import com.gewara.model.pay.Charge;

public class SellDeposit extends BaseObject {
	private static final long serialVersionUID = -6450021719520696386L;
	public static final String STATUS_NEW = "new";						//新座位
	public static final String STATUS_PAID = "paid";
	public static final String STATUS_PAID_SUCCESS = "paid_success";	//付款成功
	public static final String STATUS_PAID_USE = "paid_use";			//已使用
	
	private Long id;
	private Integer version;		//版本
	private Long otsid;
	private Long memberid;
	private String mobile;
	private Integer price;
	private Timestamp addtime;
	private Timestamp validtime;
	private String status;
	private Long chargeid;
	
	public SellDeposit(){}
	
	public SellDeposit(Charge charge, OpenTimeSale ots){
		this.chargeid = charge.getId();
		this.memberid = charge.getMemberid();
		this.otsid = ots.getId();
		this.price = charge.getTotalfee();
		this.addtime = charge.getAddtime();
		this.validtime = charge.getValidtime();
		this.status = STATUS_NEW;
		this.version = 0;
	}
	
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public Long getOtsid() {
		return otsid;
	}

	public void setOtsid(Long otsid) {
		this.otsid = otsid;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getValidtime() {
		return validtime;
	}

	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getChargeid() {
		return chargeid;
	}

	public void setChargeid(Long chargeid) {
		this.chargeid = chargeid;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public boolean hasStatus(String stats){
		if(StringUtils.isBlank(stats)){
			return false;
		}
		return StringUtils.equals(this.status, stats);
	}
}
