package com.gewara.service.ticket.impl;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.SeatConstant;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.partner.PartnerService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.NosqlService;
import com.gewara.util.BeanUtil;
import com.gewara.util.CachableCall;
import com.gewara.util.CachableServiceHelper;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
/**
 * 此模块主要用于购票的后台管理如订单查询、场次开放、场次查询等
 * @author acerge(acerge@163.com)
 * @since 10:20:18 PM Jan 4, 2010
 */
@Service("openPlayService")
public class OpenPlayServiceImpl extends BaseServiceImpl implements OpenPlayService, InitializingBean {
	private static final String SEATMAP_KEY = "OPI_SEATMAP_";
	private static final String SEATMAP_UPDATE = "OPI_SEATMAP_UPDATE_";

	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;

	@Autowired@Qualifier("partnerService")
	private PartnerService partnerService;
	
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	private CachableServiceHelper helper;
	@Override
	public List<RoomSeat> getSeatListByRoomId(Long roomId) {
		String hql = "from RoomSeat s where s.roomid = ?";
		List<RoomSeat> seatList = hibernateTemplate.find(hql, roomId);
		return seatList;
	}
	@Override
	public boolean addRowSeat(Long roomId) {
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, roomId);
		int rowno = room.getLinenum() + 1;
		List objList = new ArrayList();
		for(int rankno=1; rankno<=room.getRanknum();rankno++){
			RoomSeat rs = new RoomSeat(roomId, rowno, rankno);
			objList.add(rs);
		}
		room.setLinenum(rowno);
		objList.add(room);
		baseDao.saveObjectList(objList);
		return true;
	}
	
	@Override
	public boolean addRankSeat(Long roomId) {
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, roomId);
		int rankno = room.getRanknum() + 1;
		List objList = new ArrayList();
		for(int rowno=1; rowno<=room.getLinenum();rowno++){
			RoomSeat rs = new RoomSeat(roomId, rowno, rankno);
			objList.add(rs);
		}
		room.setRanknum(rankno);
		objList.add(room);
		baseDao.saveObjectList(objList);
		return true;
	}
	
	@Override
	public RoomSeat getRoomSeatByLocation(Long roomid, int line, int rank){
		String query = "from RoomSeat where roomid = ? and lineno = ? and rankno = ?";
		List<RoomSeat> result = hibernateTemplate.find(query, roomid, line, rank);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	@Override
	public boolean deleteRankSeat(Long roomId) {
		String update = "delete RoomSeat where roomid = ? and rankno = ? ";
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, roomId);
		hibernateTemplate.bulkUpdate(update, roomId, room.getRanknum());
		room.setRanknum(room.getRanknum() - 1);
		room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(room);
		return true;
	}
	@Override
	public boolean deleteRowSeat(Long roomId) {
		String update = "delete RoomSeat where roomid = ? and lineno = ? ";
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, roomId);
		hibernateTemplate.bulkUpdate(update, roomId, room.getLinenum());
		room.setLinenum(room.getLinenum() - 1);
		room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(room);
		return true;
	}
	@Override
	public boolean updateSeatLine(Long roomid, int lineno, String newline) {
		String update = "update RoomSeat set seatline = ? where roomid = ? and lineno = ? ";
		hibernateTemplate.bulkUpdate(update, newline, roomid, lineno);
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, roomid);
		room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(room);
		return true;
	}

	@Override
	public ErrorCode<OpenSeat> lockSeat(Long mpid, Long seatid, String locktype, String lockreason) {
		if(!SeatConstant.STATUS_LOCK_LIST.contains(locktype)) return ErrorCode.getFailure("锁定类型不对！");
		SellSeat sellSeat = baseDao.getObject(SellSeat.class, seatid);
		if(sellSeat !=null && !sellSeat.isAvailable(new Timestamp(System.currentTimeMillis()))) return ErrorCode.getFailure("此座位不能锁定！");
		OpenSeat oseat = baseDao.getObject(OpenSeat.class, seatid);
		oseat.setStatus(locktype);
		baseDao.saveObject(oseat);
		clearOpenSeatCache(mpid);
		return ErrorCode.getSuccessReturn(oseat);
	}
	@Override
	public Integer getSeatCountByRoomId(Long roomid) {
		CinemaRoom room = baseDao.getObject(CinemaRoom.class, roomid);
		return room.getSeatnum();
	}

	@Override
	public List<OpenPlayItem> getOpiList(String citycode, Long cinemaId, Long movieId, Timestamp from, Timestamp to, boolean open){
		return getOpiList(citycode, cinemaId, movieId, from, to, open, 2000);
	}
	@Override
	public List<OpenPlayItem> getOpiList(String citycode, Long cinemaId, Long movieId, Timestamp from, Timestamp to, boolean open, int maxnum){
		DetachedCriteria query = qryOpenPlayItem(citycode, cinemaId, movieId, from, to);
		if(open){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			query.add(Restrictions.le("m.opentime", cur));
			query.add(Restrictions.ge("m.closetime", cur));
			query.add(Restrictions.eq("m.status", OpiConstant.STATUS_BOOK));
			query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
		}
		query.setProjection(Projections.property("m.id"));
		query.addOrder(Order.asc("m.playtime"));
		List<Long> idList = hibernateTemplate.findByCriteria(query, 0, maxnum);
		List<OpenPlayItem> result = baseDao.getObjectList(OpenPlayItem.class, idList);
		return result;
	}
	
	private DetachedCriteria qryOpenPlayItem(String citycode, Long cinemaId, Long movieId, Timestamp from, Timestamp to){
		DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "m");
		if(cinemaId != null) query.add(Restrictions.eq("m.cinemaid", cinemaId));
		else if(citycode != null) query.add(Restrictions.eq("m.citycode", citycode));

		if(movieId != null) query.add(Restrictions.eq("m.movieid", movieId));
		if(from != null) query.add(Restrictions.ge("m.playtime", from));
		if(to != null) query.add(Restrictions.le("m.playtime", to));
		return query;
	}
	
	@Override
	public int getOpiCount(final String citycode, final Long cinemaId, final Long movieId, final Timestamp from, final Timestamp to, final boolean open){
		String key = CacheConstant.buildKey("plcgetOpiCountyvgegetOpiCount", citycode, cinemaId, movieId, open+"");
		Integer result = helper.cacheCall(key, CacheConstant.SECONDS_TWENTYMIN, new CachableCall<Integer>(){
			@Override
			public Integer call() {
				DetachedCriteria query = qryOpenPlayItem(citycode, cinemaId, movieId, from, to);
				if(open){
					Timestamp cur = new Timestamp(System.currentTimeMillis());
					query.add(Restrictions.le("m.opentime", cur));
					query.add(Restrictions.ge("m.closetime", cur));
					query.add(Restrictions.eq("m.status", OpiConstant.STATUS_BOOK));
					query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
				}
				query.setProjection(Projections.rowCount());
				List qryResult = hibernateTemplate.findByCriteria(query);
				return Integer.valueOf(qryResult.get(0)+"");
			}
		});
		return result;
	}
	
	@Override
	public List<OpenPlayItem> getDisabledOpiLlist(String citycode, Long cinemaId, int from, int maxnum){
		DetachedCriteria query = qryOpenPlayItem(citycode, cinemaId, null, null, null);
		query.add(Restrictions.eq("m.status", OpiConstant.STATUS_DISCARD));
		query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		query.add(Restrictions.ge("m.closetime", cur));
		query.setProjection(Projections.property("m.id"));
		query.addOrder(Order.asc("m.playtime"));
		List<Long> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<OpenPlayItem> opiList = baseDao.getObjectList(OpenPlayItem.class, idList);
		return opiList;
	}
	
	@Override
	public int getDisabledOpiCount(String citycode, Long cinemaId){
		DetachedCriteria query = qryOpenPlayItem(citycode, cinemaId, null, null, null);
		query.add(Restrictions.eq("m.status", OpiConstant.STATUS_DISCARD));
		query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		query.add(Restrictions.ge("m.closetime", cur));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	
	@Override
	public List<Long> getDisabledCinemaIdList(String citycode){
		DetachedCriteria query = qryOpenPlayItem(citycode, null, null, null, null);
		query.add(Restrictions.eq("m.status", OpiConstant.STATUS_DISCARD));
		query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
		Timestamp cur = DateUtil.getCurFullTimestamp();
		query.add(Restrictions.ge("m.closetime", cur));
		query.setProjection(Projections.distinct(Projections.property("m.cinemaid")));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		return idList;
	}
	
	@Override
	public List<OpenSeat> getOpenSeatList(final Long mpid){
		String key = "openSeatList" + mpid;
		List<OpenSeat> result = helper.cacheCall(key, CacheConstant.SECONDS_OPENSEAT, new CachableCall<List<OpenSeat>>(){
			@Override
			public List<OpenSeat> call() {
				String query = "from OpenSeat s where s.mpid = ?";
				List<OpenSeat> seatList = hibernateTemplate.find(query, mpid);
				return seatList;
			}
		});
		return result;
	}
	@Override
	public List<OpenSeat> refreshOpenSeatList(Long mpid) {
		String key = "openSeatList" + mpid;
		helper.clearCache(key);
		return getOpenSeatList(mpid);
	}
	@Override
	public void clearOpenSeatCache(Long mpid) {
		String key = "openSeatList" + mpid;
		helper.clearCache(key);
	}
	@Override
	public List<SellSeat> getSellSeatListByMpid(Long mpid) {
		String query = "from SellSeat s where s.mpid = ? ";
		List<SellSeat> result = hibernateTemplate.find(query, mpid);
		return result;
	}
	
	@Override
	public MoviePrice getMoviePrice(Long movieid, String citycode){
		String hql = "from MoviePrice p where p.movieid=? and p.citycode=?";
		List<MoviePrice> mpList = hibernateTemplate.find(hql, movieid, citycode);
		if(mpList.size()==0) return null;
		return mpList.get(0);
	}
	@Override
	public List<Long> getUnsynchPlayItem(Long cinemaid){
		//1)更新的
		String query = "select opi.id from OpenPlayItem opi where opi.playtime >= ? and opi.status != ? and" +
				"  exists (select mpi.id from MoviePlayItem mpi where mpi.id=opi.mpid and (" +
				"	to_char(mpi.playdate,'yyyy-mm-dd') != to_char(opi.playtime,'yyyy-mm-dd')" +
				"	or to_char(opi.playtime, 'hh24:mi') != mpi.playtime" +
				"	or mpi.movieid != opi.movieid" +
				"   or mpi.price != opi.price" +
				"	or mpi.roomid != opi.roomid " +
				"   or opi.opentype !=? and opi.seqNo!=mpi.seqNo" +
				"   or mpi.edition != opi.edition " + 
				"	or mpi.language != opi.language" +
				"	or mpi.edition is null and opi.edition is not null" +
				"	or mpi.edition is not null and opi.edition is null" +
				"	or mpi.language is null and opi.language is not null" +
				"	or mpi.language is not null and opi.language is null" +
				"	or mpi.lowest is not null and opi.lowest is not null and opi.lowest!=mpi.lowest and mpi.lowest > 0" +//最低限价
				"	or mpi.openStatus in (?,?) ))";
		//2)删除
		String query2 = "select opi.id from OpenPlayItem opi where opi.playtime >= ? and opi.status != ? and" +
				"  not exists (select mpi.id from MoviePlayItem mpi where mpi.id=opi.mpid)";

		if(cinemaid!=null){
			query += " and opi.cinemaid= ?";
			query2 += " and opi.cinemaid= ? ";
		}
		Timestamp cur = DateUtil.getBeginningTimeOfDay(new Timestamp(System.currentTimeMillis()));
		List<Long> idList = null;
		List<Long> idList2 = null;
		if(cinemaid!=null){
			idList = hibernateTemplate.find(query, cur, OpiConstant.STATUS_DISCARD, OpiConstant.OPEN_GEWARA, OpiConstant.MPI_OPENSTATUS_DISABLED, OpiConstant.MPI_OPENSTATUS_PAST, cinemaid);
			idList2 = hibernateTemplate.find(query2, cur, OpiConstant.STATUS_DISCARD, cinemaid);
		}else{
			idList = hibernateTemplate.find(query, cur, OpiConstant.STATUS_DISCARD, OpiConstant.OPEN_GEWARA, OpiConstant.MPI_OPENSTATUS_DISABLED, OpiConstant.MPI_OPENSTATUS_PAST);
			idList2 = hibernateTemplate.find(query2, cur, OpiConstant.STATUS_DISCARD);
		}
		
		idList.addAll(idList2);
		return idList;
	}

	@Override
	public List<Long> getOpiCinemaidList(final String citycode, final Long movieid){
		String key = CacheConstant.KEY_OPICINEMAIDLIST+citycode+(movieid==null?"":movieid);
		List<Long> cinemaidList = helper.cacheCall(key, CacheConstant.SECONDS_TWENTYMIN, new CachableCall<List<Long>>() {
			@Override
			public List<Long> call() {
				Timestamp curtime = new Timestamp(System.currentTimeMillis());
				DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "m");
				if(movieid!=null) query.add(Restrictions.eq("m.movieid", movieid));
				query.add(Restrictions.eq("m.status", OpiConstant.STATUS_BOOK));
				query.add(Restrictions.le("m.opentime", curtime));
				query.add(Restrictions.gt("m.closetime", curtime));
				query.add(Restrictions.eq("m.citycode", citycode));
				query.add(Restrictions.gt("m.playtime", curtime));
				query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
				query.setProjection(Projections.distinct(Projections.property("m.cinemaid")));
				List<Long> result = hibernateTemplate.findByCriteria(query);
				return result;
			}
		});
		return cinemaidList;
	}
	@Override
	public List<Long> getOpiMovieidList(String citycode, Long cinemaid){
		String key = CacheConstant.buildKey("get28Opi2ovxreidList", citycode, cinemaid);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(idList == null){
			Timestamp curtime = new Timestamp(System.currentTimeMillis());
			DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "o");
			if(cinemaid!=null) query.add(Restrictions.eq("o.cinemaid", cinemaid));
			query.add(Restrictions.eq("o.status", OpiConstant.STATUS_BOOK));
			query.add(Restrictions.le("o.opentime", curtime));
			query.add(Restrictions.gt("o.closetime", curtime));
			query.add(Restrictions.ltProperty("o.gsellnum", "o.asellnum"));
			query.add(Restrictions.eq("o.citycode", citycode));
			query.setProjection(Projections.distinct(Projections.property("movieid")));
			idList = hibernateTemplate.findByCriteria(query);
			cacheService.set(CacheConstant.REGION_TENMIN, key, idList);
		}
		return idList;
	}
	@Override
	public List<Cinema> getOpenCinemaList(String citycode, Long movieid, Date playdate, int from, int maxnum) {
		String key = CacheConstant.buildKey("getOpedk3wnCinemaList",citycode, movieid, playdate, from, maxnum);
		List<Long> idList = (List<Long>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(idList == null){
			String query = "select distinct o.cinemaid from OpenPlayItem o where o.movieid=? and o.playtime > ? and o.playtime < ? and o.citycode=? ";
			Timestamp timeFrom = new Timestamp(DateUtil.getBeginningTimeOfDay(playdate).getTime());
			Timestamp timeTo = DateUtil.getLastTimeOfDay(timeFrom);
			idList = queryByRowsRange(query, from, maxnum, movieid, timeFrom, timeTo, citycode);
			cacheService.set(CacheConstant.REGION_TENMIN, key, idList);
		}
		List<Cinema> cinemaList = baseDao.getObjectList(Cinema.class, idList);
		return cinemaList;
	}
	@Override
	public  List<Date> getMovieOpenDateList(String citycode, Long movieid){
		String key = CacheConstant.buildKey("get24vi9OpenD#teList",citycode, movieid);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(playdateList == null || playdateList.isEmpty()){
			String query = "select distinct to_char(o.playtime,'yyyy-mm-dd') from OpenPlayItem o " +
					"where o.movieid=? and o.status = ? and o.closetime > ? " +
					"and o.playtime> ? and o.gsellnum < o.asellnum and o.citycode=? ";
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			playdateList = hibernateTemplate.find(query, movieid, OpiConstant.STATUS_BOOK, cur, cur, citycode);
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>();
		for(String date:playdateList) result.add(DateUtil.parseDate(date));
		return result;
	}

	@Override
	public  List<Date> getMovieOpenDateListByCounycode(String counycode, Long movieid){
		String key = CacheConstant.buildKey("get24vi9OpenDateBycountycodeandmovieid",counycode, movieid);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_TWENTYMIN, key);
		if(playdateList == null || playdateList.isEmpty()){
			String query = "select distinct to_char(o.playtime,'yyyy-mm-dd') from OpenPlayItem o " +
					"where o.movieid=? and o.status = ? and o.closetime > ? " +
					"and o.playtime> ? and o.gsellnum < o.asellnum and exists(select c.id from Cinema c where c.id=o.cinemaid and c.countycode=?)";
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			playdateList = hibernateTemplate.find(query, movieid, OpiConstant.STATUS_BOOK, cur, cur, counycode);
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_TWENTYMIN, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>();
		for(String date:playdateList) result.add(DateUtil.parseDate(date));
		return result;
	}
	
	@Override
	public  List<Date> getCinemaAndMovieOpenDateList(Long cinemaid, Long movieid){
		String key = CacheConstant.buildKey("getCinemaAndMovie#OpenDate#xal9", cinemaid, movieid);
		List<String> playdateList = (List<String>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(playdateList == null || playdateList.size()==0){
			String query = "select distinct to_char(opi.playtime,'yyyy-mm-dd') from OpenPlayItem opi " +
					"where opi.status = ? and opi.closetime > ? and opi.playtime> ? " +
					"and opi.cinemaid = ? and opi.movieid = ? and opi.gsellnum < opi.asellnum "; 
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			playdateList = hibernateTemplate.find(query, OpiConstant.STATUS_BOOK, cur, cur, cinemaid, movieid);
			Collections.sort(playdateList);
			cacheService.set(CacheConstant.REGION_ONEHOUR, key, playdateList);
		}
		List<Date> result = new ArrayList<Date>();
		for(String date:playdateList) result.add(DateUtil.parseDate(date));
		return result;
	}
	
	@Override
	public List<Date> getMoviePlayItemDateList(String citycode, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class);
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.ge("playdate", DateUtil.getCurDate()));
		query.setProjection(Projections.distinct(Projections.property("playdate")));
		query.addOrder(Order.asc("playdate"));
		List<Date> dateList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return dateList;
	}
	
	@Override
	public String getRoomSeatMapStr(CinemaRoom room) {
		String[][] seatMap = new String[room.getLinenum()][room.getRanknum()];
		for(int i=0;i<room.getLinenum();i++){
			for(int j=0;j<room.getRanknum();j++){
				seatMap[i][j] = "O";
			}
		}
		List<RoomSeat> seatList = getSeatListByRoomId(room.getId());
		for(RoomSeat seat: seatList){
			seatMap[seat.getLineno()-1][seat.getRankno()-1] = "A";
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<room.getLinenum();i++){
			sb.append(StringUtils.join(seatMap[i], ",") + "@@");
		}
		return sb.substring(0, sb.length() -2);
	}

	@Override
	public List<Movie> getMovieListByCinemaIdAndPlaydate(Long cinemaId, Date playdate, int from, int maxnum) {
		String query = "select distinct opi.movieid from OpenPlayItem opi where opi.cinemaid=? and opi.playtime>=? and opi.playtime <? ";
		Timestamp timefrom = DateUtil.getBeginningTimeOfDay(new Timestamp(playdate.getTime()));
		Timestamp timeto = DateUtil.addDay(timefrom, 1);
		List<Long> midList = queryByRowsRange(query, from, maxnum, cinemaId, timefrom, timeto);
		List<Movie> movieList = baseDao.getObjectList(Movie.class, midList);
		return movieList;
	}
	@Override
	public List<Date> getMovieOfCinemaOpenDateList(Long cinemaid, Long movieid) {
		String key = CacheConstant.buildKey("getMovieOfCinemaOpenDateListopenDateList", cinemaid, movieid);
		List<Date> playdateList = (List<Date>) cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(playdateList == null){
			String query = "select distinct m.playdate from MoviePlayItem m where m.movieid = ? and m.cinemaid = ? and ((m.playdate = ? and m.playtime > ?) or m.playdate > ?) order by m.playdate";
			Date now = new Date();
			playdateList = hibernateTemplate.find(query, movieid,cinemaid, DateUtil.getBeginningTimeOfDay(now), DateUtil.format(now, "HH:mm"),now);
			cacheService.set(CacheConstant.REGION_TENMIN, key, playdateList);
		}
		return playdateList;
	}
	//得到热映的电影ID  List
	@Override
	public List<Long> getHotPlayMovieIdList(String cityCode,String order) {
		DetachedCriteria query = DetachedCriteria.forClass(Movie.class,"m");
		Timestamp curTime = new Timestamp(System.currentTimeMillis());
		query.setProjection(Projections.property("m.id"));
		DetachedCriteria queryHotMovie = DetachedCriteria.forClass(OpenPlayItem.class,"opi");
		queryHotMovie.add(Restrictions.ge("opi.playtime", DateUtil.addHour(curTime, 1)));
		queryHotMovie.add(Restrictions.eq("opi.citycode", cityCode));
		queryHotMovie.setProjection(Projections.distinct(Projections.property("opi.movieid")));
		queryHotMovie.add(Restrictions.eqProperty("m.id", "opi.movieid"));
		query.add(Subqueries.exists(queryHotMovie));
		query.addOrder(Order.desc(order));
		return hibernateTemplate.findByCriteria(query);
	}
	@Override
	public String[] getOpiSeatMap(Long mpid){
		String seatMap = (String) cacheService.get(CacheConstant.REGION_ONEDAY, SEATMAP_KEY + mpid);
		if(StringUtils.isNotBlank(seatMap)){
			Long updatetime = (Long) cacheService.get(CacheConstant.REGION_ONEDAY, SEATMAP_UPDATE + mpid);
			return new String[]{seatMap, updatetime==null?"":DateUtil.format(new Timestamp(updatetime), "HH:mm:ss")};
		}
		return null;
	}

	@Override
	public List<AutoSetter> getValidSetterList(Long cinemaid,String status) {
		DetachedCriteria query = DetachedCriteria.forClass(AutoSetter.class);
		if(StringUtils.isNotBlank(status)){
			if(!StringUtils.equals("ALL", status)){
				query.add(Restrictions.eq("status", status));
			}
		}else{
			query.add(Restrictions.ne("status", AutoSetter.STATUS_CLOSE));
		}
		query.add(Restrictions.eq("checkStatus", AutoSetter.CHECK_T));
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.add(Restrictions.gt("playtime2", new Timestamp(System.currentTimeMillis())));
		query.setProjection(Projections.property("id"));
		List<Long> idList = hibernateTemplate.findByCriteria(query);
		List<AutoSetter> setterList = baseDao.getObjectList(AutoSetter.class, idList);
		Collections.sort(setterList, new MultiPropertyComparator<AutoSetter>(new String[]{"movies","id"}, new boolean[]{false,true}));
		return setterList;
	}
	
	@Override
	public List<AutoSetter> getCheckSetterList(){
		String query = "select id from AutoSetter where checkStatus=? and playtime2 > ? order by cinemaid asc,ordernum desc";
		List<Long> idList = hibernateTemplate.find(query,AutoSetter.CHECK_F, new Timestamp(System.currentTimeMillis()));
		List<AutoSetter> setterList = baseDao.getObjectList(AutoSetter.class, idList);
		return setterList;
	}
	
	@Override
	public List<OpenPlayItem> getIntensiveOpiList(int seatnum) {
		String query = "select id from OpenPlayItem where playtime>? and seatnum-gsellnum-csellnum-locknum < ?";
		List<Long> opidList = hibernateTemplate.find(query, DateUtil.addHour(new Timestamp(System.currentTimeMillis()), 1), seatnum);
		return baseDao.getObjectList(OpenPlayItem.class, opidList);
	}
	@Override
	public void updateOpiSeatMap(Long mpid, CinemaRoom room, List<OpenSeat> openSeatList, List<String> hfhLockList, SeatStatusUtil seatStatusUtil) {
		Long updatetime = (Long) cacheService.get(CacheConstant.REGION_ONEDAY, "OPI_SEATMAP_UPDATE_" + mpid);
		Long cur = System.currentTimeMillis();
		if(updatetime==null || updatetime < cur - DateUtil.m_second*30){//30秒内未更新过
			String seatStr = getOpiSeatStr(room, openSeatList, hfhLockList, seatStatusUtil);
			cacheService.set(CacheConstant.REGION_ONEDAY, SEATMAP_KEY + mpid, seatStr);
			cacheService.set(CacheConstant.REGION_ONEDAY, SEATMAP_UPDATE + mpid, cur);
		}
	}
	@Override
	public void updateOpiSeatMap4Wd(Long mpid, CinemaRoom room, List<RoomSeat> seatList, List<String> lockList, SeatStatusUtil seatStatusUtil){
		Long updatetime = (Long) cacheService.get(CacheConstant.REGION_ONEDAY, "OPI_SEATMAP_UPDATE_" + mpid);
		Long cur = System.currentTimeMillis();
		if(updatetime==null || updatetime < cur - DateUtil.m_second*30){//30秒内未更新过
			String seatStr = getWdOpiSeatStr(room, seatList, lockList, seatStatusUtil);
			cacheService.set(CacheConstant.REGION_ONEDAY, SEATMAP_KEY + mpid, seatStr);
			cacheService.set(CacheConstant.REGION_ONEDAY, SEATMAP_UPDATE + mpid, cur);
		}
	}
	private String getWdOpiSeatStr(CinemaRoom room, List<RoomSeat> seatList, List<String> lockList, SeatStatusUtil seatStatusUtil){
		Map<String, RoomSeat> seatMap = BeanUtil.beanListToMap(seatList, "position");
		RoomSeat seat = null;
		String status = "";
		List<String> lineList = new ArrayList<String>();
		for(int i=1; i<= room.getLinenum(); i++){
			List<String> seatRankList = new ArrayList<String>();
			for(int j=1; j<= room.getRanknum(); j++){
				seat = seatMap.get(i + ":" + j);
				if(seat == null){
					status = "ZL"; //走廊
				}else{
					if(lockList.contains(seat.getKey())){ 
						status = "LK"; //锁定
					}else if(SeatConstant.STATUS_SOLD.equals(seatStatusUtil.getRoomSeatStatus(seat))){
						status = SeatConstant.STATUS_SOLD;
					}else{
						status = SeatConstant.STATUS_NEW;
					}
				}
				seatRankList.add(status);
			}
			lineList.add(StringUtils.join(seatRankList, ","));
		}
		return StringUtils.join(lineList, "@@");
	}
	private String getOpiSeatStr(CinemaRoom room, List<OpenSeat> openSeatList, List<String> hfhLockList, SeatStatusUtil seatStatusUtil){
		Map<String, OpenSeat> seatMap = BeanUtil.beanListToMap(openSeatList, "position");
		OpenSeat oseat = null;
		String status = "";
		List<String> lineList = new ArrayList<String>();
		for(int i=1; i<= room.getLinenum(); i++){
			List<String> seatRankList = new ArrayList<String>();
			for(int j=1; j<= room.getRanknum(); j++){
				oseat = seatMap.get(i + ":" + j);
				if(oseat == null){
					status = "ZL"; //走廊
				}else{
					if(hfhLockList.contains(oseat.getKey())){ 
						status = "LK"; //锁定
					}else if(SeatConstant.STATUS_NEW.equals(seatStatusUtil.getFullStatus(oseat))){
						status = seatStatusUtil.getFullStatus(oseat);
					}else{
						status = "LK"; //锁定
					}
				}
				seatRankList.add(status);
			}
			lineList.add(StringUtils.join(seatRankList, ","));
		}
		return StringUtils.join(lineList, "@@");
	}

	@Override
	public Map<String, Integer> getOpiCountMap(Long cinemaid) {
		String query = "select new map(to_char(playtime,'yyyy-mm-dd') as playdate, count(*) as count) from OpenPlayItem " +
				"where playtime>? and cinemaid=? group by to_char(playtime,'yyyy-mm-dd')";
		List<Map> rowList = hibernateTemplate.find(query, DateUtil.getBeginningTimeOfDay(new Timestamp(System.currentTimeMillis())), cinemaid);
		Map result = new HashMap();
		for(Map row: rowList){
			result.put(row.get("playdate"), row.get("count"));
		}
		return result;
	}

	@Override
	public List<OpenPlayItem> getOpiListByRoomId(final Long roomid, int from, int maxnum){
		String key = "getOpiByRoomId" + roomid;
		List<Long> idList = helper.cacheCall(key, CacheConstant.SECONDS_HALFHOUR, new CachableCall<List<Long>>() {
			@Override
			public List<Long> call() {
				DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "m");
				query.add(Restrictions.eq("status", "Y"));
				query.add(Restrictions.eq("m.roomid", roomid));
				query.add(Restrictions.gt("playtime", DateUtil.getCurFullTimestamp()));
				query.setProjection(Projections.id());
				query.addOrder(Order.asc("playtime"));
				List<Long> rt = hibernateTemplate.findByCriteria(query);
				return rt;
			}
		});
		List<Long> curList = BeanUtil.getSubList(idList, from, maxnum);
		List<OpenPlayItem> result = baseDao.getObjectList(OpenPlayItem.class, curList);
		return result;
	}
	
	@Override
	public List<MoviePlayItem> synchUnOpenMpi() {
		String query = "select id from MoviePlayItem t where openStatus= ? and exists (select id from OpenPlayItem o where o.mpid=t.id)";
		List<Long> mpidList = hibernateTemplate.find(query, OpiConstant.MPI_OPENSTATUS_INIT);
		List<MoviePlayItem> mpiList = new ArrayList<MoviePlayItem>(mpidList.size());
		for(Long mpid: mpidList){
			MoviePlayItem mpi = baseDao.getObject(MoviePlayItem.class, mpid);
			mpi.setOpenStatus(OpiConstant.MPI_OPENSTATUS_OPEN);
			mpiList.add(mpi);
		}
		baseDao.saveObjectList(mpiList);
		return mpiList;
	}
	
	
	/**
	 * 上海电影艺术联盟获取排片数据
	 * @param citycode
	 * @param cinemaIds
	 * @param movieIds
	 * @param from
	 * @param maxnum
	 * @return
	 */
	@Override
	public List<OpenPlayItem> getArtFilmAllianceOpi(String citycode, String cinemaIds, String movieIds, int from, int maxnum) {
		if (StringUtils.isBlank(cinemaIds) || StringUtils.isBlank(movieIds)) {
			return new ArrayList<OpenPlayItem>();
		}
		
		String key = CacheConstant.buildKey("getArtFi12lisdfwe234", citycode, cinemaIds, movieIds);
		List<Long> mpidList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if (CollectionUtils.isEmpty(mpidList)) {
			DetachedCriteria query = DetachedCriteria.forClass(OpenPlayItem.class, "m");
			List<Long> cinemaIdList = BeanUtil.getIdList(cinemaIds, ",");
			List<Long> movieIdList = BeanUtil.getIdList(movieIds, ",");
			
			if(citycode != null) query.add(Restrictions.eq("m.citycode", citycode));
			
			query.add(Restrictions.in("m.cinemaid", cinemaIdList));
			query.add(Restrictions.in("m.movieid", movieIdList));
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			query.add(Restrictions.le("m.opentime", cur));
			query.add(Restrictions.ge("m.closetime", cur));
			query.add(Restrictions.eq("m.status", OpiConstant.STATUS_BOOK));
			query.add(Restrictions.ltProperty("m.gsellnum", "m.asellnum"));
			query.addOrder(Order.asc("m.playtime"));
			query.setProjection(Projections.property("m.mpid"));
			if (maxnum != 0) {
				mpidList = hibernateTemplate.findByCriteria(query, from, maxnum);
			} else {
				mpidList = hibernateTemplate.findByCriteria(query);
			}
			cacheService.set(CacheConstant.REGION_ONEHOUR, key, mpidList);
		}
		List<OpenPlayItem> resultList = new ArrayList<OpenPlayItem>();
		for(Long mpid: mpidList){
			OpenPlayItem opi = baseDao.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
			resultList.add(opi);
		}
		return resultList;
	}
	@Override
	public ErrorCode<String> validOpiStatusByPartner(OpenPlayItem opi, ApiUser partner, Member member){
		if(!partner.isEnabled()){
			return ErrorCode.getFailure(ApiConstant.CODE_USER_NORIGHTS, "商户没有权限！");
		}
		if(opi == null){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		}
		if(!opi.isOpenToPartner()){ 
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场暂不对外开放！");
		}
		if(!opi.isOrder()){ 
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, OpiConstant.getStatusStr(opi));
		}
		
		//需要填写地址的
		if(StringUtils.contains(opi.getOtherinfo(), OpiConstant.ADDRESS)){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		}
		
		//不对格瓦拉开放的
		if(member!=null && opi.isUnShowToGewa()){
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场暂不对外开放！");
		}
		
		//场次过滤掉的
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		if(filter.excludeOpi(opi)) {
			return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场暂不对外开放！"); 
		}
		
		//有套餐
		if(member==null){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, partner.getId());
			if(goodsGift!=null){
				return ErrorCode.getFailure(ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
			}
		}
		
		return ErrorCode.SUCCESS;
	}
	
	public List<CinemaRoom> updateRoomList(String citycode, Date updatedate) {
		List<CinemaRoom> roomList = new ArrayList<CinemaRoom>();
		DetachedCriteria query = DetachedCriteria.forClass(CinemaRoom.class, "r");
		query.add(Restrictions.gt("updatetime", updatedate));
		DetachedCriteria subquery = DetachedCriteria.forClass(Cinema.class, "p");
		subquery.add(Restrictions.eq("p.booking", Cinema.BOOKING_OPEN));
		subquery.add(Restrictions.eqProperty("p.id", "r.cinemaid"));
		subquery.setProjection(Projections.property("p.id"));
		List<String> citycodeList = Arrays.asList(StringUtils.split(citycode, ","));
		if(citycodeList.size()==1){
			if(!StringUtils.equals(citycode, AdminCityContant.CITYCODE_ALL)) subquery.add(Restrictions.eq("p.citycode", citycode)); 
		}else{
			subquery.add(Restrictions.in("p.citycode", citycodeList));
		}
		query.add(Subqueries.exists(subquery));
		roomList = hibernateTemplate.findByCriteria(query);
		return roomList;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		helper =  new CachableServiceHelper("openPlayService", "ops", cacheService);
		
	}

	@Override
	public List<List<String>> getRoomSeatNavigation(CinemaRoom room,String orderSeat){
		Map<String,String> outerRingseatMap = null;
		String outerRingseats = "";
		if(StringUtils.isNotBlank(room.getOtherinfo())){
			if(JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat") != null && 
					StringUtils.equals("true",(String)JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat"))){
				outerRingseatMap = nosqlService.getOuterRingSeatByRoomId(room.getId());
				outerRingseats = StringUtils.join(outerRingseatMap.keySet(), ",");
			}
		}
		int lineStart = 1;
		int rowStart = 1;
		int lineNum = room.getLinenum();
		int rankNum = room.getRanknum();
		List<RoomSeat> seatList = getSeatListByRoomId(room.getId());
		Map<String, RoomSeat> seatMap = new HashMap<String, RoomSeat>();
		for(RoomSeat seat:seatList){
			lineNum = Math.max(lineNum, seat.getLineno());
			rankNum = Math.max(rankNum, seat.getRankno());
			seatMap.put("L" + seat.getLineno() + "R" + seat.getRankno(), seat);
		}
		int lineEnd = lineNum;
		int rowEnd = rankNum;
		if(outerRingseats.contains("L0R")){
			lineStart = 0;
		}
		if(outerRingseats.contains("L" + (lineNum + 1) + "R")){
			lineNum = lineNum + 1;
			lineEnd = lineNum;
		}
		if(outerRingseats.contains("R0")){
			rowStart = 0;
		}
		if(outerRingseats.contains("R" + (rankNum + 1))){
			rankNum = rankNum + 1;
			rowEnd = rankNum;
		}
		List<List<String>> roomSeatList = new LinkedList<List<String>>();
		for(;lineStart <= lineEnd;lineStart++){
			List<String> tmpList = new LinkedList<String>();
			for(int rowIndex = rowStart;rowIndex <= rowEnd;rowIndex++){
				String seatMark = "L" + lineStart + "R" + rowIndex;
				if(seatMap.get(seatMark) != null){
					RoomSeat seat = seatMap.get(seatMark);
					if(orderSeat.contains(seat.getSeatLabel())){
						tmpList.add("ticketSeat");
					}else{
						tmpList.add("hasSeat");
					}
				}else if(outerRingseatMap != null){
					addRoomRingseat(tmpList,seatMark, outerRingseatMap);
				}else{
					tmpList.add("noSeat");
				}
			}
			roomSeatList.add(tmpList);
		}
		return roomSeatList;
	}
	
	private void addRoomRingseat(List<String> tmpList,String seatMark,Map<String,String> outerRingseatMap){
		if(StringUtils.isNotBlank(outerRingseatMap.get(seatMark))){
			tmpList.add(outerRingseatMap.get(seatMark));	
		}else{
			tmpList.add("noSeat");
		}
	}
}
