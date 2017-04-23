package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PayInterfaceSwitch extends BaseObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7884261970859268505L;
	
	/**接口类型：OLD：老；*/
	public static final String INTERFACETYPE_OLD = "OLD";
	/**接口类型：NEW：新*/
	public static final String INTERFACETYPE_NEW = "NEW";

	private String gatewayCode;	//支付网关
	private String interfaceType;	//接口类型：OLD：老；NEW：新
	private Timestamp addTime;	//新增时间
	private Timestamp modifyTime;	//修改时间
	private String modifyUser;	//最后修改人
	
	public PayInterfaceSwitch(){
		this.addTime = new Timestamp(System.currentTimeMillis());
		this.modifyTime = new Timestamp(System.currentTimeMillis());
	}
	
	public PayInterfaceSwitch(String gatewayCode,String modifyUser){
		this();
		this.gatewayCode = gatewayCode;
		this.modifyUser = modifyUser;
	}

	@Override
	public Serializable realId() {
		return gatewayCode;
	}
	
	public String getGatewayCode() {
		return gatewayCode;
	}
	public void setGatewayCode(String gatewayCode) {
		this.gatewayCode = gatewayCode;
	}
	public String getInterfaceType() {
		return interfaceType;
	}
	public void setInterfaceType(String interfaceType) {
		this.interfaceType = interfaceType;
	}

	public Timestamp getAddTime() {
		return addTime;
	}

	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
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
