/**
 * 
 */
package com.gewara.service.gewapay;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.command.SearchRefundCommand;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.pay.AccountRefund;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.support.ErrorCode;

public interface RefundService{
	/**
	 * 统计该用户的不同退票原因的次数
	 * @param command
	 * @return
	 */
	List<Map> getRefundReason(SearchRefundCommand command);
	List<OrderRefund> getOrderRefundList(SearchRefundCommand command, String order, int from, int maxnum);
	/**
	 * 查询新旧结算价不同的退款订单
	 * @param command
	 * @param order
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<OrderRefund> getDifferentSettleOrderRefundList(SearchRefundCommand command, String order, int from, int maxnum);
	Integer getOrderRefundCount(SearchRefundCommand command);
	/**
	 * 申请未过期全额退款
	 * @param order
	 * @param opi
	 * @param userid
	 * @param status
	 * @return
	 */
	ErrorCode<OrderRefund> getCurFullTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status);
	/**
	 * 申请过期全额退款
	 * @param order
	 * @param opi
	 * @param userid
	 * @param status
	 * @return
	 */
	ErrorCode<OrderRefund> getExpFullTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status);
	/**
	 * 申请差价退款，订单要处于成功状态
	 * @param order
	 * @param opi
	 * @param userid
	 * @param status
	 * @return
	 */
	ErrorCode<OrderRefund> getSupplementTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status);
	/**
	 * 申请部分退款
	 * @param order
	 * @param opi
	 * @param userid
	 * @param status
	 * @return
	 */
	ErrorCode<OrderRefund> getPartTicketOrderRefund(TicketOrder order, OpenPlayItem opi, Long userid, String status);
	
	//话剧申请全额退款
	ErrorCode<OrderRefund> getFullDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status);
	//话剧申请差价退款，订单要处于成功状态
	ErrorCode<OrderRefund> getSupplementDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status);
	//话剧申请部分退款
	//ErrorCode<OrderRefund> getPartDramaOrderRefund(DramaOrder order, OpenDramaItem odi, Long userid, String status);
	//运动申请全额退款
	ErrorCode<OrderRefund> getFullSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status);
	//运动申请差价退款，订单要处于成功状态
	ErrorCode<OrderRefund> getSupplementSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status);
	//运动申请部分退款
	//ErrorCode<OrderRefund> getPartSportOrderRefund(SportOrder order, OpenTimeTable ott, Long userid, String status);
	
	
	ErrorCode<OrderRefund> getDramaOrderRefund(DramaOrder order, Long userid, String status);
	ErrorCode<OrderRefund> getSportOrderRefund(SportOrder order, Long userid, String status);
	ErrorCode<OrderRefund> getGymOrderRefund(GymOrder order, Long userid, String status);
	ErrorCode<OrderRefund> getGoodsOrderRefund(GoodsOrder order, Long userid, String status);
	ErrorCode<OrderRefund> getPubSaleOrderRefund(PubSaleOrder order, Long userid, String status);
	/**
	 * 退款申请提交账务
	 * @param refund
	 * @return
	 */
	ErrorCode<AccountRefund> submit2Financial(OrderRefund refund);
	List<OrderRefund> getSettleRefundList(String ordertype, Timestamp timefrom, Timestamp timeto, Long placeid);
}
