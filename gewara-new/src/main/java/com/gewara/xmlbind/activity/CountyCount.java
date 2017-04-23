package com.gewara.xmlbind.activity;

import com.gewara.xmlbind.BaseInnerResponse;

public class CountyCount extends BaseInnerResponse {
	
	private String countycode;
	private Integer count;
	
	public String getCountycode() {
		return countycode;
	}
	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
	
}
