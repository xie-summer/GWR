package com.gewara.service.partner;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.untrans.monitor.ConfigTrigger;

public interface PartnerService extends ConfigTrigger{
	List<OpenPlayItem> getPartnerOpiList(ApiUser partner, String citycode, Long cinemaid, Long movieid, Date playdate);
	
	/**
	 * 查询指定电影开放场次的日期
	 * @param apiUser
	 * @param movieid
	 * @return List<Date>
	 */
	List<Date> getPlaydateList(ApiUser apiUser, String citycode, Long movieid);
	/**
	 * 获取某天开放购票的影片
	 * @param fyrq
	 * @return
	 */
	List<Movie> getOpenMovieListByDate(ApiUser partner, String citycode, Date playdate);
	List<Movie> getOpenMovieList(ApiUser partner, String citycode, Long cinemaid, Date playdate);

	/**
	 * 根据付款时间查询交易的订单列表
	 * @param apiUser
	 * @param paidtimeFrom
	 * @param paidtimeTo
	 * @return
	 */
	List<TicketOrder> getPaidOrderListByPaidtime(ApiUser apiUser, Timestamp paidtimeFrom, Timestamp paidtimeTo);
	/**
	 * 根据查询退款的订单列表
	 * @param apiUser
	 * @param refundtimeFrom
	 * @param refundtimeTo
	 * @return
	 */
	List<TicketOrder> getRefundOrderList(ApiUser apiUser, Timestamp refundtimeFrom, Timestamp refundtimeTo);
	/**
	 * 获取某部影片某天开放购票的影院
	 */
	List<Cinema> getOpenCinemaList(ApiUser partner, String citycode, Long movieid,Date playdate);
	List<PartnerCloseRule> getCloseRuleList();
}
