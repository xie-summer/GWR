package com.gewara.xmlbind.drama;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class BusDramaList extends BaseObjectListResponse<BusDrama> {
	
	private  List<BusDrama> busDramaList = new ArrayList<BusDrama>();

	public List<BusDrama> getBusDramaList() {
		return busDramaList;
	}

	public void setBusDramaList(List<BusDrama> busDramaList) {
		this.busDramaList = busDramaList;
	}
	
	public void addBusDrama(BusDrama busDrama)
	{
		this.busDramaList.add(busDrama);
	}

	@Override
	public List<BusDrama> getObjectList() {
		return busDramaList;
	}

}
