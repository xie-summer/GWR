package com.gewara.web.action.inner.util;

import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.util.DateUtil;
import com.gewara.xmlbind.ticket.SynchPlayItem;
import com.gewara.xmlbind.ticket.TicketRoom;

public abstract class TicketRemoteUtil {

	public static final String asynchLockOrderUrl = "asynch/getRemoteLockSeat";			//异步获取锁定座位
	
	public static final String lockOrderUrl = "/inner/lockSeat.xhtml";					//下单锁座
	public static final String unLockSeatUrl = "/inner/releaseSeat.xhtml";				//解锁座位
	public static final String fixOrderUrl = "/inner/confirmOrder.xhtml";				//确认订单
	public static final String cancelOrderUrl = "/inner/cancelOrder.xhtml";				//退票
	public static final String lockSeatUrl = "/inner/getLockSeatList.xhtml";			//场次锁定座位
	public static final String cacheSeatUrl = "/inner/getLockSeatListFromCache.xhtml";	//场次缓存锁定座位
	public static final String playItemUrl = "/inner/getPlayItem.xhtml";				//
	public static final String cinemaPlayItemUrl = "/inner/getCinemaPlayItem.xhtml";	//
	public static final String roomUrl = "/inner/getCinemaRoom.xhtml";					//
	public static final String roomSeatUrl = "/inner/getRoomSeat.xhtml";				//
	public static final String getRemoteOrderUrl = "/inner/getRemoteOrder.xhtml";		//查询订单
	public static final String checkOrderUrl = "/inner/checkRemoteOrder.xhtml";			//检查订单
	public static final String featurePriceUrl = "/inner/featurePrice.xhtml";			//设置成本价
	public static final String createRemoteOrderUrl = "/inner/createRemoteOrder.xhtml";	//创建订单
	public static final String backRemoteOrderUrl = "/inner/backRemoteOrder.xhtml";		//影院退票
	public static final String planSiteStatistcUrl = "/inner/getPlanSiteStatistc.xhtml";
	public static final String getMpiSeatUrl = "/inner/getMpiSeat.xhtml";
	public static final String getMtxSellNumUrl = "/inner/getMtxSellNum.xhtml";
	
	//万达相关~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static final String getWdRemoteOrderUrl = "/inner/getWdRemoteOrder.xhtml";					//万达影院订单
	public static final String createWdRemoteOrderUrl = "/inner/createWdRemoteOrder.xhtml";	//万达影院订单
	public static final String unlockWdOrderUrl = "/inner/unlockWdOrder.xhtml";
	public static final String getWdMemberUrl = "/inner/getWdMember.xhtml";
	public static final String getWdWapParamUrl = "/inner/getWdParam.xhtml";
	public static final String getWdOrderList = "/inner/getWdOrderList.xhtml";//adddate
	
	public static void copyMoviePlayItem(MoviePlayItem mpi, SynchPlayItem playItem){
		mpi.setPrice(playItem.getPrice());
		mpi.setEdition(playItem.getEdition());
		mpi.setLanguage(playItem.getLanguage());
		mpi.setMovieid(playItem.getMovieid());
		mpi.setCinemaid(playItem.getCinemaid());
		mpi.setLowest(playItem.getLowest());
		mpi.setRoomnum(playItem.getRoomnum());
		Timestamp playtime = playItem.getPlaytime();
		Date playdate = DateUtil.getBeginningTimeOfDay(new Date(playtime.getTime()));
		mpi.setPlaydate(playdate);
		mpi.setPlaytime(DateUtil.format(playtime, "HH:mm"));
	}
	
	public static void copyCinemaRoom(CinemaRoom room, TicketRoom ticketRoom){
		room.setLinenum(ticketRoom.getLinenum());
		room.setRanknum(ticketRoom.getRanknum());
		room.setSeatnum(ticketRoom.getSeatnum());
		room.setRoomtype(ticketRoom.getRoomtype());
	}
}
