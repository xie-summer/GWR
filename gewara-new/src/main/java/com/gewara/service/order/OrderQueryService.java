package com.gewara.service.order;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.command.OrderParamsCommand;
import com.gewara.command.SearchOrderCommand;
import com.gewara.model.api.CooperUser;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.TicketOrder;

public interface OrderQueryService{
	/**
	 *    @function 根据用户ID + 时间限定 查询订单记录, 不考虑状态, 含分页 
	 * 	@author bob.hu
	 *		@date	2011-04-26 11:04:04
	 */
	List<GewaOrder> getOrderListByMemberId(Long memberid, Integer days, int from, int maxnum);
	Integer getOrderCountByMemberId(Long memberid, Integer days);
	/**
	 * 或取当前待处理订单个数
	 * @return
	 */
	int getPaidFailureOrderCount();
	String getMemberOrderHis(Long memberid);
	/**
	 * 座位待处理订单
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<TicketOrder> getPaidUnfixOrderList(int from, int maxnum);
	/**
	 * 查询场次的所有订单号
	 * @param mpid
	 * @param status
	 * @return
	 */
	List<String> getTradeNoListByMpid(String orderType, Long mpid, String status);
	/**
	 * 某用户的当前有效订单(去掉重复的)
	 * @param memberId
	 * @return
	 */
	<T extends GewaOrder> List<T> getOrderListByMemberId(Class<T> clazz, Long memberId, String status, int days, int from, int maxnum);
	/**
	 * 返回用户最后未支付的订单
	 * @param memberid
	 * @return
	 */
	<T extends GewaOrder> T getLastUnpaidOrder(Long memberid);

	/**
	 * 搜索订单
	 * @param soc
	 * @return
	 */
	List<TicketOrder> getTicketOrderList(SearchOrderCommand soc);
	
	/**
	 * 查询场次的订单
	 * @param mpid
	 * @param status
	 * @return
	 */
	List<TicketOrder> getTicketOrderListByMpid(Long mpid, String status);
	/**
	 * 获取某场次的订单数量
	 * @param mpid
	 * @return
	 */
	Integer getTicketOrderCountByMpid(Long mpid);
	/**
	 * 获取用户订过票的影片
	 * @param memberid
	 * @param maxnum
	 * @return
	 */
	List<Movie> getMemberOrderMovieList(Long memberid,int maxnum);
	/**
	 * 获取用户订过票的影院
	 * @param memberid
	 * @param maxnum
	 * @return
	 */
	List<Cinema> getMemberOrderCinemaList( Long memberid, int maxnum);
	Integer getMemberOrderCinemaCount(Long memberid);
	/**
	 * 
	 * @param memberid
	 * @param relatedid
	 */
	Integer getMemberOrderCountByMemberid(Long memberid, Long relatedid);
	Integer getMemberOrderCountByMemberid(Long memberid, Long relatedid, Timestamp fromtime, Timestamp totime, String citycode, String pricategory);
	
	List<GewaOrder> getPreferentialOrder(Long memberid,List<Long> spIdList);
	/**
	 * 查找订单
	 * @param user
	 * @param soc
	 * @return
	 */
	List<TicketOrder> getTicketOrderList(CooperUser user, SearchOrderCommand soc);
	List<Map> getTicketOrderListByDate(CooperUser user, SearchOrderCommand soc);
	List<GewaOrder> getOrderOriginListByDate(CooperUser user,SearchOrderCommand soc);
	List<GewaOrder> getOrderAppsourceListByDate(CooperUser partner, Timestamp dateFrom, Timestamp dateTo, String appsource);
	
	/**
	 * 查询电影订单数量
	 * @param memberid
	 * @param fromtime
	 * @param totime
	 * @param citycode
	 * @param status
	 */
	Integer getMemberTicketCountByMemberid(Long memberid, Timestamp fromtime, Timestamp totime, String status, String citycode);
	
	/**
	 * 查询无优惠的运动订单数量		(没有使用积分、抵值、抵用券及优惠活动)
	 * @param memberid
	 * @param fromtime
	 * @param totime
	 * @param status
	 */
	Integer getNoPreferentialSportOrderCount(Long memberid, Timestamp fromtime, Timestamp totime, String status);
	
	List<GewaOrder> getTicketOrderListByPayMethod(Timestamp starttime,Timestamp endtime,String paymethod,String tradeNo);
	/**
	 * 获取指定影片+指定会员集合>已支付订单量
	 * @param memberids
	 * @param movieid
	 * @return
	 */
	Integer getMUOrderCountByMbrids(List<Long> memberids, Long movieid, Timestamp fromtime, Timestamp totime);

	/**
	 * 获取订单超时时间
	 * @param tradeNo
	 * @return
	 */
	Long getOrderValidTime(String tradeNo);
	Long getOrderValidTimeById(Long orderid);
	/**
	 * 查询指定影院订单数量
	 * @param cinemaId
	 * @param fromtime
	 * @param totime
	 * @param status
	 * @return
	 */
	Integer getTicketOrderCountByCinema(long cinemaId, Timestamp fromtime, Timestamp totime, String status);
	
	List<GewaOrder> getOrderList(OrderParamsCommand command, int from, int maxnum);
	Integer getOrderCount(OrderParamsCommand command);
	<T extends GewaOrder> List<T> getOrderList(Class<T> clazz, OrderParamsCommand command);
	<T extends GewaOrder> List<T> getOrderList(Class<T> clazz, OrderParamsCommand command, int from, int maxnum);
	<T extends GewaOrder> Integer getOrderCount(Class<T> clazz, OrderParamsCommand command);
	
	/**
	 * 查询卖品订单
	 * @param clazz
	 * @param command
	 * @param from
	 * @param maxnum
	 * @return
	 */
	<T extends BaseGoods> List<GoodsOrder> getGoodsOrderList(Class<T> clazz, OrderParamsCommand command, int from, int maxnum);
	
	/**
	 * 查询影票订单
	 * @param clazz
	 * @param command
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<TicketOrder> getTicketOrderList(OrderParamsCommand command, String place, String item, int from, int maxnum);
	/**
	 * 获取用户的第一个订单
	 * @param memberid
	 * @return
	 */
	TicketOrder getFirstTicketOrder(long memberid);

}
