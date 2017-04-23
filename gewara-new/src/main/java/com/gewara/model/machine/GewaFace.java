package com.gewara.model.machine;

import java.util.ArrayList;
import java.util.List;

public class GewaFace extends CommonFace{
	private static final long serialVersionUID = -1929931399805662276L;
	private String opentype;
	private List<SeatFace> seatList = new ArrayList();
	private List<StandFace> standList = new ArrayList();
	
	public List<StandFace> getStandList() {
		return standList;
	}
	public void setStandList(List<StandFace> standList) {
		this.standList = standList;
	}
	public List<SeatFace> getSeatList() {
		return seatList;
	}
	public void setSeatList(List<SeatFace> seatList) {
		this.seatList = seatList;
	}
	public String getOpentype() {
		return opentype;
	}
	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}
	
}
