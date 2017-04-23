package com.gewara.xmlbind.gym;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteCoachListResponse extends BaseObjectListResponse<RemoteCoach> {
	private List<RemoteCoach> coachList = new ArrayList<RemoteCoach>();

	public List<RemoteCoach> getCoachList() {
		return coachList;
	}

	public void setCoachList(List<RemoteCoach> coachList) {
		this.coachList = coachList;
	}
	
	public void addCoach(RemoteCoach coach){
		this.coachList.add(coach);
	}

	@Override
	public List<RemoteCoach> getObjectList() {
		return coachList;
	}
}
