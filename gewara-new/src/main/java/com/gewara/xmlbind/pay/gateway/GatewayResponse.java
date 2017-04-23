package com.gewara.xmlbind.pay.gateway;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="GatewayResponse")
public class GatewayResponse {
	
	private String code;
	
	private String errMsg;
	
	private BindPayGateway gateway;
	

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public BindPayGateway getGateway() {
		return gateway;
	}

	public void setGateway(BindPayGateway gateway) {
		this.gateway = gateway;
	}

}
