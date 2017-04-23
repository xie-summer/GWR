/**
 * 
 */
package com.gewara.bank;

import org.apache.commons.lang.StringUtils;

/**
 * @author Administrator
 *
 */
public class BcOrderQry {
	private String order;		//订单号
	private String orderDate;	//订单日期
	private String orderTime;	//订单日期
	private String amount;		//金额
	private String tranDate;	//支付日期
	private String tranTime;	//支付时间
	private String tranState;	//支付交易状态 1[成功]
	private String orderState;	//订单状态[0未支付、1已支付、2已撤销、3已部分退货、4退货处理中、5已全额退货]
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getTranDate() {
		return tranDate;
	}
	public void setTranDate(String tranDate) {
		this.tranDate = tranDate;
	}
	public String getTranTime() {
		return tranTime;
	}
	public void setTranTime(String tranTime) {
		this.tranTime = tranTime;
	}
	public String getTranState() {
		return tranState;
	}
	public void setTranState(String tranState) {
		this.tranState = tranState;
	}
	public String getOrderState() {
		return orderState;
	}
	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}
	public boolean isPaid(){
		return StringUtils.equals(tranState, "1");
	}
}
