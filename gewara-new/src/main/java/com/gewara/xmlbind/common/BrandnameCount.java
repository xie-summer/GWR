package com.gewara.xmlbind.common;

import com.gewara.xmlbind.BaseInnerResponse;

public class BrandnameCount extends BaseInnerResponse {
	private String brandname;
	private Integer count;
	
	public String getBrandname() {
		return brandname;
	}
	public void setBrandname(String brandname) {
		this.brandname = brandname;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
	
}
