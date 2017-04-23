package com.gewara.xmlbind.pay;

import java.sql.Timestamp;

public class AlipayNotifyBean {
	private String trade_no;			//支付宝订单号
	private String buyer_email;		//购买者Email
	private Timestamp gmt_create;		//该笔交易创建时的时间
	private Timestamp gmt_payment;	//该笔交易付款时的时间
	private String out_trade_no;		//自己订单号
	private String trade_status;		//TRADE_FINISHED
	private Double total_fee;			//支付金额
	private String notify_id;			
	public String getTrade_no() {
		return trade_no;
	}
	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}
	public String getBuyer_email() {
		return buyer_email;
	}
	public void setBuyer_email(String buyer_email) {
		this.buyer_email = buyer_email;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getTrade_status() {
		return trade_status;
	}
	public void setTrade_status(String trade_status) {
		this.trade_status = trade_status;
	}
	public Timestamp getGmt_create() {
		return gmt_create;
	}
	public void setGmt_create(Timestamp gmt_create) {
		this.gmt_create = gmt_create;
	}
	public Timestamp getGmt_payment() {
		return gmt_payment;
	}
	public void setGmt_payment(Timestamp gmt_payment) {
		this.gmt_payment = gmt_payment;
	}
	public Double getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(Double total_fee) {
		this.total_fee = total_fee;
	}
	public String getNotify_id() {
		return notify_id;
	}
	public void setNotify_id(String notify_id) {
		this.notify_id = notify_id;
	}
}
