package com.gewara.xmlbind.activity;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class CategoryCountList extends BaseObjectListResponse<CategoryCount>{
	private List<CategoryCount> cateCountList = new ArrayList<CategoryCount>();
	
	@Override
	public List<CategoryCount> getObjectList() {
		return cateCountList;
	}
	public void addCategoryCount(CategoryCount categoryCount){
		this.cateCountList.add(categoryCount);
	}
	public List<CategoryCount> getCateCountList() {
		return cateCountList;
	}

	public void setCateCountList(List<CategoryCount> cateCountList) {
		this.cateCountList = cateCountList;
	}
	
}
