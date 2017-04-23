package com.gewara.xmlbind.ticket;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class TicketRoomList extends BaseObjectListResponse<TicketRoom>{
	private List<TicketRoom> ticketRoomList = new ArrayList<TicketRoom>();

	public List<TicketRoom> getTicketRoomList() {
		return ticketRoomList;
	}

	public void setTicketRoomList(List<TicketRoom> ticketRoomList) {
		this.ticketRoomList = ticketRoomList;
	}
	
	public void addTicketRoom(TicketRoom ticketRoom){
		this.ticketRoomList.add(ticketRoom);
	}

	@Override
	public List<TicketRoom> getObjectList() {
		return ticketRoomList;
	}
}
