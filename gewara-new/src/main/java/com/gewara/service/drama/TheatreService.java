package com.gewara.service.drama;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreRoomSeat;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.web.action.drama.SearchTheatreCommand;



public interface TheatreService {

	/**
	 * 根据hotvalue查询theatre列表
	 * @param hotvalue
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Theatre> getTheatreListByHotvalue(String citycode, Integer hotvalue, int from, int maxnum);
	
	List<Theatre> getTheatreListBySearchComment(SearchTheatreCommand stc, String citycode,int from,int maxnum);
	
	List<Theatre> getTheatreListByUpdateTime(String citycode, Timestamp updatetime);
	
	List<TheatreRoom> getTheatreRoomList(Timestamp updatetime, String timefield);
	
	List<TheatreRoomSeat> getTheatreRoomSeatList(List<Long> roomList);
	
	List<OpenDramaItem> getOpenDramItemList(List didList, Timestamp updatetime);
	
	List<TheatreSeatPrice> getSeatPriceList(List dpidList, Timestamp updatetime);
	List<OpenTheatreSeat> getOpenSeatList(List odiidList);
}
