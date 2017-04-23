package com.gewara.service.movie;

import java.util.Date;
import java.util.List;

import com.gewara.model.common.County;
import com.gewara.model.content.News;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.untrans.monitor.ConfigTrigger;

public interface FilmFestService extends ConfigTrigger{
	//查询电影节活动信息
	SpecialActivity getSpecialActivity(String tag);
	/**
	 * 2011 电影节
	 * 获取参展影片信息
	 */
	List<Long> getFilmFestMovieIdList(String flag, String specialFlag, String specialValue, int from,int maxnum);
	/**
	 * 2012 电影节
	 * 手机api获取参展影片信息
	 * @param flag
	 * @param specialFlag
	 * @param specialValue
	 * @param order
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Movie> getFilmFestMovie(String flag, String specialFlag, String specialValue,int from,int maxnum);
	
	List<Long> getSpecialActivityMovieIds(String flag);
	
	Integer getFilmFestMovieCount(String flag,String specialFlag,String specialValue);
	
	/**
	 * 2011 电影节
	 * 获取影院区域信息
	 */
	List<County> getCinemaCountyList(String flag);
	
	List<Long> getFilmFestCinema(String flag, String citycode, String countycode);
	
	/**
	 * 2011 电影节
	 * 参展影片查询(web)
	 */
	List<Long> getJoinMovieIdList(Long batch,String citycode,Date curDate,String type,String flag,String state,String moviename,Long cinemaid, String festtype, String order, int from, int maxnum);
	Integer getJoinMovieCount(Long batch,String citycode,Date curDate,String type,String flag,String state,String moviename,Long cinemaid, String festtype);
	
	//当前电影座位数总数
	Integer getCurMovieSeatSum(Long movieid, Long batch);
	List<MoviePlayItem> getMoviePlayItemList(String citycode, Long movieId, Long cinemaId, Date playDate, Long batch, String order, int from, int maxnum);
	List<Long> getMoviePlayItemIdList(String citycode, Long movieId, Long cinemaId, Date playDate, boolean  isGeHour, Long batch, String order, int from, int maxnum);
	Integer getMoviePlayItemCount(String citycode, Long movieId, Long cinemaId, Date playDate, boolean  isGeHour, Long batch);
	List<MoviePlayItem> getMaybeMoviePlayItemList(List<Long> cinemaIdList, List<Long> movieIdList, Date playDate, Long batch);
	/**
	 * 得到电影节影院开发排片的电影 
	 */
	List<Movie> getCurMovieListByCinemaIdAndDate(Long cinemaId, Date date, Long batch);
	/**
	 * 得到电影节影院开发排片的电影 
	 */
	List<MoviePlayItem> getCinemaCurMpiListByRoomIdAndDate(Long roomId, Date playdate, Long batch);
	
	List<Long> getMPIMovieIds(String citycode, long batch);
	/**
	 * 电影节查询新闻信息
	 */
	List<News> getFilmFestNewsList(String citycode,String flag,String[] newstype,Date releasedate,int from,int maxnum);
	Integer getFilmFestNewsCount(String citycode,String flag,String[] newstype,Date releasedate);
	List<String> getFilmFestNewsDateList(String citycode,String flag);
	boolean isFilmMoviePlayItem(Long batchid);
	String getCachePre();
	/**
	 * 显示不显示
	 * */
	void updateMpiOtherinfo(String[] idList, String unopengewa, String unshowgewa);
	
	void copyOpiRemark(List<OpenPlayItem> opiList, List<String> msgList);
}
