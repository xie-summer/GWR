package com.gewara.xmlbind.activity;

import com.gewara.xmlbind.BaseInnerResponse;

public class CategoryCount extends BaseInnerResponse {
	private String category;
	private Integer count;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
