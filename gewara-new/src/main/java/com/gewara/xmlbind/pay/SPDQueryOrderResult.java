package com.gewara.xmlbind.pay;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SPDQueryOrderResult {
	
	private String transName;
	private String plain;
	private String signature;
	
	public String getTransName() {
		return transName;
	}
	public void setTransName(String transName) {
		this.transName = transName;
	}
	public Map<String,String> parsePlain(){
		String[] parameters = StringUtils.split(plain,"|");
		Map<String,String> parameterMap = new HashMap<String,String>();
		for(String param:parameters){
			String[] values = StringUtils.split(param,"=");
			if(values.length == 2){
				parameterMap.put(values[0], values[1]);
			}
		}
		return parameterMap;
	}
	public String getPlain() {
		return plain;
	}
	public void setPlain(String plain) {
		this.plain = plain;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
