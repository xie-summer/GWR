/**
 * 
 */
package com.gewara.bank;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * @author Administrator
 *
 */
public class SpsdoOrderQry {
	public static final Map<String, String> STATUS_MAP = new HashMap<String, String>();
	//01-成功 02-失败 03-未知 04-退款中 05-已退款
	static {
		STATUS_MAP.put("01", "成功");
		STATUS_MAP.put("02", "失败");
		STATUS_MAP.put("03", "未知");
		STATUS_MAP.put("04", "退款中");
		STATUS_MAP.put("05", "已退款");
	}
	private String code;
	private String message;
	private String orderNo;
	private String serialNo;
	private String orderAmount;
	private String payAmount;
	private String status;
	private String payTime;
	private String requestTime;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getSerialNo() {
		return serialNo;
	}
	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	public String getOrderAmount() {
		return orderAmount;
	}
	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}
	public String getPayAmount() {
		return payAmount;
	}
	public void setPayAmount(String payAmount) {
		this.payAmount = payAmount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPayTime() {
		return payTime;
	}
	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}
	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
	public boolean isPaid(){
		return StringUtils.equals(this.status, "01");
	}
}
