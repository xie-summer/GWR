package com.gewara.service.ticket;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;

public interface OpenPlayService {
	/**
	 * 获取影厅座位表
	 * @param roomId
	 * @return
	 */
	List<RoomSeat> getSeatListByRoomId(Long roomId);
	/**
	 * 增加一行座位
	 * @param roomId
	 * @return
	 */
	boolean addRowSeat(Long roomId);
	boolean addRankSeat(Long roomId);
	boolean deleteRowSeat(Long roomId);
	boolean deleteRankSeat(Long roomId);
	/**
	 * 根据坐标查找座位
	 * @param roomid
	 * @param line
	 * @param rank
	 * @return
	 */
	RoomSeat getRoomSeatByLocation(Long roomid, int line, int rank);
	List<List<String>> getRoomSeatNavigation(CinemaRoom room,String orderSeat);
	/**
	 * 更改行编号
	 * @param roomid
	 * @param lineno
	 * @param newline
	 * @return
	 */
	boolean updateSeatLine(Long roomid, int lineno, String newline);
	/**
	 * 手工锁定座位
	 * @param mpid
	 * @param seatId
	 * @param locktype:锁定类型
	 * @param lockreason：锁定理由
	 * @return
	 */
	ErrorCode<OpenSeat> lockSeat(Long mpid, Long seatId, String locktype, String lockreason);
	/**
	 * 获取某影厅座位数量
	 * @param roomid
	 * @return
	 */
	Integer getSeatCountByRoomId(Long roomid);
	/**
	 * @param cinemaId
	 * @param movieId
	 * @param timeFrom
	 * @param timeTo
	 * @param open 只查询能购票的场次
	 * @return
	 */
	List<OpenPlayItem> getOpiList(String citycode, Long cinemaId, Long movieId, Timestamp timeFrom, Timestamp timeTo, boolean open);
	List<OpenPlayItem> getOpiList(String citycode, Long cinemaId, Long movieId, Timestamp timeFrom, Timestamp timeTo, boolean open, int maxnum);
	/**
	 * 获取某场次的座位
	 * @param mpid
	 * @return
	 */
	List<OpenSeat> getOpenSeatList(Long mpid);
	List<OpenSeat> refreshOpenSeatList(Long mpid);
	/**
	 * 获取某场次卖出过的座位
	 * @param mpid
	 * @return
	 */
	List<SellSeat> getSellSeatListByMpid(Long mpid);
	/**
	 * 获取不同步的场次（影院已更改的），注意：language，edition被忽略
	 * @param cinemaid
	 * @return List(opid)
	 */
	List<Long> getUnsynchPlayItem(Long cinemaid);
	/**
	 * 开放预订的影院ID
	 * @param movieid 为空则返回所有开放预订的影院
	 * @return
	 */
	List<Long> getOpiCinemaidList(String citycode, Long movieid);
	/**
	 * 开放预订的片ID
	 * @param cinemaid 为空则返回所有开放预订的影片
	 * @return
	 */
	List<Long> getOpiMovieidList(String citycode, Long cinemaid);
	List<Cinema> getOpenCinemaList(String citycode, Long movieId, Date playdate, int from, int maxnum);
	/**
	 * 获取可以购票的日期
	 * @param citycode
	 * @param movieid
	 * @return List<Date>
	 */
	List<Date> getMovieOpenDateList(String citycode, Long movieid);
	List<Date> getMovieOpenDateListByCounycode(String counycode, Long movieid);
	/**
	 * 获取可以购票的日期
	 * @param cinemaid
	 * @return List<Date)>
	 */
	List<Date> getCinemaAndMovieOpenDateList(Long cinemaid, Long movieid);
	/**
	 * 查询城市排片日期
	 */
	List<Date> getMoviePlayItemDateList(String citycode, int from, int maxnum);
	/**
	 * 获取影厅座位图的字符串，行与行用“@@”分隔，列用“,”分隔
	 * @param room
	 * @return
	 */
	String getRoomSeatMapStr(CinemaRoom room);
	/**
	 * 或取最底限价
	 * @param movieid
	 * @param citycode
	 * @return
	 */
	MoviePrice getMoviePrice(Long movieid, String citycode);
	/**
	 * 通过影院ID和时间获取开放排片的电影，按开放排片数量排序
	 * @param cinemaId
	 * @param playDate
	 * @return
	 */
	List<Movie> getMovieListByCinemaIdAndPlaydate(Long cinemaId, Date playDate, int from, int maxnum);
	
	/**
	 * 通过影院ID和电影ID查询当前影院该影片的排片日期及数量
	 */
	List<Date> getMovieOfCinemaOpenDateList(Long cinemaid,Long movieid);
	
	List<Long> getHotPlayMovieIdList(String cityCode,String order);
	String[] getOpiSeatMap(Long mpid);
	/**
	 * 获取某影院当前可用设置器
	 * @param cinemaid
	 * @return
	 */
	List<AutoSetter> getValidSetterList(Long cinemaid,String status);
	/**
	 * 获取有效的待审核的设置器
	 * @return
	 */
	List<AutoSetter> getCheckSetterList();
	/**
	 * 获取剩余座位数小于seatnum的场次
	 * @param seatnum
	 * @return
	 */
	List<OpenPlayItem> getIntensiveOpiList(int seatnum);
	/**
	 * 查询场次数量
	 * @param citycode
	 * @param cinemaId
	 * @param movieId
	 * @param from
	 * @param to
	 * @param open
	 * @return
	 */
	int getOpiCount(String citycode, Long cinemaId, Long movieId, Timestamp from, Timestamp to, boolean open);
	
	List<OpenPlayItem> getDisabledOpiLlist(String citycode, Long cinemaId, int from, int maxnum);
	int getDisabledOpiCount(String citycode, Long cinemaId);
	List<Long> getDisabledCinemaIdList(String citycode);
	void clearOpenSeatCache(Long mpid);
	/**
	 * 更新座位预览图
	 * @param mpid
	 * @param room
	 * @param openSeatList
	 * @param hfhLockList
	 * @param seatStatusUtil
	 */
	void updateOpiSeatMap(Long mpid, CinemaRoom room, List<OpenSeat> openSeatList, List<String> lockList, SeatStatusUtil seatStatusUtil);
	void updateOpiSeatMap4Wd(Long mpid, CinemaRoom room, List<RoomSeat> seatList, List<String> lockList, SeatStatusUtil seatStatusUtil);
	/**
	 * 查询影院排片数
	 * @param cinemaid
	 * @return List<Map(playdate,opicount)
	 */
	Map getOpiCountMap(Long cinemaid);
	List<MoviePlayItem> synchUnOpenMpi();
	
	/**
	 * 上海电影艺术联盟获取排片数据
	 * @param citycode
	 * @param cinemaIds
	 * @param movieIds
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<OpenPlayItem> getArtFilmAllianceOpi(String citycode, String cinemaIds, String movieIds, int from, int maxnum);
	ErrorCode<String> validOpiStatusByPartner(OpenPlayItem opi, ApiUser partner, Member member);
	
	List<CinemaRoom> updateRoomList(String citycode, Date updatedate);
	List<OpenPlayItem> getOpiListByRoomId(Long roomid, int from, int max);
}
