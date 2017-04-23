package com.gewara.xmlbind.pay;

import org.apache.commons.lang.StringUtils;

public class QryIpsOrder {
	private String messageType;
	private String msg;
	private String merBillNo; 	//>1101224191533799</pMerBillNo>
	private String ipsBillNo;		//>AT2010122512275749</pIpsBillNo>
	private String trdType;		//>01</pTrdType>
	private String trdAmt;			//>1.00</pTrdAmt>
	private String trdStatus;		//>02</pTrdStatus>
	public boolean isSuccess() {
		return StringUtils.equals(trdStatus, "02") && StringUtils.equals(trdType, "01");
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getMerBillNo() {
		return merBillNo;
	}
	public void setMerBillNo(String merBillNo) {
		this.merBillNo = merBillNo;
	}
	public String getIpsBillNo() {
		return ipsBillNo;
	}
	public void setIpsBillNo(String ipsBillNo) {
		this.ipsBillNo = ipsBillNo;
	}
	public String getTrdType() {
		return trdType;
	}
	public void setTrdType(String trdType) {
		this.trdType = trdType;
	}
	public String getTrdAmt() {
		return trdAmt;
	}
	public void setTrdAmt(String trdAmt) {
		this.trdAmt = trdAmt;
	}
	public String getTrdStatus() {
		return trdStatus;
	}
	public void setTrdStatus(String trdStatus) {
		this.trdStatus = trdStatus;
	}
}
