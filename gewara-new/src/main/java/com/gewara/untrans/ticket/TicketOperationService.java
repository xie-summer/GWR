package com.gewara.untrans.ticket;

import java.util.Date;
import java.util.List;

import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.model.acl.User;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;
import com.gewara.util.HttpResultCallback;
import com.gewara.xmlbind.ticket.TicketRemoteOrder;

public interface TicketOperationService {
	ErrorCode<List<String>> updateRemoteLockSeat(OpenPlayItem opi);
	/**
	 * 获取并更新某场次锁定的座位
	 * @param opi
	 * @param expireSeconds 超时秒数
	 * @param room
	 * @param force 强制刷新
	 * @return
	 */
	ErrorCode<List<String>> updateRemoteLockSeat(OpenPlayItem opi, int expireSeconds, boolean force);
	ErrorCode<List<String>> getCachedRemoteLockSeat(OpenPlayItem opi, int expireSeconds);
	void asynchUpdateSeatLock(OpenPlayItem opi, boolean asynch, HttpResultCallback asynchCallback);
	ErrorCode<List<String>> updateRemoteLockSeatFromCache(OpenPlayItem opi);
	/**
	 * 异步调用座位图，前端及API使用
	 * @param opi
	 * @return
	 */
	ErrorCode<List<String>> updateLockSeatListAsynch(OpenPlayItem opi);
	ErrorCode lockRemoteSeat(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList);
	/**
	 * 创建远程订单
	 * @param opi
	 * @param order
	 * @param seatList
	 * @return
	 */
	ErrorCode createRemoteOrder(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList);
	ErrorCode cancelRemoteTicket(OpenPlayItem opi, TicketOrder order, Long userid);
	
	ErrorCode releasePaidFailureOrderSeat(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList);
	ErrorCode<Boolean> unlockRemoteSeat(TicketOrder order, List<SellSeat> seatList);
	ErrorCode<TicketRemoteOrder> setAndFixRemoteOrder(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList) throws OrderException;

	//void removeLockSeatFromQryResponse(Long mpid, List<SellSeat> seatList);
	/**
	 * 局部加入锁定座位到QryResponse中
	 * @param order
	 * @param seatList
	 */
	
	void addLockSeatToQryResponse(Long mpid, List<SellSeat> seatList);
	/**
	 * 只是查询订单，不保存数据
	 * @param order
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> getRemoteOrder(TicketOrder order, boolean forceRefresh);
	/**
	 * 检查订单状态，状态有变化会保存更改状态
	 * @param order
	 * @param forceRefresh
	 * @return
	 */
	ErrorCode<TicketRemoteOrder> checkRemoteOrder(TicketOrder order);
	
	/**
	 * 设置成本价
	 * @param mpi
	 * @param costprice
	 * @return
	 */
	ErrorCode updateCostPrice(String seqNo, Integer costprice);
	
	/**
	 * 确认远程退票
	 * @param user
	 * @param order
	 * @param opi
	 * @return
	 */
	ErrorCode backRemoteOrder(User user, TicketOrder order, OpenPlayItem opi);
	
	/**
	 * 通过更新时间获取排片
	 * @param cinemaid
	 * @param msgList
	 * @param notUpdateWithMin 几分钟内更新过不不更新！
	 */
	ErrorCode updateMoviePlayItem(UpdateMpiContainer container, Long cinemaid, List<String> msgList, int notUpdateWithMin);
	
	/**
	 * 更新抓取下来的排片
	 * @param cinemaid
	 * @param playdate
	 * @param msgList
	 */
	void updateMoviePlayItem(Long cinemaid, Date playdate, List<String> msgList);
	
	/**
	 * 预加载热点场次座位
	 */
	void preloadHotspotPmiCache();
}
