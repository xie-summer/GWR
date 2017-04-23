package com.gewara.xmlbind.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SynchTheatreSeatList {

	private List<SynchTheatreSeat> openTheatreSeatList = new ArrayList<SynchTheatreSeat>();
	private Timestamp updatetime;
	public List<SynchTheatreSeat> getOpenTheatreSeatList() {
		return openTheatreSeatList;
	}
	public void setOpenTheatreSeatList(List<SynchTheatreSeat> openTheatreSeatList) {
		this.openTheatreSeatList = openTheatreSeatList;
	}
	public void addOpenTheatreSeat(SynchTheatreSeat ots){
		this.openTheatreSeatList.add(ots);
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
}
