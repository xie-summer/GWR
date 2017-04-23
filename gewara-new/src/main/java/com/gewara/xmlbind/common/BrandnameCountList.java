package com.gewara.xmlbind.common;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class BrandnameCountList extends BaseObjectListResponse<BrandnameCount> {
	
	private List<BrandnameCount> brandnameCountList = new ArrayList<BrandnameCount>();

	public List<BrandnameCount> getBrandnameCountList() {
		return brandnameCountList;
	}

	public void setBrandnameCountList(List<BrandnameCount> brandnameCountList) {
		this.brandnameCountList = brandnameCountList;
	}

	@Override
	public List<BrandnameCount> getObjectList() {
		return brandnameCountList;
	}
	
}
