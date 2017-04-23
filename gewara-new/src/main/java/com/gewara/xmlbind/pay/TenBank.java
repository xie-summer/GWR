package com.gewara.xmlbind.pay;

import com.alibaba.dubbo.common.utils.StringUtils;

public class TenBank {
	private String code;
	private String type;
	private String name;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getShortname(){
		if(StringUtils.isBlank(name)){
			return "";
		}
		return name.replace("-–≈”√ø®", "").replace("-¥¢–Óø®", "");
	}
}
