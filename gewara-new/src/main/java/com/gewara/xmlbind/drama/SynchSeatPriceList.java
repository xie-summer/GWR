package com.gewara.xmlbind.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SynchSeatPriceList {

	private List<SynchSeatPrice> theatreSeatPriceList = new ArrayList<SynchSeatPrice>();
	private Timestamp updatetime;
	public List<SynchSeatPrice> getTheatreSeatPriceList() {
		return theatreSeatPriceList;
	}
	public void setTheatreSeatPrice(List<SynchSeatPrice> theatreSeatPriceList) {
		this.theatreSeatPriceList = theatreSeatPriceList;
	}
	public void addTheatreSeatPrice(SynchSeatPrice tsp){
		this.theatreSeatPriceList.add(tsp);
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	
	
}
