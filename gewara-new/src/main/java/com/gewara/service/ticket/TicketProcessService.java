package com.gewara.service.ticket;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.model.partner.CallbackOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

/**
 * 付款订单的后续处理相关
 * @author gebiao(ge.biao@gewara.com)
 * @since Feb 1, 2013 5:15:27 PM
 */
public interface TicketProcessService {
	/**
	 * 订单退款：只有待处理订单才能退款
	 * @param order
	 * @param seatList
	 * @param discountList
	 * @param refund
	 * @param userid
	 * @return
	 */
	ErrorCode<CallbackOrder> refundFullCurOrder(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid);
	/**
	 * 过期订单退款
	 * @param refund
	 * @param opi
	 * @param order
	 * @param userid 
	 * @return
	 */
	ErrorCode<CallbackOrder> refundFullExpiredOrder(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid);
	/**
	 * 部分退款，不退券，不取消折扣
	 * @param order
	 * @return
	 */
	ErrorCode refundPartExpiredOrder(OrderRefund refund, TicketOrder order, Long userid);
	/**
	 * 第三方退款
	 * @param order
	 * @param seatList
	 * @param discountList
	 * @param refund
	 * @param userid
	 * @return
	 */
	ErrorCode<CallbackOrder> hfhRefund(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid);
	ErrorCode<CallbackOrder> forceHfhRefund(OrderRefund refund, OpenPlayItem opi, TicketOrder order, Long userid);
	void updateBuytimes(Long memberid, String mobile, String orderType, Timestamp addtime);
	/**
	 * 同步成本价格
	 * @param order
	 * @param userid
	 * @return
	 */
	ErrorCode synchCostprice(TicketOrder order, Long userid);
	/**
	 * 更改订单的座位
	 * 1、只有状态为paid_failure的订单才能更改座位
	 * 2、新座位的价格必须和老座位的价格保持一致
	 * 3、同步更新优惠券
	 * 4、同步更新老座位的状态（如果被此订单占用的话）
	 * @param orderid
	 * @param newseat 格式为 5:03,5:04,6:3,6:4
	 * @return
	 * @throws OrderException 
	 */
	List<OpenSeat> getNewSeatList(TicketOrder order, OpenPlayItem opi, List<SellSeat> seatList, String newseat) throws OrderException;
	List<OpenSeat> getOriginalSeat(TicketOrder order, List<SellSeat> seatList) throws OrderException;
	TicketOrder changeSeat(OpenPlayItem opi, List<OpenSeat> oseatList, TicketOrder oldOrder, boolean reChange) throws OrderException;
	TicketOrder wdChangeSeat(OpenPlayItem opi, String oldTradeNo, TicketRemoteOrder wdOrder, String wdOrderId, String randomNum) throws OrderException;
}
