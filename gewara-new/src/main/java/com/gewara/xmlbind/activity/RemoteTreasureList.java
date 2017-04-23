package com.gewara.xmlbind.activity;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteTreasureList extends BaseObjectListResponse<RemoteTreasure>{
	private  List<RemoteTreasure> treasureList = new ArrayList<RemoteTreasure>();

	public List<RemoteTreasure> getTreasureList() {
		return treasureList;
	}

	public void setTreasureList(List<RemoteTreasure> treasureList) {
		this.treasureList = treasureList;
	}
	public void addTreasure(RemoteTreasure treasure){
		this.treasureList.add(treasure);
	}

	@Override
	public List<RemoteTreasure> getObjectList() {
		return treasureList;
	}
}
