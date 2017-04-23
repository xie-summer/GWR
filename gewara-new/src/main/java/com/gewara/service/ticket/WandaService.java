package com.gewara.service.ticket;

import java.util.List;
import java.util.Map;

import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

public interface WandaService {
	/**
	 * 根据万达锁定的订单创建新订单
	 * @param memberid
	 * @param mobile
	 * @param wdOrder 
	 * @param ticketMoney 总金额
	 * @return
	 */
	TicketOrderContainer createTicketOrder(OpenPlayItem opi, Long memberid, String membername, String ukey, String mobile, 
			TicketRemoteOrder wdOrder, String randomNum, String tradeNo, ApiUser partner) throws OrderException;
	List<SellSeat> checkAndCreateSeat(OpenPlayItem opi, String seatLabel) throws OrderException;
	Map<String, String> getKeyResult(String key);
	ErrorCode<String> getWebSeatPage(String wdUserId, String seqno, Long mpid, Long memberid, String membername, String mobile);
	ErrorCode<String> getWapSeatPage(String seqno, Long mpid, Long memberid, String membername, String mobile, String callback, String relkey);
	/**
	 * 客服用来重下订单
	 * @param seqno
	 * @param mpid
	 * @param memberid
	 * @param membername
	 * @param mobile
	 * @return
	 */
	ErrorCode<String> getProxySeatPage(String seqno, Long mpid, Long memberid, String membername, String mobile, String oldTradeNo);

	ErrorCode checkAllowChangeSeat(TicketOrder order);

	ErrorCode<Map<String, Object>> validCreateWdOrder(String key, Long memberid, String wdOrderId);
}
