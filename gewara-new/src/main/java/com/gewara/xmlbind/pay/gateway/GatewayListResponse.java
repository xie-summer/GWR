package com.gewara.xmlbind.pay.gateway;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="GatewayListResponse")
public class GatewayListResponse {
	
	private String code;
	
	private String errMsg;
	
	@XmlElementWrapper(name = "gateways")
    @XmlElement(name = "gateway")
	private Set<BindPayGateway> gatewayList;
	
	public boolean isSuccess(){
		return StringUtils.equals("0000", code);
	}
	

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

	public Set<BindPayGateway> getGatewayList() {
		return gatewayList;
	}

	public void setGatewayList(Set<BindPayGateway> gatewayList) {
		this.gatewayList = gatewayList;
	}

}
