package com.gewara.xmlbind.partner;

import java.io.Serializable;

public class PartnerBoxPayUser implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String result;
	private String partnerUserId;
	private String parterId;//合作方ID
	private String iboxUserId;
	private String token;
	private String signType;//加密方式1,MD5  2 ,RSA
	private String signMsg;//签名内容
	
	//以下为订单部分 盒子支付返回属性
	private String orderSerial;//盒子订单流水号
	private String createTime;//生成订单时间
	private String bizType;//业务类型 1：电影票，2：飞机票，3：彩票
	private String orderNo;//订单号
	private String orderTime;//订单时间
	private String orderAmount;//订单金额，以分为单位
	private String callbackUrl;//支付成功后，商户需要盒子回调的接口路径
	private String orderStatus;//订单状态 Y：已支付；N：未支付
	private String payTime;
	private String cutOffTime;
	
	private String sysRefNo;
	
	public String getSysRefNo() {
		return sysRefNo;
	}
	public void setSysRefNo(String sysRefNo) {
		this.sysRefNo = sysRefNo;
	}
	
	public String getCutOffTime() {
		return cutOffTime;
	}
	public void setCutOffTime(String cutOffTime) {
		this.cutOffTime = cutOffTime;
	}
	public String getPayTime() {
		return payTime;
	}
	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}
	public String getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getPartnerUserId() {
		return partnerUserId;
	}
	public void setPartnerUserId(String partnerUserId) {
		this.partnerUserId = partnerUserId;
	}
	public String getParterId() {
		return parterId;
	}
	public void setParterId(String parterId) {
		this.parterId = parterId;
	}
	public String getIboxUserId() {
		return iboxUserId;
	}
	public void setIboxUserId(String iboxUserId) {
		this.iboxUserId = iboxUserId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getSignMsg() {
		return signMsg;
	}
	public void setSignMsg(String signMsg) {
		this.signMsg = signMsg;
	}
	public String getOrderSerial() {
		return orderSerial;
	}
	public void setOrderSerial(String orderSerial) {
		this.orderSerial = orderSerial;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public String getOrderAmount() {
		return orderAmount;
	}
	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	

}
