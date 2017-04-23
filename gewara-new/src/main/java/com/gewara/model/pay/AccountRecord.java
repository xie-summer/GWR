package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
/**
 * @author acerge(acerge@163.com)
 * @since 6:31:46 PM Aug 13, 2009
 */
public class AccountRecord extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	public static final String CHECKTYPE_SETTLE = "settle";
	public static final String CHECKTYPE_QUERY = "query";
	private Long accountid;			//账户ID
	private Long memberid;			//用户ID
	private String membername;		//用户名
	private Integer abanlance;		//账户余额
	private Integer lbanlance;		//上次余额
	private Integer charge;			//本次充值
	private Integer gewapay;		//本次消费
	private Integer refund;			//本次退款
	private Timestamp lasttime;		//
	private Timestamp updatetime;	//
	private String status;			//状态
	public AccountRecord(){}
	public AccountRecord(Long accountid, Long memberid, String membername){
		this.accountid = accountid;
		this.memberid = memberid;
		this.charge = 0;
		this.gewapay = 0;
		this.refund = 0;
		this.membername = membername;
	}
	@Override
	public Serializable realId() {
		return accountid;
	}
	public Long getAccountid() {
		return accountid;
	}
	public void setAccountid(Long accountid) {
		this.accountid = accountid;
	}
	public Integer getAbanlance() {
		return abanlance;
	}
	public void setAbanlance(Integer abanlance) {
		this.abanlance = abanlance;
	}
	public Integer getLbanlance() {
		return lbanlance;
	}
	public void setLbanlance(Integer lbanlance) {
		this.lbanlance = lbanlance;
	}
	public Integer getCharge() {
		return charge;
	}
	public void setCharge(Integer charge) {
		this.charge = charge;
	}
	public Integer getGewapay() {
		return gewapay;
	}
	public void setGewapay(Integer gewapay) {
		this.gewapay = gewapay;
	}
	public void addCharge(int scharge){
		this.charge += scharge;
	}
	public void addGewapay(int sgewapay){
		this.gewapay += sgewapay;
	}
	public void addRefund(int srefund){
		this.refund += srefund;
	}
	/**
	 * 账户是否归零平衡
	 * @return
	 */
	public int getZero(){
		return lbanlance + charge - gewapay - abanlance - refund;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Integer getRefund() {
		return refund;
	}
	public void setRefund(Integer refund) {
		this.refund = refund;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public Timestamp getLasttime() {
		return lasttime;
	}
	public void setLasttime(Timestamp lasttime) {
		this.lasttime = lasttime;
	}	
}
