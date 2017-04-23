package com.gewara.xmlbind.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseInnerResponse;

public class TicketRoomSeatList extends BaseInnerResponse {
	private List<TicketRoomSeat> ticketRoomSeatList = new ArrayList<TicketRoomSeat>();
	private Timestamp updatetime;

	public List<TicketRoomSeat> getTicketRoomSeatList() {
		return ticketRoomSeatList;
	}

	public void setTicketRoomSeatList(List<TicketRoomSeat> ticketRoomSeatList) {
		this.ticketRoomSeatList = ticketRoomSeatList;
	}
	
	public void addTicketRoomSeat(TicketRoomSeat ticketRoomSeat){
		this.ticketRoomSeatList.add(ticketRoomSeat);
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	
	
}
