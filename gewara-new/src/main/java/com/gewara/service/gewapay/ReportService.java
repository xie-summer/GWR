package com.gewara.service.gewapay;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.CinemaSettle;
import com.gewara.model.pay.GoodsOrder;

public interface ReportService{
	//根据放映时间查询
	List<Map> getTicketOrderDataByPlaytime(Long cinemaId,Long movieId, Timestamp timefrom, Timestamp timeto, String opentype);
	//根据下单时间查询
	List<Map> getTicketOrderDataByAddtime(Long cinemaId,Long movieId, Timestamp timefrom, Timestamp timeto, String opentype);
	List<CinemaSettle> getLastSettleList();
	List<Cinema> getBookingCinemaList();
	CinemaSettle getLastSettle(Long cinemaid);
	/**
	 * @param cinemaid
	 * @param timefrom
	 * @param timeto
	 * @param lasttime
	 * @param curtime
	 * @return Map(lastRefundList,curRefundList,orderMap,curRefundQuantity,lastRefundQuantity);
	 */
	Map getRefundData(Long cinemaid, Timestamp timefrom/*本次结账周期*/, Timestamp timeto, Timestamp lasttime/*上次结账时间*/, Timestamp curtime/*本次结账时间*/);
	/**
	 * @param cinemaId
	 * @param timefrom
	 * @param timeto
	 * @param movieid
	 * @param opentype
	 * @return Map(cinemaid, totalcount, totalcost, totalquantity,mpicount)
	 */
	Map getCinemaSummaryByPlaytime(Long cinemaId, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype);
	Map getCinemaSummaryByAddtime(Long cinemaId, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype);
	List<Map> getCinemaSummaryByPlaytime(List<Long> cinemaIds, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype);
	List<Map> getCinemaSummaryByAddtime(List<Long> cinemaIds, Timestamp timefrom, Timestamp timeto, Long movieid, String opentype);
	/**
	 * 根据消费时间统计，未消费的不记入
	 * @param cinemaId
	 * @param timefrom
	 * @param timeto
	 * @return
	 */
	List<GoodsOrder> getCinemaGoodsOrderByTaketime(Long cinemaId, Timestamp timefrom, Timestamp timeto);
	/**
	 * 根据下单时间统计
	 * @param cinemaId
	 * @param timefrom
	 * @param timeto
	 * @param isTake //true 为查只取过票的套餐
	 * @return
	 */
	List<GoodsOrder> getCinemaGoodsOrderByAddtime(Long cinemaId, Timestamp timefrom, Timestamp timeto,boolean isTake);
	/**
	 * 根据下单时间统计
	 * @param cinemaid
	 * @param timefrom
	 * @param timeto
	 * @param isTake //true 为查只取过票的套餐
	 * @return Map(cinemaid, totalcount, totalcost, totalquantity)
	 */
	Map getGoodsSummaryByAddtime(Long cinemaid, Timestamp timefrom, Timestamp timeto,boolean isTake);
	/**
	 * 根据消费时间统计，未消费的不记入
	 * @param cinemaid
	 * @param timefrom
	 * @param timeto
	 * @return
	 */
	Map getGoodsSummaryByTaketime(Long cinemaid, Timestamp timefrom, Timestamp timeto);
	
	Map getRefundOrderData(Long cinemaId,Long movieId, Timestamp timefrom, Timestamp timeto, String timeType);
}
