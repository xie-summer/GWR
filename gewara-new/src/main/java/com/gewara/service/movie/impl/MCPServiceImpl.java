package com.gewara.service.movie.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.gewara.command.SearchCinemaCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CharacteristicType;
import com.gewara.constant.Flag;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.common.County;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.GrabTicketMpi;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MultiPlay;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.movie.MCPService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.CachableCall;
import com.gewara.util.CachableServiceHelper;
import com.gewara.util.DateUtil;
import com.gewara.util.LongitudeAndLatitude;
import com.gewara.util.OuterSorter;

/**
 * @author acerge(acerge@163.com)
 * @since 5:31:40 PM Oct 9, 2009
 */
@Service("mcpService")
public class MCPServiceImpl extends BaseServiceImpl implements MCPService, InitializingBean {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	private CachableServiceHelper helper;
	@Override
	public List<MoviePlayItem> getCinemaCurMpiList(Long cinemaId) {
		String queryString = "from MoviePlayItem mpi where mpi.playdate >= ? and mpi.cinemaid = ? order by mpi.movieid, mpi.playdate, mpi.playtime";
		Date current = DateUtil.getBeginningTimeOfDay(new Date());
		List<MoviePlayItem> result = hibernateTemplate.find(queryString, current, cinemaId);
		return result;
	}
	
	@Override
	public List<MoviePlayItem> getCinemaMpiList(Long cinemaId, Date fyrq){
		return getCinemaMpiList(cinemaId,null,fyrq);
	}
	
