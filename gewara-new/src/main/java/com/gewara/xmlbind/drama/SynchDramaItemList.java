package com.gewara.xmlbind.drama;

import java.util.ArrayList;
import java.util.List;

public class SynchDramaItemList {

	private List<SynchDramaItem> openDramaItemList = new ArrayList<SynchDramaItem>();

	public List<SynchDramaItem> getOpenDramaItemList() {
		return openDramaItemList;
	}

	public void setOpenDramaItemList(List<SynchDramaItem> openDramaItemList) {
		this.openDramaItemList = openDramaItemList;
	}
	
	public void addOpenDramaItem(SynchDramaItem dramaItem){
		this.openDramaItemList.add(dramaItem);
	}
}
