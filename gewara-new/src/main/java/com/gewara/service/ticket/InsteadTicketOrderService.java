package com.gewara.service.ticket;

import java.util.List;

import com.gewara.model.acl.User;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.OrderException;


public interface InsteadTicketOrderService{
	
	TicketOrder addTicketOrder(OpenPlayItem opi, TicketOrder oldTicketOrder, List<Long> seatIdList, User user) throws OrderException;
	
}
