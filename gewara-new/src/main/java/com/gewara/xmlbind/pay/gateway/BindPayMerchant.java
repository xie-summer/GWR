package com.gewara.xmlbind.pay.gateway;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="merchant")
public class BindPayMerchant{

	private Long id;	//主键
	private Long gatewayId;	//支配配置ID
	private String merchantCode;	//商户号标识
	private String cityCode;	//城市
	private String acquiringBank;	//收单行
	private String description;	//商户号说明
	private String status;	//商户号状态：NO_USE：未启用；IN_USE：使用中；DESUETUDE：废弃；
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(Long gatewayId) {
		this.gatewayId = gatewayId;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getAcquiringBank() {
		return acquiringBank;
	}

	public void setAcquiringBank(String acquiringBank) {
		this.acquiringBank = acquiringBank;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
