/**
 * 
 */
package com.gewara.bank;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ChinaOrderQry {
	public static final Map<String, String> STATUS_MAP = new HashMap<String, String>();
	static {
		STATUS_MAP.put("1001", "消费交易成功");
		STATUS_MAP.put("1003", "退货交易成功");
	}
	private String responseCode;	//应答码，成功时为0
	private String merid;
	private String orderno;
	private String amount;
	private String transdate;
	private String transtype;
	private String status;
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getMerid() {
		return merid;
	}
	public void setMerid(String merid) {
		this.merid = merid;
	}
	public String getOrderno() {
		return orderno;
	}
	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getTransdate() {
		return transdate;
	}
	public void setTransdate(String transdate) {
		this.transdate = transdate;
	}
	public String getTranstype() {
		return transtype;
	}
	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isPaid(){
		return StringUtils.equals(status, "1001");
	}
}
