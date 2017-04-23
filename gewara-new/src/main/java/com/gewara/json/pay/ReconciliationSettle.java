package com.gewara.json.pay;

import java.io.Serializable;

/**
 * 银行对账mogon存储对象
 * @author gang.liu
 * 当前只用于江苏银行
 */
public class ReconciliationSettle implements Serializable{

	private static final long serialVersionUID = -5976004654721880152L;
	private String _id;
	private String tradeNo;//订单号
	private String addTime;//下单时间
	private String amount;//订单金额
	private String sysTraceNo;//银行订单号  或为银行订单系统跟踪号
	private String rspCd;//响应码
	private String settleDate;//清算日期
	private String authID;//预授权号
	private String payMethod;
	
	public String getPayMethod() {
		return payMethod;
	}
	public void setPayMethod(String payMethod) {
		this.payMethod = payMethod;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getSysTraceNo() {
		return sysTraceNo;
	}
	public void setSysTraceNo(String sysTraceNo) {
		this.sysTraceNo = sysTraceNo;
	}
	public String getRspCd() {
		return rspCd;
	}
	public void setRspCd(String rspCd) {
		this.rspCd = rspCd;
	}
	public String getSettleDate() {
		return settleDate;
	}
	public void setSettleDate(String settleDate) {
		this.settleDate = settleDate;
	}
	public String getAuthID() {
		return authID;
	}
	public void setAuthID(String authID) {
		this.authID = authID;
	}
}
