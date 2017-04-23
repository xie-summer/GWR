package com.gewara.untrans;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.json.MovieTrendsCount;
import com.gewara.model.bbs.MemberMark;

/**
 * 预测即将上映电影在未来月的情况的基础数据需求
 */
public interface MovieTrendsService {

	/**
	 * 查询指定日期所有影片的统计数据，日期为null返回new ArrayList<Map>()。
	 * 
	 * @param queryDate
	 *            查询日期
	 * @return List<Map>
	 */
	public List<Map> queryMovieTrendsCountByDate(String queryDate);

	/**
	 * 根据影片ID，日期区间查询统计的数据，参数为空返回new ArrayList
	 * 
	 * @param movieid
	 *            影片ID
	 * @param startDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @return List<MovieTrendsCount>
	 */
	public List<MovieTrendsCount> getMovieTrendsListByMovieIdDate(Long movieid, Date startDate, Date endDate);

	/**
	 * 获取默认查询的时间，如果queryDate不为空，则queryDate，如果为空则取MovieTrendsCount集合中最近的一条
	 * 
	 * @param queryDate
	 *            日期
	 * @return String 默认查询时间
	 */
	public String getDefaultQueryDate(Date queryDate);

	/**
	 * 根据影片ID，flag（Y：购票|N：未购票）获取影片评分数量
	 * 
	 * @param movieid
	 *            影片ID
	 * @param flag
	 *            Y:购票|N：未购票
	 * @param queryDate
	 *            查询日期
	 * @return Long
	 */
	public Long getMemberMarkCountByMovieid(Long movieid, String flag, Date queryDate);

	/**
	 * 根据影片ID，flag（Y：购票|N：未购票）获取评分明细
	 * 
	 * @param movieid
	 *            影片ID
	 * @param flag
	 *            Y:购票|N：未购票
	 * @param queryDate
	 *            查询日期
	 * @param from
	 *            开始条数
	 * @param maxnum
	 *            最大记录数
	 * @return List<MemberMark>
	 */
	public List<MemberMark> getMemberMarkListByMovieid(Long movieid, String flag, Date queryDate, int from, int maxnum);

	/**
	 * 定时保存影片统计数量
	 */
	public void saveMovieTrendsCount();

}
