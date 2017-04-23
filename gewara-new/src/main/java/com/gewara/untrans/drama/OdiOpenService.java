package com.gewara.untrans.drama;

import java.util.List;

import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface OdiOpenService {
	
	ErrorCode refreshAreaSeat(Long userid, OpenDramaItem odi, TheatreSeatArea seatArea, boolean refresh, final List<String> msgList);
	
	OpenDramaItem updateOdiStats(OpenDramaItem odi, int expireSeconds, boolean isFinished);

	void refreshDramaList(Long userid, String status);
	
	void asynchUpdateAreaStats(OpenDramaItem odi);
	
	ErrorCode saveOpenDramaItem(Long userid, Long dpid)throws OrderException;
}
