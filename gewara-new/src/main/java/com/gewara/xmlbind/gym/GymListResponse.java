package com.gewara.xmlbind.gym;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class GymListResponse extends BaseObjectListResponse<RemoteGym> {
	
	private List<RemoteGym> gymList = new ArrayList<RemoteGym>();

	public List<RemoteGym> getGymList() {
		return gymList;
	}

	public void setGymList(List<RemoteGym> gymList) {
		this.gymList = gymList;
	}
	
	public void addGym(RemoteGym gym){
		this.gymList.add(gym);
	}

	@Override
	public List<RemoteGym> getObjectList() {
		return gymList;
	}
}
