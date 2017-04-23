package com.gewara.service.drama;

import java.util.List;

import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.service.OrderException;


public interface DramaProcessService{

	/**
	 * 换座位，产生新的订单
	 * @param odi
	 * @param newseatList
	 * @param oldOrder
	 * @param reChange
	 * @return
	 * @throws OrderException
	 */
	DramaOrder changeSeat(OpenDramaItem odi, List<OpenTheatreSeat> newseatList, DramaOrder oldOrder, boolean reChange, List<String> remoteLockList) throws OrderException;
	
	Integer getPaidFailureOrderCount();
	
	List<DramaOrder> getPaidUnfixOrderList(int from, int maxnum);
}
