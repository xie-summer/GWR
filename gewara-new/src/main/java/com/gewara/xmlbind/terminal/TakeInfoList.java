package com.gewara.xmlbind.terminal;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class TakeInfoList extends BaseObjectListResponse<TakeInfo>{
	private List<TakeInfo> takeInfoList = new ArrayList<TakeInfo>();

	public List<TakeInfo> getTakeInfoList() {
		return takeInfoList;
	}

	public void setTakeInfoList(List<TakeInfo> takeInfoList) {
		this.takeInfoList = takeInfoList;
	}
	public void addTakeInfo(TakeInfo takeInfo){
		this.takeInfoList.add(takeInfo);
	}

	@Override
	public List<TakeInfo> getObjectList() {
		return takeInfoList;
	}
}
