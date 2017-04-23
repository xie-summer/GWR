/** 
 */
package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Jun 28, 2013  11:39:52 AM
 */
public class RepeatingPayorder extends BaseObject{
	private static final long serialVersionUID = -895559460298444232L;
	private String recordId;
	private String tradeNo;
	private String payseqNo;
	private String payMethod;
	private String status;
	private Integer amount;
	private String successPayMethod;
	private Timestamp notifyTime;
	private String confirmUser;
	private Timestamp confirmTime;
	
	public RepeatingPayorder(){}
	
	public RepeatingPayorder(String successPayMethod, String tradeno, String payseqNo, String payMethod, Integer amount){
		this.recordId = "" + tradeno + "," + payseqNo;
		this.tradeNo = tradeno;
		this.payseqNo = payseqNo;
		this.payMethod = payMethod;
		this.successPayMethod = successPayMethod;
		this.amount = amount;
		this.notifyTime = DateUtil.getCurFullTimestamp();
		this.status = "N";
	}
	
	/**
	 * @return the successPayMethod
	 */
	public String getSuccessPayMethod() {
		return successPayMethod;
	}

	/**
	 * @param successPayMethod the successPayMethod to set
	 */
	public void setSuccessPayMethod(String successPayMethod) {
		this.successPayMethod = successPayMethod;
	}

	/**
	 * @return the confirmUser
	 */
	public String getConfirmUser() {
		return confirmUser;
	}
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param confirmUser the confirmUser to set
	 */
	public void setConfirmUser(String confirmUser) {
		this.confirmUser = confirmUser;
	}

	/**
	 * @return the confirmTime
	 */
	public Timestamp getConfirmTime() {
		return confirmTime;
	}

	/**
	 * @param confirmTime the confirmTime to set
	 */
	public void setConfirmTime(Timestamp confirmTime) {
		this.confirmTime = confirmTime;
	}


	@Override
	public Serializable realId() {
		return recordId;
	}

	/**
	 * @return the recordId
	 */
	public String getRecordId() {
		return recordId;
	}

	/**
	 * @param recordId the recordId to set
	 */
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	/**
	 * @return the tradeNo
	 */
	public String getTradeNo() {
		return tradeNo;
	}

	/**
	 * @param tradeNo the tradeNo to set
	 */
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	/**
	 * @return the payseqNo
	 */
	public String getPayseqNo() {
		return payseqNo;
	}

	/**
	 * @param payseqNo the payseqNo to set
	 */
	public void setPayseqNo(String payseqNo) {
		this.payseqNo = payseqNo;
	}

	/**
	 * @return the payMethod
	 */
	public String getPayMethod() {
		return payMethod;
	}

	/**
	 * @param payMethod the payMethod to set
	 */
	public void setPayMethod(String payMethod) {
		this.payMethod = payMethod;
	}

	/**
	 * @return the amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	/**
	 * @return the notifyTime
	 */
	public Timestamp getNotifyTime() {
		return notifyTime;
	}
	/**
	 * @param notifyTime the notifyTime to set
	 */
	public void setNotifyTime(Timestamp notifyTime) {
		this.notifyTime = notifyTime;
	}
}
