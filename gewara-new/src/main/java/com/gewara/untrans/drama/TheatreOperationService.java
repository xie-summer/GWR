package com.gewara.untrans.drama;

import java.util.List;

import com.gewara.api.gpticket.vo.ticket.DramaRemoteOrderVo;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.pay.BuyItem;
import com.gewara.support.ErrorCode;

public interface TheatreOperationService {

	ErrorCode unlockRemoteSeat(Long orderid);

	ErrorCode lockRemoteSeat(OpenDramaItem odi, DramaOrder order, String mobile, List<SellDramaSeat> seatList, List<BuyItem> itemList);

	ErrorCode setAndFixRemoteOrder(OpenDramaItem odi, DramaOrder order);

	ErrorCode lockRemotePrice(OpenDramaItem odi, DramaOrder order, String mobile, List<BuyItem> itemList);

	ErrorCode<DramaRemoteOrderVo> createDramaRemoteOrder(OpenDramaItem odi, DramaOrder order, String mobile, List<SellDramaSeat> seatList, List<BuyItem> itemList);

	ErrorCode backDramaRemoteOrder(Long userid, DramaOrder order, OpenDramaItem odi);

	ErrorCode releasePaidFailureOrderSeat(DramaOrder order, List<SellDramaSeat> seatList);
	
	void addLockSeatToQryItemResponse(Long areaid, List<SellDramaSeat> seatList);

	void removeLockSeatFromQryItemResponse(Long areaid, List<SellDramaSeat> seatList);

	ErrorCode<List<String>> updateRemoteLockSeat(TheatreSeatArea seatArea, int expireSeconds, boolean refresh);
	
	ErrorCode<List<String>> updateRemoteLockPrice(TheatreSeatArea seatArea, int expireSeconds, boolean refresh);
	
	ErrorCode<List<String>> updateRemoteLock(OpenDramaItem odi, TheatreSeatArea seatArea, int expireSeconds, boolean refresh);

}
