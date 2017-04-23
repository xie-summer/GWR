package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PayGateway extends BaseObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5328656468568314767L;

	
	/**路由类型：无*/
	public static final String ROUTE_TYPE_NONE = "none";
	
	/**路由类型：所属城市*/
	public static final String ROUTE_TYPE_CITY = "city";
	
	/**路由类型：指定商户标识*/
	public static final String ROUTE_TYPE_MERCODE = "mercode";
	
	/**支付网关状态：未启用*/
	public static final String STATUS_NO_USE = "NO_USE";
	/**支付网关状态：使用中*/
	public static final String STATUS_IN_USE = "IN_USE";
	/**支付网关状态：废弃*/
	public static final String STATUS_DESUETUDE = "DESUETUDE";
	
	private Long id ;
	private String gatewayCode;	//支付网关
	private String gatewayName;	//支付网关名称
	private String supportBank;	//是否支持银行
	private String gatewayType;	//类型：PLATFORM：支付平台；BANK：银行直连；CARD：卡支付
	private String status;		//状态：NO_USE：未启用；IN_USE：使用中；DESUETUDE：废弃；
	private String bankTypeKey;	//银行类型key，这里只放特殊的，形如{"C":"信用卡","KJ":"快捷信用卡支付"}
	private Timestamp updateTime;	//同步时间
	private String routeStatus; //商户号路由状态：OPEN：开启；CLOSE：关闭；默认：CLOSE
	private Timestamp modifyTime;	//修改时间
	private String modifyUser;	//最后修改人
	
	public PayGateway(){
		this.updateTime = new Timestamp(System.currentTimeMillis());
		this.routeStatus = ROUTE_TYPE_NONE;
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

	public String getGatewayCode() {
		return gatewayCode;
	}

	public void setGatewayCode(String gatewayCode) {
		this.gatewayCode = gatewayCode;
	}

	public String getGatewayName() {
		return gatewayName;
	}

	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	public String getSupportBank() {
		return supportBank;
	}

	public void setSupportBank(String supportBank) {
		this.supportBank = supportBank;
	}

	public String getGatewayType() {
		return gatewayType;
	}

	public void setGatewayType(String gatewayType) {
		this.gatewayType = gatewayType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBankTypeKey() {
		return bankTypeKey;
	}

	public void setBankTypeKey(String bankTypeKey) {
		this.bankTypeKey = bankTypeKey;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getRouteStatus() {
		return routeStatus;
	}

	public void setRouteStatus(String routeStatus) {
		this.routeStatus = routeStatus;
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
