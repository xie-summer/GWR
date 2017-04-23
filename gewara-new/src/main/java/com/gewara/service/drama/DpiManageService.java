package com.gewara.service.drama;

import java.util.List;

import com.gewara.helper.DramaSeatStatusUtil;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.service.BaseService;

public interface DpiManageService extends BaseService {
	
	TheatreSeatArea updateSeatAreaStats(OpenDramaItem odi, TheatreSeatArea seatArea, List<String> remoteLockList, boolean isFinished);
	
	TheatreSeatArea updateTheatreSeatAreaStats(OpenDramaItem odi, TheatreSeatArea seatArea, List<String> remoteLockList, boolean isFinished);
	
	TheatreSeatArea updateTheatreSeatPriceStats(OpenDramaItem odi, TheatreSeatArea seatArea, List<String> remoteLockList, boolean isFinished);
	
	
	void refreshDramaOtherinfo(Long userid, Drama drama);
	
	int verifySeatAreaSeatLock(Long areaid);
	
	void updateAreaSeatMap(TheatreSeatArea seatArea, List<OpenTheatreSeat> openSeatList, List<String> hfhLockList, DramaSeatStatusUtil seatStatusUtil);
	String[] getAreaSeatMap(Long areaid);
}
