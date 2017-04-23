package com.gewara.xmlbind.activity;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteActivityList extends BaseObjectListResponse<RemoteActivity>{
	private List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
	
	
	public void addActivity(RemoteActivity activity){
		this.activityList.add(activity);
	}


	public List<RemoteActivity> getActivityList() {
		return activityList;
	}


	public void setActivityList(List<RemoteActivity> activityList) {
		this.activityList = activityList;
	}


	@Override
	public List<RemoteActivity> getObjectList() {
		return activityList;
	}
}
