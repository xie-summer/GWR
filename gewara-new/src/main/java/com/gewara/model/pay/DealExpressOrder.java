package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DealExpressOrder extends BaseObject {
	private static final long serialVersionUID = -5599193994725265282L;
	private Long id;
	private Long orderid;
	private String tradeNo;
	private Long dealuser;
	private String dealtype;
	private String nickname;
	private String mobile;
	private String dealStatus;
	private Timestamp addtime;
	private String remark;
	
	public DealExpressOrder() {}
	
	public DealExpressOrder(Long orderid, String tradeNo, String nickname){
		this.orderid = orderid;
		this.tradeNo = tradeNo;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.nickname = nickname;
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
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public Long getDealuser() {
		return dealuser;
	}
	public void setDealuser(Long dealuser) {
		this.dealuser = dealuser;
	}
	public String getDealtype() {
		return dealtype;
	}
	public void setDealtype(String dealtype) {
		this.dealtype = dealtype;
	}
	public String getDealStatus() {
		return dealStatus;
	}
	public void setDealStatus(String dealStatus) {
		this.dealStatus = dealStatus;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
