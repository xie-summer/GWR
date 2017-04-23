package com.gewara.untrans.ticket;

import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.support.ErrorCode;


public interface OrderProcessService {
	/**
	 * 处理支付完成的订单：将订单从paid_failure*---->paid_success转换
	 * @param order
	 * @param from
	 * @param retry
	 * @return
	 */
	ErrorCode processOrder(GewaOrder order, String from, String retry);
	/**
	 * 重新确认订单
	 * @param order
	 * @param id
	 * @param isAuto
	 * @param reChange
	 * @return
	 */
	ErrorCode reconfirmOrder(TicketOrder order, Long id, boolean isAuto, boolean reChange);
	/**
	 * 确认订单
	 * @param order
	 * @param opi
	 * @param userid
	 * @param isAuto
	 * @return
	 */
	ErrorCode confirmSuccess(TicketOrder order, OpenPlayItem opi, Long userid, boolean isAuto);
	/**
	 * 支付订单
	 * @param orderId
	 * @param memberid
	 * @return
	 */
	ErrorCode<GewaOrder> gewaPayOrderAtServer(Long orderId, Long memberid, String checkvalue, boolean ignoreCheck);
	/**
	 * 会员卡支付
	 * @param orderId
	 * @param memberid
	 * @return
	 */
	ErrorCode<GewaOrder> memberCardPayOrderAtServer(Long orderId, Long memberid);
}
