package com.gewara.service.movie;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.command.SearchCinemaCommand;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.GrabTicketMpi;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MultiPlay;

/**
 * Movie,Cinema,PlayTime Service
 * 
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public interface MCPService {
	/**
	 * 根据电影的名称查找影院
	 * @param movieName
	 * @return 符合条件的第一个电影
	 */
	Movie getMovieByName(String movieName);
	Integer getMovieCountByName(Long movieid,String movieName);
	/**
	 * 根据电影名称模糊查询电影
	 * @param moviename
	 * @return 模糊查询的电影列表
	 */
	List<Movie> searchMovieByName(String moviename);
	boolean updateMovieHotValue(Long movieId, Integer value);
	boolean updateCinemaHotValue(Long movieId, Integer value);
	/**
	 * @param orderField :排序字段,为空则按照热门程度排序
	 * @return 当前正在放映的电影列表
	 */
	List<Movie> getCurMovieList(String citycode);
	List<Movie> getCurMovieList();
	/**
	 * @param from
	 * @param maxnum
	 * @return
	 * 加入城市代码
	 */
	List<Movie> getCurMovieListByMpiCount(String citycode, int from, int maxnum);
	/**
	 * 某影院某天的影片
	 * @param date
	 * @return
	 */
	List<Movie> getCurMovieListByCinemaIdAndDate(Long cinemaId, Date date);

	/**
	 * @return 当前正在放映的电影数量
	 */
	Integer getCurMovieCount(String citycode);

	/**
	 * 获取即将上映的影片
	 * @return
	 */
	List<Movie> getFutureMovieList(int from, int maxnum,String order);
	/**
	 * @param endDate 截止日期
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Movie> getFutureMovieList(Date endDate,int from, int maxnum);
	/**
	 * @param movieId
	 * @param playdate
	 * @param from
	 * @param maxnum
	 * @return 放映该电影的影院，根据热门程度排序
	 */
	List<Long> getPlayCinemaIdList(String citycode, Long movieId, Date playdate);
	List<Long> getPlayCinemaIdListByCountycode(String countycode, Long movieid, Date playdate);
	List<Cinema> getPlayCinemaList(String citycode, Long movieId, Date playdate, int from, int maxnum);
	List<Cinema> getPlayCinemaListByCountycode(String countycode, Long movieid, Date playdate) ;
	/**
	 * @param citycode 城市代码
	 * @param movieid	电影ID
	 * @param playdate 日期
	 * @return 查询当前日期电影有排片的影院ID
	 */
	List<Long> getCurCinemaIdList(String citycode, Long movieid, Date playdate);
	/**
	 * @param movieId
	 * @param date
	 * @return 放映该电影的影院数量
	 */
	Integer getPlayCinemaCount(String citycode, Long movieId);
	/**
	 * 售该电影票的影院数量
	 * @param movieid
	 * @return
	 */
	Integer getOrderPlayCinemaCount(String citycode, Long movieid);
	/**
	 * @param cinemaId
	 * @return 当前电影院的放映影片列表
	 */
	List<Movie> getCurMovieListByCinemaId(Long cinemaId);
	/**
	 * @param movieId
	 * @return 某电影当前的排片日期
	 */
	List<Date> getCurMoviePlayDate(String citycode, Long movieId);
	List<Date> getCurMoviePlayDate2(String citycode, Long movieId);
	/**
	 * @param cinemaId
	 * @return 影院当前的排片日期
	 */
	List<Date> getCurCinemaPlayDate(Long cinemaId);
	/**
	 * @param cinemaId
	 * @param date
	 * @return 电影院某天的排片场次
	 */
	Integer getCinemaMpiCountByDate(Long cinemaId, Date date);
	/**
	 * @param cinemaId
	 * @param date
	 * @return 电影院当前某天的排片
	 */
	List<MoviePlayItem> getCinemaCurMpiListByDate(Long cinemaId, Date date);

	/**
	 * 获取某影厅某天的拍片
	 * @param roomId
	 * @return
	 */
	List<MoviePlayItem> getCinemaCurMpiListByRoomIdAndDate(Long roomId, Date playdate);
	Integer getMovieCurMpiCount(String citycode, Long movieId);
	Integer getMovieCurMpiCountByPlaydate(String citycode, Long movieId, Date playdate);
	/**
	 * 获取电影院的当前排片
	 * @param cinemaId
	 * @return 电影院的当前所有排片
	 */
	List<MoviePlayItem> getCinemaCurMpiList(Long cinemaId);
	List<MoviePlayItem> getCinemaMpiList(Long cinemaId,Long movieId, Date fyrq);
	List<MoviePlayItem> getCinemaMpiList(Long cinemaId, Date fyrq);

	int getCinemaMpiCount(Long cinemaId,Long movieid, Date fyrq);
	/**
	 * @param cinemaId
	 * @param movieId
	 * @param playdate
	 * @return 某影院某影片某天的当前排片
	 */
	List<MoviePlayItem> getCurMpiList(Long cinemaId, Long movieId, Date playdate);
	/**
	 * 得到放映影片的id列表(根据排片数排序)
	 * @return
	 */
	List<Long> getCurMovieIdList(String citycode);
	/**
	 * 得到放映影片的id列表(根据排片数排序)
	 * @return
	 */
	List<Long> getCurMovieIdList(String citycode,Date playdate);
	/**
	 * 根据电影名称搜索
	 * @param keyname
	 * @return
	 */
	List<Long> getMovieIdByMoviename(String keyname);
	/**
	 * 根据影院及影厅名称获取影厅ID
	 * @param cinemaId
	 * @param playroom
	 * @return
	 */
	CinemaRoom getRoomByRoomname(Long cinemaId, String playroom);
	CinemaRoom getRoomByRoomnum(Long cinemaId, String playroom);
	List<Map> getGroupMoviePlayItemByCinema_Time(String citycode,Long mid,Date d,String playtime,String time1,String time2);
	void sortMoviesByMpiCount(String citycode, List<Movie> movieList);
	void sortTodayMoviesByMpiCount(String citycode,List<Movie> movieList);
	/**
	 * 根据影厅、放映日期、放映时间找到唯一排片
	 * @param roomId
	 * @param movieId
	 * @param date
	 * @param playtime
	 * @return
	 */
	MoviePlayItem getUniqueMpi(String opentype, Long cinemaid, Long roomId, Date playdate, String playtime);
	/**
	 * 不分厅排片
	 * @param cinemaid
	 * @param movieid
	 * @param playdate
	 * @param playtime
	 * @return
	 */
	MoviePlayItem getUniqueMpi2(Long cinemaid, Long movieid, Date playdate, String playtime);
	GrabTicketMpi getGrabTicketMpiListByMpid(Long mpid);
	/**
	 * 查询影院数量
	 */
	Integer getTicketCinemaCount(String citycode, String countycode,String indexareacode, String cname);
	Integer getCinemaCount(String citycode, String countycode, String indexareacode, String cname, boolean booking);
	
	/**
	 * 根据经纬度范围查询附近影院
	 * @param maxLd
	 * @param minLd
	 * @param maxLa
	 * @param minLa
	 * @param movieid 有排片的
	 * @return
	 */
	List<Cinema> getNearCinemaList(double pointxx, double pointyy, int distant, Long movieid, String citycode, Date playdate);
	List<Cinema> getCinemaListByNearOrder(double ld/*pointx*/, double la, List<Cinema> cinemaList, int distance, boolean validDistance);
	/**
	 * 查询影院列表
	 * @param countycode
	 * @param indexareacode
	 * @param movieid（放映）
	 * @return
	 */
	List<Cinema> getCinemaListByIndexareaCodeCountycodeMovie(String countycode, String indexareacode, Long movieid);
	/**
	 * 获取城市开放订票的影院
	 * @param citycode 多个用“,”分隔，AdminCityContant.CITYCODE_ALL表示所有城市
	 * @param countycode
	 * @return
	 */
	List<Long> getBookingCinemaIdList(String citycode, String countycode);
	/**
	 * 公开订票影院列表
	 * @return
	 */
	List<Cinema> getBookingCinemaList(String citycode);
	
	/**
	 * 查询所有影院
	 */
	List<Cinema> getCinemaListByCitycode(String citycode, int from, int maxnum);
	/**
	 * 查询所有影院的数量
	 */
	Integer getCinemaCountByCitycode(String citycode);
	/**
	 * 获取开放订票的影片，按排片数量排序
	 * @param citycode
	 * @return
	 */
	List<Movie> getOpenMovieList(String citycode);
	List<Long> getSpecialActivityOpiMovieList(Long aid, Date playdate, String citycode);
	/**
	 * 获取影院当前连映影片
	 * @param cinemaid
	 * @return
	 */
	List<MultiPlay> getCurMultyPlayList(Long cinemaid);
	
	/**
	 *  @function  可订票 + 热门
	 * 	@author bob.hu
	 *	@date	2011-07-06 18:11:18
	 */
	List<Cinema> getHotBookingCinames(String citycode, int from, int maxnum);
	List<Cinema> getCinemaList(String citycode,String countycode, int from,int maxnum,Long movieid, boolean onlyBooking, boolean hasPlay);
	
	List<Cinema> getCinemaListBySearchCmd(SearchCinemaCommand cmd, String citycode, int from, int maxnum);
	List<Long> getCinemaIdListBySearchCmd(SearchCinemaCommand cmd, String citycode);
	/**
	 * 有线上卖品的影院ID列表，根据数量排序
	 * @param citycode
	 * @return
	 */
	List<Long> getCinemaIdListByGoods(String citycode);
	//DetachedCriteria getCinemaQuery(SearchCinemaCommand cmd, String citycode);
	List<Movie> getReleaseMovieList(Date date);
	
	List<Map<String, Object>> getMovieProjectionCount(Date date, String status);
	/**
	 * 获取该影院某天放映的电影数量
	 * @param cinemaId
	 * @param date
	 * @return
	 */
	Integer getCinemaMovieCountByDate(Long cinemaId, Date date);
	List<Map<String, Object>> getReleaseDateMovieCount();
	MoviePlayItem getMpiBySeqNo(String seqNo);
	List<Movie> getHotPlayMovieList(String citycode);
	
	/**
	 * 特色统计影院
	 * @return
	 */
	Map<String,Integer> getFeatureCinema(String citycode,String... feature);
	/**
	 * 特色影厅的影院统计
	 * IMAX，10家   4D 11家 。。。
	 * @param citycode
	 * @return
	 */
	Map<String,Integer> getRoomFeatureCinema(String citycode);
	/**
	 * 某个影院的特色影厅类型
	 * @param cinemaId
	 * @return
	 */
	List<String> getCharacteristicCinemaRoomByCinema(long cinemaId);
	/**
	 * 根据特色影厅类型查询特色影厅影院id列表
	 * @param cType
	 * @param citycode
	 * @return
	 */
	List<Long> getCinemaIdListByRoomCharacteristic(String cType,String citycode);
	
	List<Long> getRoomIdListByCinemaAndCtype(long cinemaId,String ctype);
	
	Integer getPlayCinemaCountByPlayDate(String citycode, Long movieId, Date playdate);
	Date getMinPlaydateByMovieid(Long movieid);
	Integer getMovieCurMpiCount(String citycode, Long movieId, Date startdate, Date enddate);
	//根据电影拿到特设影厅类型
	List<CinemaRoom> getCurCinemaRoomByMovieId(String citycode, Long movieid, Date playdate);
	//根据影院拿到当前有场次的影厅ID
	List<Long> getRoomIdListByOpi(Long cinemaid);
}