	@Override
	public List<MoviePlayItem> getCinemaMpiList(Long cinemaId,Long movieId, Date fyrq){
		String key = CacheConstant.buildKey("getCindda23jnxreidList", cinemaId,movieId, fyrq);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(idList==null ){
			DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
			query.add(Restrictions.eq("cinemaid", cinemaId));
			if(movieId != null){
				query.add(Restrictions.eq("movieid", movieId));
			}
			query.add(Restrictions.eq("playdate", fyrq));
			Date cur = DateUtil.currentTime();
			Date date = DateUtil.getBeginningTimeOfDay(cur);
			if(DateUtil.getDiffDay(date, fyrq)==0){
				String playtime=DateUtil.format(cur, "HH:mm");
				query.add(Restrictions.ge("playtime", playtime));
			}
			query.addOrder(Order.asc("playtime"));
			query.setProjection(Projections.id());
			idList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, idList);
		}
		List<MoviePlayItem> playItemList = baseDao.getObjectList(MoviePlayItem.class, idList);
		return playItemList;
	}
	
	@Override
	public int getCinemaMpiCount(final Long cinemaId, final Long movieid, final Date fyrq){
		String key = CacheConstant.buildKey("plcxj34usdjnemaMpiCount", cinemaId, movieid, fyrq);
		Integer result = helper.cacheCall(key, CacheConstant.SECONDS_HALFHOUR, new CachableCall<Integer>(){
			@Override
			public Integer call() {
				DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
				query.add(Restrictions.eq("cinemaid", cinemaId));
				if(fyrq != null){
					query.add(Restrictions.eq("playdate", fyrq));
				}else{
					query.add(Restrictions.ge("playdate", DateUtil.getBeginningTimeOfDay(new Date())));
				}
				if(movieid != null){
					query.add(Restrictions.eq("movieid", movieid));
				}
				if(fyrq != null){
					Date cur = DateUtil.currentTime();
					Date date = DateUtil.getBeginningTimeOfDay(cur);
					if(DateUtil.getDiffDay(date, fyrq)==0){
						String playtime=DateUtil.format(cur, "HH:mm");
						query.add(Restrictions.ge("playtime", playtime));
					}
				}
				query.setProjection(Projections.rowCount());
				List qryResult = hibernateTemplate.findByCriteria(query);
				Integer c = Integer.parseInt(qryResult.get(0) + "");
				return c;
			}
		});
		return result;
	}
	@Override
	public Integer getMovieCurMpiCount(String citycode, Long movieId) {
		String key = CacheConstant.buildKey("plcxjwyingetyvgetMovieCurMpiCount", citycode, movieId);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(count!=null) return count;
		Date current = DateUtil.getBeginningTimeOfDay(new Date());
		String queryString = "select count(*) from MoviePlayItem mpi where mpi.playdate >= ? and mpi.movieid = ? and  mpi.citycode = ? ";
		List result = hibernateTemplate.find(queryString, current, movieId, citycode);
		Integer c = new Integer(result.get(0)+"");
		cacheService.set(CacheConstant.REGION_TWENTYMIN, key, c);
		return c;
	}
	@Override
	public Integer getMovieCurMpiCountByPlaydate(final String citycode, final Long movieId, final Date playdate) {
		final Date current = DateUtil.getBeginningTimeOfDay(playdate);
		String key = CacheConstant.buildKey("plcxjwyingetyvgetgetMovieCurMpiCountByPlaydate", citycode, movieId, playdate);
		Integer count = helper.cacheCall(key, CacheConstant.SECONDS_TWENTYMIN, new CachableCall<Integer>(){
			@Override
			public Integer call() {
				String playtime = "00:00";
				Date curDate = DateUtil.getCurDate();
				if(DateUtil.getDiffDay(curDate, playdate)==0){
					playtime=DateUtil.format(new Date(), "HH:mm");
				}
				String queryString = "select count(id) from MoviePlayItem mpi where mpi.playdate = ? and mpi.playtime>=? and mpi.movieid = ? and  mpi.citycode = ? ";
				List result = hibernateTemplate.find(queryString, current, playtime, movieId, citycode);
				Integer c = new Integer(result.get(0)+"");
				return c;
			}
		});
		return count;
	}
	
	@Override
	public Integer getMovieCurMpiCount(String citycode, Long movieId, Date startdate, Date enddate) {
		String key = CacheConstant.buildKey("getMovieCurMpiCountDaDate", citycode, movieId, startdate, enddate);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(count!=null) return count;
		String queryString = "select count(*) from MoviePlayItem mpi where mpi.playdate>=? and mpi.playdate<=? and mpi.movieid = ? and  mpi.citycode = ?";
		List result = hibernateTemplate.find(queryString, startdate, enddate, movieId, citycode);
		Integer c = new Integer(result.get(0)+"");
		cacheService.set(CacheConstant.REGION_HALFHOUR, key, c);
		return c;
	}
	
	@Override
	public Integer getPlayCinemaCountByPlayDate(final String citycode, final Long movieId, Date playdate){
		final Date date = DateUtil.getBeginningTimeOfDay(playdate);
		String key = CacheConstant.buildKey("plcgCiCountByPlayDate", citycode, movieId, playdate);
		Integer count = helper.cacheCall(key, CacheConstant.SECONDS_TWENTYMIN, new CachableCall<Integer>(){
			@Override
			public Integer call() {
				String playtime = "00:00";
				Date curDate = DateUtil.getCurDate();
				if(DateUtil.getDiffDay(curDate, date)==0){
					playtime=DateUtil.format(new Date(), "HH:mm");
				}
				String chql = "select count(distinct mpi.cinemaid) from MoviePlayItem mpi where mpi.movieid = ? and  mpi.citycode = ? and mpi.playdate = ? and playtime>?";
				List result = hibernateTemplate.find(chql, movieId, citycode, date, playtime);
				Integer c = Integer.parseInt(""+result.get(0));
				return c;
			}
		});
		return count;
	}
	@Override
	public Movie getMovieByName(String movieName){
		List<Long> movieidList = hibernateTemplate.find("select id from Movie m where m.moviename = ?",movieName);
		if(movieidList.size()>0) return baseDao.getObject(Movie.class, movieidList.get(0));
		return null;
	}
	@Override
	public Integer getMovieCountByName(Long movieid, String movieName){
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.eq("moviename", movieName));
		if(movieid != null) query.add(Restrictions.ne("id", movieid));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}

	//获取当前在放映电影的数量
	@Override
	public Integer getCurMovieCount(final String citycode) {
		String key = CacheConstant.buildKey("getCuovieXCount", citycode);
		Integer count = helper.cacheCall(key, CacheConstant.SECONDS_HALFHOUR, new CachableCall<Integer>() {
			@Override
			public Integer call() {
				String query = "select count(distinct mpi.movieid) from MoviePlayItem mpi where mpi.playdate >= ?"
						+ " and  mpi.citycode = ? ";
				List result = hibernateTemplate.find(query, DateUtil.getBeginningTimeOfDay(new Date()), citycode);
				Integer ct = Integer.parseInt("" + result.get(0));
				return ct;
			}
		});
		
		return count;
	}
	@Override
	public List<Movie> getCurMovieList(String citycode) {
		String key = CacheConstant.buildKey("getCu234xvieList", citycode);
		List<Long> movieidList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(movieidList==null){
			String query = "select distinct mpi.movieid from MoviePlayItem mpi where mpi.playdate >= ?"
					+ " and  mpi.citycode = ? ";
			movieidList = hibernateTemplate.find(query, DateUtil.getCurDate(), citycode);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, movieidList);
		}
		List<Movie> movieList = baseDao.getObjectList(Movie.class, movieidList);
		Collections.sort(movieList, new PropertyComparator("clickedtimes", false, false));
		return movieList;
	}
	@Override
	public List<Movie> getCurMovieList() {
		String query = "select distinct mpi.movieid from MoviePlayItem mpi where mpi.playdate >= ?";
		List<Long> movieidList = hibernateTemplate.find(query, DateUtil.getCurDate());
		List<Movie> movieList = baseDao.getObjectList(Movie.class, movieidList);
		Collections.sort(movieList, new PropertyComparator("moviename", false, true));
		return movieList;
	}
	@Override
	public boolean updateMovieHotValue(Long movieId, Integer value) {
		Movie movie = baseDao.getObject(Movie.class, movieId);
		if(movie!=null){
			movie.setHotvalue(value);
			movie.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(movie);
		}
		return true;
	}
	@Override
	public boolean updateCinemaHotValue(Long cinemaId, Integer value) {
		Cinema cinema = baseDao.getObject(Cinema.class, cinemaId);
		if(cinema!=null){
			cinema.setHotvalue(value);
			baseDao.saveObject(cinema);
		}
		return true;
	}
	
	@Override
	public List<Movie> getFutureMovieList(Date endDate,int from, int maxnum) {
		String key = CacheConstant.buildKey("getFutureMovieListendDatefrommaxnum", endDate, from, maxnum);
		List<Long> idList = (List<Long>)cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(idList == null){
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
			if(endDate!=null) query.add(Restrictions.le("releasedate", endDate));
			else query.add(Restrictions.isNotNull("releasedate"));
			query.add(Restrictions.gt("releasedate", DateUtil.getCurDate()));
			query.setProjection(Projections.property("id"));
			query.addOrder(Order.asc("releasedate"));
			idList = readOnlyTemplate.findByCriteria(query, from, maxnum);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		List<Movie> result = baseDao.getObjectList(Movie.class, idList);
		return result;
	}
	@Override
	public List<Movie> getFutureMovieList(int from, int maxnum, String order) {
		String key = CacheConstant.buildKey("getFutureMovieListdeaedfa", from, maxnum, order);
		List<Movie> result = (List<Movie>)cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(result != null){
			return result;
		}
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.gt("playdate", DateUtil.formatDate(DateUtil.getCurDate())));
		query.add(Restrictions.isNotNull("playdate"));
		query.setProjection(Projections.property("id"));
		if(StringUtils.isNotBlank(order)) {
			query.addOrder(Order.desc(order));
		}else {
			query.addOrder(Order.asc("releasedate"));
			query.addOrder(Order.asc("playdate"));
		}
		List<Long> idList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		result = baseDao.getObjectList(Movie.class, idList);
		if(!result.isEmpty()){
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, result);
		}
		return result;
	}
	@Override
	public List<MoviePlayItem> getCurMpiList(final Long cinemaId, final Long movieId, final Date playdate) {
		Assert.notNull(playdate);
		if(cinemaId==null && movieId == null) throw new IllegalArgumentException("参数不全");
		
		String key = CacheConstant.buildKey("getCurMpiLisXyz", cinemaId, movieId, playdate);
		List<Long> idList = helper.cacheCall(key, CacheConstant.SECONDS_TENMIN, new CachableCall<List<Long>>(){
			@Override
			public List<Long> call() {
				DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class, "m");
				query.add(Restrictions.eq("m.cinemaid", cinemaId));
				if(movieId !=null) query.add(Restrictions.eq("m.movieid", movieId));
				query.add(Restrictions.eq("m.playdate", playdate));
				query.setProjection(Projections.property("id"));
				query.addOrder(Order.asc("m.playtime"));
				List<Long> result = hibernateTemplate.findByCriteria(query);
				return result;
			}
		});
		
		List<MoviePlayItem> result = baseDao.getObjectList(MoviePlayItem.class, idList); 
		return result;
	}
	
	@Override
	public List<Long> getPlayCinemaIdList(final String citycode, final Long movieId, final Date playdate) {
		String key = CacheConstant.buildKey("plcxjwyinYWeaIdList", citycode, movieId, playdate);
		List<Long> result = helper.cacheCall(key, CacheConstant.SECONDS_TWENTYMIN, new CachableCall<List<Long>>(){
			@Override
			public List<Long> call() {
				DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
				query.add(Restrictions.eq("citycode", citycode));
				if(playdate != null) {
					query.add(Restrictions.eq("playdate", playdate));
				}
				if(movieId != null) {
					query.add(Restrictions.eq("movieid", movieId));
				}
				query.setProjection(Projections.distinct(Projections.property("cinemaid")));
				List<Long> ret = hibernateTemplate.findByCriteria(query);
				return ret;
			}
		});
		return result;
	}
	@Override
	public List<Cinema> getPlayCinemaList(String citycode, Long movieId, Date playdate, int from, int maxnum) {
		List<Long> idList = getPlayCinemaIdList(citycode, movieId, playdate);
		if(idList.size() <= from) return new ArrayList<Cinema>();
		List<Cinema> cinemaList = baseDao.getObjectList(Cinema.class, idList);
		Collections.sort(cinemaList, new MultiPropertyComparator(new String[]{"booking", "hotvalue", "clickedtimes"}, new boolean[]{false, false, false}));
		
		return BeanUtil.getSubList(cinemaList, from, maxnum);
	}
	@Override
	public List<Long> getPlayCinemaIdListByCountycode(final String countycode, final Long movieId, final Date playdate) {
		String key = CacheConstant.buildKey("plcYWnYByCountyCodet", countycode, movieId, playdate);
		List<Long> result = helper.cacheCall(key, CacheConstant.SECONDS_TWENTYMIN, new CachableCall<List<Long>>(){
			@Override
			public List<Long> call() {
				DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "c");
				query.add(Restrictions.eq("countycode", countycode));
				DetachedCriteria mpiquery = DetachedCriteria.forClass(MoviePlayItem.class, "mpi");
				if(movieId!=null){
					mpiquery.add(Restrictions.eq("mpi.movieid", movieId));
				}
				if(playdate!=null){
					mpiquery.add(Restrictions.eq("mpi.playdate", playdate));
				}
				mpiquery.add(Restrictions.eqProperty("c.id", "mpi.cinemaid"));
				mpiquery.setProjection(Projections.distinct(Projections.property("mpi.cinemaid")));
				
				query.add(Subqueries.exists(mpiquery));
				query.addOrder(Order.desc("c.booking"));
				query.addOrder(Order.desc("c.hotvalue"));
				query.addOrder(Order.desc("c.clickedtimes"));
				query.setProjection(Projections.property("id"));
				List<Long> ret = hibernateTemplate.findByCriteria(query);
				return ret;
			}
		});
		return result;
	}
	@Override
	public List<Cinema> getPlayCinemaListByCountycode(String countycode, Long movieid, Date playdate) {
		List<Long> idList = getPlayCinemaIdListByCountycode(countycode, movieid, playdate);
		List<Cinema> cinemaList = baseDao.getObjectList(Cinema.class, idList);
		return cinemaList;
	}
	
	@Override
	public List<Long> getCurCinemaIdList(String citycode, Long movieid, Date playdate){
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class, "m");
		query.add(Restrictions.eq("m.citycode", citycode));
		query.add(Restrictions.eq("m.movieid", movieid));

		query.add(Restrictions.eq("m.playdate", playdate));
		Date cur = DateUtil.currentTime();
		Date date = DateUtil.getBeginningTimeOfDay(cur);
		if(DateUtil.getDiffDay(date, playdate)==0){
			String playtime=DateUtil.format(cur, "HH:mm");
			query.add(Restrictions.ge("m.playtime", playtime));
		}
		query.setProjection(Projections.distinct(Projections.property("m.cinemaid")));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		return idList;
	}
	@Override
	public List<CinemaRoom> getCurCinemaRoomByMovieId(String citycode, Long movieid, Date playdate){
		DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "c");
		DetachedCriteria subquery = DetachedCriteria.forClass(MoviePlayItem.class,"m");
		subquery.add(Restrictions.eqProperty("c.id", "m.roomid"));
		subquery.add(Restrictions.eq("m.playdate", playdate));
		subquery.add(Restrictions.eq("m.movieid", movieid));
		subquery.add(Restrictions.eq("m.citycode", citycode));
		Date cur = DateUtil.currentTime();
		Date date = DateUtil.getBeginningTimeOfDay(cur);
		if(DateUtil.getDiffDay(date, playdate)==0){
			String playtime=DateUtil.format(cur, "HH:mm");
			subquery.add(Restrictions.ge("m.playtime", playtime));
		}
		subquery.setProjection(Projections.property("m.roomid"));
		query.add(Subqueries.exists(subquery));
		query.add(Restrictions.isNotNull("c.characteristic"));
		return hibernateTemplate.findByCriteria(query);
	}
	
	private final String getPlayCinemaCount = "select count(distinct mpi.cinemaid) from MoviePlayItem mpi where mpi.movieid = ?"
		 + " and  mpi.citycode = ? and mpi.playdate >= ?";
	@Override
	public Integer getPlayCinemaCount(String citycode, Long movieId){
		String key = CacheConstant.buildKey("plcxjwyingetxxPlayCinemaCount", citycode, movieId);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(count!=null) return count;
		List result = hibernateTemplate.find(getPlayCinemaCount, movieId, citycode,DateUtil.getBeginningTimeOfDay(new Date()));
		Integer c = Integer.parseInt(""+result.get(0));
		cacheService.set(CacheConstant.REGION_TENMIN, key, c);
		return c;
	}
	
	@Override
	public Integer getOrderPlayCinemaCount(String citycode, Long movieid) {
		String query = "select count(distinct mpi.cinemaid) from MoviePlayItem mpi where mpi.movieid = ? and mpi.cinemaid in (select cp.id from CinemaProfile cp where cp.status = ?)"
		+ " and  mpi.citycode = ? ";
		List result = hibernateTemplate.find(query, movieid, CinemaProfile.STATUS_OPEN, citycode);
		return Integer.parseInt(""+result.get(0));
	}
	
	final String qm = "from Movie m where m.id in (select distinct mpi.movieid from MoviePlayItem mpi where mpi.cinemaid = ? and mpi.playdate = ?) order by m.hotvalue desc, m.clickedtimes desc";
	@Override
	public List<Date> getCurMoviePlayDate(String citycode, Long movieId) {
		Date cur = DateUtil.getBeginningTimeOfDay(new Date());
		String key = CacheConstant.buildKey("xy3CurxePlayDatexy", citycode, movieId, cur);
		List<Date> result = (List<Date>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(result==null || result.isEmpty()){
			String psql1 = "select distinct playdate from MoviePlayItem mpi where mpi.movieid=? and mpi.playdate >= ? and mpi.citycode=?";
			result = hibernateTemplate.find(psql1, movieId, cur, citycode);
			Collections.sort(result);
			cacheService.set(CacheConstant.REGION_TENMIN, key, result);
		}
		
		return result;
	}
	@Override
	public List<Date> getCurMoviePlayDate2(String citycode, Long movieId) {
		String key = CacheConstant.buildKey("xy3CurxePlayDatexyAndHHmm", citycode, movieId);
		List<Date> result = (List<Date>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(result==null || result.isEmpty()){
			Date curDate = new Date();
			Date cur = DateUtil.getBeginningTimeOfDay(curDate);
			String psql1 = "select distinct playdate from MoviePlayItem mpi where mpi.movieid=? and ((mpi.playdate = ? and mpi.playtime > ?) or mpi.playdate > ?) and mpi.citycode=?";
			result = hibernateTemplate.find(psql1, movieId, cur, DateUtil.format(curDate, "HH:mm"), cur,citycode);
			Collections.sort(result);
			cacheService.set(CacheConstant.REGION_TENMIN, key, result);
		}
		
		return result;
	}
	@Override
	public List<Date> getCurCinemaPlayDate(Long cinemaId) {
		String psql2 = "select distinct playdate from MoviePlayItem mpi where mpi.cinemaid=? and mpi.playdate >= ?";
		List<Date> result = hibernateTemplate.find(psql2, cinemaId, DateUtil.getBeginningTimeOfDay(new Date()));
		Collections.sort(result);
		return result;
	}
	@Override
	public List<Movie> getCurMovieListByCinemaId(Long cinemaId) {
		String key = CacheConstant.buildKey("getCuroviexjk23tListBnemaId", cinemaId);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(idList == null){
			String qcurMovie = "select m.id from Movie m where m.id in (select distinct mpi.movieid from MoviePlayItem mpi where mpi.cinemaid = ? and mpi.playdate >=?) order by m.hotvalue desc, m.clickedtimes desc";
			idList = hibernateTemplate.find(qcurMovie, cinemaId,DateUtil.getBeginningTimeOfDay(new Date()));
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, idList);
		}
		return this.baseDao.getObjectList(Movie.class, idList);
	}
	@Override
	public Integer getCinemaMpiCountByDate(Long cinemaId, Date date) {
		String key = CacheConstant.buildKey("japosihghrlgegetCinemaMpiCountByDate", cinemaId, date);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(count!=null) {
			return count;
		}
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("cinemaid", cinemaId));
		query.add(Restrictions.eq("playdate", date));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		Integer c = new Integer(result.isEmpty() ? "0" : result.get(0)+"");
		cacheService.set(CacheConstant.REGION_TENMIN, key, c);
		return c;
	}
	
	@Override
	public Integer getCinemaMovieCountByDate(final Long cinemaId, final Date date) {
		String key = CacheConstant.buildKey("japosihghrlgetCinemaMovieCountByDate", cinemaId, date);
		Integer count = helper.cacheCall(key, CacheConstant.SECONDS_HALFHOUR, new CachableCall<Integer>() {
			@Override
			public Integer call() {
				Date mydate = date;
				String qry = "select count(distinct movieid) from MoviePlayItem mpi where mpi.cinemaid=? ";
				if(date != null){
					qry += " and mpi.playdate=? ";
				}else{
					mydate = DateUtil.getBeginningTimeOfDay(new Date());
					qry += " and mpi.playdate >= ? ";
				}
				List result = hibernateTemplate.find(qry, cinemaId, mydate);
				Integer c = new Integer(result.get(0)+"");
				return c;
			}
		});
		return count;
	}
	@Override
	public List<Movie> searchMovieByName(String moviename) {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.ilike("moviename", moviename, MatchMode.ANYWHERE));
		List result = hibernateTemplate.findByCriteria(query);
		return result;
	}
	@Override
	public List<MoviePlayItem> getCinemaCurMpiListByDate(Long cinemaId, Date date) {
		String query = "select id from MoviePlayItem mpi where mpi.cinemaid = ? and mpi.playdate = ? order by mpi.playtime, mpi.id";
		List<Long> idList = hibernateTemplate.find(query, cinemaId, date);
		List<MoviePlayItem> result = baseDao.getObjectList(MoviePlayItem.class, idList);
		return result;
	}
	@Override
	public List<MoviePlayItem> getCinemaCurMpiListByRoomIdAndDate(Long roomId, Date playdate) {
		String query = "select id from MoviePlayItem mpi where mpi.roomid = ? and mpi.playdate = ? order by playtime";
		List<Long> idList = hibernateTemplate.find(query, roomId, playdate);
		List<MoviePlayItem> result = baseDao.getObjectList(MoviePlayItem.class, idList);
		return result;
	}
	@Override
	public List<Movie> getCurMovieListByCinemaIdAndDate(Long cinemaId, Date date) {
		String query = "select movieid from MoviePlayItem mpi where mpi.cinemaid = ? and mpi.playdate = ? group by mpi.movieid order by count(mpi.movieid) desc";
		List<Long> idList = hibernateTemplate.find(query, cinemaId, date);
		List<Movie> movieList = baseDao.getObjectList(Movie.class, idList);
		return movieList;
	}
	/**
	 *  查询当前正在上映的影片
	 * */
	@Override
	public List<Movie> getCurMovieListByMpiCount(String citycode, int from, int maxnum){
		List<Long> idList = getCurMovieIdList(citycode);
		idList = BeanUtil.getSubList(idList, from, maxnum);
		List<Movie> movieList = baseDao.getObjectList(Movie.class, idList);
		return movieList;
	}
	@Override
	public List<Long> getCurMovieIdList(String citycode){
		return getCurMovieIdList(citycode,null);
	}
	@Override
	public List<Long> getCurMovieIdList(String citycode,Date playdate){
		List<Map> rowList = getMovieMpiCountList(citycode,playdate);
		List<Long> idList = new ArrayList<Long>();
		for(Map row:rowList){
			idList.add((Long) row.get("movieid"));
		}
		return idList;
	}
	@Override
	public List<Movie> getHotPlayMovieList(String citycode){
		List<Map> rowList = getMovieMpiCountList(citycode,null);
		List<Long> idList = new ArrayList<Long>();
		for(Map row:rowList){
			idList.add((Long) row.get("movieid"));
		}
		List<Movie> tmpList = baseDao.getObjectList(Movie.class, idList);
		Date last = DateUtil.addDay(DateUtil.getCurDate(), -30);
		List<Movie> movieList = new ArrayList<Movie>();
		for(Movie movie:tmpList){
			if(movie.getReleasedate()!=null && movie.getReleasedate().after(last)){
				movieList.add(movie);
			}
		}
		return movieList;
	}
	private List<Map> getMovieMpiCountList(String citycode, Date date) {
		String key = CacheConstant.buildKey("getMov3MpdjeyidytList", citycode, date);
		List<Map> result = (List<Map>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(result==null){
			DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class, "m");
			List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
			if(citycodeList.size()==1){
				if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)) query.add(Restrictions.eq("m.citycode", citycode)); 
			}else if(citycodeList.size()>1){
				query.add(Restrictions.in("m.citycode", citycodeList));
			}
			
			if(date == null) query.add(Restrictions.ge("m.playdate",DateUtil.getBeginningTimeOfDay(new Date())));
			else query.add(Restrictions.eq("m.playdate",DateUtil.getBeginningTimeOfDay(date)));
			query.setProjection(Projections.projectionList()
					.add(Projections.alias(Projections.groupProperty("m.movieid"),"movieid"))
					.add(Projections.alias(Projections.rowCount(),"count")));
			query.addOrder(Order.desc("count"));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			result = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, result);
		}
		return new ArrayList<Map>(result);
	}
	@Override
	public List<Long> getMovieIdByMoviename(String keyname) {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.like("moviename", keyname, MatchMode.ANYWHERE));
		query.setProjection(Projections.property("id"));
		List<Long> list = hibernateTemplate.findByCriteria(query);
		return list;
	}

	@Override
	public CinemaRoom getRoomByRoomname(Long cinemaId, String roomname) {
		if(StringUtils.isBlank(roomname)) return null;
		String query = "from CinemaRoom r where r.cinemaid = ? and r.roomname = ?";
		List<CinemaRoom> result = hibernateTemplate.find(query, cinemaId, roomname);
		if(result.size() >0 ) return result.get(0);
		return null;
	}
	@Override
	public CinemaRoom getRoomByRoomnum(Long cinemaId, String roomname) {
		if(StringUtils.isBlank(roomname)) return null;
		String query = "from CinemaRoom r where r.cinemaid = ? and r.num = ?";
		List<CinemaRoom> result = hibernateTemplate.find(query, cinemaId, roomname);
		if(result.size() >0 ) return result.get(0);
		return null;
	}
	@Override
	public List<Map> getGroupMoviePlayItemByCinema_Time(String citycode,Long mid,Date d,String playtime,String time1,String time2){
		String hql = "select new map(c.countycode as code, count(c.id) as cinemacount) from Cinema c where c.countycode is not null and c.citycode=? and c.id in (select distinct m.cinemaid from MoviePlayItem m where m.movieid=? and m.playdate = ? and m.playtime>=? and m.playtime>=? and m.playtime<?) group by c.countycode having count(*) > 0";
		List<Map> list = hibernateTemplate.find(hql,citycode, mid , d , playtime,time1,time2);
		for(Map entry:list){
			entry.put("county", baseDao.getObject(County.class, (String)entry.get("code")));
		}
		return list;
	}
	@Override
	public void sortMoviesByMpiCount(String citycode, List<Movie> movieList){
		List<Long> movieidList = getCurMovieIdList(citycode);
		Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
		Movie tmp = null;
		movieList.clear();
		for(Long movieid: movieidList){
			tmp = movieMap.remove(movieid);
			if(tmp!=null) movieList.add(tmp);
		}
		movieList.addAll(movieMap.values());
	}
	
	@Override
	public MoviePlayItem getUniqueMpi(String opentype, Long cinemaid, Long roomid, Date playdate, String playtime){
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.add(Restrictions.eq("playdate", playdate));
		query.add(Restrictions.eq("playtime", playtime));
		query.add(Restrictions.eq("opentype", opentype));
		
		if(roomid!=null) query.add(Restrictions.eq("roomid", roomid));
		else query.add(Restrictions.isNull("roomid"));
		query.addOrder(Order.asc("id"));
		List<MoviePlayItem> mpiList = hibernateTemplate.findByCriteria(query);
		if(mpiList.size() > 0) return mpiList.get(0);
		return null;
	}
	@Override
	public MoviePlayItem getUniqueMpi2(Long cinemaid, Long movieid, Date playdate, String playtime){
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.add(Restrictions.eq("playdate", playdate));
		query.add(Restrictions.eq("playtime", playtime));
		query.add(Restrictions.eq("movieid", movieid));
		query.add(Restrictions.isNull("roomid"));
		query.addOrder(Order.asc("id"));
		List<MoviePlayItem> mpiList = hibernateTemplate.findByCriteria(query);
		if(mpiList.size() > 0) return mpiList.get(0);
		return null;
	}
	@Override
	public GrabTicketMpi getGrabTicketMpiListByMpid(Long mpid) {
		DetachedCriteria query = DetachedCriteria.forClass(GrabTicketMpi.class);
		query.add(Restrictions.eq("mpid", mpid));
		List<GrabTicketMpi> list = hibernateTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}
	@Override
	public Integer getTicketCinemaCount(String citycode, String countycode, String indexareacode, String cname) {
		return getCinemaCount(citycode, countycode, indexareacode, cname, true);
	}
	
	@Override
	public Integer getCinemaCount(String citycode, String countycode, String indexareacode, String cname, boolean booking){
		String key = CacheConstant.buildKey("getTicket3xstCinemaCount", citycode, countycode, indexareacode, cname, booking);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(count == null){
			DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "p");
			if(StringUtils.isNotBlank(indexareacode)){
				query.add(Restrictions.eq("p.indexareacode", indexareacode));
			}else if(StringUtils.isNotBlank(countycode)){
				query.add(Restrictions.eq("p.countycode", countycode));
			}else {
				query.add(Restrictions.eq("p.citycode", citycode));
			}
			if(StringUtils.isNotBlank(cname)) query.add(Restrictions.ilike("p.name", cname.trim().toString(),MatchMode.ANYWHERE));
			if(booking){
				query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
			}
			query.setProjection(Projections.rowCount());
			List result = hibernateTemplate.findByCriteria(query);
			count = new Integer(result.get(0)+"");
			cacheService.set(CacheConstant.REGION_TENMIN, key, count);
		}
		return count;
	}
	
	@Override
	public List<Cinema> getNearCinemaList(double ld/*pointx*/, double la/*pointy*/, int distance/*meter*/, Long movieid, String citycode, Date playdate) {
		List<Cinema> cinemaList = getPlayCinemaList(citycode, movieid, playdate, 0, 300);
		return getCinemaListByNearOrder(ld, la, cinemaList, distance, true);
	}
	@Override
	public List<Cinema> getCinemaListByNearOrder(double ld/*pointx*/, double la, List<Cinema> cinemaList, int distance, boolean validDistance) {
		OuterSorter sorter = new OuterSorter<Cinema>(false);
		for(Cinema cinema: cinemaList){
			try{
				long length = Math.round(LongitudeAndLatitude.getDistance(ld, la, Double.parseDouble(cinema.getPointx()), Double.parseDouble(cinema.getPointy())));
				if(validDistance){
					if(length < distance) {
						sorter.addBean(length, cinema);
					}
				}else {
					sorter.addBean(length, cinema);
				}
			}catch(Exception e){
				dbLogger.warn(e.getMessage() + ",cinemaId:" + cinema.getId());
			}
		}
		cinemaList = sorter.getAscResult();
		return cinemaList;
	}
	@Override
	public List<Cinema> getCinemaListByIndexareaCodeCountycodeMovie(String countycode, String indexareacode, Long movieid) {
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "c");
		if(StringUtils.isNotBlank(indexareacode)) query.add(Restrictions.eq("c.indexareacode", indexareacode));
		else query.add(Restrictions.eq("c.countycode", countycode));
		if(movieid!=null){
			DetachedCriteria subQuery = DetachedCriteria.forClass(MoviePlayItem.class, "mp");
			subQuery.add(Restrictions.eq("mp.movieid", movieid));
			subQuery.add(Restrictions.eqProperty("mp.cinemaid", "c.id"));
			subQuery.setProjection(Projections.property("mp.id"));
			query.add(Subqueries.exists(subQuery));
		}
		
		List<Cinema> cinemaList = hibernateTemplate.findByCriteria(query);
		return cinemaList;
	}
	@Override
	public List<Long> getBookingCinemaIdList(String citycode, String countycode){
		String key = CacheConstant.buildKey("bookingXxnemaidlist", citycode, countycode);
		List<Long> result = (List<Long>)cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(result != null){
			return result;
		}
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
		List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
		if(StringUtils.isNotBlank(countycode)){
			query.add(Restrictions.eq("countycode", countycode));
		}else if(citycodeList.size()==1){
			if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)) query.add(Restrictions.eq("citycode", citycode)); 
		}else{
			query.add(Restrictions.in("citycode", citycodeList));
		}
		query.setProjection(Projections.id());
		query.addOrder(Order.desc("avggeneral"));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		cacheService.set(CacheConstant.REGION_HALFHOUR, key, idList);
		return idList;
	}
	@Override
	public List<Cinema> getBookingCinemaList(String citycode) {
		List<Long> cinemaidList = getBookingCinemaIdList(citycode, null);
		List<Cinema> result = baseDao.getObjectList(Cinema.class, cinemaidList);
		return result;
	}
	@Override
	public List<Cinema> getCinemaListByCitycode(String citycode, int from, int maxnum) {
		//TODO:新增加影院清理
		String key = CacheConstant.buildKey("get244CinemaLi46st", citycode);
		List<Long> cinemaidList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(cinemaidList == null){
			DetachedCriteria query=DetachedCriteria.forClass(Cinema.class);
			query.setProjection(Projections.property("id"));
			query.add(Restrictions.eq("citycode", citycode));
			query.addOrder(Order.desc("booking"));
			query.addOrder(Order.desc("clickedtimes"));
			cinemaidList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_ONEHOUR, key, cinemaidList);
		}
		List<Long> idList = BeanUtil.getSubList(cinemaidList, from, maxnum);
		List<Cinema> cinemaList = baseDao.getObjectList(Cinema.class, idList);
		return cinemaList;
	}
	@Override
	public Integer getCinemaCountByCitycode(String citycode) {
		DetachedCriteria query=DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.setProjection(Projections.rowCount());
		List list=hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	private static String getOpenMovieListQuery = "select distinct movieid from OpenPlayItem opi " +
			"where status = ? and opentime<=? and closetime > ? and playtime> ? and gsellnum < asellnum " + 
			"and opi.citycode=? ";
	@Override
	public List<Movie> getOpenMovieList(final String citycode) {
		String key = CacheConstant.buildKey("getOpes6nMovieL478ist", citycode);
		List<Long> movieidList = helper.cacheCall(key, CacheConstant.SECONDS_HALFHOUR, new CachableCall<List<Long>>(){
			@Override
			public List<Long> call() {
				Timestamp cur = new Timestamp(System.currentTimeMillis());
				List<Long> ret = hibernateTemplate.find(getOpenMovieListQuery, OpiConstant.STATUS_BOOK, cur, cur, cur, citycode);
				return ret;
			}
		});
		List<Movie> movieList = baseDao.getObjectList(Movie.class, movieidList);
		return movieList;
	}
	@Override
	public List<Long> getSpecialActivityOpiMovieList(Long aid, Date playdate, String citycode) {
		String key = CacheConstant.KEY_SAOPI + (playdate==null?"":DateUtil.formatDate(playdate)) + aid;
		List<Long> rowList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(rowList == null){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			Timestamp timeFrom, timeTo;
			if(playdate==null){
				timeFrom = cur;
				timeTo = DateUtil.addDay(cur, 15);
			}else{
				timeFrom = DateUtil.getBeginningTimeOfDay(new Timestamp(playdate.getTime()));
				timeTo = DateUtil.getLastTimeOfDay(timeFrom);
			}
			String hql = "select distinct opi.movieid from OpenPlayItem opi where status = ? and opentime<=? and closetime > ? " +
					"and gsellnum < asellnum and opi.playtime >= ? and opi.playtime <= ? and opi.citycode = ? and " +
					"opi.mpid in (select mpi.id from MoviePlayItem mpi where opi.mpid=mpi.id and mpi.batch = ?)";
			rowList = hibernateTemplate.find(hql, OpiConstant.STATUS_BOOK, cur, cur, timeFrom, timeTo, citycode, aid);
			cacheService.set(CacheConstant.REGION_TENMIN, key, rowList);
		}
		return rowList;
	}
	
	@Override
	public List<MultiPlay> getCurMultyPlayList(Long cinemaid) {
		Date cur = DateUtil.getCurDate();
		String query = "from MultiPlay where cinemaid=? and playdate>=? ";
		List<MultiPlay> multiList = hibernateTemplate.find(query, cinemaid, cur);
		return multiList;
	}

	@Override
	public List<Cinema> getHotBookingCinames(String citycode, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.addOrder(Order.desc("booking"));
		query.addOrder(Order.desc("clickedtimes"));
		List<Cinema> cinemaList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return cinemaList;
	}
	@Override
	public void sortTodayMoviesByMpiCount(String citycode, List<Movie> movieList) {
		List<Map> rowList = getMovieMpiCountList(citycode,new Date());
		List<Long> movieidList = new ArrayList<Long>();
		for(Map row:rowList){
			movieidList.add((Long) row.get("movieid"));
		}
		Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
		Movie tmp = null;
		movieList.clear();
		for(Long movieid: movieidList){
			tmp = movieMap.remove(movieid);
			if(tmp!=null) movieList.add(tmp);
		}
		movieList.addAll(movieMap.values());
	}
	@Override
	public List<Cinema> getCinemaList(String citycode, String countycode, int from, int maxnum, Long movieid, boolean onlyBooking,boolean hasPlay){
		List<Long> idList = null;
		String key = CacheConstant.buildKey("searchCinema254y", citycode, countycode, movieid, onlyBooking);
		idList = (List<Long>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(idList == null){
			DetachedCriteria  query = DetachedCriteria.forClass(Cinema.class,"c");
			if(onlyBooking){
				query.add(Restrictions.eq("c.booking", Cinema.BOOKING_OPEN));
			}
			if(StringUtils.isNotBlank(countycode)){
				query.add(Restrictions.eq("c.countycode",countycode));
			}else{
				query.add(Restrictions.eq("c.citycode", citycode));
			}
			if(movieid != null){
				DetachedCriteria playItem = null;
				if(onlyBooking){
					Timestamp curtime = new Timestamp(System.currentTimeMillis());
					playItem = DetachedCriteria.forClass(OpenPlayItem.class, "mp");
					playItem.add(Restrictions.eq("mp.movieid", movieid));
					playItem.add(Restrictions.eq("mp.status", OpiConstant.STATUS_BOOK));
					playItem.add(Restrictions.le("mp.opentime", curtime));
					playItem.add(Restrictions.gt("mp.closetime", curtime));
					playItem.add(Restrictions.eq("mp.citycode", citycode));
					playItem.add(Restrictions.gt("mp.playtime", curtime));
					playItem.add(Restrictions.ltProperty("mp.gsellnum", "mp.asellnum"));
					playItem.add(Restrictions.eqProperty("mp.cinemaid", "c.id"));
					playItem.setProjection(Projections.property("mp.id"));
				}else {
					playItem = DetachedCriteria.forClass(MoviePlayItem.class,"mp");
					playItem.add(Restrictions.eqProperty("mp.cinemaid", "c.id"));
					playItem.add(Restrictions.ge("mp.playdate", DateUtil.getBeginningTimeOfDay(new Date())));
					playItem.add(Restrictions.eq("mp.movieid", movieid));
					playItem.setProjection(Projections.property("mp.id"));
				}
				query.add(Subqueries.exists(playItem));
			}
			query.addOrder(Order.desc("generalmark"));
			query.setProjection(Projections.id());
			idList = readOnlyTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, idList);
		}
		List<Long> tmpList = BeanUtil.getSubList(idList, from, maxnum);
		return baseDao.getObjectList(Cinema.class, tmpList);
	}
	
	@Override
	public Map<String,Integer> getFeatureCinema(String citycode,String... fetures){
		Map<String,Integer> f = new HashMap<String,Integer>();
		for(String feture : fetures){
			String key = CacheConstant.buildKey("getFeatureCinema235", citycode,feture);
			Integer c = (Integer)cacheService.get(CacheConstant.REGION_HALFDAY, key);
			if(c != null){
				f.put(feture, c);
				continue;
			}
			DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "c");
			if(feture.equals(Flag.SERVICE_PARK)){
				query.add(Restrictions.like("c.otherinfo",Flag.SERVICE_PARK, MatchMode.ANYWHERE));
				query.add(Restrictions.like("c.otherinfo","\"" + Flag.SERVICE_PARK_RECOMMEND + "\":\"free", MatchMode.ANYWHERE));
			}else if(feture.equals(Flag.SERVICE_SALE)){
				DetachedCriteria subquery2=DetachedCriteria.forClass(CinemaProfile.class, "cp");
				subquery2.add(Restrictions.eq("cp.popcorn", CinemaProfile.POPCORN_STATUS_Y));
				subquery2.add(Restrictions.eqProperty("cp.id", "c.id"));
				subquery2.setProjection(Projections.property("cp.id"));
				query.add(Subqueries.exists(subquery2));
			}else{
				query.add(Restrictions.like("c.otherinfo",feture, MatchMode.ANYWHERE));
			}
			query.add(Restrictions.eq("c.citycode", citycode));
			query.setProjection(Projections.rowCount());
			c = Integer.parseInt(readOnlyTemplate.findByCriteria(query).get(0) + "");
			cacheService.set(CacheConstant.REGION_HALFDAY, key, c);
			f.put(feture, c);
		}
		return f;
	}
	@Override
	public Map<String,Integer> getRoomFeatureCinema(String citycode){
		String key = CacheConstant.buildKey("getRoomFeatureCinemard3sdd235", citycode);
		Map<String,Integer> f = (Map<String,Integer>)cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(f != null){
			return f;
		}
		DetachedCriteria subQuery = DetachedCriteria.forClass(Cinema.class, "m");
		subQuery.add(Restrictions.eq("m.citycode", citycode));
		subQuery.setProjection(Projections.property("m.id"));
		DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "c");
		query.add(Subqueries.propertyIn("c.cinemaid", subQuery));
		query.setProjection(Projections.projectionList().add(Projections.groupProperty("characteristic"),"roomFetures")
				.add(Projections.groupProperty("cinemaid"),"cinemaid")
				.add(Projections.count("characteristic"),"roomFeturesCount"));
		query.add(Restrictions.isNotNull("characteristic"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> mapList = readOnlyTemplate.findByCriteria(query);
		f = new HashMap<String,Integer>();
		for(String c : CharacteristicType.cTypeList){
			f.put(c, 0);
		}
		for(Map map : mapList){
			String feture = (String)map.get("roomFetures");
			if(CharacteristicType.cTypeList.contains(feture)){
				if(feture != null){
					if(f.get(feture) != null){
						f.put(feture,f.get(feture) + 1);
					}else{
						f.put(feture, 1);
					}
				}
			}
		}
		ArrayList<Entry<String,Integer>> tmpL = new ArrayList<Entry<String,Integer>>(f.entrySet());    
        Collections.sort(tmpL, new Comparator<Map.Entry<String, Integer>>() {    
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {    
                return (o2.getValue() - o1.getValue());    
            }    
        });
        f = new LinkedHashMap<String,Integer>();
        for(Entry<String,Integer> e : tmpL) {  
            f.put(e.getKey(),e.getValue());  
        }
		cacheService.set(CacheConstant.REGION_ONEHOUR, key, f);
		return f;
	}
	@Override
	public List<Cinema> getCinemaListBySearchCmd(SearchCinemaCommand cmd, String citycode, int from, int maxnum){
		List<Long> idList = getCinemaIdListBySearchCmd(cmd, citycode);
		idList = BeanUtil.getSubList(idList, from, maxnum);
		List<Cinema> cinemaList = baseDao.getObjectList(Cinema.class, idList);
		return cinemaList;
	}
	@Override
	public List<Long> getCinemaIdListBySearchCmd(SearchCinemaCommand cmd, String citycode){
		//TODO:缓存
		DetachedCriteria query = getCinemaQuery(cmd, citycode);
		query.setProjection(Projections.id());
		List<Long> idList = readOnlyTemplate.findByCriteria(query);
		return idList;
	}
	private DetachedCriteria getCinemaQuery(SearchCinemaCommand cmd, String citycode){
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "c");
		if(StringUtils.isNotBlank(cmd.indexareacode)){//indexareacode 和 countycode 只要一个
			query.add(Restrictions.eq("c.indexareacode", cmd.indexareacode));
		}else if(StringUtils.isNotBlank(cmd.countycode)){
			query.add(Restrictions.eq("c.countycode", cmd.countycode));
		}else{
			query.add(Restrictions.eq("c.citycode", citycode));
			if(cmd.lineId!=null){
				query.add(Restrictions.like("c.lineidlist", String.valueOf(cmd.lineId), MatchMode.ANYWHERE));
				if(cmd.stationid!=null){
					query.add(Restrictions.eq("c.stationid", new Long(cmd.stationid)));
				}
			}
		}
		if(StringUtils.isNotBlank(cmd.getHotcinema())){
			query.add(Restrictions.gt("c.hotvalue", 0));
		}
		if(StringUtils.isNotBlank(cmd.getCinemaname())){
			query.add(Restrictions.ilike("c.name", cmd.getCinemaname(), MatchMode.ANYWHERE));
		}
		if(cmd.lineId!=null){
			query.add(Restrictions.like("c.lineidlist", String.valueOf(cmd.lineId), MatchMode.ANYWHERE));
		}
		//停车位
		if(StringUtils.isNotBlank(cmd.getPark())){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_PARK, MatchMode.ANYWHERE));
			if(StringUtils.equals(cmd.getPark(), "freePark")){
				query.add(Restrictions.like("c.otherinfo","\"" + Flag.SERVICE_PARK_RECOMMEND + "\":\"free", MatchMode.ANYWHERE));
			}
		}
		//刷卡
		if(StringUtils.isNotBlank(cmd.getVisacard()) && cmd.getVisacard()!=null){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_VISACARD, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(cmd.getRefund())){
			DetachedCriteria subquery2=DetachedCriteria.forClass(CinemaProfile.class, "cp");
			subquery2.add(Restrictions.eq("cp.isRefund", CinemaProfile.POPCORN_STATUS_Y));
			subquery2.add(Restrictions.eqProperty("cp.id", "c.id"));
			subquery2.setProjection(Projections.property("cp.id"));
			query.add(Subqueries.exists(subquery2));
		}
		//爆米花
		if(StringUtils.isNotBlank(cmd.getPopcorn())){
			DetachedCriteria subquery2=DetachedCriteria.forClass(CinemaProfile.class, "cp");
			subquery2.add(Restrictions.eq("cp.popcorn", CinemaProfile.POPCORN_STATUS_Y));
			subquery2.add(Restrictions.eqProperty("cp.id", "c.id"));
			subquery2.setProjection(Projections.property("cp.id"));
			query.add(Subqueries.exists(subquery2));
		}
		//情侣座
		if(StringUtils.isNotBlank(cmd.getPairseat()) && cmd.getPairseat()!=null){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_PAIRSEAT, MatchMode.ANYWHERE));
		}
		//IMAX
		if(StringUtils.isNotBlank(cmd.getImax()) && cmd.getImax() != null){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_IMAX, MatchMode.ANYWHERE));
		}
		//儿童套餐
		if(StringUtils.isNotBlank(cmd.getChild()) && cmd.getChild() != null){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_CHILD, MatchMode.ANYWHERE));
		}
		
		if(StringUtils.equals(cmd.getBooking(), Cinema.BOOKING_OPEN)){
			if(StringUtils.equals("310000", citycode)){
				query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
			}else {
				DetachedCriteria subQry = DetachedCriteria.forClass(MoviePlayItem.class, "m");
				subQry.add(Restrictions.ge("m.playdate", DateUtil.addDay(new Date(), -1)));
				subQry.add(Restrictions.eqProperty("m.cinemaid", "c.id"));
				subQry.setProjection(Projections.property("m.cinemaid"));
				query.add(Subqueries.exists(subQry));
			}
		}
		query.addOrder(Order.desc("c.booking"));
		if(StringUtils.isNotBlank(cmd.order)){
			query.addOrder(Order.desc(cmd.order));
		}else{
			query.addOrder(Order.desc("c.hotvalue"));
		}
		query.addOrder(Order.desc("c.clickedtimes"));
		return query;
	}
	
	@Override
	public List<Long> getCinemaIdListByGoods(String citycode){
		String key = CacheConstant.buildKey("getCinemaIdListByGoodssafs223",citycode);
		List<Long> cinemaIdList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(cinemaIdList != null){
			return cinemaIdList;
		}
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(Goods.class, "g");
		query.add(Restrictions.ne("g.status", Status.DEL));
		query.add(Restrictions.eq("g.tag", GoodsConstant.GOODS_TAG_BMH));
		query.add(Restrictions.gt("g.totime", curtime));
		query.add(Restrictions.le("g.releasetime", curtime));
		query.add(Restrictions.gt("g.goodssort", 0));
		query.add(Restrictions.eq("g.citycode", citycode));
		DetachedCriteria sub = DetachedCriteria.forClass(GoodsGift.class, "f");
		sub.add(Restrictions.eqProperty("f.goodsid", "g.id"));
		sub.setProjection(Projections.property("f.id"));
		query.add(Subqueries.notExists(sub));
		query.setProjection(Projections.projectionList().add(Projections.groupProperty("g.relatedid"),"cinemaId")
				.add(Projections.count("g.relatedid"),"gCount"));
		query.addOrder(Order.desc("gCount"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List tmpList = hibernateTemplate.findByCriteria(query);
		cinemaIdList = BeanUtil.getBeanPropertyList(tmpList, "cinemaId", true);
		cacheService.set(CacheConstant.REGION_ONEHOUR, key, cinemaIdList);
		return cinemaIdList;
	}
	@Override
	public List<Movie> getReleaseMovieList(Date date) {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.gt("releasedate", date));
		query.add(Restrictions.le("releasedate", DateUtil.addDay(date,7)));
		List<Movie> movieList = readOnlyTemplate.findByCriteria(query);
		return movieList;
	}
	@Override
	public List<Map<String, Object>> getMovieProjectionCount(Date date, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.le("releasedate", date));
		query.add(Restrictions.ge("releasedate", DateUtil.addDay(date, -19)));
		query.setProjection(Projections.property("id"));
		List<Long> movieidlist = readOnlyTemplate.findByCriteria(query);
		if(movieidlist.isEmpty()){
			return new ArrayList<Map<String,Object>>();
		}
		String movieid = StringUtils.join(movieidlist, ",");
		String hql = "select new map(movieid as movieid, count(id) as count) from MoviePlayItem where movieid in ("+movieid+") and openStatus=? group by movieid" ;
		List<Map<String, Object>> list = readOnlyTemplate.find(hql, status);
		return list;
	}
	
	@Override
	public List<Map<String, Object>> getReleaseDateMovieCount(){
		String key = CacheConstant.buildKey("get23ReleaseDateMovieCount23");
		List<Map<String, Object>> movieCountList = (List<Map<String, Object>>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(movieCountList == null || movieCountList.isEmpty()){
			String hql = "select new map(to_char(m.releasedate,'yyyy-mm') as releasedate, count(id) as count) " +
					"from Movie m where m.releasedate>? group by to_char(m.releasedate,'yyyy-mm')";
			movieCountList = hibernateTemplate.find(hql, DateUtil.getCurDate());
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, movieCountList);
		}
		Collections.sort(movieCountList, new MultiPropertyComparator(new String[]{"releasedate"}, new boolean[]{true}));
		return movieCountList;
	}
	@Override
	public MoviePlayItem getMpiBySeqNo(String seqNo){
		String query = "from MoviePlayItem where seqNo=? ";
		List<MoviePlayItem> mpiList = hibernateTemplate.find(query, seqNo);
		if(mpiList.isEmpty()) return null;
		return mpiList.get(0);
	}
	
	@Override
	public Date getMinPlaydateByMovieid(Long movieid){
		String query = "select min(playdate) from MoviePlayItem where movieid=? ";
		List<Date> playdateList = hibernateTemplate.find(query, movieid);
		return playdateList.get(0);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		helper =  new CachableServiceHelper("mcpService", "mcp", cacheService);
	}

	@Override
	public List<String> getCharacteristicCinemaRoomByCinema(long cinemaId) {
		String key = CacheConstant.buildKey("getCharacteristicCinemaRoomByCinemadsdf",cinemaId);
		List<String> cTypeList = (List<String>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(cTypeList == null){
			DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "r");
			query.add(Restrictions.eq("r.cinemaid", cinemaId));
			query.setProjection(Projections.groupProperty("characteristic").as("ctype"));
			cTypeList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, cTypeList);
		}
		return cTypeList;
	}

	@Override
	public List<Long> getCinemaIdListByRoomCharacteristic(String cType,String citycode) {
		String key = CacheConstant.buildKey("getCinemaIdListByRoomCharacteristicd2asdf",cType,citycode);
		List<Long> cIdList = (List<Long>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(cIdList == null){
			DetachedCriteria subQuery = DetachedCriteria.forClass(Cinema.class, "c");
			subQuery.add(Restrictions.eq("c.citycode", citycode));
			subQuery.setProjection(Projections.property("c.id"));
			DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "r");
			query.add(Subqueries.propertyIn("r.cinemaid", subQuery));
			query.add(Restrictions.eq("r.characteristic", cType));
			query.setProjection(Projections.groupProperty("r.cinemaid").as("cinemaid"));
			cIdList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, cIdList);
		}
		return cIdList;
	}

	@Override
	public List<Long> getRoomIdListByCinemaAndCtype(long cinemaId, String ctype) {
		String key = CacheConstant.buildKey("getRoomByCinemaAndCtyperisticd2dasd8f",cinemaId,ctype);
		List<Long> roomList = (List<Long>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(roomList == null){
			DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "r");
			query.add(Restrictions.eq("r.characteristic", ctype));
			query.add(Restrictions.eq("r.cinemaid", cinemaId));
			query.setProjection(Projections.property("id"));
			roomList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, roomList);
		}
		return roomList;
	}
	
	@Override
	public List<Long> getRoomIdListByOpi(Long cinemaid){
		DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "cr");
		query.add(Restrictions.isNotNull("cr.characteristic"));
		DetachedCriteria subquery = DetachedCriteria.forClass(OpenPlayItem.class, "opi");
		subquery.add(Restrictions.eq("opi.status", "Y"));
		subquery.add(Restrictions.gt("opi.playtime", DateUtil.addHour(DateUtil.getCurFullTimestamp(), 1)));
		subquery.add(Restrictions.eq("opi.cinemaid", cinemaid));
		subquery.add(Restrictions.eqProperty("opi.roomid", "cr.id"));
		subquery.setProjection(Projections.property("opi.roomid"));
		query.add(Subqueries.exists(subquery));
		query.setProjection(Projections.distinct(Projections.property("cr.id")));
		List rowList = hibernateTemplate.findByCriteria(query);
		return rowList;
	}
}
