package com.gewara.model.pay;

import java.sql.Timestamp;

import com.gewara.constant.SmsConstant;

public class SMSRecord extends SMSRecordBase{
	private static final long serialVersionUID = -6741243368211585296L;
	public SMSRecord() {}
	
	public SMSRecord(String contact) {
		this.sendnum = 0;
		this.status = SmsConstant.STATUS_N;
		this.contact = contact;
	}
	public void copyFrom(SMSRecord another){
		this.relatedid = another.relatedid;
		this.tradeNo = another.tradeNo;
		this.contact = another.contact;
		this.content = another.content;
		this.sendtime = another.sendtime;
		this.validtime = another.validtime;
		this.smstype = another.smstype;
		this.sendnum = another.sendnum;
	}
	public SMSRecord(String tradeNo, String contact, String content, 
			Timestamp sendtime, Timestamp validtime, String smstype) {
		this(contact);
		this.tradeNo = tradeNo;
		this.contact = contact;
		this.content = content;
		this.sendtime = sendtime;
		this.validtime = validtime;
		this.smstype = smstype;
	}
	public SMSRecord(Long relatedid, String tradeNo, String contact, String content, 
			Timestamp sendtime, Timestamp validtime, String smstype) {
		this(tradeNo, contact, content, sendtime, validtime, smstype);
		this.relatedid = relatedid;
	}
}
