package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
/**
 * @author acerge(acerge@163.com)
 * @since 6:31:46 PM Aug 13, 2009
 * 
 */
public class CheckRecord extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	public static final String CHECKTYPE_SETTLE = "settle";
	public static final String CHECKTYPE_QUERY = "query";
	public static final String STATUS_STEP1 = "step1";
	public static final String STATUS_STEP2 = "step2";
	private Long id;				//ID
	private Timestamp fromtime;		//上次结账时间
	private Timestamp checktime;	//结账时间
	private Long checkuser;			//操作人
	private Integer accountsum;		//所有账户剩余余额
	private Integer wabisum;		//瓦币剩余
	private String status;			//状态：是否检查完
	private String remark;			//备注
	
	public CheckRecord(){}
	
	public CheckRecord(Long userid, Timestamp fromtime, Timestamp checktime){
		this.fromtime = fromtime;
		this.checktime = checktime;
		this.checkuser = userid;
		this.accountsum = 0;
		this.status = STATUS_STEP1;
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
	public Timestamp getChecktime() {
		return checktime;
	}
	public void setChecktime(Timestamp checktime) {
		this.checktime = checktime;
	}
	public Long getCheckuser() {
		return checkuser;
	}
	public void setCheckuser(Long checkuser) {
		this.checkuser = checkuser;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Timestamp getFromtime() {
		return fromtime;
	}
	public void setFromtime(Timestamp fromtime) {
		this.fromtime = fromtime;
	}
	public Integer getAccountsum() {
		return accountsum;
	}
	public void setAccountsum(Integer accountsum) {
		this.accountsum = accountsum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getWabisum() {
		return wabisum;
	}

	public void setWabisum(Integer wabisum) {
		this.wabisum = wabisum;
	}

}
