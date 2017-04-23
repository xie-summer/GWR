package com.gewara.service.ticket;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.order.GewaOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.WdOrder;

/**
 * 下单及其基本操作
 * @author gebiao(ge.biao@gewara.com)
 * @since Feb 1, 2013 7:40:50 PM
 */
public interface TicketOrderService extends GewaOrderService {
	int MAXSEAT_PER_ORDER = 5;

	OpenSeat getOpenSeatByLoc(Long mpid, String seatline, String seatrank);
	ErrorCode checkOrderSeat(TicketOrder order, List<SellSeat> seatList);
	ErrorCode validLoveSeat(List<OpenSeat> oseatList);
	/**
	 * 检查座位是否有问题
	 * @param oseatList
	 * @param oseatList
	 * @param sellSeatMap
	 * @param hfhLockList
	 * @return
	 */
	ErrorCode validateSeatLock(List<OpenSeat> oseatList, Map<Long/*id*/, SellSeat> sellSeatMap, List<String> hfhLockList);
	
	/**
	 * 创建SellSeat
	 * @param oseatList
	 * @return
	 */
	List<SellSeat> createSellSeat(List<OpenSeat> oseatList);
	/**
	 * 取消锁定失败订单
	 * @param tradeNo
	 * @param memberid
	 * @param status
	 * @param reason
	 * @return
	 */
	void cancelLockFailureOrder(TicketOrder order);
	/**
	 * @param tradeNo
	 * @param memberid
	 * @param status
	 * @param reason
	 * @return
	 */
	TicketOrder cancelTicketOrder2(String tradeNo, Long memberid, String status, String reason);
	/**
	 * 某场次最后一个未支付的订单
	 * @param ukey
	 * @param mpid
	 * @return
	 */
	TicketOrder getLastUnpaidTicketOrder(Long memberid, String ukey, long mpid);
	/**
	 * 处理订单支付，确认支付过的订单（用于银行接口回调、订单重新确认）
	 * paid_failure ----> paid_unfixed
	 * @param order
	 * @param opi
	 * @param seatList
	 * @return OrderContainer(discountList,spcounter,order,
	 * @throws OrderException
	 */
	OrderContainer processOrderPay(TicketOrder order, OpenPlayItem opi, List<SellSeat> seatList) throws OrderException;
	List<SellSeat> getOrderSeatList(Long orderid);
	
	ErrorCode<GoodsGift> addOrderGoodsGift(TicketOrder order, OpenPlayItem opi, Long goodsid, Integer quantity);
	/**
	 * 暂停售票
	 * @param opi
	 * @return
	 */
	ErrorCode checkPauseBooking(OpenPlayItem opi);
	void processSuccess(TicketOrder torder, List<SellSeat> seatList, Timestamp curtime);
	/**
	 * 生成购票订单
	 * @param opi
	 * @param seatidList
	 * @param memberid
	 * @param membername
	 * @param mobile
	 * @param point
	 * @param randomNum
	 * @param hfhLockList
	 * @param partnerid
	 * @return TicketOrderContainer(order,opi,seatList,oseatList,bindGift)
	 * @throws OrderException
	 */
	TicketOrderContainer addTicketOrder(OpenPlayItem opi, List<Long> seatidList, Long memberid, String membername, String mobile, Integer point, String randomNum, List<String> hfhLockList) throws OrderException;
	/**
	 * @param opi
	 * @param seatLabel
	 * @param member
	 * @param partner
	 * @param mobile
	 * @param checkpass
	 * @param hfhLockList
	 * @return
	 * @throws OrderException
	 */
	TicketOrderContainer addTicketOrder(OpenPlayItem opi, String seatLabel, Member member, ApiUser partner, String mobile, String randomNum, List<String> hfhLockList) throws OrderException;
	/**
	 * @param opi
	 * @param seatLabel
	 * @param member
	 * @param partner
	 * @param mobile
	 * @param checkpass
	 * @param hfhLockList
	 * @return
	 * @throws OrderException
	 */
	TicketOrderContainer addTicketOrder(OpenPlayItem opi, List<Long> seatidList, Member member, ApiUser partner, String mobile, String randomNum, List<String> hfhLockList) throws OrderException;
	/**
	 * 增加订单
	 * @param mpid
	 * @param seatidList
	 * @param apiUser
	 * @param mobile
	 * @param checkpass
	 * @return
	 * @throws OrderException
	 */
	TicketOrderContainer addPartnerTicketOrder(OpenPlayItem opi, List<Long> seatidList, ApiUser apiUser, 
			String mobile, String checkpass, String ukey, String userid, String paymethod, String paybank, List<String> hfhLockList) throws OrderException;
	TicketOrderContainer addPartnerTicketOrder(OpenPlayItem opi, String seatLabel, ApiUser apiUser, 
			String mobile, String checkpass, String ukey, String userid, String paymethod, String paybank, List<String> hfhLockList) throws OrderException;
	/**
	 * 合作伙伴的用户订单
	 * @param parentid
	 * @param ukey
	 * @param status
	 * @return
	 */
	List<TicketOrder> getTicketOrderListByUkey(Long parentid, String ukey, String status);
	/**
	 * 增加赠送的订单
	 * @param order
	 */
	ErrorCode<GoodsOrder> addBindGoodsOrder(TicketOrder torder, String randomNum);
	/**
	 * 验证座位合法性
	 * @param mpid
	 * @param seatLabel
	 * @param hfhLockList
	 * @return
	 */
	ErrorCode<String> isValidateSeatPosition(OpenPlayItem opi, String seatLabel, List<String> hfhLockList);
	String getOrderHis(String mobile);
	//void setOrderDescription(TicketOrder order, Collection<SellSeat> seatList, OpenPlayItem opi);
	/**
	 * 处理用户最后一笔订单：
	 * 1）有待处理订单，则返回错误信息
	 * 2）有未付款，则自动取消
	 * @param memberid
	 * @return
	 */
	ErrorCode processLastOrder(Long memberid, String ukey, String msg);
	
	/**
	 * 万达订单和格瓦拉订单对比
	 * @param date
	 * @return 返回格瓦拉系统有的订单，在万达系统中未同步到
	 */
	List<TicketOrder> wdOrderContrast(Date date,List<WdOrder> wdOrderList);
	/**
	 * 获取全局订单号
	 * @return
	 */
	String getTicketTradeNo();
	ErrorCode<TicketOrder> removeBuyItem(Long memberid, Long itemid);
}
