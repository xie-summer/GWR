package com.gewara.web.action.subject.admin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.ticket.MpiPair;
import com.gewara.json.TempRoomSeat;
import com.gewara.json.ViewFilmSchedule;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
@Controller
public class FilmFestivalAdminController extends BaseAdminController implements InitializingBean {
	//电影节专用后台
	private static final String KEY_FILMFEST_CACHE = "filmFestCache";		//电影节页面缓存
	
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@RequestMapping("/admin/filmfest/refreshCache.xhtml")
	public String refreshCache(ModelMap model){
		GewaConfig config = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_FILMFEST_CACHE);
		config.setContent(DateUtil.format(new Timestamp(System.currentTimeMillis()), "ddHHmmss"));
		configCenter.refresh(Config.SYSTEMID, KEY_FILMFEST_CACHE);
		return forwardMessage(model, "success!" );
	}
	//电影节所有电影
	@RequestMapping("/admin/filmfest/movieList.xhtml")
	public String movieList(String tag, Long movieid, String flag, String state, String type, String moviename, ModelMap model){
		if(StringUtils.isBlank(tag)) tag = FilmFestConstant.TAG_FILMFEST_16;
		List<Long> movieidList = new ArrayList<Long>();
		String flagStr = flag;
		String order = "hotvalue";
		if(movieid == null) movieidList = filmFestService.getJoinMovieIdList(null, AdminCityContant.CITYCODE_SH, null, type, flagStr, state, moviename, null, tag, order, 0, 500);
		else movieidList.add(movieid);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		model.put("movieList", movieList);
		model.put("tag", tag);
		return "admin/movie/filmFestList.vm";
	}
	//电影节所有场次
	@RequestMapping("/admin/filmfest/mpiList.xhtml")
	public String mpiList(String tag, Long cid, Long mid, Date date, ModelMap model){
		if(StringUtils.isBlank(tag)) tag = FilmFestConstant.TAG_FILMFEST_16;
		SpecialActivity sa = filmFestService.getSpecialActivity(tag);
		User user = getLogonUser();
		Date cur = DateUtil.getCurDate();
		if(date == null) date = cur;
		List<Long> bookingidList = filmFestService.getFilmFestCinema(tag, AdminCityContant.CITYCODE_SH, null);
		Map<Long, Cinema> cinemaMap = daoService.getObjectMap(Cinema.class, bookingidList);
		if(bookingidList.isEmpty()) return "admin/movie/filmfest/mpiList.vm";
		if(cid == null){
			 cid = bookingidList.get(0);
		}
		Map<String, String> dateMap = new HashMap<String, String>();
		List<String> dateList = new ArrayList<String>();
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for(int i=0;i< 14; i++){
			String d = DateUtil.formatDate(DateUtil.addDay(cur, i));
			dateList.add(d);
			String dateStr = DateUtil.format(DateUtil.addDay(cur, i), "M月d日");
			dateMap.put(d, dateStr + " " + DateUtil.getCnWeek(DateUtil.addDay(cur, i)));
			Integer count = filmFestService.getMoviePlayItemCount(AdminCityContant.CITYCODE_SH, null, cid, DateUtil.addDay(cur, i), false, sa.getId());
			countMap.put(d, count);
		}
		model.put("dateList", dateList);
		model.put("dateMap", dateMap);
		model.put("countMap", countMap);
		model.put("cinemaMap", cinemaMap);
		Cinema cinema = cinemaMap.get(cid);
		if(cinema==null) return forwardMessage(model, "此影院未开放订票！");
		model.put("cinema", cinema);
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cid);
		model.put("profile", profile);
		model.put("user", user);
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinema.getId());
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		Map<Long, CinemaRoom> roomMap = BeanUtil.beanListToMap(roomList, "id");
		List<Long> roomIdList = BeanUtil.getBeanPropertyList(roomList, Long.class, "id", true);
		Map<Long, List<TempRoomSeat>> tempSeatMap = getTemplateMap(roomIdList);
		model.put("roomMap", roomMap);
		model.put("tempSeatMap", tempSeatMap);
		model.put("roomList", roomList);
		List<Movie> movieList = filmFestService.getCurMovieListByCinemaIdAndDate(cid, date, sa.getId());
		Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
		model.put("movieList", movieList);
		model.put("movieMap", movieMap);
		List<Long> mpiIdList = filmFestService.getMoviePlayItemIdList(AdminCityContant.CITYCODE_SH, mid, cid, date, false, sa.getId(), null, 0, 1000); 
		List<MoviePlayItem> playitemList = daoService.getObjectList(MoviePlayItem.class, mpiIdList);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		Map<Long, Goods> goodMap=new HashMap<Long, Goods>();
		for (MoviePlayItem moviePlayItem : playitemList) {
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", moviePlayItem.getId(), true);
			if(opi != null){
				GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, null);
				if(goodsGift!=null) {
					Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
					goodMap.put(opi.getId(), goods);
				}
				opiList.add(opi);
			}
		}
		List<Long> mpidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "mpid", true);
		List<MoviePlayItem> tmpMpiList = new ArrayList<MoviePlayItem>(playitemList);
		for(MoviePlayItem mpi: tmpMpiList) if(mpidList.contains(mpi.getId())) playitemList.remove(mpi);
		model.put("goodMap", goodMap);
		List<Long> movieidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "movieid", true);
		model.put("opiList", opiList);
		model.put("movieidList", movieidList);
		Map movienameMap = BeanUtil.getKeyValuePairMap(opiList, "movieid", "moviename");
		model.put("movienameMap", movienameMap);
		model.put("playitemList", playitemList);
		model.put("curDate", DateUtil.formatDate(date));
		model.put("curTime", DateUtil.formatTimestamp(new Timestamp(System.currentTimeMillis())));
		model.put("userid", getLogonUser().getId());
		model.put("editionList", OpiConstant.EDITIONS);//版本列表
		Map<String, String> eMap = new HashMap<String, String>();
		int i = 0;
		for(String s : OpiConstant.EDITIONS){
			eMap.put(s, "ed"+(i++));
		}
		model.put("eMap", eMap);
		List<String> languages = BeanUtil.getBeanPropertyList(opiList, String.class, "language", true);
		Map<String, String> languageMap = new HashMap<String, String>();
		for(String language : languages){
			languageMap.put(language, "language" + (i++));
		}
		model.put("languageMap",languageMap);
		model.put("languages",languages);
		model.put("parnterMap", OpiConstant.partnerTextMap);
		return "admin/movie/filmfest/mpiList.vm";
	}
	
	@RequestMapping("/admin/filmfest/copyOpiRemark.xhtml")
	public String copyOpiRemark(String tag, ModelMap model){
		if(StringUtils.isBlank(tag)) tag = FilmFestConstant.TAG_FILMFEST_16;
		SpecialActivity sa = filmFestService.getSpecialActivity(tag);
		List<MoviePlayItem> itemList = filmFestService.getMoviePlayItemList(AdminCityContant.CITYCODE_SH, null, null, null, sa.getId(), null, 0, 2000);
		Collections.sort(itemList, new MultiPropertyComparator(new String[]{"cinemaid", "roomnum", "playdate", "playtime"}, new boolean[]{true,true,true,true}));
		
		Map<String, List<OpenPlayItem>> opiMap = new HashMap<String, List<OpenPlayItem>>();
		for (MoviePlayItem item : itemList) {
			String key = item.getCinemaid() + "_" + StringUtils.trim(item.getRoomnum()) + "_" + item.getMovieid()+ "_" + DateUtil.formatDate(item.getPlaydate()) + "_" + item.getPlaytime();
			List<OpenPlayItem> opiList = opiMap.get(key);
			if(opiList == null){
				opiList = new ArrayList<OpenPlayItem>();
				opiMap.put(key, opiList);
			}
			OpenPlayItem odi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", item.getId(), true);
			opiList.add(odi);
		}
		List<String> msgList = new LinkedList<String>();
		for (String key : opiMap.keySet()) {
			List<OpenPlayItem> opiList = opiMap.get(key);
			filmFestService.copyOpiRemark(opiList, msgList);
		}
		return forwardMessage(model, msgList);
	}
	
	//电影节所有场次
	@RequestMapping("/admin/filmfest/mpiViewList.xhtml")
	public String mpiViewList(String tag, Date playDate, String order, Integer pageNo, ModelMap model){
		if(StringUtils.isBlank(tag)) tag = FilmFestConstant.TAG_FILMFEST_16;
		SpecialActivity sa = filmFestService.getSpecialActivity(tag);
		if(playDate == null) playDate = DateUtil.getCurDate();
		if(pageNo == null) pageNo = 0;
		int maxnum = 500;
		int from = pageNo * maxnum;
		List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList(AdminCityContant.CITYCODE_SH, null, null, playDate, sa.getId(), order, from, maxnum);
		Map<String/*movieid|cinemaid+roomnum+playdate+playtime*/, MpiPair> pairMap = new LinkedHashMap<String, MpiPair>();
		
		
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		for (MoviePlayItem mpi : mpiList) {
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
			if(opi != null) opiMap.put(mpi.getId(), opi);
			String key = mpi.getMovieid() + "|" + mpi.getCinemaid() + mpi.getFullPlaytime() + mpi.getRoomnum();
			MpiPair pair = pairMap.get(key);
			if(pair==null){
				pair = new MpiPair();
				pairMap.put(key, pair);
			}
			if(mpi.hasGewara()){
				pair.setGmpi(mpi);
			}else{
				pair.setPmpi(mpi);
			}
		}
		List<Long> movieIdList = BeanUtil.getBeanPropertyList(mpiList, Long.class, "movieid", true);
		List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(mpiList, Long.class, "cinemaid", true);
		Map<Long, Movie> movieMap = daoService.getObjectMap(Movie.class, movieIdList);
		Map<Long, Cinema> cinemaMap = daoService.getObjectMap(Cinema.class, cinemaIdList);
		model.put("opiMap", opiMap);
		model.put("mpiList", mpiList);
		model.put("movieMap", movieMap);
		model.put("cinemaMap", cinemaMap);
		model.put("tag", tag);
		model.put("pairList", new ArrayList<MpiPair>(pairMap.values()));
		return "admin/movie/filmFestMpiList.vm";
	}
	//保存mpi.otherinfo
	@RequestMapping("/admin/filmfest/updateMpiOtherinfo.xhtml")
	public String updateMpiOtherinfo(String mpids, String unopengewa, String unshowgewa, ModelMap model){
		String[] idList = StringUtils.split(mpids, ",");
		if(idList == null) return showJsonError(model, "至少选择一个场次！");
		filmFestService.updateMpiOtherinfo(idList, unopengewa, unshowgewa);
		return showJsonSuccess(model);
	}

	//疑似电影节场次
	@RequestMapping("/admin/filmfest/maybeMpiList.xhtml")
	public String maybeMpiList(Date playDate, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(pageNo == null) pageNo = 0;
		List<Long> cinemaIdList = filmFestService.getFilmFestCinema(FilmFestConstant.TAG_FILMFEST_16, AdminCityContant.CITYCODE_SH, null);
		Map<Long, Cinema> cinemaMap = daoService.getObjectMap(Cinema.class, cinemaIdList);
		List<Long> movieidList = filmFestService.getFilmFestMovieIdList(FilmFestConstant.TAG_FILMFEST_16, null, null, 0, 400);
		List<MoviePlayItem> maybeMpiList = filmFestService.getMaybeMoviePlayItemList(cinemaIdList, movieidList, playDate, sa.getId());
		Map<Long, Movie> movieMap = daoService.getObjectMap(Movie.class, movieidList);
		model.put("movieMap", movieMap);
		model.put("mpiList", maybeMpiList);
		model.put("cinemaMap", cinemaMap);
		model.put("batch", sa.getId());
		return "admin/movie/filmfest/filmFestMayBeMpiList.vm";
	}
	//加入电影节场次
	@RequestMapping("/admin/filmfest/joinMpiList.xhtml")
	public String joinMpiList(String tag, Long mpid, ModelMap model){
		if(StringUtils.isBlank(tag)) tag = FilmFestConstant.TAG_FILMFEST_16;
		SpecialActivity sa = filmFestService.getSpecialActivity(tag);
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		if(mpi == null) return showJsonError(model, "该场次不存在！");
		ChangeEntry changeEntry = new ChangeEntry(mpi);
		mpi.setBatch(sa.getId());
		daoService.saveObject(mpi);
		monitorService.saveChangeLog(getLogonUser().getId(), MoviePlayItem.class, mpi.getId(),changeEntry.getChangeMap(mpi));
		return showJsonSuccess(model);
	}
	//移出电影节场次
	@RequestMapping("/admin/filmfest/removeMpiList.xhtml")
	public String removeMpiList(String tag, Long mpid, ModelMap model){
		if(StringUtils.isBlank(tag)) tag = FilmFestConstant.TAG_FILMFEST_16;
		SpecialActivity sa = filmFestService.getSpecialActivity(tag);
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		if(mpi == null) return showJsonError(model, "该场次不存在！");
		if(mpi.getBatch() != null && !mpi.getBatch().equals(sa.getId())) return showJsonError(model, "该场次不为电影节场次！");
		ChangeEntry changeEntry = new ChangeEntry(mpi);
		mpi.setBatch(null);
		daoService.saveObject(mpi);
		monitorService.saveChangeLog(getLogonUser().getId(), MoviePlayItem.class, mpi.getId(),changeEntry.getChangeMap(mpi));
		return showJsonSuccess(model);
	}
	//得到单个电影的单元类型
	@RequestMapping("/admin/filmfest/getMovie.xhtml")
	public String getMovie(Long id, String tag, ModelMap model){
		Movie movie = daoService.getObject(Movie.class, id);
		if(movie != null && movie.getFlag().contains(tag)){
			List<GewaCommend> gcList = commonService.getGewaCommendListByParentid(SignName.FILM_MOVIE_LINK, 0l,true);
			model.put("gcList", gcList);
			model.put("movie", movie);
			model.put("tag", tag);
		}
		return "admin/movie/filmFestFlag.vm";
	}
	//保存电影的单元类型
	@RequestMapping("/admin/filmfest/saveMovieFlag.xhtml")
	public String saveMovieFlag(Long id, String flag, String tag, ModelMap model){
		Movie movie = daoService.getObject(Movie.class, id);
		if(movie != null && movie.getFlag().contains(tag)){
			if(StringUtils.isBlank(flag)) flag = tag;
			else flag = tag + "," + flag;
			movie.setFlag(flag);
			daoService.saveObject(movie);
			return showJsonSuccess(model);
		}else {
			return showJsonError(model, "未找到改电影或不属于电影节电影！");
		}
	}
	//统计第十六届电影节
	@RequestMapping("/admin/filmfest/estimated16Number.xhtml")
	public String estimated16Number(String type, ModelMap model){
		String namespace = ViewFilmSchedule.class.getCanonicalName();
		String groupByFiled = "mpid";
		DBObject cond = new BasicDBObject();
		cond.put("type", "schedule");
		if(StringUtils.equals(type, TagConstant.TAG_MOVIE)){
			cond.put("type", "movie");
			groupByFiled = "movieId";
		}
		BasicDBObject initial = new BasicDBObject();   
		initial.put("total", 0);
		String reduce = "function(obj,prev) { prev.total++; }";
		List<Map> list = mongoService.getGroupBy(namespace, groupByFiled, cond, initial, reduce);
		Collections.sort(list, new MultiPropertyComparator(new String[]{"total"}, new boolean[]{false}));
		model.put("vfsList", list);
		if(StringUtils.equals(type, TagConstant.TAG_MOVIE)){
			List<Long> movieIdList = BeanUtil.getBeanPropertyList(list, Long.class, "movieId", true);
			Map movieMap = daoService.getObjectMap(Movie.class, movieIdList);
			model.put("movieMap", movieMap);
		}else{
			List<Long> mpiIdList = BeanUtil.getBeanPropertyList(list, Long.class, "mpid", true);
			List<MoviePlayItem> mpiList = daoService.getObjectList(MoviePlayItem.class, mpiIdList);
			List<Long> movieIdList = BeanUtil.getBeanPropertyList(mpiList, Long.class, "movieid", true);
			Map movieMap = daoService.getObjectMap(Movie.class, movieIdList);
			model.put("movieMap", movieMap);
			List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(mpiList, Long.class, "cinemaid", true);
			Map cinemaMap = daoService.getObjectMap(Cinema.class, cinemaIdList);
			model.put("cinemaMap", cinemaMap);
			model.put("mpiMap", BeanUtil.beanListToMap(mpiList, "id"));
		}
		model.put("type", type);
		return "admin/movie/estimated.vm";
	}
	@RequestMapping("/admin/filmfest/estimatedAll.xhtml")
	public String estimatedAll(String type, ModelMap model){
		if(StringUtils.isBlank(type)) type = ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST;
		Map params = new HashMap();
		params.put("type", type);
		List vfsList = mongoService.find(ViewFilmSchedule.class.getCanonicalName(), params, "addtime", false);
		model.put("vfsList", vfsList);
		if(StringUtils.equals(type, TagConstant.TAG_MOVIE)){
			List<Long> movieIdList = BeanUtil.getBeanPropertyList(vfsList, Long.class, "movieId", true);
			Map movieMap = daoService.getObjectMap(Movie.class, movieIdList);
			model.put("movieMap", movieMap);
		}else{
			List<Long> mpiIdList = BeanUtil.getBeanPropertyList(vfsList, Long.class, "mpid", true);
			List<MoviePlayItem> mpiList = daoService.getObjectList(MoviePlayItem.class, mpiIdList);
			List<Long> movieIdList = BeanUtil.getBeanPropertyList(mpiList, Long.class, "movieid", true);
			Map movieMap = daoService.getObjectMap(Movie.class, movieIdList);
			model.put("movieMap", movieMap);
			List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(mpiList, Long.class, "cinemaid", true);
			Map cinemaMap = daoService.getObjectMap(Cinema.class, cinemaIdList);
			model.put("cinemaMap", cinemaMap);
			model.put("mpiMap", BeanUtil.beanListToMap(mpiList, "id"));
		}
		model.put("type", type);
		model.put("isAll", true);
		return "admin/movie/estimated.vm";
	}
	
	@RequestMapping("/admin/filmfest/updateOtherInfoByIds.xhtml")
	public String updateOtherInfoByIds(String ids, String type, ModelMap model){
		String[] idList = StringUtils.split(ids, ",");
		List<Movie> movieList = new ArrayList<Movie>();
		String errIds = "";
		for(String id : idList){
			if(ValidateUtil.isNumber(id)){
				Movie movie = daoService.getObject(Movie.class, Long.parseLong(id));
				if(movie == null){
					errIds += ","+id;
				}else{
					if(!StringUtils.contains(movie.getFlag(), type)){
						if(StringUtils.isNotBlank(movie.getFlag())){
							movie.setFlag(movie.getFlag()+","+type);
							movieList.add(movie);
						}else{
							errIds += ","+id;
						}
					}
				}
			}else{
				errIds += ","+id;
			}
		}
		daoService.saveObjectList(movieList);
		String result = "success :"+movieList.size()+"      err:"+errIds;
		return showJsonSuccess(model,result);
	}
	private Map<Long, List<TempRoomSeat>> getTemplateMap(List<Long> roomIdList){
		DBObject queryCondition = mongoService.queryBasicDBObject("roomid", "in", roomIdList);
		List<TempRoomSeat> resultList = mongoService.getObjectList(TempRoomSeat.class, queryCondition);
		Collections.sort(resultList, new MultiPropertyComparator(new String[]{"tmpname"}, new boolean[]{true}));
		Map<Long, List<TempRoomSeat>> dataMap = BeanUtil.groupBeanList(resultList, "roomid");
		return dataMap;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		configCenter.register(Config.SYSTEMID,KEY_FILMFEST_CACHE, filmFestService);
	}
}
