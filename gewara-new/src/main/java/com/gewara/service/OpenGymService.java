package com.gewara.service;

import java.util.Date;
import java.util.List;

import com.gewara.command.SearchOrderCommand;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.OrderContainer;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.gym.CardItem;
public interface OpenGymService {
	
	/**
	 * 
	 * @param gymCardItem
	 * @param quantity
	 * @param mobile
	 * @param speciallist
	 * @param startdate
	 * @param member
	 * @return
	 */
	ErrorCode<GymOrder> addGymOrder(CardItem cardItem, Integer quantity, String mobile, String speciallist, Date startdate, Member member, String origin);

	/**
	 * 
	 * @param memberid
	 * @param ukey
	 * @return
	 */
	GymOrder getLastPaidFailureOrder(Long memberid, String ukey);
	
	/**
	 * 
	 * @param memberid
	 * @param ukey
	 * @param gci
	 * @return
	 */
	GymOrder getLastUnpaidGymOrder(Long memberid, String ukey, Long gci);
	
	
	OrderContainer processOrderPay(GymOrder order) throws OrderException;
	ErrorCode processGymOrder(GymOrder order);
	/**
	 * 
	 * @param order
	 * @return
	 */
	ErrorCode<GymOrder> checkOrderCard(GymOrder order);

	
	/**
	 * 通过订单号、用户ID取消订单
	 * @param tradeNo
	 * @param memberid
	 * @param reason
	 * @return
	 */
	ErrorCode cancelGymOrder(String tradeNo, Long memberid, String status, String reason);
	
	
	/**
	 * 使用积分数据
	 * @param orderId		订单ID
	 * @param item			健身卡数据
	 * @param memberId		用户ID
	 * @param usePoint		积分数
	 * @return
	 */
	ErrorCode usePoint(Long orderId, CardItem item, Long memberId, int usePoint);
	
	/**
	 * 使用兑换券数据
	 * @param orderId		订单ID
	 * @param item			健身卡数据	
	 * @param card			兑换券数据
	 * @param memberid		用户ID
	 * @return
	 */
	ErrorCode<GymOrder> useElecCard(Long orderId, CardItem item, ElecCard card, Long memberid);
	
	/**
	 * 使用特价活动
	 * @param orderId		订单数据
	 * @param jsonValue 
	 * @param jsonKey 
	 * @param sd			特价活动数据
	 * @return
	 */
	ErrorCode<OrderContainer> useSpecialDiscount(Long orderId, SpecialDiscount sd, OrderCallback callback);
	
	/**
	 * 查询订单信息
	 * @param soc		
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<GymOrder> getGymOrderList(SearchOrderCommand soc, int from, int maxnum);
}
