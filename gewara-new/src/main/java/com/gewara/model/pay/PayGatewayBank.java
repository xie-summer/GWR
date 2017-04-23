package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class PayGatewayBank extends BaseObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6345576359683946792L;
	
	private Long id;	//主键
	private Long gatewayId;	//支配配置ID
	private String gwraBankCode;	//格瓦银行代码
	private String bankName;	//银行名称
	private String bankType;	//银行类型，少数支付平台银行代码不一样，如支付宝，默认值为：DEFAULT
	private Timestamp updateTime;	//同步时间
	
	public PayGatewayBank(){
		this.updateTime = new Timestamp(System.currentTimeMillis());
	}
		
	public String getPayBank(){
		if(StringUtils.isBlank(bankType) || StringUtils.equals("DEFAULT", bankType)){
			return gwraBankCode;
		}
		return gwraBankCode + "_" + bankType;
	}
	
	@Override
	public Serializable realId() {
		// TODO Auto-generated method stub
		return id;
	}

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

	public String getGwraBankCode() {
		return gwraBankCode;
	}

	public void setGwraBankCode(String gwraBankCode) {
		this.gwraBankCode = gwraBankCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankType() {
		return bankType;
	}

	public void setBankType(String bankType) {
		this.bankType = bankType;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
