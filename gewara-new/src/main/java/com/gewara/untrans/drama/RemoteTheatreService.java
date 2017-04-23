package com.gewara.untrans.drama;

import java.util.Date;
import java.util.List;

import com.gewara.api.gpticket.vo.ticket.FieldAreaSeatVo;
import com.gewara.api.gpticket.vo.ticket.ShowSeatVo;
import com.gewara.helper.UpdateDpiContainer;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface RemoteTheatreService {

	ErrorCode<List<TheatreField>> updateTheatreField(Long userid, Theatre theatre, final List<String> msgList);

	ErrorCode<List<TheatreRoom>> updateTheatreRoom(Long userid, TheatreField field, final List<String> msgList);
	
	ErrorCode<List<FieldAreaSeatVo>> updateTheatreRoomSeat(Long userid, TheatreRoom room, boolean refresh, final List<String> msgList);

	void updateDramaPlayItem(Long userid, Long theatreid, Date playdate, final List<String> msgList);
	
	ErrorCode updateDramaPlayItem(Long userid, Long theatreid, final List<String> msgList, int notUpdateWithMin);
	
	ErrorCode<List<ShowSeatVo>> refreshAreaSeat(Long userid, OpenDramaItem odi, TheatreSeatArea seatArea, boolean refresh, final List<String> msgList);
	
	ErrorCode<List<TheatreSeatArea>> updateTheateSeatArea(Long userid,UpdateDpiContainer container, DramaPlayItem dpi, final List<String> msgList);
	
	ErrorCode updateSeatPrice(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea);
	
	ErrorCode updateDisQuantity(Long userid, UpdateDpiContainer container, TheatreSeatArea seatArea);
	
	ErrorCode updateSeatPrice(Long userid, UpdateDpiContainer container, DramaPlayItem item, List<TheatreSeatArea> seatAreaList, final List<String> msgList);

	ErrorCode<List<String>> openDramPlayitem(DramaPlayItem dpi, Theatre theatre, Drama drama, TheatreProfile profile) throws OrderException;
	/**
	 * 代售模式，默认取消积分、券、活动等优惠
	 * @param odi
	 * @param drama
	 */
	void clearOdiPreferential(OpenDramaItem odi, Drama drama);
	
	ErrorCode<List<String>> refreshOpenTheatreSeat(DramaPlayItem dpi);
}
