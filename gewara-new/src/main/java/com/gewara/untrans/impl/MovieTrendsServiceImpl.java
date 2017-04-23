package com.gewara.untrans.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.json.MovieTrendsCount;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.movie.Movie;
import com.gewara.mongo.MongoService;
import com.gewara.service.movie.MCPService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MovieTrendsService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.xmlbind.bbs.CountByMovieIdAddDate;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 预测即将上映电影在未来月的情况的基础数据需求
 */
@Service("movieTrendsService")
public class MovieTrendsServiceImpl implements MovieTrendsService {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	@Autowired
	@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;

	@Autowired
	@Qualifier("mcpService")
	private MCPService mcpService;

	/**
	 * 查询指定日期所有影片的统计数据
	 * 
	 * @param queryDate
	 *            查询日期
	 * @return List<Map>
	 */
	public List<Map> queryMovieTrendsCountByDate(String queryDate) {
		if (StringUtils.isBlank(queryDate)) {
			return new ArrayList<Map>();
		}
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("countDate", queryDate);
		List<Map> movieTrendsCountList = mongoService.find(MovieTrendsCount.class.getCanonicalName(), paramsMap, new String[] { "boughtcount",
				"releasedate" }, new boolean[] { false, false }, 0, 0);
		return movieTrendsCountList;
	}

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
	public List<MovieTrendsCount> getMovieTrendsListByMovieIdDate(Long movieid, Date startDate, Date endDate) {
		if (movieid == null || startDate == null || endDate == null) {
			return new ArrayList<MovieTrendsCount>();
		}
		List<MovieTrendsCount> resultList = new ArrayList<MovieTrendsCount>();
		DBObject queryCondition = new BasicDBObject().append("movieId", movieid);
		DBObject dateDbObject = new BasicDBObject().append("$gte", DateUtil.formatDate(startDate)).append("$lte", DateUtil.formatDate(endDate));
		queryCondition.put("countDate", dateDbObject);
		resultList = mongoService.getObjectList(MovieTrendsCount.class, queryCondition, "countDate", true, 0, 0);
		return resultList;
	}

