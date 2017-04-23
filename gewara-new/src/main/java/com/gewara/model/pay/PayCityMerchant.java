package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PayCityMerchant extends BaseObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = -481387113451749535L;
	
	/**区域类型：P：省*/
	public static final String AREATYPE_P = "P";
	/**区域类型：C：城市*/
	public static final String AREATYPE_C = "C";
	
	private Long id ;
	private Long gatewayId;//网关配置ID
	private String areaCode;//区域代码
	private String areaType;//区域类型：P：省；C：城市；
	private String merchantCode;//商户号标识
	private Timestamp modifyTime;	//修改时间
	private String modifyUser;	//最后修改人
	
	public PayCityMerchant(){
		this.modifyTime = new Timestamp(System.currentTimeMillis());
	}
	
	public PayCityMerchant(String modifyUser){
		this();
		this.modifyUser = modifyUser;
	}
	
	@Override
	public Serializable realId() {
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

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getAreaType() {
		return areaType;
	}

	public void setAreaType(String areaType) {
		this.areaType = areaType;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public Timestamp getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}
}
