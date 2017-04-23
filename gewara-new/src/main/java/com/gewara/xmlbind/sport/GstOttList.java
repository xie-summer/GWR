/**
 * 
 */
package com.gewara.xmlbind.sport;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class GstOttList extends BaseObjectListResponse {
	private List<GstOtt> ottList = new ArrayList<GstOtt>();

	public List<GstOtt> getOttList() {
		return ottList;
	}

	public void setOttList(List<GstOtt> ottList) {
		this.ottList = ottList;
	}
	public void addOtt(GstOtt ott){
		this.ottList.add(ott);
	}

	@Override
	public List getObjectList() {
		return ottList;
	}
}
