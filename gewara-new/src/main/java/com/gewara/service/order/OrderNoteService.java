package com.gewara.service.order;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.pay.OrderNote;
import com.gewara.service.BaseService;

public interface OrderNoteService extends BaseService {

	List<OrderNote> getOrderNoteByPlaceid(String placetype, Long placeid, Timestamp fromtime, Timestamp totime);
	List<OrderNote> getOrderNoteByPlaceid(String placetype, Long placeid, Timestamp fromtime, Timestamp totime, int from, int maxnum);
	Integer getOrderNoteCountByPlaceid(String placetype, Long placeid, Timestamp fromtime, Timestamp totime);

	List<OrderNote> getOrderNoteByItemid(String itemtype, Long itemid, Timestamp fromtime, Timestamp totime);
	List<OrderNote> getOrderNoteByItemid(String itemtype, Long itemid, Timestamp fromtime, Timestamp totime, int from, int maxnum);
	Integer getOrderNoteCountByItemid(String itemtype, Long itemid, Timestamp fromtime, Timestamp totime);

	List<OrderNote> getOrderNoteBySmallitemid(String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime);
	List<OrderNote> getOrderNoteBySmallitemid(String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime, int from, int maxnum);
	Integer getOrderNoteCountBySmallitemid(String smallitemtype, Long smallitemid, Timestamp fromtime, Timestamp totime);
	
	List<OrderNote> getOrderNoteByOrdertype(String ordertype, Timestamp fromtime, Timestamp totime);
	List<OrderNote> getOrderNoteByOrdertype(String ordertype, Timestamp fromtime, Timestamp totime, int from, int maxnum);
	Integer getOrderNoteCountByOrdertype(String ordertype, Timestamp fromtime, Timestamp totime);
	List<OrderNote> getOrderNoteListByPlaceids(String ordertype, String mobile, String checkpass, Timestamp fromtime, Timestamp totime,
			String placeid, String itemids, int from, int maxnum);
	int getOrderNoteCountByPlaceids(String ordertype, String mobile, String checkpass, Timestamp fromtime, Timestamp totime, String placeid,
			String itemids);

}