	/**
	 * 获取默认查询的时间，如果queryDate不为空，则queryDate，如果为空则取MovieTrendsCount集合中最近的一条
	 * 
	 * @param queryDate
	 *            日期
	 * @return String 默认查询时间
	 */
	public String getDefaultQueryDate(Date queryDate) {
		String queryDateStr = "";
		queryDateStr = DateUtil.formatDate(DateUtil.addDay(new Date(), -1));
		if (queryDate != null) {
			queryDateStr = DateUtil.formatDate(queryDate);
		} else {
			List<MovieTrendsCount> temp = mongoService.getObjectList(MovieTrendsCount.class, new HashMap(), "countDate", false, 0, 1);
			if (CollectionUtils.isNotEmpty(temp)) {
				queryDateStr = temp.get(0).getCountDate();
			}
		}

		return queryDateStr;
	}

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
	public Long getMemberMarkCountByMovieid(Long movieid, String flag, Date queryDate) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);
		query.add(Restrictions.eq("relatedid", movieid));
		query.add(Restrictions.eq("markname", "generalmark"));
		if (StringUtils.isNotBlank(flag))
			query.add(Restrictions.eq("flag", flag));
		if (queryDate != null)
			query.add(Restrictions.lt("addtime", DateUtil.addDay(queryDate, 1)));
		query.setProjection(Projections.rowCount());
		List<Long> result = readOnlyTemplate.findByCriteria(query);
		return CollectionUtils.isEmpty(result) ? 0L : result.get(0);
	}

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
	public List<MemberMark> getMemberMarkListByMovieid(Long movieid, String flag, Date queryDate, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);

		query.add(Restrictions.eq("relatedid", movieid));
		query.add(Restrictions.eq("markname", "generalmark"));
		if (StringUtils.isNotBlank(flag))
			query.add(Restrictions.eq("flag", flag));
		if (queryDate != null)
			query.add(Restrictions.lt("addtime", DateUtil.addDay(queryDate, 1)));
		query.addOrder(Order.desc("addtime"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}

	/**
	 * 定时保存影片统计数量
	 */
	public void saveMovieTrendsCount() {
		Date currentDate = new Date();
		// 热映影片
		List<Movie> curMovieList = mcpService.getCurMovieList();
		// 暂时先设置200，理论上待上映影片不会超过200
		List<Movie> futureMovieList = mcpService.getFutureMovieList(0, 200, null);

		List<Movie> allMovieList = new LinkedList<Movie>();
		allMovieList.addAll(curMovieList);
		allMovieList.addAll(futureMovieList);

		// 所有movieId
		Map<Long, Movie> allMovieIdMap = BeanUtil.beanListToMap(allMovieList, "id");
		Set<Long> notAlreadyMovieIdSet = allMovieIdMap.keySet();

		// 取得已经聚合过的movieId
		Map<Long, MovieTrendsCount> alreadyCountMovieMap = getAlreadyCountMovieId(currentDate);
		Set<Long> alreadyCountMovieIdSet = alreadyCountMovieMap.keySet();

		// 取得未聚合过的movieId
		if (CollectionUtils.isNotEmpty(notAlreadyMovieIdSet)) {
			notAlreadyMovieIdSet.removeAll(alreadyCountMovieIdSet);
		}

		// 用户评分数
		Map<String, Long> memberMarkCountMap = getMemberMarkMap(currentDate, notAlreadyMovieIdSet, alreadyCountMovieMap);

		// 影评数
		Map<String, Long> diaryCountMap = getDiaryCountMap(currentDate, notAlreadyMovieIdSet, alreadyCountMovieMap);

		Map<String, CountByMovieIdAddDate> walaAllCountMap = getWalaCountMap(currentDate, notAlreadyMovieIdSet, alreadyCountMovieMap);
		saveMovieTrendsCount(currentDate, allMovieList, memberMarkCountMap, diaryCountMap, walaAllCountMap, alreadyCountMovieMap);
	}

	/**
	 * 构造用户评分的Map<movieId, 评分个数>
	 * 
	 * @param currentDate
	 *            查询日期
	 * @param movieList
	 *            List<Movie>
	 * 
	 * @return Map<movieId, 影评Count>
	 */
	private Map<String, Long> getMemberMarkMap(Date currentDate, Set<Long> notAlreadyMovieIdSet, Map<Long, MovieTrendsCount> alreadyCountMovieMap) {

		Map<String, Long> alreadyCountMovieResult = new HashMap<String, Long>();
		// 已经统计过的movieId取增量
		if (MapUtils.isNotEmpty(alreadyCountMovieMap)) {
			Set<Long> alreadyCountMovieIdSet = alreadyCountMovieMap.keySet();
			DetachedCriteria query = getMemberMarkCountDetachedCriteria(alreadyCountMovieIdSet, "one", currentDate);
			List<Map<String, Object>> diaryOneDayCountMapList = readOnlyTemplate.findByCriteria(query);
			Map<String, Long> alreadyCountMovieResultMap = listToMap(diaryOneDayCountMapList);
			for (Map.Entry<Long, MovieTrendsCount> entry : alreadyCountMovieMap.entrySet()) {
				String movieIdStr = String.valueOf(entry.getKey());
				alreadyCountMovieResult.put(
						movieIdStr + Status.N,
						entry.getValue().getMemberMarkCountN()
								+ (alreadyCountMovieResultMap.get(movieIdStr + Status.N) == null ? 0L : alreadyCountMovieResultMap.get(movieIdStr
										+ Status.N)));

				alreadyCountMovieResult.put(
						movieIdStr + Status.Y,
						entry.getValue().getMemberMarkCountY()
								+ (alreadyCountMovieResultMap.get(movieIdStr + Status.Y) == null ? 0L : alreadyCountMovieResultMap.get(movieIdStr
										+ Status.Y)));
			}
		}
		// 未统计过的movieId取总量
		Map<String, Long> notAlreadyCountMovieResult = new HashMap<String, Long>();
		if (CollectionUtils.isNotEmpty(notAlreadyMovieIdSet)) {
			DetachedCriteria query = getMemberMarkCountDetachedCriteria(notAlreadyMovieIdSet, "all", currentDate);
			List<Map<String, Object>> diaryAllCountMapList = readOnlyTemplate.findByCriteria(query);
			notAlreadyCountMovieResult = listToMap(diaryAllCountMapList);
		}
		Map<String, Long> resultmemberMarkCountCountMap = new HashMap<String, Long>();
		resultmemberMarkCountCountMap.putAll(alreadyCountMovieResult);
		resultmemberMarkCountCountMap.putAll(notAlreadyCountMovieResult);

		return resultmemberMarkCountCountMap;
	}

	/**
	 * 构造影片影评的Map<movieId, 影评个数>
	 * 
	 * @param currentDate
	 *            查询日期
	 * @param movieList
	 *            List<Movie>
	 * @return Map<movieId, 影评Count>
	 */
	private Map<String, Long> getDiaryCountMap(Date currentDate, Set<Long> notAlreadyMovieIdSet, Map<Long, MovieTrendsCount> alreadyCountMovieMap) {
		Map<String, Long> alreadyCountMovieResult = new HashMap<String, Long>();
		// 已经统计过的movieId取增量
		if (MapUtils.isNotEmpty(alreadyCountMovieMap)) {
			Set<Long> alreadyCountMovieIdSet = alreadyCountMovieMap.keySet();
			DetachedCriteria query = getDiaryCountDetachedCriteria(alreadyCountMovieIdSet, "one", currentDate);
			List<Map<String, Object>> diaryOneDayCountMapList = readOnlyTemplate.findByCriteria(query);
			Map<String, Long> alreadyCountMovieResultMap = listToMap(diaryOneDayCountMapList);
			for (Map.Entry<Long, MovieTrendsCount> entry : alreadyCountMovieMap.entrySet()) {
				String movieIdStr = String.valueOf(entry.getKey());
				alreadyCountMovieResult.put(movieIdStr, entry.getValue().getDiaryCount()
						+ (alreadyCountMovieResultMap.get(movieIdStr) == null ? 0L : alreadyCountMovieResultMap.get(movieIdStr)));
			}
		}
		// 未统计过的movieId取总量
		Map<String, Long> notAlreadyCountMovieResult = new HashMap<String, Long>();
		if (CollectionUtils.isNotEmpty(notAlreadyMovieIdSet)) {
			DetachedCriteria query = getDiaryCountDetachedCriteria(notAlreadyMovieIdSet, "all", currentDate);
			List<Map<String, Object>> diaryAllCountMapList = readOnlyTemplate.findByCriteria(query);
			notAlreadyCountMovieResult = listToMap(diaryAllCountMapList);
		}
		Map<String, Long> resultDiaryCountMap = new HashMap<String, Long>();
		resultDiaryCountMap.putAll(alreadyCountMovieResult);
		resultDiaryCountMap.putAll(notAlreadyCountMovieResult);
		return resultDiaryCountMap;
	}

	/**
	 * 构造保存MovieTrendsCount的Map<String, CountByMovieIdAddDate>参数
	 * 
	 * @param currentDate
	 *            查询日期
	 * @param allMovieList
	 *            List<Movie>
	 * @return Map<movieId, CountByMovieIdAddDate>
	 */
	private Map<String, CountByMovieIdAddDate> getWalaCountMap(Date currentDate, Set<Long> notAlreadyMovieIdSet,
			Map<Long, MovieTrendsCount> alreadyCountMovieMap) {
		Map<String, CountByMovieIdAddDate> walaDayCountMap = new HashMap<String, CountByMovieIdAddDate>();
		boolean alreadyCountMovieMapIsNotEmpty = MapUtils.isNotEmpty(alreadyCountMovieMap);
		// 对于已经存在哇啦数的movieid只取增量
		if (alreadyCountMovieMapIsNotEmpty) {
			Set<Long> alreadyCountMovieIdSet = alreadyCountMovieMap.keySet();
			walaDayCountMap = getCountByMovieIdAddDate(alreadyCountMovieIdSet, "one", currentDate);
		}
		// 对于没有哇啦数的movieId取所有
		Map<String, CountByMovieIdAddDate> walaAllCountMap = getCountByMovieIdAddDate(notAlreadyMovieIdSet, "all", currentDate);
		if (alreadyCountMovieMapIsNotEmpty) {
			for (Map.Entry<Long, MovieTrendsCount> entry : alreadyCountMovieMap.entrySet()) {
				String movieIdStr = String.valueOf(entry.getKey());
				CountByMovieIdAddDate temp = walaDayCountMap.get(movieIdStr);
				if (temp == null) {
					temp = new CountByMovieIdAddDate();
				}
				Long walaCount = entry.getValue().getWalaCount() + (temp.getCount() == null ? 0L : temp.getCount());
				temp.setCount(walaCount);
				temp.setMovieId(movieIdStr);
				walaAllCountMap.put(movieIdStr, temp);
			}
		}
		return walaAllCountMap;
	}

	/**
	 * 调用哇啦API获取根据movieids获取哇啦数
	 * 
	 * @param movieIdSet
	 * @param type
	 *            one:当天 |all：所有
	 * @param currentDate
	 *            查询时间
	 * @return Map<movieId, CountByMovieIdAddDate>
	 */
	private Map<String, CountByMovieIdAddDate> getCountByMovieIdAddDate(Set<Long> movieIdSet, String type, Date currentDate) {
		if (CollectionUtils.isEmpty(movieIdSet)) {
			return new HashMap<String, CountByMovieIdAddDate>();
		}
		StringBuilder movieIds = new StringBuilder();
		for (Long movieId : movieIdSet) {
			movieIds.append(movieId).append(",");
		}
		movieIds.deleteCharAt(movieIds.length() - 1);
		List<CountByMovieIdAddDate> resultList = commentService.getCountByMovieIdAddDate(movieIds.toString(), type, currentDate);
		Map<String, CountByMovieIdAddDate> resultMap = BeanUtil.beanListToMap(resultList, "movieId");
		return resultMap;
	}

	/**
	 * 查询获取已经存在的哇啦数的movieId
	 * 
	 * @param currentDate
	 *            查询日期
	 * @return Map<movieId, MovieTrendsCount>
	 */
	private Map<Long, MovieTrendsCount> getAlreadyCountMovieId(Date currentDate) {
		String yesterday = DateUtil.formatDate(DateUtil.addDay(currentDate, -1));
		DBObject queryCondition = new BasicDBObject();

		queryCondition.putAll(mongoService.queryBasicDBObject("countDate", "=", yesterday));

		List<MovieTrendsCount> movieTrendsCountList = mongoService.getObjectList(MovieTrendsCount.class, queryCondition);
		if (CollectionUtils.isEmpty(movieTrendsCountList)) {
			return new HashMap<Long, MovieTrendsCount>();
		}
		Map resultMap = BeanUtil.beanListToMap(movieTrendsCountList, "movieId");
		return resultMap;
	}

	private DetachedCriteria getMemberMarkCountDetachedCriteria(Set<Long> movieIdSet, String queryType, Date queryDate) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);
		Date fromQueryDate = DateUtil.getBeginningTimeOfDay(DateUtil.addDay(queryDate, -1));
		Date endQueryDate = DateUtil.getBeginningTimeOfDay(queryDate);
		// 由于定时在每天的3:00～5:00，所以开始日期-1，统计前一天的
		if (StringUtils.endsWithIgnoreCase(queryType, "one")) {
			query.add(Restrictions.gt("addtime", fromQueryDate));
		}
		query.add(Restrictions.le("addtime", endQueryDate));

		query.add(Restrictions.in("relatedid", movieIdSet));
		query.add(Restrictions.eq("markname", "generalmark"));
		query.setProjection(Projections.sqlGroupProjection("relatedid || flag as mapkey, count(1) as countResult", "relatedid, flag", new String[] {
				"mapkey", "countResult" }, new Type[] { StandardBasicTypes.STRING, StandardBasicTypes.LONG }));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		return query;
	}

	private DetachedCriteria getDiaryCountDetachedCriteria(Set<Long> movieIdSet, String queryType, Date queryDate) {
		DetachedCriteria query = DetachedCriteria.forClass(Diary.class);
		// 由于定时在每天的3:00～5:00，所以开始日期-1，统计前一天的
		Date fromQueryDate = DateUtil.getBeginningTimeOfDay(DateUtil.addDay(queryDate, -1));
		Date endQueryDate = DateUtil.getBeginningTimeOfDay(queryDate);
		if (StringUtils.endsWithIgnoreCase(queryType, "one")) {
			query.add(Restrictions.gt("addtime", fromQueryDate));
		}
		query.add(Restrictions.le("addtime", endQueryDate));

		query.add(Restrictions.like("type", DiaryConstant.DIARY_TYPE_COMMENT, MatchMode.START));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.in("categoryid", movieIdSet));
		query.add(Restrictions.eq("category", "movie"));
		query.setProjection(Projections.sqlGroupProjection("categoryid as mapkey, count(1) as countResult", "categoryid", new String[] { "mapkey",
				"countResult" }, new Type[] { StandardBasicTypes.STRING, StandardBasicTypes.LONG }));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		return query;
	}

	/**
	 * 保存定时统计的数据
	 * 
	 * @param currentDate
	 *            统计时间
	 * @param movieList
	 *            List<Movie>
	 * @param memberMarkCountMap
	 *            用户评分Map<movieId, 评分个数Count>
	 * @param diaryCountMap
	 *            影评Map<movieId, 影评个数Count>
	 * @param walaCountMap
	 *            哇啦Map<movieId, 哇啦个数Count>
	 */
	private void saveMovieTrendsCount(Date currentDate, List<Movie> movieList, Map<String, Long> memberMarkCountMap, Map<String, Long> diaryCountMap,
			Map<String, CountByMovieIdAddDate> walaCountMap, Map<Long, MovieTrendsCount> alreadyCountMovieMap) {
		String today = DateUtil.formatDate(currentDate);
		String time = DateUtil.formatTime(currentDate);
		List<MovieTrendsCount> movieTrendsCountList = new ArrayList<MovieTrendsCount>();

		if (CollectionUtils.isNotEmpty(movieList)) {
			for (Movie movie : movieList) {

				String movieId = String.valueOf(movie.getId());
				try {
					// 构造MovieTrendsCount的数据
					MovieTrendsCount movieTrendsCount = new MovieTrendsCount();
					movieTrendsCount.setId(movie.getId() + today);
					movieTrendsCount.setCountDate(today);
					movieTrendsCount.setCountTime(time);
					movieTrendsCount.setMovieId(movie.getId());
					movieTrendsCount.setMoviename(movie.getMoviename());
					movieTrendsCount.setReleasedate(movie.getReleasedate() == null ? null : movie.getReleasedate().getTime());
					movieTrendsCount.setClicktimesCount(movie.getRclickedtimes() == null ? 0L : Long.valueOf(movie.getRclickedtimes()));
					movieTrendsCount.setBoughtcount(movie.getBoughtcount() == null ? 0L : Long.valueOf(movie.getBoughtcount()));
					if (movie.getReleasedate() == null || currentDate.before(movie.getReleasedate())) {
						movieTrendsCount.setCollectedtimesCountN(movie.getCollectedtimes() == null ? 0L : Long.valueOf(movie.getCollectedtimes()));
						movieTrendsCount.setCollectedtimesCountY(movie.getCollectedtimes() == null ? 0L : Long.valueOf(movie.getCollectedtimes()));
					} else {
						movieTrendsCount.setCollectedtimesCountN(MapUtils.isEmpty(alreadyCountMovieMap)
								|| alreadyCountMovieMap.get(movie.getId()) == null ? 0L : alreadyCountMovieMap.get(movie.getId())
								.getCollectedtimesCountN());
						movieTrendsCount.setCollectedtimesCountY(movie.getCollectedtimes() == null ? 0L : Long.valueOf(movie.getCollectedtimes()));
					}

					movieTrendsCount.setMemberMarkCountN(memberMarkCountMap.get(movieId + Status.N) == null ? 0L : memberMarkCountMap.get(movieId
							+ Status.N));
					movieTrendsCount.setMemberMarkCountY(memberMarkCountMap.get(movieId + Status.Y) == null ? 0L : memberMarkCountMap.get(movieId
							+ Status.Y));
					movieTrendsCount.setWalaCount(walaCountMap.get(movieId) == null ? 0L : walaCountMap.get(movieId).getCount());
					movieTrendsCount.setDiaryCount(diaryCountMap.get(movieId) == null ? 0L : diaryCountMap.get(movieId));

					movieTrendsCount.setUpdatetime(currentDate.getTime());

					movieTrendsCountList.add(movieTrendsCount);
				} catch (Exception e) {
					dbLogger.error("saveMovieTrendsCount" + movieId, e);
				}
			}
			mongoService.saveOrUpdateObjectList(movieTrendsCountList, "id");
		}
	}

	private Map<String, Long> listToMap(List<Map<String, Object>> paramMapList) {
		Map<String, Long> resultMap = new HashMap<String, Long>();
		if (CollectionUtils.isEmpty(paramMapList)) {
			return resultMap;
		}
		for (Map<String, Object> tempMap : paramMapList) {
			resultMap.put(String.valueOf(tempMap.get("mapkey")),
					Long.valueOf(tempMap.get("countResult") == null ? "0" : String.valueOf(tempMap.get("countResult"))));
		}
		return resultMap;
	}

}
