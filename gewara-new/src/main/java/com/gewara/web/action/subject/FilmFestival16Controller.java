package com.gewara.web.action.subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.PageView;
import com.gewara.json.ViewFilmSchedule;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
@Controller
public class FilmFestival16Controller extends AnnotationController{
	private static final String MOVIE_STATUS_TICKET = "ticket";		//选座购票
	private static final String MOVIE_STATUS_NONE = "none";			//卖光了
	private static final String MOVIE_STATUS_JOIN = "join";			//没有排片加入片单
	private static final String MOVIE_STATUS_SHOW = "show";			//排片购票
	private static final String MOVIE_TYPE_JINJUE = "jinjue";
	private static final String MOVIE_TYPE_CANZHAN = "canzhan";
	private static final String MOVIE_TYPE_XINREN = "xinren";
	
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	//第十六届电影节strat
	//首页
	private static final String cacheKey = "c";
	@RequestMapping("/filmfest/sixteen.xhtml")
	public String sixteen(HttpServletRequest request, ModelMap model){
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams();
			params.addSingleString(cacheKey, filmFestService.getCachePre());
			PageView pageView = pageCacheService.getPageView(request, "filmfest/sixteen.xhtml", params, AdminCityContant.CITYCODE_SH);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");

		//热门推荐电影
		List<GewaCommend> gcList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "sp"+sa.getId(), null, "movie", true, 0, 20);
		List<Movie> hotMovieList = new ArrayList<Movie>();
		Map<Long, String> hotMovieStatusMap = new HashMap<Long, String>();
		putMovieStatus(gcList, hotMovieList, hotMovieStatusMap, sa.getId());
		List<Movie> hotMovie1List = BeanUtil.getSubList(hotMovieList, 0, 10);
		List<Movie> hotMovie2List = BeanUtil.getSubList(hotMovieList, 10, 10);
		model.put("hotMovie1List", hotMovie1List);
		model.put("hotMovie2List", hotMovie2List);
		model.put("hotMovieStatusMap", hotMovieStatusMap);
		//推荐场次
		List<GewaCommend> gcMpiList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "sp"+sa.getId(), null, "mpi", true, 0, 20);
		List<Long> mpiIdList = BeanUtil.getBeanPropertyList(gcMpiList, Long.class, "relatedid", true);
		List<MoviePlayItem> topMpiList = daoService.getObjectList(MoviePlayItem.class, mpiIdList);
		Map<Long, Cinema> topCinemaMap = new HashMap<Long, Cinema>();
		Map<Long, OpenPlayItem> topOpiMap = new HashMap<Long, OpenPlayItem>();

		Set<Long> movieidList = putMpiDataMap(topMpiList, topCinemaMap, topOpiMap);
		Map<Long, Movie> topMovieMap = daoService.getObjectMap(Movie.class, movieidList);
		
		model.put("topMpi1List", BeanUtil.getSubList(topMpiList, 0, 10));
		model.put("topMpi2List", BeanUtil.getSubList(topMpiList, 10, 10));
		model.put("topCinemaMap", topCinemaMap);
		model.put("topMovieMap", topMovieMap);
		model.put("topOpiMap", topOpiMap);
		//焦点趣闻
		List<GewaCommend> gcNewsList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "sp"+sa.getId(), null, "news", true, 0, 8);
		List<Long> newsIdList = BeanUtil.getBeanPropertyList(gcNewsList, Long.class, "relatedid", true);
		List<News> newsList = daoService.getObjectList(News.class, newsIdList);
		model.put("newsList", newsList);
		//排片下载链接
		List<GewaCommend> uploadList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "upload"+sa.getId(), null, "other", true, 0, 1);
		if(uploadList != null && !uploadList.isEmpty()) model.put("uploadUrl", uploadList.get(0).getTitle());
		//我在现场
		List<GewaCommend> gcXCList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "xc"+sa.getId(), null, "news", true, 0, 1);
		List<Long> xcIdList = BeanUtil.getBeanPropertyList(gcXCList, Long.class, "relatedid", true);
		if(!xcIdList.isEmpty()){
			News xcNews = daoService.getObject(News.class, xcIdList.get(0));
			model.put("xcNews", xcNews);
		}
		List<Map> bannerList = getBannerList(sa.getId());
		model.put("bannerList", bannerList);
		//影片数量
		Integer jinJueCount = filmFestService.getFilmFestMovieCount(FilmFestConstant.TAG_FILMFEST_16, "flag", "金爵");
		Integer canZhanCount = filmFestService.getFilmFestMovieCount(FilmFestConstant.TAG_FILMFEST_16, "flag", "参展");
		Integer xinRenCount = filmFestService.getFilmFestMovieCount(FilmFestConstant.TAG_FILMFEST_16, "flag", "亚洲新人");
		model.put("jinJueCount", jinJueCount);
		model.put("canZhanCount", canZhanCount);
		model.put("xinRenCount", xinRenCount);
		return "subject/filmfest/2013/index.vm";
	}
	private List<Map> getBannerList(Long sid){
		//广告位
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "sp"+sid);
		params.put(MongoData.ACTION_TAG, "16filmBanner");
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> bannerList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 8);
		return bannerList;
	}
	//首页热门活动
	@RequestMapping("/filmfest/ajax/getHotActivity.xhtml")
	public String getHotActivity(ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() != null){
			int count = commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, "activity", true);
			if(count > 0){
				List<GewaCommend> gcList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "activity", true, 0, 12);
				List<Long> activityIdList = BeanUtil.getBeanPropertyList(gcList, Long.class, "relatedid", true);
				ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityListByIds(activityIdList);
				if(code.isSuccess()){
					List<RemoteActivity> hotAcList = code.getRetval();
					List<RemoteActivity> hotAc1List = BeanUtil.getSubList(hotAcList, 0, 4);
					List<RemoteActivity> hotAc2List = BeanUtil.getSubList(hotAcList, 4, 4);
					List<RemoteActivity> hotAc3List = BeanUtil.getSubList(hotAcList, 8, 4);
					model.put("hotAc1List", hotAc1List);
					model.put("hotAc2List", hotAc2List);
					model.put("hotAc3List", hotAc3List);
					model.put("hotActivityCount", count);
				}
			}
		}
		return "subject/filmfest/2013/hotActivityList.vm";
	}
	//首页下方电影推荐
	@RequestMapping("/filmfest/ajax/getMovieList.xhtml")
	public String getMovieList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String type, HttpServletRequest request, ModelMap model){
		if(StringUtils.equals(type, MOVIE_TYPE_JINJUE) || StringUtils.equals(type, MOVIE_TYPE_CANZHAN) || StringUtils.equals(type, MOVIE_TYPE_XINREN)){
			SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
			if(sa.getId() != null){
				Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
				if(member != null) putMyJoinData(member.getId(), model);
				List<GewaCommend> gcList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, type+sa.getId(), null, "movie", true, 0, 6);
				List<Long> movieIdList = BeanUtil.getBeanPropertyList(gcList, Long.class, "relatedid", true);
				Map<Long, List<MoviePlayItem>> movieMpiMap = new HashMap<Long, List<MoviePlayItem>>();
				Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
				Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
				putMovieOpi(movieIdList, null, null, movieMpiMap, opiMap, cinemaMap, sa.getId());
				List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
				model.put("movieList", movieList);
				model.put("movieMpiMap", movieMpiMap);
				model.put("opiMap", opiMap);
				model.put("cinemaMap", cinemaMap);
			}
		}
		return "subject/filmfest/2013/indexMovieList.vm";
	}
	//保存片单、日程
	@RequestMapping("/filmfest/ajax/saveViewFilmSchedule.xhtml")
	public String saveViewFilmSchedule(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long relatedid, String tag, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showJsonError(model, "暂无此活动相关信息！");
		String opkey = "saveViewFilmSchedule" + member.getId();
		boolean allow = operationService.updateOperation(opkey, OperationService.HALF_MINUTE, 5);
		if(!allow) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		MoviePlayItem mpi = null;
		if(ViewFilmSchedule.TYPE_MOVIE_FILMFEST.equals(tag)){
			Movie movie = daoService.getObject(Movie.class, relatedid);
			if(movie == null) return showJsonError(model, "未找到该电影信息！");
			if(!StringUtils.contains(movie.getFlag(), FilmFestConstant.TAG_FILMFEST_16)) return showJsonError(model, "该电影不属于电影节！");
		}else if(ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST.equals(tag)){
			mpi = daoService.getObject(MoviePlayItem.class, relatedid);
			if(mpi == null) return showJsonError(model, "未找到该场次信息！");
			if(mpi.getBatch() == null || !mpi.getBatch().equals(sa.getId())) return showJsonError(model, "该场次不属于电影节");
		}else{
			return showJsonError(model, "信息错误！");
		}
		ViewFilmSchedule vfs = nosqlService.addViewFilmSchedule(mpi, tag, relatedid, member.getId(),"web");
		if(vfs == null) return showJsonError(model, "你已添加过了，请不要重复添加！");
		return showJsonSuccess(model);
	}
	//删除我的片单、日程
	@RequestMapping("/filmfest/ajax/deleteViewFilmSchedule.xhtml")
	public String deleteViewFilmSchedule(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long relatedid, String type, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		Map params = new HashMap();
		if(StringUtils.equals(type, ViewFilmSchedule.TYPE_MOVIE_FILMFEST)){
			params.put("type", ViewFilmSchedule.TYPE_MOVIE_FILMFEST);
			params.put("movieId", relatedid);
		}else{
			params.put("type", ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST);
			params.put("mpid", relatedid);
		}
		params.put("memberId", member.getId());
		List<ViewFilmSchedule> vfsList = mongoService.getObjectList(ViewFilmSchedule.class, params, "addtime", false, 0, 1);
		if(vfsList.isEmpty()) return showJsonError(model, "未找到此片单或日程！");
		mongoService.removeObject(vfsList.get(0), "_id");
		return showJsonSuccess(model);
	}
	//我的片单
	@RequestMapping("/filmfest/myViewFilm.xhtml")
	public String myViewFilm(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Integer pageNo, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
			if(pageNo == null) pageNo = 0;
			int maxnum = 20;
			int from = pageNo * maxnum;
			Map params = new HashMap();
			params.put("type", ViewFilmSchedule.TYPE_MOVIE_FILMFEST);
			params.put("memberId", member.getId());
			int count = mongoService.getObjectCount(ViewFilmSchedule.class, params);
			if(count > 0){
				List<ViewFilmSchedule> vfsList = mongoService.getObjectList(ViewFilmSchedule.class, params, "addtime", false, from, maxnum);
				List<Long> movieIdList = BeanUtil.getBeanPropertyList(vfsList, Long.class, "movieId", true);
				Map<Long, List<MoviePlayItem>> movieMpiMap = new HashMap<Long, List<MoviePlayItem>>();
				Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
				Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
				putMovieOpi(movieIdList, null, null, movieMpiMap, opiMap, cinemaMap, sa.getId());
				List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
				model.put("movieList", movieList);
				model.put("movieMpiMap", movieMpiMap);
				model.put("opiMap", opiMap);
				model.put("cinemaMap", cinemaMap);
				PageUtil pageUtil = new PageUtil(count,maxnum,pageNo,"filmfest/myViewFilm.xhtml",true,true);
				pageUtil.initPageInfo(new HashMap());
				model.put("pageUtil",pageUtil);
				params = new HashMap();
				params.put("type", ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST);
				params.put("memberId", member.getId());
				List<Long> joinMpidList = mongoService.getDistinctPropertyList(ViewFilmSchedule.class.getCanonicalName(), params, "mpid");
				model.put("joinMpidList", joinMpidList);
			}
		}
		return "subject/filmfest/2013/viewFilm.vm";
	}
	//我的日程
	@RequestMapping("/filmfest/myViewSchedule.xhtml")
	public String myViewSchedule(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");
		List<GewaCommend> gcDayList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "15day"+sa.getId(), null, "other", true, 0, 9);
		model.put("gcDayList", gcDayList);
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		int morningLength = 1;
		int afternoonLength = 1;
		int nightLength = 1;
		int mpiPlanCount = 0;
		int datePlanCount = 0;
		if(member != null){
			List<Long> orderMpiIdList = new ArrayList<Long>();
			saveViewFilmScheduleByOrder(member.getId(), sa.getId(), orderMpiIdList);
			model.put("orderMpiIdList", orderMpiIdList);
			model.put("logonMember", member);
			DBObject queryCondition = new BasicDBObject();
			DBObject relate1 = mongoService.queryBasicDBObject("type", "=", ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST);
			DBObject relate2 = mongoService.queryBasicDBObject("memberId", "=", member.getId());
			queryCondition.putAll(relate1);
			queryCondition.putAll(relate2);
			List<ViewFilmSchedule> vfsList = mongoService.getObjectList(ViewFilmSchedule.class, queryCondition);
			Map<String, Map<String, List<MoviePlayItem>>> outerMap = new HashMap<String, Map<String,List<MoviePlayItem>>>();
			Map<String, List<MoviePlayItem>> dateMap = null;
			List<MoviePlayItem> mpiList = null;
			Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
			Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
			Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
			List<Date> dateList = new ArrayList<Date>();
			for (ViewFilmSchedule vfs : vfsList) {
				MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, vfs.getMpid());
				if(mpi == null){
					mongoService.removeObject(vfs, "_id");
				}else{
					if(cinemaMap.get(mpi.getCinemaid()) == null) cinemaMap.put(mpi.getCinemaid(), daoService.getObject(Cinema.class, mpi.getCinemaid()));
					if(movieMap.get(mpi.getMovieid()) == null) movieMap.put(mpi.getMovieid(), daoService.getObject(Movie.class, mpi.getMovieid()));
					if(StringUtils.equals(mpi.getOpenStatus(), OpiConstant.MPI_OPENSTATUS_OPEN)){
						OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
						if(opi != null) opiMap.put(mpi.getId(), opi);
					}
					int hour = Integer.parseInt(StringUtils.replace(mpi.getPlaytime(), ":", ""));
					String key = "night";
					if(hour > 600 && hour <= 1200){//上午
						key = "morning";
					}else if(hour > 1200 && hour < 1800){//下午
						key = "afternoon";
					}
					dateMap = outerMap.get(key); 
					if(dateMap == null) dateMap = new HashMap<String, List<MoviePlayItem>>();
					outerMap.put(key, dateMap);
					String playDateString = DateUtil.formatDate(mpi.getPlaydate());
					mpiList = dateMap.get(playDateString);
					if(mpiList == null) mpiList = new ArrayList<MoviePlayItem>();
					dateMap.put(playDateString, mpiList);
					mpiList.add(mpi);
					if(hour > 300 && hour <= 1200){
						if(morningLength < mpiList.size()) morningLength = mpiList.size();
					}else if(hour > 1200 && hour < 1800){
						if(afternoonLength < mpiList.size()) afternoonLength = mpiList.size();
					}else {
						if(nightLength < mpiList.size()) nightLength = mpiList.size();
					}
					if(!dateList.contains(mpi.getPlaydate()))dateList.add(mpi.getPlaydate());
					mpiPlanCount ++;
				}
			}
			datePlanCount = dateList.size();
			model.put("outerMap", outerMap);
			model.put("opiMap", opiMap);
			model.put("cinemaMap", cinemaMap);
			model.put("movieMap", movieMap);
		}
		model.put("datePlanCount", datePlanCount);
		model.put("mpiPlanCount", mpiPlanCount);
		model.put("morningLength", morningLength);
		model.put("afternoonLength", afternoonLength);
		model.put("nightLength", nightLength);
		return "subject/filmfest/2013/schedule.vm";
	}
	//快速购票页面
	@RequestMapping("/filmfest/sixteenMovieList.xhtml")
	public String sixteenMovieList(Date date, Long movieid, String flag, String state, String type, String moviename, Long cinemaid, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");
		if(pageNo == null) pageNo = 0;
		int maxnum = 20;
		int from = pageNo * maxnum;
		List<GewaCommend> flagList = commonService.getGewaCommendListByParentid(SignName.FILM_MOVIE_LINK, 0l,true);
		model.put("flagList", flagList);
		List<Long> cinemaIdList = filmFestService.getFilmFestCinema(FilmFestConstant.TAG_FILMFEST_16, AdminCityContant.CITYCODE_SH, null);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
		List<Long> movieidList = new ArrayList<Long>();
		String flagStr = flag;
		String order = "hotvalue";
		if(movieid == null){
			if(date != null || StringUtils.isNotBlank(type) || StringUtils.isNotBlank(flagStr) || StringUtils.isNotBlank(state) || StringUtils.isNotBlank(moviename) || cinemaid != null){
				movieidList = filmFestService.getJoinMovieIdList(sa.getId(), AdminCityContant.CITYCODE_SH, date, type, flagStr, state, moviename, cinemaid, FilmFestConstant.TAG_FILMFEST_16, order, from, maxnum);
			}else{
				List<GewaCommend> gcList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, "sp"+sa.getId(), null, "movie", true, from, maxnum);
				movieidList = BeanUtil.getBeanPropertyList(gcList, Long.class, "relatedid", false);
				if(movieidList.size() == 0){
					int count = commonService.getGewaCommendCount(AdminCityContant.CITYCODE_SH, "sp"+sa.getId(), null, "movie", true);
					int ynum = maxnum - (count % maxnum);
					int bnum = count / maxnum;
					if(ynum > 0) bnum ++;
					int pnum = pageNo - bnum;
					from = pnum * maxnum + ynum;
					movieidList.addAll(filmFestService.getJoinMovieIdList(sa.getId(), AdminCityContant.CITYCODE_SH, date, type, flagStr, state, moviename, cinemaid, FilmFestConstant.TAG_FILMFEST_16, order, from, maxnum));
				}else if(movieidList.size() < 20){
					maxnum = 20 - movieidList.size();
					movieidList.addAll(filmFestService.getJoinMovieIdList(sa.getId(), AdminCityContant.CITYCODE_SH, date, type, flagStr, state, moviename, cinemaid, FilmFestConstant.TAG_FILMFEST_16, order, 0, maxnum));
				}
			}
		}else {
			movieidList.add(movieid);
		}
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		Map<Long, List<MoviePlayItem>> movieMpiMap = new HashMap<Long, List<MoviePlayItem>>();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		putMovieOpi(movieidList, cinemaid, date, movieMpiMap, opiMap, cinemaMap, sa.getId());
		model.put("movieList", movieList);
		model.put("movieMpiMap", movieMpiMap);
		model.put("opiMap", opiMap);
		model.put("cinemaMap", cinemaMap);
		if(movieid == null){
			Integer count = filmFestService.getJoinMovieCount(sa.getId(), AdminCityContant.CITYCODE_SH, date, type, flagStr, state, moviename, cinemaid, FilmFestConstant.TAG_FILMFEST_16);
			Map params = new HashMap();
			params.put("date", DateUtil.format(date,"yyyy-MM-dd"));
			params.put("flag", flag);
			params.put("state",state);
			params.put("type",type);
			params.put("cinemaid", cinemaid);
			params.put("moviename", moviename);
			PageUtil pageUtil = new PageUtil(count,maxnum,pageNo,"filmfest/sixteenMovieList.xhtml",true,true);
			pageUtil.initPageInfo(params);
			model.put("pageUtil",pageUtil);
		}
		model.put("cinemaList", cinemaList);
		return "subject/filmfest/2013/opiList.vm";
	}
	//电影节快讯页面
	@RequestMapping("/filmfest/sixteenNewsList.xhtml")
	public String sixteenNewsList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,  Integer pageNo, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
		}
		if(pageNo == null) pageNo = 0;
		int maxnum = 15;
		int from = pageNo * maxnum;
		List<News> newsList = filmFestService.getFilmFestNewsList(AdminCityContant.CITYCODE_SH, FilmFestConstant.TAG_FILMFEST_16, null, null, from, maxnum);
		PageUtil pageUtil = new PageUtil(filmFestService.getFilmFestNewsCount(AdminCityContant.CITYCODE_SH, FilmFestConstant.TAG_FILMFEST_16, null, null),maxnum,pageNo,"filmfest/sixteenNewsList.xhtml",true,true);
		pageUtil.initPageInfo(new HashMap());
		model.put("pageUtil",pageUtil);
		model.put("newsList", newsList);
		//我在现场
		List<String> newsDataList = filmFestService.getFilmFestNewsDateList(AdminCityContant.CITYCODE_SH, FilmFestConstant.TAG_FILMFEST_16+"xc");
		model.put("newsDataList", newsDataList);
		//红毯瞬间
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		params.put(MongoData.ACTION_TAG, "16filmNews");
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> inHereList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 21);
		model.put("inHereList", inHereList);
		return "subject/filmfest/2013/newsFlash.vm";
	}
	@RequestMapping("/filmfest/ajax/newsXCList.xhtml")
	public String newsXCList(Date releasedate, ModelMap model){
		if(releasedate == null) releasedate = DateUtil.parseDate("2013-06-15");
		List<News> topNewsList = filmFestService.getFilmFestNewsList(AdminCityContant.CITYCODE_SH, FilmFestConstant.TAG_FILMFEST_16+"xc", null, releasedate, 0, 40);
		model.put("topNewsList", topNewsList);
		model.put("releasedate", DateUtil.formatDate(releasedate));
		return "subject/filmfest/2013/newsXCList.vm";
	}
	//影院攻略页面
	@RequestMapping("/filmfest/sixteenRaiders.xhtml")
	public String sixteenRaiders(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		return sixteenNotice(sessid, "cinema", request, model);
	}
	//购票须知页面
	@RequestMapping("/filmfest/sixteenNotice.xhtml")
	public String sixteenNotice(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String type, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
		}
		String signname = "sp" + sa.getId();
		if(StringUtils.equals(type, "cinema")){
			signname = "cinema" + sa.getId();
		}
		List<GewaCommend> gcList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, signname, null, "diary", true, 0, 4);
		if(!gcList.isEmpty()){
			List<String> contentList = new ArrayList<String>();
			for(GewaCommend gc : gcList){
				DiaryBase topic = diaryService.getDiaryBase(gc.getRelatedid());
				String body = blogService.getDiaryBody(topic.getId());
				contentList.add(body);
			}
			model.put("contentList", contentList);
		}
		model.put("type", type);
		return "subject/filmfest/2013/opiNotice.vm";
	}
	//电影·旅行
	@RequestMapping("/filmfest/sixteenMovieTravel.xhtml")
	public String sixteenMovieTravel(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String type, Integer pageNo, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(FilmFestConstant.TAG_FILMFEST_16);
		if(sa.getId() == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
			putMyJoinData(member.getId(), model);
		}
		if(pageNo == null) pageNo = 0;
		int maxnum = 10;
		int from = pageNo * maxnum;
		String flagValue = "旅行";
		String url = "filmfest/sixteenMovieTravel.xhtml";
		if(StringUtils.equals(type, "humanities")){
			flagValue = "人文";
			url = "filmfest/sixteenMovieHumanities.xhtml";
		}else if(StringUtils.equals(type, "music")) {
			flagValue = "音乐";
			url = "filmfest/sixteenMovieMusic.xhtml";
		}
		int count = filmFestService.getFilmFestMovieCount(FilmFestConstant.TAG_FILMFEST_16, "flag", flagValue);
		if(count > 0){
			List<Long> movieIdList = filmFestService.getFilmFestMovieIdList(FilmFestConstant.TAG_FILMFEST_16, "flag", flagValue, from, maxnum);
			List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
			
			Map<Long, List<MoviePlayItem>> movieMpiMap = new HashMap<Long, List<MoviePlayItem>>();
			Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
			Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
			putMovieOpi(movieIdList, null, null, movieMpiMap, opiMap, cinemaMap, sa.getId());
			model.put("movieList", movieList);
			model.put("movieMpiMap", movieMpiMap);
			model.put("opiMap", opiMap);
			model.put("cinemaMap", cinemaMap);
			PageUtil pageUtil = new PageUtil(count, maxnum, pageNo, url, true, true);
			pageUtil.initPageInfo(new HashMap());
			model.put("pageUtil", pageUtil);
		}
		model.put("type", type);
		return "subject/filmfest/2013/movieTravel.vm";
	}
	//电影·人文
	@RequestMapping("/filmfest/sixteenMovieHumanities.xhtml")
	public String sixteenMovieHumanities(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Integer pageNo, HttpServletRequest request, ModelMap model){
		return sixteenMovieTravel(sessid, "humanities", pageNo, request, model);
	}
	//电影·音乐
	@RequestMapping("/filmfest/sixteenMovieMusic.xhtml")
	public String sixteenMovieMusic(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Integer pageNo, HttpServletRequest request, ModelMap model){
		return sixteenMovieTravel(sessid, "music", pageNo, request, model);
	}
	//电影·音乐
	@RequestMapping("/filmfest/movieAround.xhtml")
	public String movieAround(){
		return "subject/filmfest/2013/movieAround.vm";
	}
	private void putMyJoinData(Long memberid, ModelMap model){
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("memberId", "=", memberid);
		queryCondition.putAll(relate1);
		List<ViewFilmSchedule> vfsList = mongoService.getObjectList(ViewFilmSchedule.class, queryCondition);
		model.put("joinMovieIdList", BeanUtil.getBeanPropertyList(vfsList, Long.class, "movieId", true));
		model.put("joinMpidList", BeanUtil.getBeanPropertyList(vfsList, Long.class, "mpid", true));
	}
	private void putMovieStatus(List<GewaCommend> gcList, List<Movie> movieList, Map<Long, String> movieStatusMap, Long bath){
		if(gcList != null && !gcList.isEmpty()){
			for(GewaCommend gc : gcList){
				Long movieid = gc.getRelatedid();
				Movie movie = daoService.getObject(Movie.class, movieid);
				movieList.add(movie);
				List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList(AdminCityContant.CITYCODE_SH, movie.getId(), null, null, bath, null, 0, 1);
				if(mpiList.isEmpty()){
					movieStatusMap.put(movieid, MOVIE_STATUS_JOIN);
				}else{
					int sum = filmFestService.getCurMovieSeatSum(movieid, bath);
					if(sum == 0) movieStatusMap.put(movieid, MOVIE_STATUS_NONE);
					else if(sum == -1) movieStatusMap.put(movieid, MOVIE_STATUS_SHOW);
					else movieStatusMap.put(movieid, MOVIE_STATUS_TICKET);
				}
			}
		}
	}
	private void putMovieOpi(List<Long> movieIdList, Long cinemaId, Date playDate, Map<Long, List<MoviePlayItem>> map, Map<Long, OpenPlayItem> opiMap, Map<Long, Cinema> cinemaMap, Long bath){
		if(movieIdList != null && !movieIdList.isEmpty()){
			for(Long movieId : movieIdList){
				List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList(AdminCityContant.CITYCODE_SH, movieId , cinemaId, playDate, bath, null, 0, 20);
				putMpiDataMap(mpiList, cinemaMap, opiMap);
				map.put(movieId, mpiList);
			}
		}
	}
	private Set<Long> putMpiDataMap(List<MoviePlayItem> mpiList, Map<Long, Cinema> cinemaMap, Map<Long, OpenPlayItem> opiMap){
		Set<Long> movieIdList = new LinkedHashSet<Long>();
		Set<Long> cinemaIdList = new LinkedHashSet<Long>();
		if(mpiList != null && !mpiList.isEmpty()){
			Iterator<MoviePlayItem> it = mpiList.iterator();
			while(it.hasNext()) {
				MoviePlayItem mpi = it.next();
				if(mpi.isUnOpenToGewa() || mpi.isUnShowToGewa()){
					it.remove();
				}else{
					if(StringUtils.equals(mpi.getOpenStatus(), OpiConstant.MPI_OPENSTATUS_OPEN)){
						OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
						if(opi != null && opi.isBooking() && !opi.isUnOpenToGewa() && !opi.isUnShowToGewa()){
							movieIdList.add(mpi.getMovieid());
							cinemaIdList.add(mpi.getCinemaid());
							opiMap.put(mpi.getId(), opi);
						}else{
							it.remove();
						}
					}else {
						it.remove();
					}
				}
			}
			cinemaMap.putAll(daoService.getObjectMap(Cinema.class, cinemaIdList));
		}
		return movieIdList;
	}
	private void saveViewFilmScheduleByOrder(Long memberid, Long batch, List<Long> orderMpiIdList){
		List<TicketOrder> orderList = orderQueryService.getOrderListByMemberId(TicketOrder.class, memberid, OrderConstant.STATUS_PAID_SUCCESS, 15, 0, 50);
		if(orderList != null && !orderList.isEmpty()){
			for (TicketOrder torder : orderList) {
				MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, torder.getMpid());
				if(mpi != null && mpi.getBatch() != null && mpi.getBatch().equals(batch)){
					nosqlService.addViewFilmSchedule(mpi, ViewFilmSchedule.TYPE_SCHEDULE_FILMFEST, null, memberid, "web");
					if(orderMpiIdList != null && !orderMpiIdList.contains(mpi.getId())){
						orderMpiIdList.add(mpi.getId());
					}
				}
			}
		}
	}
	@RequestMapping("/filmfest/ajax/loadIndexHead.xhtml")
	public String loadIndexHead(ModelMap model, @CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
		}
		return "subject/filmfest/2013/loginBox.vm";
	}
	@RequestMapping("/filmfest/ajax/loadHead.xhtml")
	public String loadHead(ModelMap model, @CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
		}
		return "subject/filmfest/2013/loginHeader.vm";
	}
	@RequestMapping("/filmfest/ajax/loadMyJoinData.xhtml")
	public String loadMyJoinData(ModelMap model, @CookieValue(value = LOGIN_COOKIE_NAME, required = false)
			String sessid, HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Map jsonMap = new HashMap();
		if(member != null){
			model.put("logonMember", member);
			putMyJoinData(member.getId(), model);
			jsonMap.put("joinMovieIdList", model.get("joinMovieIdList"));
			jsonMap.put("joinMpidList", model.get("joinMpidList"));
			jsonMap.put("joinMpidCount", ((List)model.get("joinMpidList")).size());
		}
		return showJsonSuccess(model, jsonMap);
	}
	
	//第十六届电影节end
}
