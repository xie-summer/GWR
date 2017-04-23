package com.gewara.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.api.OrderResult;
import com.gewara.model.api.Synch;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
public interface SynchService {
	List<OrderResult> getToDoOrderList(String citycode);
	Map<String, Long> getGewaNumByTimeCinema(final Long cinemaid, Timestamp starttime, Timestamp endtime);
	Map<String, Long> getSynchNumByTimeCinema(final Long cinemaid, Timestamp starttime, Timestamp endtime);
	/**
	 * 获取高峰场次
	 * @param cinemaId
	 * @param starttime
	 * @param endtime
	 */
	List<OpenPlayItem> getSynchPeakOpi(Long cinemaId,Timestamp starttime, Timestamp endtime);
	/**
	 * 获取高峰演出场次
	 * @param cinemaId
	 * @param starttime
	 * @param endtime
	 */
	List<OpenPlayItem> getSynchPeakOdi(Long id,Timestamp starttime, Timestamp endtime);
	/**
	 * 获取高峰时段
	 * @param 
	 * @param starttime
	 * @param endtime
	 */
	List<Map> getSynchPeakPeriod(Long id,Timestamp starttime, Timestamp endtime,String tag);
	/**
	 * 影院时间段内场次未取票订单列表
	 * @param starttime
	 * @param endtime
	 * @param cinemaid
	 * @return
	 */
	List<GewaOrder> getNotGetOrderByTimeCinema(Timestamp starttime, Timestamp endtime, Long cinemaid);
	
	/**
	 * 影院时间段内取票数
	 * @param starttime
	 * @param endtime
	 * @param cinemaid
	 * @return
	 */
	Integer getTicketGetNumByTimeCinema(Timestamp starttime, Timestamp endtime, Long cinemaid);

	/**
	 * 影院取票机实际订单数 和票数
	 * @param curtime
	 * @param addDay
	 * @param cinemaid
	 * @return
	 */
	Map<String,Long>  getSynchTotalOrderNumByTimeCinema(Long cinemaid, Timestamp starttime, Timestamp endtime);

	/**
	 * 未同步订单
	 * @param cinemaid
	 * @return
	 */
	List<GewaOrder> getNoSynchOrderByCinema(Long cinemaid);

	/**
	 * 同步监控影院机器列表
	 * @param citycode
	 * @return
	 */
	List<Synch> getSynchListByCityCode(String citycode, Class clazz);
	/**
	 * 未同步一体机信息
	 * @param citycode
	 * @param clazz
	 * @return
	 */
	List<Synch> getOffNetworkSynchListByCityCode(String citycode, Class clazz);
	
	List<TicketOrder> getOrderListByCinemaIdAndLasttime(Long cinemaid, Timestamp lasttime);
	List<GoodsOrder> getGoodsOrderListByRelatedidAndLasttime(Long relatedid, Timestamp lasttime,String goodsTag);
	List<GoodsOrder> getGoodsOrderListByPlaceidAndLasttime(Long placeid, Timestamp lasttime, String category);
	List<SportOrder> getOrderListBySportIdAndLasttime(Long sportid, Long sportItemId, Timestamp lasttime);
	List<Map<String, Object>> getOrderListByPlaceidAndLasttime(Long venueId, Timestamp lasttime);
	/**
	 * 同步监控剧院机器列表
	 * @param citycode
	 * @return
	 */
	List<TicketOrder> getOrderListByCinemaIdAndLasttime(Long cinemaid, Timestamp lasttime,int pageSize);
	
	Map<String, Long> getGewaNumByTimeSport(final Long sportid, Timestamp starttime, Timestamp endtime);
	Integer getSynchTotalOrderNumByTimeSport(final Long sportid, Timestamp starttime, Timestamp endtime);
	List<GewaOrder> getNotGetOrderByTimeSport(Long sportid,Timestamp starttime,Timestamp endtime);
	List<GewaOrder> getNoSynchOrderBySport(Long sportid,Timestamp starttime,Timestamp endtime);
	List<OrderResult> getToDoSportOrderList(String tradeno);
	
	/**
	 * 健身订单同步
	 */
	List<OrderNote> getOrderNoteList(Long placeid, Timestamp lasttime, int pageSize);
	/**
	 * 已取票的订单（流水号）
	 * @param tradenos
	 * @return
	 */
	List<String> getSerialnoList(String tradenos);
	/**
	 * 自定义票纸
	 * @param tradeNo
	 * @param member
	 * @param specialComents
	 * @return
	 */
	ErrorCode selfTicket(String tradeNo, Member member, String specialComents);
	/**
	 * 话剧订单统计（OrderNote）
	 * @param placeid
	 * @param statrtime
	 * @param endtime
	 * @return
	 */
	Map<String, Long> getOrderNoteCountByPlaceid(Long placeid, Timestamp stattime, Timestamp endtime);
	Map<String, Long> getOrderNoteSynchNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime);
	Integer getOrderNoteGetNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime);
	Map<String, Long> getOrderNoteNumByPlaceid(Long placeid, Timestamp statrtime, Timestamp endtime);
	Map<String,Long> getOrderNoteSynchTotalNumByPlaceid(Long placeid, Timestamp starttime, Timestamp endtime);
	List<OrderNote> getOrderNoteNotGetOrderByPlaceid(Timestamp starttime, Timestamp endtime, Long placeid);
	List<OrderNote> getOrderNoteNoSynchOrderByPlaceid(Long placeid);
	Timestamp getTakeTime(GewaOrder order);

}
