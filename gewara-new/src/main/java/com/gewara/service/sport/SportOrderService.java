package com.gewara.service.sport;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.command.SearchOrderCommand;
import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.SportOrderContainer;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportField;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.order.GewaOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;

public interface SportOrderService extends GewaOrderService {
	Map<Integer, List<Sport>> getProfileSportList();
	ErrorCode validateFieldLock(List<OpenTimeItem> otiList);
	ErrorCode checkOrderField(SportOrder order, List<OpenTimeItem> otiList);
	/**
	 * 最后一次未支付的订单
	 * @param ukey
	 * @return
	 */
	SportOrder getLastUnpaidSportOrder(Long memberid, String ukey, Long ottid);
	void cancelSportOrder(SportOrder order, Long memberid, String reason);
	ErrorCode<SportOrder> addSportOrder(OpenTimeTable ott, String fields, Long cardid, ErrorCode<RemoteMemberCardInfo> rmcode, String mobile, Member member, ApiUser partner) throws OrderException;
	ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer quantity, String mobile, Member member, ApiUser partner);
	ErrorCode<SportOrder> addSportOrder(OpenTimeSale ots) throws OrderException;
	ErrorCode<SportOrder> addSportOrder(Long otiid, String starttime, Integer time, Integer quantity, String mobile, Member member, ApiUser partner);
	Integer getSellTimeTableCount(Long otiid, String starttime);
	List<OpenTimeTable> getOttList(Long sportid, Long itemid, Date from, Date to, boolean open);
	Integer getOttCount(Long sportid, Long itemid, Date from, Date to, boolean open);
	List<SportItem> getOpenSportItemList(Long sportid, Date from, Date to, boolean open);
	List<SportOrder> getSportOrderList(SearchOrderCommand soc, int from, int maxnum);
	List<SportOrder> getValidSportOrderList(SearchOrderCommand soc, int from, int maxnum);
	List<SportOrder> getSportOrderList(SearchOrderCommand soc);
	List<SportField> getSportFieldList(Long sportid, Long itemid);
	List<SportField> getSportFieldList(Long ottid);
	List<SportField> getAllSportFieldList(Long ottid);
	OpenTimeTable getOtt(Long sportid, Long itemid, Date playdate);
	
	List<String> getPlayHourList(Long ottid, String status);
	/**
	 * @param sportid
	 * @param maxnum
	 * @return
	 */
	List<Long> getMemberidListBySportid(Long sportid, Timestamp addtime, int from, int maxnum);
	/**
	 * 根据用户查询订单
	 * @param memberid
	 * @param maxnum
	 * @return
	 */
	List<SportOrder> getOrderListByMemberid(Long memberid);
	/**
	 * 使用积分，进行折扣
	 * @param orderId
	 * @param memberId
	 * @param usePoint
	 * @return
	 */
	ErrorCode usePoint(Long orderId, Long memberId, int usePoint);
	/**
	 * 使用优惠券
	 * @param orderId
	 * @param card
	 * @param memberid
	 * @return
	 */
	ErrorCode<SportOrderContainer> useElecCard(Long orderId, ElecCard card, Long memberid);
	/**
	 * @param orderid
	 * @return
	 */
	//TODO:命名问题
	List<OpenTimeItem> getMyOtiList(Long orderid);
	String getMyOtiHour(Long orderid);
	/**
	 * 远程场次中场地的id
	 * @param otiList
	 * @return
	 */
	String getRemoteOtiids(List<OpenTimeItem> otiList);
	/**
	 * @param orderId
	 * @param sd
	 * @param jsonValue 
	 * @param jsonKey 
	 * @return
	 * @throws Exception
	 */
	ErrorCode<OrderContainer> useSpecialDiscount(Long orderId, SpecialDiscount sd, OrderCallback callback);
	
	Integer getSportOpenTimeTableCount(Long sportid);
	OrderContainer processOrderPay(SportOrder order, OpenTimeTable ott) throws OrderException;
	void processSportOrder(SportOrder order, OpenTimeTable ott, List<OpenTimeItem> otiList) throws OrderException;
	/**
	 * 特定状态的场次数量
	 */
	Integer getOpenTimeItemCount(Long ottid, String status, String hour);
	/**
	 * 删除场次
	 * @param ottid
	 * @return
	 */
	ErrorCode<String> delOtt(Long ottid);
	
	ErrorCode<SportOrder> processLastOrder(Long memberid, String ukey);
}
