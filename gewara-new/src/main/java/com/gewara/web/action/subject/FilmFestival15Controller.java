package com.gewara.web.action.subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.CommonType;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PlayItemMessage;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.ShareMember;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
@Controller
public class FilmFestival15Controller extends AnnotationController{
	public static final String ACTIVITYTYPE_FILMFEST_15 = "15film";

	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService){
		this.blogService = blogService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	
	//第十五届电影节strat
	//首页
	@RequestMapping("/filmfest/fifteen.xhtml")
	public String fifteenIndex(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		List<PlayItemMessage> playList = addPlayItems(member, model);
		List<Long> movieIdList = BeanUtil.getBeanPropertyList(playList, Long.class, "categoryid", true);
		addCacheMember(model, BeanUtil.getBeanPropertyList(playList, Long.class, "memberid", true));
		model.put("movieMap", BeanUtil.beanListToMap(daoService.getObjectList(Movie.class, movieIdList), "id"));

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
		List<GewaCommend> videoList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "video", true, 0, 6);
		commonService.initGewaCommendList("videoList", rh, videoList);
		model.put("videoList", videoList);
		//活动
		List<GewaCommend> activityList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "activity", true, 0, 2);
		int activityCount = commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, "activity", true);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
		model.put("activityCount", activityCount);
		//人气排行
		Map paramsMap = new HashMap();
		paramsMap.put("type", "movie");
		List<Map> movieRenqiList = mongoService.find(MongoData.NS_FILMFEST_FIFTEEN, paramsMap, "count", false, 0 ,10);
		model.put("movieRenqiList", movieRenqiList);
		//热销排行
		/*
		List<Movie> movieReXiaoList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_15, null, null, "boughtcount", 0, 10);
		model.put("movieReXiaoList", movieReXiaoList);*/
		//金爵奖
		List<Movie> movieJinJueList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_15, "flag", "金爵奖影片", 0, 6);
		Integer jinJueCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_15, "flag", "金爵奖影片");
		model.put("movieJinJueList", movieJinJueList);
		model.put("jinJueCount", jinJueCount);
		//参展影片
		List<Movie> movieCanZhanList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_15, "flag", "参展影片", 0, 6);
		Integer canZhanCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_15, "flag", "参展影片");
		model.put("movieCanZhanList", movieCanZhanList);
		model.put("canZhanCount", canZhanCount);
		//亚洲新人奖
		List<Movie> movieXinRenList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_15, "flag", "亚洲新人奖影片", 0, 6);
		Integer xinRenCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_15, "flag", "亚洲新人奖影片");
		model.put("movieXinRenList", movieXinRenList);	
		model.put("xinRenCount", xinRenCount);
		Map movieOpiMap = new HashMap();
		Map movieOpiStatusMap = new HashMap();
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		putMovieOpi(movieJinJueList, movieOpiMap, movieOpiStatusMap, cinemaMap, sa.getId());
		putMovieOpi(movieCanZhanList, movieOpiMap, movieOpiStatusMap, cinemaMap, sa.getId());
		putMovieOpi(movieXinRenList, movieOpiMap, movieOpiStatusMap, cinemaMap, sa.getId());
		model.put("movieOpiMap", movieOpiMap);
		model.put("movieOpiStatusMap", movieOpiStatusMap);
		model.put("cinemaMap", cinemaMap);
		//下载
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		params.put(MongoData.ACTION_TAG, "15filmIndex");
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> downIndex = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 1);
		model.put("downIndex", downIndex);
		//十日谈入口
		params.put(MongoData.ACTION_TAG, "15filmTenTalk");
		List<Map> tenTalkList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 1);
		model.put("tenTalkList", tenTalkList);
		//页面通告
		params.put(MongoData.ACTION_TAG, "15filmTongGao");
		List<Map> tongGaoList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 1);
		model.put("tongGaoList", tongGaoList);
		return "subject/filmfest/2012/index.vm";
	}
	private long lasttime = System.currentTimeMillis();
	private List<PlayItemMessage> cachePlayList = null;
	private List<PlayItemMessage> addPlayItems(Member member, ModelMap model){
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", "cinema");
		DBObject relate3 = mongoService.queryBasicDBObject("mpid", "!=", null);
		DBObject relate4 = mongoService.queryBasicDBObject("type", "!=", DrawActivity.SHOWSITE_WEB);
		DBObject relate5 = mongoService.queryBasicDBObject("status", "!=", Status.DEL);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate3);
		queryCondition.putAll(relate4);
		queryCondition.putAll(relate5);
		if(cachePlayList == null || System.currentTimeMillis() - lasttime > DateUtil.m_minute*10){
			cachePlayList = mongoService.getObjectList(PlayItemMessage.class, queryCondition, "adddate", false, 0, 10);
			lasttime = System.currentTimeMillis();
		}
		if(member != null){
			addCacheMember(model, member.getId());
			model.put("logonMember", member);
			DBObject relate2 = mongoService.queryBasicDBObject("memberid", "=", member.getId());
			queryCondition.putAll(relate2);
			int pimCount = mongoService.getObjectCount(PlayItemMessage.class, queryCondition);
			model.put("pimCount", pimCount);
		}
		model.put("playList", cachePlayList);
		return cachePlayList;
	}
	//购票页面
	@RequestMapping("/filmfest/fifteenMovieList.xhtml")
	public String fifteenMovieList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Date date, Long movieid, String flag, String state, String type, String moviename, Long cinemaid, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null) pageNo = 0;
		int maxnum = 20;
		int from = pageNo * maxnum;
		List<GewaCommend> gwCinemaList = commonService.getGewaCommendList("310000","sp"+sa.getId(), null, TagConstant.TAG_CINEMA, true, 0, 35);
		List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(gwCinemaList, Long.class,"relatedid", true);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
		List<Long> movieidList = new ArrayList<Long>();
		String flagStr = flag;
		if(StringUtils.equals(flagStr, "金爵奖")) flagStr = "金爵奖影片";
		if(StringUtils.equals(flagStr, "亚洲新人奖")) flagStr = "亚洲新人奖影片";
		if(StringUtils.equals(flagStr, "展映单元")) flagStr = "参展影片";
		String order = "hotvalue";
		if(movieid == null) movieidList = filmFestService.getJoinMovieIdList(sa.getId(), "310000", date, type, flagStr, state, moviename, cinemaid, ACTIVITYTYPE_FILMFEST_15, order, from, maxnum);
		else movieidList.add(movieid);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		Map movieOpiMap = new HashMap();
		Map movieOpiStatusMap = new HashMap();
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		putMovieOpi(movieList, movieOpiMap, movieOpiStatusMap, cinemaMap, sa.getId());
		model.put("movieOpiMap", movieOpiMap);
		model.put("movieOpiStatusMap", movieOpiStatusMap);
		model.put("cinemaMap", cinemaMap);
		if(movieid == null){
			Integer count = filmFestService.getJoinMovieCount(sa.getId(), "310000", date, type, flagStr, state, moviename, cinemaid, ACTIVITYTYPE_FILMFEST_15);
			Map params = new HashMap();
			params.put("date", DateUtil.format(date,"yyyy-MM-dd"));
			params.put("flag", flag);
			params.put("state",state);
			params.put("type",type);
			params.put("cinemaid", cinemaid);
			params.put("moviename", moviename);
			PageUtil pageUtil = new PageUtil(count,maxnum,pageNo,"filmfest/fifteenMovieList.xhtml",true,true);
			pageUtil.initPageInfo(params);
			model.put("pageUtil",pageUtil);
		}
		model.put("movieList", movieList);
		model.put("cinemaList", cinemaList);
		return "subject/filmfest/2012/opiList.vm";
	}
	
	//活动页面
	@RequestMapping("/filmfest/fifteenActivityList.xhtml")
	public String fifteenActivityList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null) pageNo = 0;
		int maxnum = 6;
		int from = pageNo * maxnum;
		List<GewaCommend> activityList = commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "activity", true, from, maxnum);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, "activity", true),maxnum,pageNo,"filmfest/fifteenActivityList.xhtml",true,true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		//右侧电影
		List<GewaCommend> gcList=  commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "movie", true, 0, 4);
		Map<Long, String> movieStatusMap = new HashMap<Long, String>(); 
		List<Movie> movieList = new ArrayList<Movie>();
		putMovieStatus(gcList, movieList, movieStatusMap, sa.getId());
		model.put("movieStatusMap", movieStatusMap);
		model.put("movieHotList", movieList);
		return "subject/filmfest/2012/activityList.vm";
	}
	
	//公告页面
	@RequestMapping("/filmfest/fifteenNotice.xhtml")
	public String fifteenNotice(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		DiaryBase topic = diaryService.getDiaryBase(3230698L);
		model.put("topic", topic);
		if(topic != null)	model.put("diaryBody", blogService.getDiaryBody(topic.getId()));
		return "subject/filmfest/2012/opiNotice.vm";
	}
	
	//新闻页面
	@RequestMapping("/filmfest/fifteenNewsList.xhtml")
	public String fifteenNewsList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
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
		PageUtil pageUtil = new PageUtil(commonService.getGewaCommendCount(null,"sp"+sa.getId(), null, CommonType.NEWSTAG_NEWS, true),maxnum,pageNo,"filmfest/fifteenNewsList.xhtml",true,true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		//右侧电影
		List<GewaCommend> gcList=  commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "movie", true, 0, 4);
		Map<Long, String> movieStatusMap = new HashMap<Long, String>(); 
		List<Movie> movieList = new ArrayList<Movie>();
		putMovieStatus(gcList, movieList, movieStatusMap, sa.getId());
		model.put("movieStatusMap", movieStatusMap);
		model.put("movieHotList", movieList);
		return "subject/filmfest/2012/newsList.vm";
	}
	
	//新闻页面（后台mongo推送）
	@RequestMapping("/filmfest/fifteenTalk.xhtml")
	public String fifteenTalk(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, Integer pageNo, ModelMap model){
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		if(pageNo == null){
			if(DateUtil.isAfter(DateUtil.parseDate("2012-06-14")))
				pageNo = 0;
			else if(DateUtil.isAfter(DateUtil.parseDate("2012-06-26")))
				pageNo = DateUtil.getDiffDay(DateUtil.getCurDate(), DateUtil.parseDate("2012-06-14"));
			else pageNo = 0;
		}
		int maxnum = 6;
		int from = pageNo * maxnum;
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "sp"+sa.getId());
		params.put(MongoData.ACTION_TAG, ACTIVITYTYPE_FILMFEST_15);
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> picList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, from, maxnum);
		model.put("talkList", picList);
		params.put(MongoData.ACTION_TAG, "15filmDownload");
		List<Map> downList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 10);
		model.put("downList", downList);
		params.put(MongoData.ACTION_TAG, "15filmNotice");
		List<Map> noticeList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 2);
		model.put("noticeList", noticeList);
		model.put("pageNo", pageNo);
		return "subject/filmfest/2012/talk.vm";
	}
	
	//购票通知页面
	@RequestMapping("/filmfest/fifteenTalkInfo.xhtml")
	public String fifteenTalkInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			Date startDate = DateUtil.parseDate("2012-06-10");
			Date endDate = DateUtil.parseDate("2012-06-30");
			model.put("logonMember", member);
			DBObject queryCondition = new BasicDBObject();
			DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", "cinema");
			DBObject relate2 = mongoService.queryBasicDBObject("memberid", "=", member.getId());
			DBObject relate3 = mongoService.queryBasicDBObject("mpid", "!=", null);
			DBObject relate4 = mongoService.queryBasicDBObject("status", "!=", Status.DEL);
			DBObject relate5 = mongoService.queryBasicDBObject("type", "!=", DrawActivity.SHOWSITE_WEB);
			DBObject relate6 = mongoService.queryAdvancedDBObject("playdate", new String[]{">", "<"}, new Date[]{startDate, endDate});
			queryCondition.putAll(relate1);
			queryCondition.putAll(relate2);
			queryCondition.putAll(relate3);
			queryCondition.putAll(relate4);
			queryCondition.putAll(relate5);
			queryCondition.putAll(relate6);
			List<PlayItemMessage> pimList = mongoService.getObjectList(PlayItemMessage.class, queryCondition);
			Map<String, List> pimMap = new HashMap<String, List>();
			Map<Long, OpenPlayItem> opiStatusMap = new HashMap();
			Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
			Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
			Map<Long, MoviePlayItem> mpiMap = new HashMap<Long, MoviePlayItem>();
			int length = 0;
			int priceNum = 0;
			int ticketNum = 0;
			for(PlayItemMessage pim: pimList){
				String keyvalue = "";
				if(mpiMap.get(pim.getMpid()) == null){
					MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, pim.getMpid());
					if(mpi != null){
						mpiMap.put(pim.getMpid(), mpi);
						keyvalue = DateUtil.format(mpi.getPlaydate(), "yyyy-MM-dd");
						if(mpi.getGewaprice() != null){
							priceNum += mpi.getGewaprice() * pim.getWantBuyNumber();
						}
						ticketNum += pim.getWantBuyNumber();
						OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", pim.getMpid(), true);
						if(opi != null) opiStatusMap.put(pim.getMpid(), opi);
					}
				}else{
					MoviePlayItem mpi = mpiMap.get(pim.getMpid());
					keyvalue = DateUtil.format(mpi.getPlaydate(), "yyyy-MM-dd");
					if(mpi.getGewaprice() != null){
						priceNum += mpi.getGewaprice() * pim.getWantBuyNumber();	
					}
					ticketNum += pim.getWantBuyNumber();
				}
				if(cinemaMap.get(pim.getRelatedid()) == null){
					Cinema cinema = daoService.getObject(Cinema.class, pim.getRelatedid());
					cinemaMap.put(cinema.getId(), cinema);
				}
				if(movieMap.get(pim.getCategoryid()) == null){
					Movie movie = daoService.getObject(Movie.class, pim.getCategoryid());
					movieMap.put(movie.getId(), movie);
				}
				List tmpList = pimMap.get(keyvalue);
				if(tmpList==null){
					tmpList = new ArrayList();
					pimMap.put(keyvalue, tmpList);
				}
				tmpList.add(pim);
				if(tmpList.size() > length) length = tmpList.size();
			}
			model.put("opiStatusMap", opiStatusMap);
			model.put("priceNum", priceNum);
			model.put("pimList", pimList);
			model.put("mpiMap", mpiMap);
			model.put("ticketNum", ticketNum);
			model.put("length", length);
			model.put("pimMap", pimMap);
			model.put("movieMap", movieMap);
			model.put("cinemaMap", cinemaMap);
		}
		return "subject/filmfest/2012/ticketInfo.vm";
	}
	
	@RequestMapping("/filmfest/cancelTalkInfo.xhtml")
	public String cancelTalkInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String id, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(id)) return showJsonError(model, "参数错误！");
		PlayItemMessage pim = mongoService.getObject(PlayItemMessage.class, "id", id);
		if(pim == null) return showJsonError(model, "没有此排片提醒！");
		pim.setStatus(Status.DEL);
		mongoService.saveOrUpdateObject(pim, "id");
		return showJsonSuccess(model);
	}
	//投票页面
	@RequestMapping("/filmfest/fifteenVoteList.xhtml")
	public String fifteenVoteList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, String type, String searchName, Integer pageNo, ModelMap model){
		if(!DateUtil.isAfter(DateUtil.parseDate("2012-06-24"))) return "redirect:/filmfest/fifteenVoteResult.xhtml";
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		String movieType = "";
		if(StringUtils.isBlank(searchName)){
			if(StringUtils.isBlank(type)) movieType = "金爵奖影片";
			else if(StringUtils.equals(type, "xr")) movieType = "亚洲新人奖影片";
			else movieType = "参展影片";
			if(pageNo == null) pageNo = 0;
			int maxnum = 28;
			int from = pageNo * maxnum;
			List<Movie> movieList = filmFestService.getFilmFestMovie(ACTIVITYTYPE_FILMFEST_15, "flag", movieType,  from, maxnum);
			model.put("movieList", movieList);
			Integer movieCount = filmFestService.getFilmFestMovieCount(ACTIVITYTYPE_FILMFEST_15, "flag", movieType);
			Map params = new HashMap();
			params.put("type", type);
			PageUtil pageUtil = new PageUtil(movieCount,maxnum,pageNo,"filmfest/fifteenVoteList.xhtml",true,true);
			pageUtil.initPageInfo(params);
			model.put("pageUtil",pageUtil);
		}else{
			List<Long> movieidList = filmFestService.getJoinMovieIdList(sa.getId(), "310000", null, null, null, null, searchName, null, ACTIVITYTYPE_FILMFEST_15, null, 0, 14);
			List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
			model.put("movieList", movieList);
			model.put("searchName", searchName);
		}
		return "subject/filmfest/2012/voteList.vm";
	}
	//投票结果页面
	@RequestMapping("/filmfest/fifteenVoteResult.xhtml")
	public String fifteenVoteResult(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		model.put("logonMember", member);
		Map movieMap = new HashMap();
		movieMap.put(MongoData.ACTION_TYPE, ACTIVITYTYPE_FILMFEST_15);
		movieMap.put(MongoData.ACTION_TAG, "jinJue");
		List<Map> jinJueList = mongoService.find(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, movieMap, MongoData.ACTION_SUPPORT, false, 0, 10);
		movieMap.put(MongoData.ACTION_TAG, "xinRen");
		List<Map> xinRenList = mongoService.find(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, movieMap, MongoData.ACTION_SUPPORT, false, 0, 10);
		movieMap.put(MongoData.ACTION_TAG, "canZhan");
		List<Map> canZhanList = mongoService.find(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, movieMap, MongoData.ACTION_SUPPORT, false, 0, 10);
		model.put("jinJueList", jinJueList);
		model.put("xinRenList", xinRenList);
		model.put("canZhanList", canZhanList);
		return "subject/filmfest/2012/voteResult.vm";
	}
	
	//投票
	@RequestMapping("/filmfest/fifteenMemberVote.xhtml")
	public String fifteenMemberVote(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long movieid, HttpServletRequest request, ModelMap model){
		if(DateUtil.isAfter(DateUtil.parseDate("2012-06-16"))) return showJsonError(model, "投票还未开始！");
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(movieid == null) return showJsonError(model, "参数错误！");
		boolean allow = operationService.updateOperation("voteMovieFilmFest" + member.getId(), OperationService.HALF_MINUTE, 5);
		if(!allow) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return showMessage(model, "暂无此活动相关信息！");
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return showJsonError(model, "不存在此电影！");
		if(!StringUtils.contains(movie.getFlag(), ACTIVITYTYPE_FILMFEST_15)) return showJsonError(model, "此电影不属于电影节 ！");
		String tag = "";
		if(StringUtils.contains(movie.getFlag(), "金爵奖影片")) tag="jinJue";
		else if(StringUtils.contains(movie.getFlag(), "亚洲新人奖影片")) tag="xinRen";
		else tag="canZhan";
		Map dataMap = new HashMap();
		dataMap.put(MongoData.GEWA_CUP_MEMBERID, member.getId());
		dataMap.put(MongoData.ACTION_TYPE, ACTIVITYTYPE_FILMFEST_15);
		dataMap.put(MongoData.ACTION_TAG, tag);
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, dataMap);
		if(count > 0) return showJsonError(model, "你已经参与过此类型的投票！");
		dataMap.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
		dataMap.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
		dataMap.put(MongoData.ACTION_TITLE, movie.getName());
		dataMap.put(MongoData.ACTION_RELATEDID, movieid);
		dataMap.put(MongoData.ACTION_MEMBERNAME, member.getNickname());
		mongoService.saveOrUpdateMap(dataMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		Map movieMap = new HashMap();
		movieMap.put(MongoData.ACTION_TYPE, ACTIVITYTYPE_FILMFEST_15);
		movieMap.put(MongoData.ACTION_TAG, tag);
		movieMap.put(MongoData.ACTION_TITLE, movie.getName());
		movieMap.put(MongoData.ACTION_RELATEDID, movieid);
		Map map = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, movieMap);
		int voteCount = 1;
		int ticketCount = orderQueryService.getMemberOrderCountByMemberid(member.getId(), movieid);
		if(ticketCount > 0) voteCount = 5;
		if(map == null){
			map = new HashMap();
			map.putAll(movieMap);
			map.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
			map.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
			map.put(MongoData.ACTION_SUPPORT, voteCount);
		}else{
			map.put(MongoData.ACTION_SUPPORT, voteCount + Integer.valueOf(map.get(MongoData.ACTION_SUPPORT)+""));
		}
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
		return showJsonSuccess(model);
	}
	
	//分享微博
	@RequestMapping("/filmfest/sharesTicketInfo.xhtml")
	public String sharesTicketInfo(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, String content, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if(StringUtils.isBlank(content)) return showJsonError(model, "分享内容不能为空！");
		boolean allow = operationService.updateOperation("shareFilmFest" + member.getId(), OperationService.HALF_MINUTE, 5);
		if(!allow) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		List<ShareMember>  shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA), member.getId());
		if(shareMemberList.isEmpty()) return showJsonError(model, "请先绑定新浪微博！");
		shareService.sendShareInfo(ACTIVITYTYPE_FILMFEST_15, null, member.getId(), content, null);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/filmfest/savePlayItemMessage.xhtml")
	public String savePlayItemMessage(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String mptag, Long mprelatedid, Long mpcategoryid, String playdate, String mobile, Long mpid, Integer wantBuyNumber,  HttpServletRequest request, ModelMap model){
		String datatemp = playdate;
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		if (!PlayItemMessage.TAG_LIST.contains(mptag) || mprelatedid == null || mpcategoryid == null || mpid == null || wantBuyNumber == null)
			return showJsonError(model, "参数错误！");
		if(wantBuyNumber < 0 || wantBuyNumber > 5 )
			return showJsonError(model, "参数错误！");
		if (!ValidateUtil.isMobile(mobile))
			return showJsonError(model, "手机号格式不合法！");
		if (!DateUtil.isValidDate(playdate))
			return showJsonError(model, "时间格式不对或不能为空！");
		Date curDate = DateUtil.addDay(DateUtil.getCurDate(), 30);
		Date playDate = DateUtil.parseDate(playdate);
		if (playDate.after(curDate))
			return showJsonError(model, "时间错误！");
		String opkey = "playitemfilmfest" + member.getId();
		boolean allow = operationService.isAllowOperation(opkey, 30, OperationService.ONE_DAY, 50);
		if (!allow)	return showJsonError(model, "你操作过于频繁，请稍后再试！");
		Cinema cinema = daoService.getObject(Cinema.class, mprelatedid);
		if (cinema == null)
			return showJsonError(model, "关联电影院不存在！");
		Movie movie = daoService.getObject(Movie.class, mpcategoryid);
		if (movie == null)
			return showJsonError(model, "关联电影不存在！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi != null && opi.isBooking()) return showJsonError(model, "当前场次已经可以购票！");
		Map params = new HashMap();
		params.put("tag", mptag);
		params.put("relatedid", mprelatedid);
		params.put("categoryid", mpcategoryid);
		params.put("mobile", mobile);
		params.put("memberid", member.getId());
		params.put("playdate", playDate);
		params.put("type", PlayItemMessage.TYPE_WEB_FILMFEST);
		params.put("status", Status.N);
		params.put("mpid", mpid);
		List<PlayItemMessage> playItemList = mongoService.find(PlayItemMessage.class, params);
		if (!playItemList.isEmpty())
			return showJsonError(model, "不能重复添加！");
		PlayItemMessage playItemMessage = new PlayItemMessage(mptag, mprelatedid, mpcategoryid, playDate, mobile, PlayItemMessage.TYPE_WEB_FILMFEST);
		playItemMessage.setId(ServiceHelper.assignID(mobile));
		playItemMessage.setMemberid(member.getId());
		playItemMessage.setMpid(mpid);
		playItemMessage.setWantBuyNumber(wantBuyNumber);
		String msg = DateUtil.format(playDate, "MM月dd日") + " " + cinema.getRealBriefname() + " " + movie.getRealBriefname()
				+ "已开放购票，手机登陆http://t.cn/Sb2z2G 查看，安装客户端买票更方便";
		playItemMessage.setMsg(msg);
		mongoService.saveOrUpdateObject(playItemMessage, MongoData.DEFAULT_ID_NAME);
		Map mpiCountMap = mongoService.findOne(MongoData.NS_FILMFEST_FIFTEEN, MongoData.SYSTEM_ID, "mpi"+mpid);
		if(mpiCountMap == null){
			mpiCountMap = new HashMap();
			mpiCountMap.put(MongoData.SYSTEM_ID, "mpi"+mpid);
			mpiCountMap.put(MongoData.ACTION_COUNT, wantBuyNumber);
			mpiCountMap.put("cinemaname", cinema.getName());
			mpiCountMap.put("moviename", movie.getName());
			mpiCountMap.put("playdate", datatemp);
			mpiCountMap.put("type", "mpi");
		}else{
			int count = Integer.parseInt(mpiCountMap.get(MongoData.ACTION_COUNT)+"")+wantBuyNumber;
			mpiCountMap.put(MongoData.ACTION_COUNT, count);
		}
		Map movieCountMap = mongoService.findOne(MongoData.NS_FILMFEST_FIFTEEN, MongoData.SYSTEM_ID, "movie"+movie.getId());
		if(movieCountMap == null){
			movieCountMap = new HashMap();
			movieCountMap.put(MongoData.SYSTEM_ID, "movie"+movie.getId());
			movieCountMap.put(MongoData.ACTION_COUNT, wantBuyNumber);
			movieCountMap.put("moviename", movie.getName());
			movieCountMap.put("type", "movie");
		}else{
			int count = Integer.parseInt(movieCountMap.get(MongoData.ACTION_COUNT)+"")+wantBuyNumber;
			movieCountMap.put(MongoData.ACTION_COUNT, count);
		}
		mongoService.saveOrUpdateMap(movieCountMap, MongoData.SYSTEM_ID, MongoData.NS_FILMFEST_FIFTEEN);
		mongoService.saveOrUpdateMap(mpiCountMap, MongoData.SYSTEM_ID, MongoData.NS_FILMFEST_FIFTEEN);
		operationService.updateOperation(opkey, 30, OperationService.ONE_DAY, 50);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/recommend/estimatedNumber.xhtml")
	public String estimatedNumber(Integer pageNo, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int maxnum = 50;
		int from = pageNo * maxnum;
		Map params = new HashMap();
		params.put("type", "mpi");
		List mpiList = mongoService.find(MongoData.NS_FILMFEST_FIFTEEN, params, "count", false, from, maxnum);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_FILMFEST_FIFTEEN,params),maxnum,pageNo,"admin/recommend/estimatedNumber.xhtml",true,true);
		Map param = new HashMap();
		pageUtil.initPageInfo(param);
		model.put("pageUtil",pageUtil);
		model.put("mpiList", mpiList);
		return "subject/filmfest/2012/mpiCount.vm";
	}
	
	@RequestMapping("/admin/recommend/checkPlayItemMessage.xhtml")
	public String checkPlayItemMessage(ModelMap model){
		Date startDate = DateUtil.parseDate("2012-05-1");
		Date endDate = DateUtil.parseDate("2012-06-30");
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", "cinema");
		DBObject relate3 = mongoService.queryBasicDBObject("mpid", "!=", null);
		DBObject relate4 = mongoService.queryBasicDBObject("status", "=", Status.N);
		DBObject relate5 = mongoService.queryBasicDBObject("type", "!=", DrawActivity.SHOWSITE_WEB);
		DBObject relate6 = mongoService.queryAdvancedDBObject("playdate", new String[]{">", "<"}, new Date[]{startDate, endDate});
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate3);
		queryCondition.putAll(relate4);
		queryCondition.putAll(relate5);
		queryCondition.putAll(relate6);
		List<PlayItemMessage> pimList = mongoService.getObjectList(PlayItemMessage.class, queryCondition);
		List<PlayItemMessage> delPimList = new ArrayList();
		List<PlayItemMessage> moviePimList = new ArrayList();
		Map<Long, MoviePlayItem> mpiMap = new HashMap<Long, MoviePlayItem>();
		for(PlayItemMessage pim : pimList){
			MoviePlayItem mPlayItem = mpiMap.get(pim.getMpid());
			if(mPlayItem == null){
				mPlayItem = daoService.getObject(MoviePlayItem.class, pim.getMpid());
				if(mPlayItem!=null){
					mpiMap.put(pim.getMpid(), mPlayItem);
				}
			}
			if(mPlayItem == null){
				delPimList.add(pim);
			}else{
				if(!mPlayItem.getMovieid().equals(pim.getCategoryid())){
					moviePimList.add(pim);
				}
			}
		}
		model.put("delPimList", delPimList);
		model.put("moviePimList", moviePimList);
		return "subject/filmfest/2012/checkFilmFestMessage.vm";
	}
	
	@RequestMapping("/admin/recommend/deleteFilmFestMessage.xhtml")
	public String deleteFilmFestMessage(String id, ModelMap model){
		if(StringUtils.isBlank(id))return showJsonError(model, "参数错误！");
		PlayItemMessage pim = mongoService.getObject(PlayItemMessage.class, "id", id);
		if(pim == null) return showJsonError(model, "不存在此排片提醒！");
		GewaraUser user = getLogonUser();
		dbLogger.warn("admin："+user.getRealname()+"...删除错误排片提醒，场次ID："+pim.getMpid()+"  电影ID："+pim.getCategoryid()+"  影院ID："+pim.getRelatedid());
		mongoService.removeObjectById(PlayItemMessage.class, "id", id);
		return showJsonSuccess(model);
	}
	
	private final User getLogonUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null) return null;
		if(auth.isAuthenticated() && !auth.getName().equals("anonymous")){//登录
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			if(user instanceof User) return (User)user;
		}
		return null;
	}
	
	@RequestMapping("/subject/flash/fifteen.xhtml")
	@ResponseBody
	public String fifteen(String type){
		if(!StringUtils.equals(type, "fifteen")) return "error";
		SpecialActivity sa = filmFestService.getSpecialActivity(ACTIVITYTYPE_FILMFEST_15);
		if(sa == null) return "暂无此活动相关信息！";
		List<GewaCommend> gcList=  commonService.getGewaCommendList(null,"sp"+sa.getId(), null, "movie", true, 5, 5);
		String result = "";
		int i = 1;
		for(GewaCommend gc : gcList){
			Long movieid = gc.getRelatedid();
			Movie movie = daoService.getObject(Movie.class, movieid);
			String status = "";
			List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList("310000", movie.getId(), null, null, sa.getId(), null, 0, 10);
			if(mpiList.isEmpty()){
				status = "1";//无排片
			}else{
				int sum = filmFestService.getCurMovieSeatSum(movieid, sa.getId());
				if(sum == 0) status = "2";//卖光
				else if(sum == -1) status = "3";//购票提醒
				else status = "4";//立即购票
			}
			String temp = movie.getHighlight()==null?"":movie.getHighlight();
			result += "m"+i+"tp=http://img5.gewara.cn/cw150h200/"+movie.getLogo()+"&m"+i+"z1="+movie.getName()+"&m"+i+"z2="+temp+"&m"+i+"zt=" +status + "&m"+i+"lj=http://www.gewara.com/filmfest/fifteenMovieList.xhtml?movieid="+movie.getId();
			if(i<5) result += "&";
			i++;
		}
		return result;
	}
	
	private void putMovieStatus(List<GewaCommend> gcList, List<Movie> movieList, Map<Long,String> map, Long bath){
		for(GewaCommend gc : gcList){
			Long movieid = gc.getRelatedid();
			Movie movie = daoService.getObject(Movie.class, movieid);
			movieList.add(movie);
			List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList("310000", movie.getId(), null, null, bath, null, 0, 10);
			if(mpiList.isEmpty()){
				map.put(movieid, "暂无排片");
			}else{
				int sum = filmFestService.getCurMovieSeatSum(movieid, bath);
				if(sum == 0) map.put(movieid, "卖光了");
				else if(sum == -1) map.put(movieid, "购票提醒");
				else map.put(movieid, "立即购票");
			}
		}
	}
	
	private void putMovieOpi(List<Movie> movieList, Map map, Map statusMap, Map<Long, Cinema> cinemaMap, Long bath){
		for(Movie movie : movieList){
			List<MoviePlayItem> mpiList = filmFestService.getMoviePlayItemList("310000", movie.getId(), null, null, bath, null, 0, 10);
			List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getBeanPropertyList(mpiList, Long.class, "cinemaid", true));
			cinemaMap.putAll(BeanUtil.beanListToMap(cinemaList, "id"));
			map.put(movie.getId(), mpiList);
			if(!mpiList.isEmpty()){
				for(MoviePlayItem mpi : mpiList){
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
					if(opi != null){
						statusMap.put(mpi.getId(), opi);
					}
				}
			}
		}
	}
	//第十五届电影节end
}
