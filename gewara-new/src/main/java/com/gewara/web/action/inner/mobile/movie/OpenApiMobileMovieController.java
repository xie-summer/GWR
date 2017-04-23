package com.gewara.web.action.inner.mobile.movie;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.model.api.ApiUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.Video;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MovieVideo;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.content.VideoService;
import com.gewara.service.member.TreasureService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
@Controller
public class OpenApiMobileMovieController extends BaseOpenApiMobileMovieController {
	public static final String FUTURE_TAG_WEEK = "week";
	public static final String FUTURE_TAG_MONTH = "month";
	
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	/**
	 * 正在热映电影(图片, 名称, 评分, 已购票人数, 看点)
	 * 可不传 表示查询全部
	 * 参数必传，根据citycode分页查询各个城市当前正在上映的影片列表
	 */
	@RequestMapping("/openapi/mobile/movie/getCurHotMovies.xhtml")
	public String getCurHotMovies(String citycode, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		if (StringUtils.isNotBlank(citycode)) {
			if (!partner.supportsCity(citycode)) {
				return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
			}
		} else {
			citycode = partner.getDefaultCity();
		}
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 50;
		if(maxnum>100) maxnum=100;
		List<Movie> movieList = mcpService.getOpenMovieList(citycode);
		if(movieList.isEmpty()) {
			movieList = mcpService.getCurMovieListByMpiCount(citycode, from, maxnum);
		}else{
			mcpService.sortTodayMoviesByMpiCount(citycode, movieList);	
		}
		
		String signName = SignName.INDEX_MOVIELIST;
		if(StringUtils.equals(AdminCityContant.CITYCODE_SH, citycode)){
			signName = SignName.INDEX_MOVIELIST_NEW;
		}
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(citycode,signName, null, null, true, 0, 13);
		List<Movie> newMovieList = new ArrayList<Movie>();
		for(GewaCommend commend : gcMovieList){
			if(commend.getRelatedid()!=null) {
				Movie movie = daoService.getObject(Movie.class, commend.getRelatedid());
				if(movie!=null){
					movieList.remove(movie);
					newMovieList.add(movie);
				}
			}
		}
		newMovieList.addAll(movieList);
		if(newMovieList!=null) {
			List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
			CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
			filter.filterMovie(newMovieList);
		}
		newMovieList = BeanUtil.getSubList(newMovieList, from, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		Date curDate = DateUtil.currentTime();
		Date newdate = curDate;
		String playtime=DateUtil.format(curDate, "HH:mm");
		boolean isToday = true;
		if(playtime.compareTo("22:00")>=0){
			isToday = false;
			curDate = DateUtil.addDay(curDate, 1);
			curDate = DateUtil.getBeginTimestamp(curDate);
		}
		Long begintime = DateUtil.getBeginningTimeOfDay(new Date()).getTime();
		for(Movie movie:newMovieList){
			//距离上映日期天数
			Map<String, Object> params = getMovieData(movie);
			long diff =0;
			if( null!=movie.getReleasedate()){
				diff= movie.getReleasedate().getTime()- begintime;
				diff=Math.round((diff/1000)/(3600*24));
			}
			int presell = 0;
			Long movieid = movie.getId();
			//影片排片数量
			Integer mpicount = mcpService.getMovieCurMpiCountByPlaydate(citycode, movieid, curDate);
			//播放影片影院数量
			Integer	cinemacount = mcpService.getPlayCinemaCountByPlayDate(citycode, movieid, curDate);
			MovieVideo mv = videoService.getMovieVideo(movie.getId());
			String countdes = "";
			if(movie.getReleasedate()!=null && newdate.compareTo(movie.getReleasedate())<0 && StringUtils.isNotBlank(movie.getPlaydate())){
				Date pdate = DateUtil.parseDate(movie.getPlaydate());
				String des = movie.getPlaydate();
				if(pdate!=null){
					des = DateUtil.format(pdate, "MM月dd日");
				}
				countdes = des + "上映";
				presell = 1;
			}else {
				if(cinemacount>0){
					if(isToday){
						countdes = "今天" + cinemacount + "家影院余" + mpicount + "场";
					}else {
						countdes = "明天" + cinemacount + "家影院余" + mpicount + "场";
					}
				}else {
					countdes = "今天排片尚未公布";
				}
			}
			params.put("presell", presell);
			params.put("diffrelease", diff);
			params.put("countdes", countdes);
			params.put("cinemacount", cinemacount);
			params.put("mpicount", mpicount);
			params.put("videoid", mv!=null?mv.getVideoid():null);
			resMapList.add(params);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 影片信息
	 */
	@RequestMapping("/openapi/mobile/movie/movieDetail.xhtml")
	public String movieDetail(Long movieid, String memberEncode, String citycode, ModelMap model, HttpServletRequest request) {
		if (movieid == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少必要参数！");
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "未找到此电影！");
		Treasure treasure = null;
		if (StringUtils.isNotBlank(memberEncode)) {
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_MOVIE, member.getId(), movieid, Treasure.ACTION_COLLECT);
		}
		Map<String, Object> resMap = getMovieData(movie);
		MovieVideo mv = videoService.getMovieVideo(movieid);
		if(StringUtils.isNotBlank(citycode)){
			Integer mpiCount = mcpService.getMovieCurMpiCount(citycode, movieid);
			Integer opiCount = openPlayService.getOpiCount(citycode, null, movieid, null, null, true);
			resMap.put("mpicount", mpiCount);
			resMap.put("opicount", opiCount);
		}
		int diaryResult = 0;
		String diaryContent = getDiaryContent(movieid);
		if(diaryContent!=null){
			diaryResult = 1;
			resMap.put("diaryContent", diaryContent);
		}
		resMap.put("diaryResult", diaryResult);
		resMap.put("videoid", mv!=null?mv.getVideoid():null);
		resMap.put("iscollect", treasure == null ? 0 : 1);
		model.put("resMap", resMap);
		initField(model, request);
		putMovieNode(model);
		return getOpenApiXmlDetail(model);
	}
	//单个影片的电影
	private Map<Long, String> diaryMap = new ConcurrentHashMap<Long, String>();
	private String getDiaryContent(Long movieid){
		String content = diaryMap.get(movieid);
		if(content==null){
			List<Diary> diaryList = diaryService.getDiaryList(Diary.class, null, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_MOVIE, movieid, 0, 1, "flowernum");
			if (!diaryList.isEmpty()) {
				content = blogService.getDiaryBody(diaryList.get(0).getId());
				diaryMap.put(movieid, content);//TODO:缓存时长
			}
		}
		return content;
	}
	/**
	 * 根据影院id获取当前影院放映的电影
	 */
	@RequestMapping("/openapi/mobile/movie/getMovieByCinemaId.xhtml")
	public String getMovieByCinemaId(Long cinemaid, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		if (cinemaid == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传入参数有误！");
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if (cinema == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影院不存在！");
		List<Movie> movieList = mcpService.getCurMovieListByCinemaId(cinemaid);
		List<Movie> openBookMovieList = mcpService.getOpenMovieList(cinema.getCitycode());
		if (maxnum != null && maxnum > 30) maxnum = 10;
		if (from == null || from <0) from = 0;
		if (maxnum == null) maxnum = 50;
		movieList = BeanUtil.getSubList(movieList, from, maxnum);
		mcpService.sortTodayMoviesByMpiCount(cinema.getCitycode(), movieList);
		String citycode = cinema.getCitycode();
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Movie movie : movieList){
			Integer cinemacount = mcpService.getPlayCinemaCount(citycode, movie.getId());
			Integer mpicount = mcpService.getMovieCurMpiCount(citycode, movie.getId());
			Map<String, Object> params = getMovieData(movie);
			params.put("cinemacount", cinemacount);
			params.put("mpicount", mpicount);
			int bookstatus = openBookMovieList.contains(movie.getId())?0:1;
			params.put("bookstatus", bookstatus);
			resMapList.add(params);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}

	/**
	 * 即将上映
	 * 即将上映类别（一周，一个月，全部）
	 * 参数必传，tag除外
	 * tag：month：查询当月即将上映影片，week：查询本周即将上映影片，为空，查询全部即将上映影片
	 */
	@RequestMapping("/openapi/mobile/movie/futureMovieList.xhtml")
	public String futureMovieList(String citycode, String tag, String appVersion, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		if(from==null) from = 0;
		if(maxnum ==null) maxnum = 10;
		if (maxnum > 100) maxnum = 100;
		if(appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_6)>=0){
			return getfmList(citycode, from, maxnum, model, request);
		}
		Date endDate = null;
		if (FUTURE_TAG_MONTH.equals(tag)) {
			endDate = DateUtil.getMonthLastDay(new Date());
		} else if (FUTURE_TAG_WEEK.equals(tag)) {
			endDate = DateUtil.getWeekLastDay(new Date());
		}
		List<Long> curMovieidList = mcpService.getCurMovieIdList(citycode);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<Movie> movieList = mcpService.getFutureMovieList(endDate, from, maxnum);
		for (Movie movie : movieList) {
			Map<String, Object> params = getMovieData(movie);
			MovieVideo mv = videoService.getMovieVideo(movie.getId());
			params.put("videoid", mv!=null?mv.getVideoid():null);
			int presell = 0;
			int mpicount = 0;
			int opicount = 0;
			if(curMovieidList.contains(movie.getId())){
				mpicount = mcpService.getMovieCurMpiCount(citycode, movie.getId());
				if(mpicount>0){
					opicount = openPlayService.getOpiCount(citycode, null, movie.getId(), DateUtil.getMillTimestamp(), null, true);
					if(opicount>0){
						presell = 1;
					}
				}
			}
			params.put("presell", presell); //是否开放预售
			params.put("mpicount", mpicount); 
			params.put("opicount", opicount);
			resMapList.add(params);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	private String getfmList(String citycode, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		List<Long> curMovieidList = mcpService.getCurMovieIdList(citycode);
		List<Movie> movieList = mcpService.getFutureMovieList(null, 0, 100);
		initField(model, request);
		List<Map<String, Object>> hotMapList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> curWeekMapList = new ArrayList<Map<String, Object>>();
		if(from==0){
			Collections.sort(movieList, new MultiPropertyComparator(new String[] {"clickedtimes"}, new boolean[] {false}));
			for (Movie movie : movieList) {
				if(hotMapList.size()>=3) break;
				Map<String, Object> params = getMovieData(movie);
				MovieVideo mv = videoService.getMovieVideo(movie.getId());
				params.put("videoid", mv!=null?mv.getVideoid():null);
				getFmMap(curMovieidList, movie, params, citycode);
				hotMapList.add(params);
			}
		}
		Collections.sort(movieList, new MultiPropertyComparator(new String[] {"releasedate"}, new boolean[] {true}));
		Date startdate = DateUtil.getCurDate();
		Date weeklastdate = DateUtil.getWeekLastDay(new Date());
		weeklastdate = DateUtil.getBeginningTimeOfDay(weeklastdate);
		List<Movie> tmpMovieList = new ArrayList<Movie>();
		for (Movie movie : movieList) {
			Integer count = mcpService.getMovieCurMpiCount(citycode, movie.getId(), startdate, weeklastdate);
			if(count>0){
				if(from==0){
					Map<String, Object> params = getMovieData(movie);
					MovieVideo mv = videoService.getMovieVideo(movie.getId());
					params.put("videoid", mv!=null?mv.getVideoid():null);
					getFmMap(curMovieidList, movie, params, citycode);
					curWeekMapList.add(params);
				}
				tmpMovieList.add(movie);
			}
		}
		model.put("hotMapList", hotMapList);
		model.put("curWeekMapList", curWeekMapList);
		movieList.removeAll(tmpMovieList);
		movieList = BeanUtil.getSubList(movieList, from, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for (Movie movie : movieList) {
			Map<String, Object> params = getMovieData(movie);
			MovieVideo mv = videoService.getMovieVideo(movie.getId());
			params.put("videoid", mv!=null?mv.getVideoid():null);
			getFmMap(curMovieidList, movie, params, citycode);
			resMapList.add(params);
		}
		model.put("resMapList", resMapList);
		return getXmlView(model, "inner/mobile/movie/futureMovieList.vm");
	}
	private void getFmMap(List<Long> curMovieidList, Movie movie, Map<String, Object> params, String citycode){
		params.put("xiangkan", movie.getXiangqu()); 
		int presell = 0;
		if(curMovieidList.contains(movie.getId())){
			List<Long> counts=openPlayService.getOpiCinemaidList(citycode,movie.getId());
			if(counts.size()>0) {
				presell = 1;
			}
		}
		params.put("presell", presell); //是否开放预售
	}
	/**
	 * 过去一天电影排行
	 */
	@RequestMapping("/openapi/mobile/movie/lastDayRankMovieList.xhtml")
	public String aweekRankMovieList(ModelMap model, HttpServletRequest request) {
		List<Map> movieMapList = nosqlService.getBuyTicketRanking();
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for (Map map : movieMapList) {
			Long movieid = Long.valueOf(map.get("movieId")+"");
			Movie movie = daoService.getObject(Movie.class, movieid);
			Map<String, Object> params = getMovieData(movie);
			String rankres = map.get("orderRelatively")+"";
			int rank = 0;
			if(StringUtils.equalsIgnoreCase(rankres, "drop")) rank = -1;
			else if(StringUtils.equalsIgnoreCase(rankres, "rise")) rank = 1;
			params.put("rank", rank);
			resMapList.add(params);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	/**
	 * 影片视频接口
	 */
	@RequestMapping("/openapi/mobile/movie/videoList.xhtml")
	public String videoList(Long movieid, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from==null) from = 0;
		if(movieid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "movieid不能为空！");
		if(maxnum==null) maxnum = 10;
		//FIXME:错误的逻辑
		List<MovieVideo> videoList = new ArrayList<MovieVideo>();
		MovieVideo video = videoService.getMovieVideo(movieid);
		if(video!=null){
			videoList = Arrays.asList(video);
		}
		getMovieVideoListMap(videoList, model, request);
		model.put("root", "movieVideoList");
		model.put("nextroot", "movieVideo");
		return getOpenApiXmlList(model);
	}
	/**
	 * 影片视频接口
	 */
	@RequestMapping("/openapi/mobile/movie/ykvideoList.xhtml")
	public String ykvideoList(Long movieid, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from==null) from = 0;
		if(movieid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "movieid不能为空！");
		if(maxnum==null) maxnum = 10;
		List<Video> videoList = videoService.getVideoListByTag(TagConstant.TAG_MOVIE, movieid, from, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Video mv : videoList){
			Map<String, Object> params = GewaApiMovieHelper.getVideoData(mv);
			resMapList.add(params);
		}
		return getOpenApiXmlList(resMapList, "videoList,video", model, request);
	}
	/**
	 * 根据影片id集合查询影片列表
	 */
	@RequestMapping("/openapi/mobile/movie/movieListByMovieids.xhtml")
	public String movieDetail(String movieids, String memberEncode, String citycode, ModelMap model, HttpServletRequest request) {
		String[] mds = StringUtils.split(movieids, ",");
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(String md : mds){
			Long movieid = Long.valueOf(md);
			Movie movie = daoService.getObject(Movie.class, movieid);
			if(movie == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "未找到此电影！");
			Treasure treasure = null;
			if (StringUtils.isNotBlank(memberEncode)) {
				Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
				treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_MOVIE, member.getId(), movieid, Treasure.ACTION_COLLECT);
			}
			Map<String, Object> resMap = getMovieData(movie);
			MovieVideo mv = videoService.getMovieVideo(movieid);
			if(StringUtils.isNotBlank(citycode)){
				Integer mpiCount = mcpService.getMovieCurMpiCount(citycode, movieid);
				Integer opiCount = openPlayService.getOpiCount(citycode, null, movieid, null, null, true);
				resMap.put("mpicount", mpiCount);
				resMap.put("opicount", opiCount);
			}
			resMap.put("videoid", mv!=null?mv.getVideoid():null);
			resMap.put("iscollect", treasure == null ? 0 : 1);
			resMapList.add(resMap);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 我收藏的影片列表（返回数据 正在热映列表、正在上映列表）
	 */
	@RequestMapping("/openapi/mobile/movie/movieListByMy.xhtml")
	public String movieListByMy(Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 10;
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<Long> movieidList = treasureService.getTreasureIdList(member.getId(), TagConstant.TAG_MOVIE, Treasure.ACTION_COLLECT);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<Movie> movieList = new ArrayList<Movie>(); 
		for(Long movieid : movieidList){
			Movie movie = daoService.getObject(Movie.class, movieid);
			movieList.add(movie);
		}
		movieList = BeanUtil.getSubList(movieList, from, maxnum);
		for(Movie movie : movieList){
			Map<String, Object> resMap = getMovieData(movie);
			resMap.put("iscollect", 1);
			resMapList.add(resMap);
		}
		initField(model, request);
		putMovieListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 根据影院放映的影片和场次
	 */
	@RequestMapping("/openapi/mobile/movie/getMovieAndMpiList.xhtml")
	public String getCurMpiList(Long cinemaid, Date playdate, ModelMap model, HttpServletRequest request) {
		List<MoviePlayItem> mpiList = mcpService.getCinemaMpiList(cinemaid, playdate);
		Map<Long, List<MoviePlayItem>> movieidListMap = BeanUtil.groupBeanList(mpiList, "movieid");
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Long movieid : movieidListMap.keySet()){
			Movie movie = daoService.getObject(Movie.class, movieid);
			Map<String, Object> resMap = getMovieData(movie);
			List<MoviePlayItem> mmpiList = movieidListMap.get(movieid);
			Collections.sort(mmpiList, new MultiPropertyComparator(new String[] { "playtime"}, new boolean[] { true}));
			List<String> playtimeList = BeanUtil.getBeanPropertyList(mmpiList, String.class, "playtime", true);
			resMap.put("playtimes", StringUtils.join(playtimeList, ","));
			resMapList.add(resMap);
		}
		initField(model, request);
		putMovieListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 电影评分详情
	 */
	@RequestMapping("/openapi/mobile/movie/getMovieMarkDetail.xhtml")
	public String getCurMpiList(Long movieid, ModelMap model, HttpServletRequest request) {
		List<Map> resMapList = markService.getGradeDetail(TagConstant.TAG_MOVIE, movieid);
		initField(model, request);
		model.put("root", "markList");
		model.put("nextroot", "mark");
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 预定相关查询
	 */
	@RequestMapping("/openapi/mobile/count/bookingCount.xhtml")
	public String bookingCount(String citycode, ModelMap model, HttpServletRequest request) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("cinemaBookingCount", getBookingPlaceCount(Cinema.class, citycode));
		resMap.put("sportBookingCount", getBookingPlaceCount(Sport.class, citycode));
	
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		query.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("odi.citycode", citycode));
		query.add(Restrictions.le("odi.opentime", curtime));
		query.add(Restrictions.gt("odi.closetime", curtime));
		query.setProjection(Projections.distinct(Projections.property("odi.dramaid")));
		query.setProjection(Projections.rowCount());
		Integer dramaBookingCount = Integer.valueOf(hibernateTemplate.findByCriteria(query).get(0)+"");
		resMap.put("dramaBookingCount", dramaBookingCount);
		
		initField(model, request);
		model.put("resMap", resMap);
		model.put("root", "bookingCount");
		return getOpenApiXmlDetail(model);
	}
	private Integer getBookingPlaceCount(Class clazz, String citycode){
		DetachedCriteria qry = DetachedCriteria.forClass(clazz);
		if(StringUtils.isNotBlank(citycode)){
			qry.add(Restrictions.eq("citycode", citycode));
		}
		qry.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
		qry.setProjection(Projections.rowCount());
		Integer count = Integer.valueOf(hibernateTemplate.findByCriteria(qry).get(0)+"");
		return count;
	}
}
