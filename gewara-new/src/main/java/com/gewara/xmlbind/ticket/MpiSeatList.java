package com.gewara.xmlbind.ticket;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class MpiSeatList extends BaseObjectListResponse<MpiSeat> {
	private List<MpiSeat> mpiSeatList = new ArrayList<MpiSeat>();

	public List<MpiSeat> getMpiSeatList() {
		return mpiSeatList;
	}

	public void setMpiSeatList(List<MpiSeat> mpiSeatList) {
		this.mpiSeatList = mpiSeatList;
	}
	
	public void addMpiSeat(MpiSeat mpiSeat){
		this.mpiSeatList.add(mpiSeat);
	}


	@Override
	public List<MpiSeat> getObjectList() {
		return mpiSeatList;
	}

}
