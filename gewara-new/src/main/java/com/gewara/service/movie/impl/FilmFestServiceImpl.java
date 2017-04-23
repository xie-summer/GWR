package com.gewara.service.movie.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
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
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.Status;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.common.County;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.JsonData;
import com.gewara.model.content.News;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.movie.FilmFestService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

@Service("filmFestService")
public class FilmFestServiceImpl extends BaseServiceImpl implements FilmFestService, InitializingBean {
	private String cachePre = "xx";
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
	@Override
	public SpecialActivity getSpecialActivity(String tag) {
		SpecialActivity result = baseDao.getObjectByUkey(SpecialActivity.class, "tag", tag, true);
		return result;
	}
	@Override
	public boolean isFilmMoviePlayItem(Long batchid) {
		if(batchid==null) return false;
		SpecialActivity sa = getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId()==null) return false;
		return sa.getId().equals(batchid);
	}
	@Override
	public List<Long> getFilmFestMovieIdList(String flag, String specialFlag, String specialValue, int from, int maxnum) {
		String key = CacheConstant.buildKey("get1223FilmFestMovieId223327List", specialFlag, specialValue, cachePre);
		List<Long> movieIdList = (List<Long>)cacheService.get(CacheConstant.REGION_TENMIN, key);
		if (movieIdList == null) {
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
			query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(specialFlag)) {
				query.add(Restrictions.like(specialFlag, specialValue,MatchMode.ANYWHERE));
			}
			query.addOrder(Order.asc("flag"));
			query.addOrder(Order.asc("id"));
			query.setProjection(Projections.property("id"));
			movieIdList = hibernateTemplate.findByCriteria(query, 0, 1000);
			cacheService.set(CacheConstant.REGION_TENMIN, key, movieIdList);
		}
		return BeanUtil.getSubList(movieIdList, from, maxnum);
	}
	@Override
	public Integer getFilmFestMovieCount(String flag, String specialFlag, String specialValue) {
		String key = CacheConstant.buildKey("festMovieCountsks", flag, specialFlag, specialValue, cachePre);
		Integer count = (Integer) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(count==null){
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
			query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(specialFlag)) query.add(Restrictions.like(specialFlag, specialValue,MatchMode.ANYWHERE));
			query.setProjection(Projections.rowCount());
			List result = hibernateTemplate.findByCriteria(query);
			if (result.isEmpty()) {
				return 0;
			} else {
				count = Integer.parseInt(""+result.get(0));
			}
			cacheService.set(CacheConstant.REGION_TENMIN, key, count);
		}
		return count;
	}
	@Override
	public List<Movie> getFilmFestMovie(String flag, String specialFlag, String specialValue, int from, int maxnum) {
		String key = CacheConstant.buildKey("getFilmFestMovXhy478ist",specialFlag,specialValue,from,maxnum,flag, cachePre);
		List<Long> movieidList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(movieidList == null){
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
			query.add(Restrictions.like("flag", "hot", MatchMode.ANYWHERE));
			query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(specialFlag) && StringUtils.isNotBlank(specialValue)) {
				query.add(Restrictions.like(specialFlag, specialValue,MatchMode.ANYWHERE));
			}
			query.setProjection(Projections.id());
			query.addOrder(Order.desc( "boughtcount"));
			query.addOrder(Order.asc("id"));
			movieidList = hibernateTemplate.findByCriteria(query);
			if(!movieidList.isEmpty()){
				cacheService.set(CacheConstant.REGION_TENMIN, key, movieidList);
			}
		}
		List<Movie> hotMovie = baseDao.getObjectList(Movie.class, movieidList);
		int size = hotMovie.size(); 
		int to = from + maxnum;
		if(to <= size){
			return BeanUtil.getSubList(hotMovie, from, maxnum);
		}
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
		query.add(Restrictions.not(Restrictions.like("flag", "hot", MatchMode.ANYWHERE)));
		query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(specialFlag) && StringUtils.isNotBlank(specialValue)){
			query.add(Restrictions.like(specialFlag, specialValue,MatchMode.ANYWHERE));
		}
		query.addOrder(Order.desc( "boughtcount"));
		query.addOrder(Order.asc("id"));
		if(to > size && from < size){
			hotMovie = BeanUtil.getSubList(hotMovie, from, maxnum);
			hotMovie.addAll(hibernateTemplate.findByCriteria(query, 0, maxnum - hotMovie.size()));
			return hotMovie;
		}
		return hibernateTemplate.findByCriteria(query, from - size, maxnum);
	}
	
	@Override
	public List<Long> getSpecialActivityMovieIds(String flag){
		String key = CacheConstant.buildKey("getSpecialActivityMovieIdsL478ist",flag, cachePre);
		List<Long> movieIds = (List<Long>)cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(movieIds == null || movieIds.size() == 0){
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class);
			query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
			query.setProjection(Projections.property("id"));
			movieIds = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, movieIds);
		}
		return movieIds;
	}
	
	@Override
	public List<County> getCinemaCountyList(String flag) {
		String hql = "select distinct countycode from Cinema c where c.flag like ? order by countycode";
		List<Long> countyIdList = hibernateTemplate.find(hql,"%"+flag+"%");
		List<County> countyList = baseDao.getObjectList(County.class, countyIdList);
		return countyList;
	}

	@Override
	public List<Long> getJoinMovieIdList(Long batch,String citycode, Date curDate, String type, String flag,
			String state, String moviename, Long cinemaid, String festtype, String order, int from, int maxnum) {
		String key = CacheConstant.buildKey("get123Join112Movie123415Id6789List11", citycode, batch,curDate,type,flag,state,cinemaid,festtype,order,from,maxnum, cachePre);
		List<Long> movieidList = (List<Long>)cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(movieidList==null || StringUtils.isNotBlank(moviename)){
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class, "m");
			if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("type",type,MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(flag)) query.add(Restrictions.like("flag", flag,MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(state)) query.add(Restrictions.like("state",state,MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(moviename)) query.add(Restrictions.like("moviename", moviename,MatchMode.ANYWHERE));
			query.add(Restrictions.like("flag", festtype,MatchMode.ANYWHERE));
			if(curDate != null || cinemaid != null){
				DetachedCriteria subQuery = DetachedCriteria.forClass(MoviePlayItem.class, "mpi");
				if(curDate != null) subQuery.add(Restrictions.eq("playdate", curDate));
				if(cinemaid != null) subQuery.add(Restrictions.eq("cinemaid", cinemaid));
				subQuery.add(Restrictions.ge("playdate", DateUtil.getBeginningTimeOfDay(DateUtil.currentTime())));
				subQuery.add(Restrictions.eq("batch", batch));
				subQuery.add(Restrictions.eq("citycode", citycode));
				subQuery.add(Restrictions.eqProperty("m.id", "mpi.movieid"));
				subQuery.setProjection(Projections.property("id"));
				query.add(Subqueries.exists(subQuery));
			}
			query.setProjection(Projections.property("id"));
			if(StringUtils.isBlank(order))query.addOrder(Order.asc("m.moviename"));
			else query.addOrder(Order.asc(order));
			query.addOrder(Order.asc("m.id"));
			movieidList = hibernateTemplate.findByCriteria(query, from, maxnum);
			if(StringUtils.isBlank(moviename)) cacheService.set(CacheConstant.REGION_TENMIN, key, movieidList);
		}
		return movieidList;
	}
	@Override
	public Integer getJoinMovieCount(Long batch, String citycode, Date curDate, String type, String flag, String state, String moviename,Long cinemaid, String festtype) {
		String key = CacheConstant.buildKey("get12334Join1133Movie23412Count78973", citycode, batch,curDate,type,flag,state,cinemaid,festtype, cachePre);
		Integer count = (Integer)cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(count==null || StringUtils.isNotBlank(moviename)){
			DetachedCriteria query = DetachedCriteria.forClass(Movie.class, "m");
			if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("type",type,MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(flag)) query.add(Restrictions.like("flag", flag,MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(state)) query.add(Restrictions.like("state",state,MatchMode.ANYWHERE));
			if(StringUtils.isNotBlank(moviename)) query.add(Restrictions.like("moviename", moviename,MatchMode.ANYWHERE));
			query.add(Restrictions.like("flag", festtype,MatchMode.ANYWHERE));
			if(curDate != null || cinemaid != null){
				DetachedCriteria subQuery = DetachedCriteria.forClass(MoviePlayItem.class, "mpi");
				if(curDate != null) subQuery.add(Restrictions.eq("playdate", curDate));
				if(cinemaid != null) subQuery.add(Restrictions.eq("cinemaid", cinemaid));
				subQuery.add(Restrictions.eq("batch", batch));
				subQuery.add(Restrictions.eq("citycode", citycode));
				subQuery.add(Restrictions.eqProperty("m.id", "mpi.movieid"));
				subQuery.setProjection(Projections.property("id"));
				query.add(Subqueries.exists(subQuery));
			}
			query.setProjection(Projections.rowCount());
			List result = hibernateTemplate.findByCriteria(query);
			if (result.isEmpty()) {
				return 0;
			} else {
				count = Integer.parseInt(""+result.get(0));
			}
			if(StringUtils.isBlank(moviename)) cacheService.set(CacheConstant.REGION_TENMIN, key, count);
		}
		return count;
	}
	@Override
	public Integer getCurMovieSeatSum(Long movieid, Long batch){
		Timestamp timestamp = DateUtil.getCurFullTimestamp();
		String hql = "select sum(o.seatnum) - sum(o.gsellnum) - sum(o.csellnum) - sum(o.locknum) from OpenPlayItem o where o.movieid = ? and o.opentime < ? and o.closetime > ? and o.status = ? and o.citycode = ? " +
				" and exists( select m.id from MoviePlayItem m where m.id=o.mpid and m.batch=? )";
		List<Long> seatList = hibernateTemplate.find(hql, movieid, timestamp, timestamp, Status.Y, AdminCityContant.CITYCODE_SH, batch);
		if(seatList.isEmpty() || seatList.get(0) == null) return -1;
		return Integer.parseInt(seatList.get(0)+"");
	} 
	@Override
	public List<MoviePlayItem> getMoviePlayItemList(String citycode, Long movieId, Long cinemaId, Date playdate, Long batch, String order, int from, int maxnum) {
		List<Long> mpidList = getMoviePlayItemIdList(citycode, movieId, cinemaId, playdate, true, batch, order, from, maxnum);
		return baseDao.getObjectList(MoviePlayItem.class, mpidList);
	}
	@Override
	public Integer getMoviePlayItemCount(String citycode, Long movieId, Long cinemaId, Date playDate, boolean isGeHour, Long batch){
		Date curDate = DateUtil.getCurDate();//无时间
		if(playDate != null && playDate.before(curDate)){
			return 0;
		}
		DetachedCriteria query = qryMpiString(citycode, movieId, cinemaId, playDate, isGeHour, batch, curDate);
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if (result.isEmpty()) return 0;
		return Integer.parseInt(""+result.get(0));
	}
	@Override
	public List<Long> getMoviePlayItemIdList(String citycode, Long movieId, Long cinemaId, Date playdate, boolean  isGeHour, Long batch, String order, int from, int maxnum) {
		String key = CacheConstant.buildKey("festMPILIST", citycode, movieId, cinemaId, playdate, isGeHour, batch, order, from, maxnum, cachePre);
		List<Long> mpidList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEMIN, key);
		if(mpidList ==null){
			Date curDate = DateUtil.getCurDate();//无时间
			if(playdate!=null && playdate.before(curDate)){
				return new ArrayList<Long>(0);
			}
			DetachedCriteria query = qryMpiString(citycode, movieId, cinemaId, playdate, isGeHour, batch, curDate);
			if(StringUtils.isNotBlank(order)){
				query.addOrder(Order.desc(order));
			}
			query.addOrder(Order.asc("playdate"));
			query.addOrder(Order.asc("playtime"));
			query.setProjection(Projections.id());
			mpidList = hibernateTemplate.findByCriteria(query, from, maxnum);
			cacheService.set(CacheConstant.REGION_ONEMIN, key, mpidList);
		}
		return mpidList;
	}
	@Override
	public List<MoviePlayItem> getMaybeMoviePlayItemList(List<Long> cinemaidList, List<Long> movieIdList, Date playdate, Long batch){
		Date curDate = DateUtil.getCurDate();//无时间
		if(playdate!=null && playdate.before(curDate)){
			return new ArrayList();
		}
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("citycode", AdminCityContant.CITYCODE_SH));
		query.add(Restrictions.in("movieid", movieIdList));
		query.add(Restrictions.in("cinemaid", cinemaidList));
		query.add(Restrictions.eq("opentype", OpiConstant.OPEN_GEWARA));
		query.add(Restrictions.or(Restrictions.ne("batch", batch), Restrictions.isNull("batch")));
		if(playdate!=null){
			query.add(Restrictions.eq("playdate", playdate));
			if(curDate.equals(playdate)){
				String hour = DateUtil.format(new Date(), "HH:mm");
				query.add(Restrictions.ge("playtime", hour));
			}
		}else{
			String hour = DateUtil.format(new Date(), "HH:mm");
			query.add(Restrictions.or(
					Restrictions.gt("playdate", curDate),
					Restrictions.and(Restrictions.eq("playdate", curDate), Restrictions.ge("playtime", hour))
			));
		}

		query.addOrder(Order.asc("playdate"));
		query.addOrder(Order.asc("playtime"));
		query.setProjection(Projections.id());
		List<Long> mpidList = hibernateTemplate.findByCriteria(query);
		return baseDao.getObjectList(MoviePlayItem.class, mpidList);
	}
	
	@Override
	public List<Long> getFilmFestCinema(String flag, String citycode, String countycode){
		String key = CacheConstant.buildKey("filmFestCinemaxk3", flag, citycode, countycode, cachePre);
		List<Long> result = (List<Long>)cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(result==null){
			DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
			query.add(Restrictions.like("flag", flag, MatchMode.ANYWHERE));
			query.add(Restrictions.eq("citycode", citycode));
			if(StringUtils.isNotBlank(countycode)) query.add(Restrictions.eq("countycode", countycode));
			query.addOrder(Order.asc("flag"));
			query.setProjection(Projections.id());
			result = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_ONEHOUR, key, result);
		}
		return result;
	}
	
	@Override
	public List<Movie> getCurMovieListByCinemaIdAndDate(Long cinemaId, Date date, Long batch) {
		String query = "select movieid from MoviePlayItem mpi where mpi.cinemaid = ? and mpi.playdate = ? and batch=? group by mpi.movieid order by count(mpi.movieid) desc";
		List<Long> idList = hibernateTemplate.find(query, cinemaId, date, batch);
		List<Movie> movieList = baseDao.getObjectList(Movie.class, idList);
		return movieList;
	}
	
	@Override
	public List<MoviePlayItem> getCinemaCurMpiListByRoomIdAndDate(Long roomId, Date playdate, Long batch) {
		String query = "select id from MoviePlayItem mpi where mpi.roomid = ? and mpi.playdate = ? and batch=? order by playtime";
		List<Long> idList = hibernateTemplate.find(query, roomId, playdate, batch);
		List<MoviePlayItem> result = baseDao.getObjectList(MoviePlayItem.class, idList);
		return result;
	}
	@Override
	public List<News> getFilmFestNewsList(String citycode, String flag,String[] newstype,Date releasedate,int from, int maxnum) {
		String key = CacheConstant.buildKey("get11Film22Fest33112News2345dList", citycode, flag, newstype, releasedate, from, maxnum, cachePre);
		List<Long> newsIdList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(newsIdList == null){
			DetachedCriteria query = DetachedCriteria.forClass(News.class);
			if(!ArrayUtils.isEmpty(newstype)){
				if(ArrayUtils.getLength(newstype) == 1)
					query.add(Restrictions.eq("newstype", newstype[0]));
				else 
					query.add(Restrictions.in("newstype",newstype));
			}
			if(releasedate != null) query.add(Restrictions.between("releasetime", DateUtil.getBeginningTimeOfDay(releasedate), DateUtil.getLastTimeOfDay(releasedate)));
			if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.eq("citycode", citycode)));
			if(StringUtils.isNotBlank(flag)) query.add(Restrictions.eq("flag",flag));
			query.addOrder(Order.desc("releasetime"));
			query.setProjection(Projections.property("id"));
			newsIdList = hibernateTemplate.findByCriteria(query, from, maxnum);
			cacheService.set(CacheConstant.REGION_TENMIN, key, newsIdList);
		}
		List<News> result = baseDao.getObjectList(News.class, newsIdList);
		return result;
	}

	@Override
	public Integer getFilmFestNewsCount(String citycode, String flag,	String[] newstype,Date releasedate) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(!ArrayUtils.isEmpty(newstype)){
			if(ArrayUtils.getLength(newstype) == 1)
				query.add(Restrictions.eq("newstype", newstype[0]));
			else	
				query.add(Restrictions.in("newstype",newstype));
		}
		if(releasedate != null) query.add(Restrictions.eq("releasetime", releasedate));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.eq("citycode", citycode)));
		if(StringUtils.isNotBlank(flag)) query.add(Restrictions.eq("flag",flag));
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		return new Integer(result.get(0)+"");
	}
	@Override
	public List<String> getFilmFestNewsDateList(String citycode,String flag) {
		String hql = "select distinct(to_char(releasetime,'yyyy-mm-dd')) from News where (citycode = ? or citycode = ?) and flag = ? order by to_char(releasetime,'yyyy-mm-dd') desc";
		List result = hibernateTemplate.find(hql, AdminCityContant.CITYCODE_ALL, citycode, flag);
		return result;
	}
	
	private DetachedCriteria qryMpiString(String citycode, Long movieId, Long cinemaId, Date playdate, boolean isGeHour, Long batch, Date curDate){
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(movieId != null){
			query.add(Restrictions.eq("movieid", movieId));
		}
		if(cinemaId != null){
			query.add(Restrictions.eq("cinemaid", cinemaId));
		}
		if(batch != null){
			query.add(Restrictions.eq("batch", batch));
		}
		if(playdate!=null){
			query.add(Restrictions.eq("playdate", playdate));
			if(curDate.equals(playdate) && isGeHour){
				String hour = DateUtil.format(new Date(), "HH:mm");
				query.add(Restrictions.ge("playtime", hour));
			}
		}else{
			String hour = DateUtil.format(new Date(), "HH:mm");
			query.add(Restrictions.or(
					Restrictions.gt("playdate", curDate),
					Restrictions.and(Restrictions.eq("playdate", curDate), Restrictions.ge("playtime", hour))
			));
		}
		return query;
	}
	@Override
	public void updateMpiOtherinfo(String[] idList, String unopengewa, String unshowgewa){
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		List<MoviePlayItem> mpiList = new ArrayList<MoviePlayItem>();
		for (String id : idList) {
			MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, Long.parseLong(id));
			OpenPlayItem opi = null;
			Map otherinfo = null;
			if(StringUtils.equals(mpi.getOpenStatus(), OpiConstant.MPI_OPENSTATUS_OPEN)){
				opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
			}
			Map mpiOtherinfo = JsonUtils.readJsonToMap(mpi.getOtherinfo());
			if(opi != null) otherinfo = JsonUtils.readJsonToMap(opi.getOtherinfo());
			if(StringUtils.equals(unopengewa, "true")){
				if(opi != null) otherinfo.put(OpiConstant.UNOPENGEWA, unopengewa);
				mpiOtherinfo.put(OpiConstant.UNOPENGEWA, unopengewa);
			}else{
				if(opi != null) otherinfo.remove(OpiConstant.UNOPENGEWA);
				mpiOtherinfo.remove(OpiConstant.UNOPENGEWA);
			}
			if(StringUtils.equals(unshowgewa, "true")){
				if(opi != null) otherinfo.put(OpiConstant.UNSHOWGEWA, unshowgewa);
				mpiOtherinfo.put(OpiConstant.UNSHOWGEWA, unshowgewa);
			}else{
				if(opi != null) otherinfo.remove(OpiConstant.UNSHOWGEWA);
				mpiOtherinfo.remove(OpiConstant.UNSHOWGEWA);
			}
			if(opi != null) opi.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
			mpi.setOtherinfo(JsonUtils.writeMapToJson(mpiOtherinfo));
			if(opi != null) opiList.add(opi);
			mpiList.add(mpi);
		}
		if(!opiList.isEmpty()) baseDao.saveObjectList(opiList);
		if(!mpiList.isEmpty()) baseDao.saveObjectList(mpiList);
	}
	
	@Override
	public void copyOpiRemark(List<OpenPlayItem> opiList, List<String> msgList){
		if(CollectionUtils.isEmpty(opiList) || opiList.size()<2) return;
		OpenPlayItem item1 = opiList.get(0);
		OpenPlayItem item2 = opiList.get(1);
		if(item1 == null || item2 == null){
			msgList.add("场次没有对应！");
			return;
		}
		if(StringUtils.equals(item1.getOpentype(), OpiConstant.OPEN_GEWARA)){
			String msg = "odi_ID:" + item2.getId() + ",remark:" + item2.getRemark(); 
			item2.setRemark(item1.getRemark());
			msg += "--->" + item1.getRemark();
			msgList.add(msg);
			JsonData template1 = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE + item1.getId());
			JsonData template2 = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE + item2.getId());
			if(template1 != null){
				if(template2 == null){
					template2 = new JsonData(JsonDataKey.KEY_SMSTEMPLATE + item2.getId());
				}
				msg = "odi_ID:" + item2.getId() + ",jsonData_msg:" + template2.getData();
				template2.setValidtime(DateUtil.addDay(item2.getPlaytime(), 1));
				template2.setData(template1.getData());
				msg += "--->" + template1.getData();
				msgList.add(msg);
				baseDao.saveObject(template2);
			}
			baseDao.saveObject(item2);
		}else if(StringUtils.equals(item2.getOpentype(), OpiConstant.OPEN_GEWARA)){
			String msg = "odi_ID:" + item1.getId() + ",remark:" + item1.getRemark(); 
			item1.setRemark(item2.getRemark());
			msg += "--->" + item2.getRemark();
			msgList.add(msg);
			JsonData template2 = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE + item2.getId());
			JsonData template1 = baseDao.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE + item1.getId());
			if(template2 != null){
				if(template1 == null){
					template1 = new JsonData(JsonDataKey.KEY_SMSTEMPLATE + item1.getId());
				}
				msg = "odi_ID:" + item1.getId() + ",jsonData_msg:" + template1.getData();
				template1.setValidtime(DateUtil.addDay(item1.getPlaytime(), 1));
				template1.setData(template2.getData());
				msg += "--->" + template2.getData();
				msgList.add(msg);
				baseDao.saveObject(template1);
			}
			baseDao.saveObject(item1);
		}
	}
	
	/**
	 * 查询电影节期间有排片的电影
	 */
	@Override
	public List<Long> getMPIMovieIds(String citycode, long batch) {
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("batch", batch));
		Date curDate = DateUtil.getCurDate();
		String hour = DateUtil.format(new Date(), "HH:mm");
		query.add(Restrictions.or(
				Restrictions.gt("playdate", curDate),
				Restrictions.and(Restrictions.eq("playdate", curDate), Restrictions.ge("playtime", hour))
		));
		query.setProjection(Projections.groupProperty("movieid"));
		List<Long> movieIdList = hibernateTemplate.findByCriteria(query);
		return movieIdList;
	}
	@Override
	public void refreshCurrent(String newConfig) {
		GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_FILMFEST_CACHE);
		cachePre = config.getContent();
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_FILMFEST_CACHE);
		cachePre = config.getContent();
	}
	@Override
	public String getCachePre() {
		return cachePre;
	}
}
