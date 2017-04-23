package com.gewara.xmlbind.activity;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class CountyCountList extends BaseObjectListResponse<CountyCount> {
	
	private List<CountyCount> countyCountList = new ArrayList<CountyCount>();

	public List<CountyCount> getCountyCountList() {
		return countyCountList;
	}

	public void setCountyCountList(List<CountyCount> countyCountList) {
		this.countyCountList = countyCountList;
	}
	
	public void addCountyCount(CountyCount countyCount){
		this.countyCountList.add(countyCount);
	}

	@Override
	public List<CountyCount> getObjectList() {
		return countyCountList;
	}
}
