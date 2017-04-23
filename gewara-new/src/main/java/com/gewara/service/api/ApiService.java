package com.gewara.service.api;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.api.ApiUser;
import com.gewara.model.api.Synch;


public interface ApiService {
	List<ApiUser> getApiUserList(String status);
	Synch saveSynchWithCinema(Synch synch, Timestamp updatetime, Timestamp successtime, String ticketnum, String ip);
	void saveOrderResult(String[] tradeMap);
	Synch saveSynchGoodsWithCinema(Synch synch, Timestamp updatetime, Timestamp successtime, String ticketnum, String ip);
}
	
