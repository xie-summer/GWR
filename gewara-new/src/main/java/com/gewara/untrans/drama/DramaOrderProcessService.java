package com.gewara.untrans.drama;

import java.util.List;

import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface DramaOrderProcessService {

	ErrorCode reconfirmOrder(DramaOrder order, Long userid,	boolean isAuto, boolean reChange);
	
	ErrorCode confirmSuccess(DramaOrder order, Long userid, boolean isAuto);
	
	List<OpenTheatreSeat> getOriginalSeat(DramaOrder order, List<SellDramaSeat> seatList) throws OrderException;
}
