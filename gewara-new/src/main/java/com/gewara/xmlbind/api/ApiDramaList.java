package com.gewara.xmlbind.api;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.drama.Drama;

public class ApiDramaList {
	
	private  List<Drama> dramaList = new ArrayList<Drama>();

	public List<Drama> getDramaList() {
		return dramaList;
	}

	public void setDramaList(List<Drama> dramaList) {
		this.dramaList = dramaList;
	}
	
	public void addDrama(Drama drama)
	{
		this.dramaList.add(drama);
	}

}
