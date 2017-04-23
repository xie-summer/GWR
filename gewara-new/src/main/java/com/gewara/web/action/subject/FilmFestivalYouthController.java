package com.gewara.web.action.subject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.content.CommonType;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
/***
 * =================
 * ==杭州亚洲青年电影展===
 * =================
 */
@Controller
public class FilmFestivalYouthController extends AnnotationController {
	public static final String ACTIVITYTYPE_FILMFEST_YOUTH = "youthfilm";//杭州亚洲青年电影节
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService){
		this.blogService = blogService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	private static String HANGZHOU = "330100";
	
	@RequestMapping("/youthFilm/index.xhtml")
	public String fifteenIndex(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			model.put("logonMember", member);
			addCacheMember(model, member.getId());
		}
		//头部电影
		List<GewaCommend> gcList=  commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "movie", true, 0, 5);
		Map<Long, String> movieStatusMap = new HashMap<Long, String>(); 
		List<Movie> movieList = new ArrayList<Movie>();
		putMovieStatus(gcList, movieList, movieStatusMap, sa.getId());
		model.put("movieStatusMap", movieStatusMap);
		model.put("movieHotList", movieList);
		//资讯新闻
		List<GewaCommend> newsList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, CommonType.NEWSTAG_NEWS, true, 0, 7);
		int newsCount = commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, CommonType.NEWSTAG_NEWS, true);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("newsList", rh, newsList);
		model.put("newsCount", newsCount);
		model.put("newesList", newsList);
		//视频
		List<GewaCommend> videoList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "video", true, 0, 4);
		commonService.initGewaCommendList("videoList", rh, videoList);
		model.put("videoList", videoList);
		//活动
		List<GewaCommend> activityList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "activity", true, 0, 2);
		int activityCount = commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, "activity", true);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
		model.put("activityCount", activityCount);
		//首页头部滚动信息
		Map rollMap = new HashMap();
		rollMap.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		rollMap.put(MongoData.ACTION_TAG, "youthfilmroll");
		List<Map> rollList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, rollMap, MongoData.ACTION_ORDERNUM, true);
		model.put("rollList", rollList);
		//外链活动
		Map actMap = new HashMap();
		actMap.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		actMap.put(MongoData.ACTION_TAG, "filmAct");
		List<Map> filmActivityList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, actMap, MongoData.ACTION_ORDERNUM, true,0, 4);
		model.put("filmActivityList", filmActivityList);
		//影展评审团
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		param.put(MongoData.ACTION_TAG, "filmReview");
		List<Map> filmReviewList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, param, MongoData.ACTION_ORDERNUM, true);
		model.put("filmReviewList", filmReviewList);
		//亚洲之光
		List<Movie> movieYazhouList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "亚洲之光", 0, 4);
		Integer yaZhouCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "亚洲之光");
		model.put("movieYazhouList", movieYazhouList);
		model.put("yaZhouCount", yaZhouCount);
		//小川绅介
		List<Movie> movieXiaoChuanList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "小川绅介", 0, 4);
		Integer xiaoChuanCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "小川绅介");
		model.put("movieXiaoChuanList", movieXiaoChuanList);
		model.put("xiaoChuanCount", xiaoChuanCount);
		//焦点影人
		List<Movie> movieJiaoDianList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "焦点影人", 0, 4);
		Integer jiaoDianCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "焦点影人");
		model.put("movieJiaoDianList", movieJiaoDianList);	
		model.put("jiaoDianCount", jiaoDianCount);
		//台湾新象
		List<Movie> movieTaiWanList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "台湾新象", 0, 4);
		Integer movieTaiWanCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "台湾新象");
		model.put("movieTaiWanList", movieTaiWanList);	
		model.put("movieTaiWanCount", movieTaiWanCount);
		//最好的时光
		List<Movie> movieShiGuangList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "最好的时光", 0, 4);
		Integer movieShiGuangCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_YOUTH, "flag", "最好的时光");
		model.put("movieShiGuangList", movieShiGuangList);	
		model.put("movieShiGuangCount", movieShiGuangCount);	
		
		Map<Long,List<OpenPlayItem>> opiMap = new HashMap<Long, List<OpenPlayItem>>();
		getMovieOpi(movieYazhouList,opiMap);
		getMovieOpi(movieXiaoChuanList,opiMap);	
		getMovieOpi(movieYazhouList,opiMap);	
		getMovieOpi(movieJiaoDianList,opiMap);	
		getMovieOpi(movieTaiWanList,opiMap);
		getMovieOpi(movieShiGuangList,opiMap);
		model.put("opiMap", opiMap);
		return "subject/youthShow/index.vm";
	}
	private void putMovieStatus(List<GewaCommend> gcList, List<Movie> movieList, Map<Long,String> map, Long bath){
		for(GewaCommend gc : gcList){
			Long movieid = gc.getRelatedid();
			Movie movie = daoService.getObject(Movie.class, movieid);
			movieList.add(movie);
			List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList(HANGZHOU, movie.getId(), null, null, bath, null, 0, 10);
			if(mpiList.isEmpty()){
				map.put(movieid, "查看详情");
			}else{
				int sum = filmFestService.getCurMovieSeatSum(movieid, bath);
				if(sum == 0) map.put(movieid, "查看详情");
				else if(sum >0 && sum <10) map.put(movieid, "座位紧张");
				else map.put(movieid, "免费抢票");
			}
		}
	}
	//购票页面
	@RequestMapping("/youthFilm/bookingList.xhtml")
	public String bookingMovieList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Date date, Long movieid, String flag, String state, String type, String moviename, Long cinemaid, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null) pageNo = 0;
		int maxnum = 10;
		int from = pageNo * maxnum;
		List<Long> movieidList = new ArrayList<Long>();
		if(movieid == null) movieidList = filmFestService.getJoinMovieIdList(sa.getId(), HANGZHOU, date, type, flag, state, moviename, cinemaid, ACTIVITYTYPE_FILMFEST_YOUTH, "hotvalue", 0, 60);
		else movieidList.add(movieid);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		List<OpenPlayItem> openPlayItems = new ArrayList<OpenPlayItem>();
		Map<Long, List<Movie>> movieMap = new HashMap<Long, List<Movie>>();
		Map<Long, Movie> mMap = new HashMap<Long, Movie>();
		getOpenPlayItems(movieList,sa,openPlayItems,movieMap,mMap);
		Collections.sort(openPlayItems, new MultiPropertyComparator(new String[]{"playtime"}, new boolean[]{true}));
		List<OpenPlayItem> openPlayItemList = BeanUtil.getSubList(openPlayItems, from, maxnum);	
		PageUtil pageUtil = new PageUtil(openPlayItems.size(), maxnum, pageNo, "youthFilm/bookingList.xhtml", true, true);
		Map params = new HashMap();
		params.put("date", date);
		params.put("flag", flag);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("movieMap", movieMap);
		model.put("mMap", mMap);
		model.put("openPlayItems", openPlayItems);
		model.put("movieList", movieList);
		initPieceInfoBySchool("zhejiang", model);
		initPieceInfoBySchool("gongye", model);
		initPieceInfoBySchool("chuanmei", model);
		initPieceInfoBySchool("meishu", model);
		model.put("openPlayItemList", openPlayItemList);
		return "subject/youthShow/freeRobTicket.vm";
	}

	//影片列表页
	@RequestMapping("/youthFilm/movieList.xhtml")
	public String fifteenMovieList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Date date, Long movieid, String flag, String state, String type, String moviename, Long cinemaid, String order, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null) pageNo = 0;
		int maxnum = 20;
		int from = pageNo * maxnum;
		List<Long> movieidList = new ArrayList<Long>();
		String flagStr = flag;
		if(StringUtils.equals(flagStr, "亚洲之光")) flagStr = "亚洲之光";
		if(StringUtils.equals(flagStr, "小川绅介")) flagStr = "小川绅介";
		if(StringUtils.equals(flagStr, "焦点影人")) flagStr = "焦点影人";
		if(StringUtils.equals(flagStr, "台湾新象")) flagStr = "台湾新象";
		if(StringUtils.equals(flagStr, "最好的时光")) flagStr = "最好的时光";
		order = "hotvalue";
		if(movieid == null) movieidList = filmFestService.getJoinMovieIdList(sa.getId(), HANGZHOU, date, type, flagStr, state, moviename, cinemaid, ACTIVITYTYPE_FILMFEST_YOUTH, order, 0, 60);
		else movieidList.add(movieid);
		List<Movie> movies = daoService.getObjectList(Movie.class, movieidList);
		List<Movie> movieList = BeanUtil.getSubList(movies, from, maxnum);
		Integer count = movies.size();
		Map params = new HashMap();
		params.put("date", DateUtil.format(date,"yyyy-MM-dd"));
		params.put("flag", flag);
		params.put("state",state);
		params.put("type",type);
		params.put("cinemaid", cinemaid);
		params.put("moviename", moviename);
		PageUtil pageUtil = new PageUtil(count, maxnum, pageNo, "youthFilm/movieList.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("movieList", movieList);
		return "subject/youthShow/showMovie.vm";
	}
	//单元介绍
	@RequestMapping("/youthFilm/introduction.xhtml")
	public String filmIntroductio(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model ,String tag, Integer pageNo){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		if(pageNo == null) pageNo = 0;
		int maxnum = 10;
		int from = pageNo * maxnum;
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("member", member);
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "introduction");
		param.put(MongoData.ACTION_TAG, tag);
      List<Map> list= mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, param);
		model.put("map", list.get(0));
		model.put("pit", tag);
		List<Long> movieidList = new ArrayList<Long>();
		String flagStr = tag;
		if(StringUtils.equals(flagStr, "yaZhou")) flagStr = "亚洲之光";
		if(StringUtils.equals(flagStr, "xiaoChuan")) flagStr = "小川绅介";
		if(StringUtils.equals(flagStr, "jiaoDian")) flagStr = "焦点影人";
		if(StringUtils.equals(flagStr, "taiWan")) flagStr = "台湾新象";
		if(StringUtils.equals(flagStr, "shiGuang")) flagStr = "最好的时光";
		movieidList = filmFestService.getJoinMovieIdList(sa.getId(), HANGZHOU, null, null, flagStr, null, null, null, ACTIVITYTYPE_FILMFEST_YOUTH, null, 0, 60);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		List<OpenPlayItem> openPlayItems = new ArrayList<OpenPlayItem>();
		Map<Long, List<Movie>> movieMap = new HashMap<Long, List<Movie>>();
		Map<Long, Movie> mMap = new HashMap<Long, Movie>();
		getOpenPlayItems(movieList,sa,openPlayItems,movieMap,mMap);
		model.put("movieMap", movieMap);
		model.put("mMap", mMap);
		model.put("openPlayItems", openPlayItems);
		model.put("movieList", movieList);
		initPieceInfoBySchool("zhejiang", model);
		initPieceInfoBySchool("gongye", model);
		initPieceInfoBySchool("chuanmei", model);
		initPieceInfoBySchool("meishu", model);
		List<OpenPlayItem> openPlayItemList = BeanUtil.getSubList(openPlayItems, from, maxnum);
		PageUtil pageUtil = new PageUtil(openPlayItems.size(), maxnum, pageNo, "youthFilm/introduction.xhtml", true, true);
		Map params = new HashMap();
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("openPlayItemList", openPlayItemList);
		return "subject/youthShow/unit.vm";
	}
	//焦点新闻
	@RequestMapping("/youthFilm/newsList.xhtml")
	public String fifteenNewsList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null) pageNo = 0;
		int maxnum = 30;
		int from = pageNo * maxnum;
		List<GewaCommend> newsList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, CommonType.NEWSTAG_NEWS, true, from, maxnum);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("newsList", rh, newsList);
		model.put("newesList", newsList);
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, CommonType.NEWSTAG_NEWS, true),maxnum,pageNo,"youthFilm/newsList.xhtml",true,true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		//右侧电影
		List<GewaCommend> gcList=  commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "movie", true, 0, 4);
		Map<Long, String> movieStatusMap = new HashMap<Long, String>(); 
		List<Movie> movieList = new ArrayList<Movie>();
		putMovieStatus(gcList, movieList, movieStatusMap, sa.getId());
		model.put("movieStatusMap", movieStatusMap);
		model.put("movieHotList", movieList);
		//右侧视频
		List<GewaCommend> videoList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "video", true, 0, 4);
		commonService.initGewaCommendList("videoList", rh, videoList);
		model.put("videoList", videoList);
		//右侧外链活动
		Map actMap = new HashMap();
		actMap.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		actMap.put(MongoData.ACTION_TAG, "filmAct");
		List<Map> filmActivityList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, actMap, MongoData.ACTION_ORDERNUM, true,0, 4);
		model.put("filmActivityList", filmActivityList);
		return "subject/youthShow/fileNews.vm";
	}
	//热点活动
	@RequestMapping("/youthFilm/ativityList.xhtml")
	public String fifteenActivityList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null) pageNo = 0;
		int maxnum = 20;
		int from = pageNo * maxnum;
		List<GewaCommend> activityList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "activity", true, from, maxnum);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, "activity", true),maxnum,pageNo,"youthFilm/ativityList.xhtml",true,true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		//右侧电影
		List<GewaCommend> gcList=  commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "movie", true, 0, 4);
		Map<Long, String> movieStatusMap = new HashMap<Long, String>(); 
		List<Movie> movieList = new ArrayList<Movie>();
		putMovieStatus(gcList, movieList, movieStatusMap, sa.getId());
		model.put("movieStatusMap", movieStatusMap);
		model.put("movieHotList", movieList);
		//右侧视频
		List<GewaCommend> videoList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "video", true, 0, 4);
		commonService.initGewaCommendList("videoList", rh, videoList);
		model.put("videoList", videoList);
		return "subject/youthShow/hotAct.vm";
	}
	//我的观影日程
	@RequestMapping("/youthFilm/talkInfo.xhtml")
	public String talkInfo(ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		return "subject/youthShow/talkinfo.vm";
	}
	//购票须知
	@RequestMapping("/youthFilm/notes.xhtml")
	public String fifteenNotice(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		DiaryBase topic = diaryService.getDiaryBase(3259446L);
		model.put("topic", topic);
		if(topic != null)	model.put("diaryBody", blogService.getDiaryBody(topic.getId()));
		return "subject/youthShow/notice.vm";
	}
	@RequestMapping("/subject/flash/youth.xhtml")
	@ResponseBody
	public String fifteen(String type){
		if(!StringUtils.equals(type, "youth")) return "error";
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_YOUTH);
		if(sa == null) return "暂无此活动相关信息！";
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "introduction");
		List<Map> topList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true,0,5);
		String result = "";
		int i = 1;
		for(Map map : topList){
			String temp = map.get("content")==null?"":map.get("content")+"";
			result += "m"+i+"tp=http://img5.gewara.cn/cw150h200/"+map.get("newslogo")+"&m"+i+"z1="+map.get("title")+"&m"+i+"z2="+temp+"&m"+i+"zt=" +"1" + "&m"+i+"lj=http://www.gewara.com/youthFilm/introduction.xhtml?tag="+map.get("tag");
			if(i<5) result += "&";
			i++;
		}
		return result;
	}
	private void getOpenPlayItems(List<Movie> movieList, SpecialActivity sa,List<OpenPlayItem> openPlayItems,Map<Long, List<Movie>> movieMap,Map<Long, Movie> mMap){
		String[] movieIds = null;
		List<Long> allMovieidList = new ArrayList<Long>();
		for(Movie movie : movieList){
			List<OpenPlayItem> opiList = openPlayService.getOpiList(HANGZHOU, null, movie.getId(), new Timestamp(System.currentTimeMillis()), null, false);
			if(!opiList.isEmpty()){
				Collections.sort(opiList, new MultiPropertyComparator<OpenPlayItem>(new String[]{"booking", "id"}, new boolean[]{false, true}));
				OpenPlayItem opi = opiList.get(0);
				openPlayItems.add(opi);
				Map params = new HashMap();
				params.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
				params.put(MongoData.ACTION_TAG, "showtime");
				params.put(MongoData.ACTION_CONTENT, opi.getId()+"");
				Map opiMap= mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params);
				if(opiMap != null && opiMap.get("content2") != null){
					movieIds = opiMap.get("content2").toString().split(",");
					allMovieidList.clear();
					for(String tempid : movieIds){
						allMovieidList.add(Long.valueOf(tempid));
					}
					List<Movie> allMovieList = daoService.getObjectList(Movie.class, allMovieidList);
					if(!allMovieidList.isEmpty()){
						movieMap.put(opi.getId(), allMovieList);
					}
				}
				mMap.put(opi.getId(), movie);	
			}
		}
	}
	private void initPieceInfoBySchool(String tag,ModelMap model){
 		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "school");
		params.put(MongoData.ACTION_TAG, tag);
		List<Map> list = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true);
		Map<String, List<Movie>> pieceMap = new HashMap<String, List<Movie>>();
		for(Map map : list){
			String[] movieIds= map.get("content2").toString().split(",");
			List<Movie> movies = new ArrayList<Movie>();
			for(String tempid : movieIds){
				Movie movie = daoService.getObject(Movie.class, Long.parseLong(tempid));
				movies.add(movie);
			}
			pieceMap.put(map.get("content")+"", movies);
		}
		if("zhejiang".equals(tag)) {
			model.put("zjList", list);
			model.put("zjPieceMap", pieceMap);
		}else if ("gongye".equals(tag)) {
			model.put("gyList", list);
			model.put("gyPieceMap", pieceMap);
		}else if ("chuanmei".equals(tag)) {
			model.put("cmList", list);
			model.put("cmPieceMap", pieceMap);
		}else if ("meishu".equals(tag)) {
			model.put("msList", list);
			model.put("msPieceMap", pieceMap);
		}
	}
	private void getMovieOpi(List<Movie> movieList,Map<Long,List<OpenPlayItem>> opiMap){
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for(Movie movie : movieList){
			opiList = daoService.getObjectListByField(OpenPlayItem.class, "movieid", movie.getId());
			if(!opiList.isEmpty()){
				opiMap.put(movie.getId(), opiList);
			}	
		}	
	}
}
