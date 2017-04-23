package com.gewara.xmlbind.activity;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteActivityMpiList extends BaseObjectListResponse<RemoteActivityMpi>{
	private List<RemoteActivityMpi> activityMpiList = new ArrayList<RemoteActivityMpi>();

	public List<RemoteActivityMpi> getActivityMpiList() {
		return activityMpiList;
	}

	public void setActivityMpiList(List<RemoteActivityMpi> activityMpiList) {
		this.activityMpiList = activityMpiList;
	}
	public void addActivityMpi(RemoteActivityMpi activityMpi){
		this.activityMpiList.add(activityMpi);
	}
	@Override
	public List<RemoteActivityMpi> getObjectList() {
		return activityMpiList;
	}
}
