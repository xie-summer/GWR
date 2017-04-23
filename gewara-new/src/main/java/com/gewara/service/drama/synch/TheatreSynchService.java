package com.gewara.service.drama.synch;

import java.util.List;

import com.gewara.api.gpticket.vo.ticket.FieldAreaSeatVo;
import com.gewara.api.gpticket.vo.ticket.ShowAreaVo;
import com.gewara.api.gpticket.vo.ticket.ShowItemVo;
import com.gewara.api.gpticket.vo.ticket.ShowPackPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowPriceVo;
import com.gewara.api.gpticket.vo.ticket.ShowSeatVo;
import com.gewara.helper.UpdateDpiContainer;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.service.BaseService;
import com.gewara.support.ErrorCode;

public interface TheatreSynchService extends BaseService{

	void updateShowItemVo(UpdateDpiContainer container, ShowItemVo itemVo, DramaPlayItem dpi, Theatre theatre, TheatreField field, List<String> msgList);

	ErrorCode updatePlayItem(DramaPlayItem dpi);
	
	ErrorCode<List<String>> refreshAreaSeat(Long userid, TheatreSeatArea seatArea, List<ShowSeatVo> seatVoList);
	
	Integer refreshSellSeatId(TheatreSeatArea seatArea);
	
	ErrorCode<List<TheatreSeatArea>> updateShowAreaVo(Long userid, UpdateDpiContainer container, DramaPlayItem dpi, List<ShowAreaVo> areaVoList, final List<String> msgList);
	
	ErrorCode updateSeatPriceVo(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea, List<ShowPriceVo> priceVoList);
	
	ErrorCode<List<String>> updateShowPackPriceVo(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea, List<ShowPackPriceVo> packPriceVoList);
	
	List<String> updateRoomSeatList(TheatreRoom room, List<FieldAreaSeatVo> seatVoList, boolean forceUpdate);
	
}
