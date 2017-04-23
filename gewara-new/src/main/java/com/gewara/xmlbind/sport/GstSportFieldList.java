/**
 * 
 */
package com.gewara.xmlbind.sport;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class GstSportFieldList extends BaseObjectListResponse{
	
	private List<GstSportField> sportFieldList = new ArrayList<GstSportField>();

	public List<GstSportField> getSportFieldList() {
		return sportFieldList;
	}

	public void setSportFieldList(List<GstSportField> sportFieldList) {
		this.sportFieldList = sportFieldList;
	}
	public void addSportField(GstSportField sportField){
		sportFieldList.add(sportField);
	}

	@Override
	public List getObjectList() {
		return sportFieldList;
	}
}
