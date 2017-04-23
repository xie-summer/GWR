package com.gewara.xmlbind.ticket;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class SynchPlayItemList extends BaseObjectListResponse<SynchPlayItem> {
	private List<SynchPlayItem> synchPlayItemList = new ArrayList<SynchPlayItem>();

	public List<SynchPlayItem> getSynchPlayItemList() {
		return synchPlayItemList;
	}

	public void setSynchPlayItemList(List<SynchPlayItem> synchPlayItemList) {
		this.synchPlayItemList = synchPlayItemList;
	}
	
	public void addSynchPlayItem(SynchPlayItem synchPlayItem){
		this.synchPlayItemList.add(synchPlayItem);
	}

	@Override
	public List<SynchPlayItem> getObjectList() {
		return synchPlayItemList;
	}
}
