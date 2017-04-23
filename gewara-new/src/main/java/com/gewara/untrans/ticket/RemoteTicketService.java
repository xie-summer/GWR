package com.gewara.untrans.ticket;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.MpiSeat;
import com.gewara.xmlbind.ticket.SynchPlayItem;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;
import com.gewara.xmlbind.ticket.TicketRoom;
import com.gewara.xmlbind.ticket.TicketRoomSeatList;
import com.gewara.xmlbind.ticket.WdOrder;
import com.gewara.xmlbind.ticket.WdParam;

public interface RemoteTicketService {
	/**
	 * 提交订单并锁订座位
	 * @param orderid	订单号信息
	 * @param seqno		排片编号
	 * @param mobile	手机号
	 * @param seatList	座位信息
	 * @param priceList	座位号的成本价信息
	 * @param playtime HHmm，放映时间，用于验证
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> remoteLockSeat(TicketOrder order, String seqno, String mobile, List<String> seatList, List<Integer> priceList, String playtime);
	/**
	 * 创建远程订单
	 * @param orderid
	 * @param seqno
	 * @param mobile
	 * @param seatList
	 * @param priceList
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> createRemoteOrder(TicketOrder order, String seqno, String mobile, List<String> seatList);
	/**
	 * 取消订单信息
	 * @param orderid	订单号信息
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> remoteUnLockSeat(TicketOrder order);
	
	/**
	 * 确认订单信息
	 * @param orderid	订单号信息
	 * @param seqno		排片编号
	 * @param mobile	手机号
	 * @param seatList	座位信息
	 * @param priceList	座位号的成本价信息
	 * @param playtime HHmm，放映时间，用于验证
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> remoteFixOrder(TicketOrder order, String seqno, String mobile, int unitPrice, List<String> seatList, List<Integer> priceList, String playtime);
	
	/**
	 * 退票
	 * @param orderid
	 * @param description
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> remoteCancelOrder(TicketOrder order, String description);
	
	/**
	 * 查询订单状态
	 * @param orderid	订单号信息
	 * @param forceRefresh 
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> getRemoteOrder(TicketOrder order, boolean forceRefresh);
	
	/**
	 * 检查远程状态，如果是确认订单，则保存状态
	 * @param orderid
	 * @param forceRefresh
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> checkRemoteOrder(TicketOrder order);
	/**
	 * 获取排片锁定的座位信息
	 * @param seqno			远程序排片编号
	 * @return
	 */
	ErrorCode<String> getRemoteLockSeat(OpenPlayItem opi);
	
	/**
	 * 获取排片缓存锁定的座位信息
	 * @param seqno		远程序排片编号
	 * @return
	 */
	ErrorCode<String> getLockSeatListFromCache(OpenPlayItem opi);
	
	/**
	 * 获取排片信息
	 * @param updatetime	更新时间
	 * @return
	 */
	ErrorCode<List<SynchPlayItem>> getRemotePlayItemListByUpdatetime(Timestamp updatetime, Long cinemaid);
	
	/**
	 * 获取影院当前日期排片信息
	 * @param cinema		影院信息
	 * @param playdate		日期
	 * @return
	 */
	ErrorCode<List<SynchPlayItem>> getRemotePlayItemList(Cinema cinema, Date playdate);
	
	/**
	 * 获取影厅信息
	 * @param cinema	影院信息
	 * @return
	 */
	ErrorCode<List<TicketRoom>> getRemoteRoomList(Cinema cinema);
	
	/**
	 * 获取影厅座位图
	 * @param room	影厅信息
	 * @return
	 */
	ErrorCode<TicketRoomSeatList> getRemoteRoomSeatList(CinemaRoom room);
	
	
	/**
	 * 设置影院场次结算价
	 * @param seqno			场次编号
	 * @param costprice		结算价
	 * @return	
	 */
	ErrorCode<Integer> featurePrice(String seqno, Integer costprice);
	
	ErrorCode<TicketRemoteOrder> backRemoteOrder(Long orderid);

	List<Integer> getPriceList(TicketOrder order, int size);
	/**
	 * 统计场次卖出数量
	 * @param seqno
	 * @return
	 */
	ErrorCode<Integer> getPlanSiteStatistc(String seqno);
	/**
	 * 查询或创建万达订单
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> getWdRemoteOrder(String wdOrderId, String seqNo, Long cinemaId);
	ErrorCode<TicketRemoteOrder> createWdRemoteOrder(String seqno, String wdOrderId, Long orderid, Long cinemaId);
	/**
	 * 解锁wanda订单（我们订单系统还未生成）
	 * @param orderId
	 */
	boolean unlockWandaOrder(String orderId, Long cinemaId);
	ErrorCode<String> getWdUserId(String memberUkey);
	ErrorCode<WdParam> getWdParam(String memberUkey, String seqno);
	/**
	 * 获取排期座位
	 * @param seqno
	 * @return
	 */
	ErrorCode<List<MpiSeat>> getMpiSeat(MoviePlayItem mpi);
	
	ErrorCode<List<WdOrder>> getWDOrderList(Date addDate);
	
	/**
	 * 获取满天星售出数量
	 * @param mpi
	 * @return
	 */
	ErrorCode<Integer> getRemoteMtxSellNum(MoviePlayItem mpi);
}
