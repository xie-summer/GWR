package com.gewara.xmlbind.api;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.sport.SportField;

public class ApiSportField {
	private List<SportField> sportFieldList = new ArrayList<SportField>();

	public List<SportField> getSportFieldList() {
		return sportFieldList;
	}

	public void setSportFieldList(List<SportField> sportFieldList) {
		this.sportFieldList = sportFieldList;
	}
	
	public void addSportField(SportField sportField){
		sportFieldList.add(sportField);
	}
	
}
