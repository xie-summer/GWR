package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class SpCode extends BaseObject {
	public static final String PASSPRE = "I";	//码前缀
	private static final long serialVersionUID = 7131577325572302638L;
	private Long id;
	private String codepass;	//加密后的密码
	private Integer version;	//
	private Long sdid;			//特价活动
	private Long memberid;		//用户ID	
	private String mobile;		//手机
	private Timestamp sendtime;	//发出时间
	private Long orderid;		//关联订单
	private Integer usedcount;	//使用次数
	private String temppass;
	public SpCode(){
	}

	public SpCode(String codepass, Long sdid){
		this.codepass = codepass;
		this.sdid = sdid;
		this.usedcount = 0;
		this.version = 0;
	}
	
	@Override
	public Serializable realId() {
		return codepass;
	}
	public String getCodepass() {
		return codepass;
	}
	public void setCodepass(String codepass) {
		this.codepass = codepass;
	}
	public Long getSdid() {
		return sdid;
	}
	public void setSdid(Long sdid) {
		this.sdid = sdid;
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
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public Timestamp getSendtime() {
		return sendtime;
	}
	public void setSendtime(Timestamp sendtime) {
		this.sendtime = sendtime;
	}

	public Integer getUsedcount() {
		return usedcount;
	}

	public void setUsedcount(Integer usedcount) {
		this.usedcount = usedcount;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String gainTemppass() {
		return temppass;
	}

	public void setTemppass(String temppass) {
		this.temppass = temppass;
	}
	
}
