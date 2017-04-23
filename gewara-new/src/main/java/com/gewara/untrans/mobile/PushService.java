package com.gewara.untrans.mobile;

import java.sql.Timestamp;

import com.gewara.model.pay.TicketOrder;

public interface PushService {
	void saveTakeTicketAutoPush(Timestamp playTime, String cname,String cnAddress, String mnmae, Long mid,TicketOrder order);
	
	void saveFilmwatchRemindAutoPush(Timestamp playTime, String cname, String caddress, String mnmae,Long movieId, Long mid, String tradeNo);
	
	void saveSendWalaAutoPush(Timestamp endTime,String mnmae,Long mid,Long movieId, String tradeNo);
}
