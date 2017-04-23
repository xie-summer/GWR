package com.gewara.web.action.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportProfile;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.SpecialActivityService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.sport.AgencyService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;
import com.gewara.xmlbind.gym.RemoteSpecialCourse;

@Controller
public class NewsInfoController extends AnnotationController{
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("dramaService")
	private DramaService dramaService;
	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	public void setOpenDramaService(OpenDramaService openDramaService) {
		this.openDramaService = openDramaService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService) {
		this.dramaPlayItemService = dramaPlayItemService;
	}
	@Autowired@Qualifier("specialActivityService")
	private SpecialActivityService specialActivityService;
	public void setSpecialActivityService(SpecialActivityService specialActivityService) {
		this.specialActivityService = specialActivityService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("sportService")
	protected SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("agencyService")
	private AgencyService agencyService;
	
	private static Map<String,String > newTagMap = new HashMap<String, String>();
	private static Map<String, String> newCategoryMap = new HashMap<String, String>();
	static{
		newTagMap.put(TagConstant.TAG_THEATRE, TagConstant.TAG_THEATRE);
		newTagMap.put(TagConstant.TAG_SPORT, TagConstant.TAG_SPORT);
		newTagMap.put(TagConstant.TAG_CINEMA, TagConstant.TAG_CINEMA);
		newTagMap.put(TagConstant.TAG_GYM, TagConstant.TAG_GYM);
		newTagMap.put(TagConstant.TAG_BAR, TagConstant.TAG_BAR);
		newTagMap.put(TagConstant.TAG_KTV, TagConstant.TAG_KTV);
		newCategoryMap.put(TagConstant.TAG_CINEMA, TagConstant.TAG_MOVIE);
		newCategoryMap.put(TagConstant.TAG_THEATRE, TagConstant.TAG_DRAMA);
		newCategoryMap.put(TagConstant.TAG_SPORT, TagConstant.TAG_SPORTITEM);
		newCategoryMap.put(TagConstant.TAG_GYM, TagConstant.TAG_GYMCOURSE);
	}

	// 运动 BaseSportController.java 共用查询页面
	public Map<String, Object> getSportHeadMap(ModelMap model, String citycode){
		// 区县列表
		model.put("g_countyList", placeService.getCountyByCityCode(citycode));
		// 所有场馆
		model.put("g_allCanbookingSports", placeService.getPlaceList(citycode, Sport.class, "clickedtimes", false, 0, 20));
		// 热门搜索
		List<GewaCommend> searchList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_SEARCH, null,null, true,0, 5);
		model.put("g_searchList", searchList);
		return model;
	}

	
	@RequestMapping("/news/newsDetail.xhtml")
	public String newnewsDetail(@RequestParam("nid")Long nid, Integer pageno, ModelMap model, 
			HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		News news = daoService.getObject(News.class, nid);
		if (news == null) {
			return show404(model, "资讯不存在或已经删除！");
		}
		cacheDataService.getAndSetIdsFromCachePool(News.class, nid);
		cacheDataService.getAndSetClazzKeyCount(News.class, nid);
		model.put("news", news);
		String newsContent = VmUtils.htmlabbr(news.getContent(), 80);
		newsContent =StringUtils.replace(newsContent, "\n\t", "");
		model.put("newsContent", newsContent);
		//内容
		if(pageno != null && pageno > 1){
			NewsPage np = newsService.getNewsPageByNewsidAndPageno(news.getId(), pageno);
			if(np != null){
				model.put("content", np.getContent());
			}else{
				pageno = 1;
			}
		}else{
			model.put("content", news.getContent());
			pageno = 1;
		}
		String tag = news.getTag();
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		Map<Long, MarkCountData> markCountMap2 = new HashMap<Long, MarkCountData>();
		model.put("markCountMap", markCountMap);
		model.put("markCountMap2", markCountMap2);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		//关联
		Map relatedMap = new HashMap();
		Object relate = relateService.getRelatedObject(tag, news.getRelatedid());
		model.put("relate", relate);
		markCountMap.put(news.getRelatedid(), markService.getMarkCountByTagRelatedid(news.getTag(), news.getRelatedid()));
		model.put("relatedMap", relatedMap);
		
		getRelatedInfo(tag, news.getRelatedid(), relatedMap, citycode);
		Object relate2 = relateService.getRelatedObject(news.getCategory(), news.getCategoryid());
		model.put("relate2", relate2);
		markCountMap2.put(news.getCategoryid(), markService.getMarkCountByTagRelatedid(news.getCategory(), news.getCategoryid()));
		getRelatedInfo(news.getCategory(), news.getCategoryid(), relatedMap, citycode);
		model.put("relatedMap", relatedMap);
		if(StringUtils.equals(news.getCategory(), TagConstant.TAG_DRAMASTAR)){
			DramaStar dramaStar = daoService.getObject(DramaStar.class, news.getCategoryid());
			List<Map<String, String>> historyMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
			model.put("historyMapList", historyMapList);
		}else if(StringUtils.equals(news.getCategory(), TagConstant.TAG_DRAMA)){
			if(relate2 != null){
				Drama drama = (Drama) relate2;
				if(StringUtils.isNotBlank(drama.getActors())){
					List<Long> actorIdList = BeanUtil.getIdList(drama.getActors(), ",");
					List<DramaStar> actorsList = daoService.getObjectList(DramaStar.class, actorIdList);
					model.put("actorsList", actorsList);
				}
				if(StringUtils.isNotBlank(drama.getDirector())){
					List<Long> directorIdList = BeanUtil.getIdList(drama.getDirector(), ",");
					List<DramaStar> directorsList = daoService.getObjectList(DramaStar.class, directorIdList);
					model.put("directorsList", directorsList);
				}
			}
		}
		//相关新闻
		List<News> newsList = newsService.getNewsListByTagAndCategory(citycode, tag, news.getNewslabel(), 0, 8);
		if(!newsList.isEmpty()) newsList.remove(news);
		if(newsList.size() < 8){
			if(news.getRelatedid() != null){
				List<News> newsList2 = newsService.getNewsByRelatedidAndTag(tag, news.getRelatedid(), 0, 8-newsList.size());
				newsList.removeAll(newsList2);
				newsList.addAll(newsList2);
			}
		}
		if(!newsList.isEmpty()) newsList.remove(news);
		model.put("newsList", newsList);
		//下一篇
		News nextNews = newsService.getNextNews(tag, nid);
		model.put("nextNews", nextNews);
		//热门资讯
		Timestamp curTimestamp = null;
		String order = "";
		if(StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
			curTimestamp = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -7);
			order = "clickedtimes";
		}else if(StringUtils.equals(tag, TagConstant.TAG_THEATRE) || StringUtils.equals(tag, TagConstant.TAG_SPORT)){
			curTimestamp = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -30);
			order = "clickedtimes";
		}
		List<News> hotNewsList = newsService.getNewsListByTag(citycode, tag, news.getNewstype(), null, curTimestamp, order, 0, 6);
		model.put("hotNewsList", hotNewsList);
		//热门活动
		String atype = "";
		if(StringUtils.equals(tag, TagConstant.TAG_CINEMA) ||  StringUtils.equals(tag, TagConstant.TAG_THEATRE)) atype = RemoteActivity.ATYPE_GEWA;
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTimetype(citycode, atype, RemoteActivity.TIME_CURRENT, null, news.getTag(), null, null, null, 0, 4);
		if(code.isSuccess()){
			activityList = code.getRetval();
		}
		model.put("activityList", activityList);
		//推荐专题
		List<GewaCommend> subjectList = commonService.getGewaCommendList(citycode, tag + SignName.NEWS_SUBJECT, null, null, true, 0, 2);
		model.put("subjectList", subjectList);
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		return "common/newsDetail.vm";
	} 
	
	private void getRelatedInfo(String tag, Long id, Map relatedMap, String citycode){
		if(id != null){
			if(StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
				relatedMap.put(tag + "_" + id, openPlayService.getMovieListByCinemaIdAndPlaydate(id, DateUtil.currentTime(), 0, 2));
			}else if(StringUtils.equals(tag, TagConstant.TAG_THEATRE)){
				relatedMap.put(tag + "_" + id, dramaService.getCurPlayDramaList(id, 0, 2));
			}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT)){
				relatedMap.put(tag + "_" + id, sportService.getSportItemListBySportId(id, SportProfile.STATUS_OPEN));
			}else if(StringUtils.equals(tag, TagConstant.TAG_GYM)){
				ErrorCode<List<RemoteSpecialCourse>>  code = synchGymService.getSpecialCourseListByGymId(id);
				if(code.isSuccess()){
					List<RemoteSpecialCourse> scList = code.getRetval();
					for(RemoteSpecialCourse sc : scList){
						ErrorCode<RemoteCourse> code2 = synchGymService.getRemoteCourse(sc.getCourseid(), true);
						if(code2.isSuccess()) sc.setCourse(code2.getRetval());
					}
					relatedMap.put(tag + "_" + id, scList);
				}
			}else if(StringUtils.equals(tag, TagConstant.TAG_SPORTITEM)){
				relatedMap.put(tag + "_" + id, sportService.getSportByItemAndClickTimes(id, 0, 2));
			}else if(StringUtils.equals(tag, TagConstant.TAG_GYMCOURSE)){
				ErrorCode<List<RemoteGym>> code = synchGymService.getGymListByCourseId(id, "clickedtimes", false, 0, 2);
				if(code.isSuccess()) relatedMap.put(tag + "_" + id, code.getRetval());
			}else if(StringUtils.equals(tag, TagConstant.TAG_DRAMASTAR)){
				relatedMap.put(tag + "_" + id, openDramaService.getDramaByStarid(id, 0, 3));
			}else if(StringUtils.equals(tag, TagConstant.TAG_MOVIE)){
				List<Long> idList = openPlayService.getOpiMovieidList(citycode, null);
				boolean isBooking = false;
				if(idList.contains(id)) isBooking = true;
				relatedMap.put(tag + "_" + id, isBooking);
			}else if(StringUtils.equals(tag, TagConstant.TAG_DRAMA)){
				relatedMap.put(tag + "_" + id, dramaPlayItemService.isBookingByDramaId(id));
			}
		}
	}
	
	@RequestMapping("/ajax/news/addTips.xhtml")
	public String addTips(HttpServletRequest request, Long nid, ModelMap model){
		News news = daoService.getObject(News.class, nid);
		if(news == null) return showJsonError(model, "资讯不存在！");
		String ip = WebUtils.getRemoteIp(request);
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		String opsmskey = "news_" + ip + "_" + news.getId()+"_sms"+DateUtil.formatDate(curtime)+"_tips";
		boolean allow = operationService.isAllowOperation(opsmskey, OperationService.ONE_DAY, 10);
		if(!allow) return showJsonError(model, "您今天点的太多了，休息一下吧！");
		String opkey = "newsAddTips_" + news.getId()+"_tips";
		allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY, 1000);
		if(!allow) return showJsonError(model, "今天太多人喜欢了，为防止恶意点击请稍后再试！");
		newsService.updateTips(nid);
		operationService.updateOperation(opsmskey, OperationService.ONE_DAY, 10);
		operationService.updateOperation(opkey, OperationService.ONE_DAY, 1000);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/news/newsList.xhtml")
	public String newnewsList(String tag, String newstype, String type, String searchKey, Integer pageNo, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		if(StringUtils.isBlank(tag)) return show404(model, "新闻链接不正确！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		// V3.1.1 运动分站局部问题修改.doc 3.根据分站要求，运动资讯页头部的三列推荐内容也需要区分一下城市。（添加citycode）
		List<News> topNewsList = null;
		if (StringUtils.equals(tag, TagConstant.TAG_SPORT)) {
			topNewsList = newsService.getNewsListByTag(citycode, newTagMap.get(tag), null, null, null, null, 0, 5);
		}
		// 如果分站没有数据，那么还是从上海站取数据
		if (CollectionUtils.isEmpty(topNewsList)) {
			topNewsList = newsService.getNewsListByTag("310000", newTagMap.get(tag), null, null, null, null, 0, 5);
		}
		
		model.put("topNewsList", topNewsList);
		getRightInfo(tag, citycode, model);
		
		if(StringUtils.equals(tag, TagConstant.TAG_SPORT) && !StringUtils.equals(citycode, "310000")){
			citycode = null;
		}
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 0;
		if(VmUtils.eq(type, "subject")) rowsPerPage = 21;
		else rowsPerPage = 30;
		int first = rowsPerPage * pageNo;
		Integer rowscount = 0;
		if(StringUtils.equals(type, "news") || StringUtils.isBlank(type)){
			List<News> newsList = newsService.getNewsListByTag(citycode, newTagMap.get(tag), newstype, searchKey, null, null, first, rowsPerPage);
			model.put("newsList", newsList);
			rowscount = newsService.getNewsCountByTag(citycode, newTagMap.get(tag), newstype, searchKey);
		}else if(StringUtils.equals(type, "subject")){
			List<SpecialActivity> spList = specialActivityService.getSpecialActivityList(Status.Y, tag, null, searchKey, first, rowsPerPage);
			Map<Long, String> spLogoMap = new HashMap<Long, String>();
			for(SpecialActivity sp : spList){
				GewaCommend gewaCommend_logo = daoService.getObject(GewaCommend.class, sp.getLogo());
				if(gewaCommend_logo != null) spLogoMap.put(sp.getId(), gewaCommend_logo.getLogo());
				else spLogoMap.put(sp.getId(), "");
			}
			model.put("spList", spList);
			model.put("spLogoMap", spLogoMap);
			rowscount = specialActivityService.getSpecialActivityCount(Status.Y, tag, null, searchKey);
		}
		int figureCount = newsService.getNewsCountByTag(citycode, newTagMap.get(tag), "5", null);
		if(figureCount > 0) model.put("figureCount", figureCount);
		PageUtil pageUtil = new PageUtil(rowscount, rowsPerPage, pageNo, "news/" + tag, true, true);
		Map params = new HashMap();
		params.put("newstype", newstype);
		params.put("type", type);
		model.put("searchKey", searchKey);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
	//	getRightInfo(tag, citycode, model);
	//	List<News> topNewsList = newsService.getNewsListByTag("310000", newTagMap.get(tag), null, null, null, null, 0, 5);
	//	model.put("topNewsList", topNewsList);
		//演出板块的资讯页面用的是演出2.0新的
		if(StringUtils.equals(tag,TagConstant.TAG_THEATRE)){
			//获取你可能喜欢的演出
			List<Long> bookingList = openDramaService.getCurDramaidList(citycode);
			List<Drama> bookList = daoService.getObjectList(Drama.class, bookingList);
			Collections.sort(bookList, new MultiPropertyComparator(new String[]{"boughtcount", "clickedtimes"}, new boolean[]{false,false}));
			List<Drama> interestDramaList = BeanUtil.getSubList(bookList, 0, 4);
			Map<Long, List<Integer>> priceListMap = new HashMap<Long, List<Integer>>();
			for(Drama curDrama : interestDramaList){
				priceListMap.put(curDrama.getId(), dramaPlayItemService.getPriceList(null, curDrama.getId(), null, null, false));
			}
			List<Long> openseatList = openDramaService.getCurDramaidList(citycode, OdiConstant.OPEN_TYPE_SEAT);
			model.put("openseatList",openseatList);
			model.put("priceListMap", priceListMap);
			model.put("bookingList", bookingList);
			model.put("interestDramaList",interestDramaList);
			return "drama/wide_dramaInfo.vm";
		}else if(StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
			model.put("cinemaCount",openPlayService.getOpiCinemaidList(citycode, null).size());
			model.put("fetureMovieCount",mcpService.getFutureMovieList(0, 200, null).size());
			model.put("newsCinema",true);
			model.put("movieIdList", new ArrayList<Long>());
			return "movie/wide_movieInfo.vm";
		}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT)){
			//总场馆数量
			int leftSportCount = sportService.getSportCountByCode(citycode, null, null);
			model.put("leftSportCount", leftSportCount);
			//总项目
			int leftSportItemCount = sportService.getSportItemCount(null, 0L, null);
			model.put("leftSportItemCount", leftSportItemCount);
			//总培训课程
			int leftCurriculumCount = agencyService.getTrainingGoodsCount(citycode, null, null, null, null, null, null, null, null);
			model.put("leftCurriculumCount", leftCurriculumCount);
			return "sport/wide_sportInfo.vm";
		}else{
			return "common/newsList.vm";
		}
	}
	
	private void getRightInfo(String tag, String citycode, ModelMap model){
		//右侧资讯
		Timestamp curTimestamp = null;
		String order = "";
		if(StringUtils.equals(tag, TagConstant.TAG_THEATRE) || StringUtils.equals(tag, TagConstant.TAG_SPORT) || StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
			curTimestamp = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -30);
			order = "clickedtimes";
		}
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		if(StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
			//正在热映
			Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
			List<Movie> movieList = mcpService.getCurMovieList(citycode);
			model.put("curMovieListCount", openPlayService.getOpiMovieidList(citycode, null).size());
			mcpService.sortMoviesByMpiCount(citycode, movieList);
			movieList = BeanUtil.getSubList(movieList, 0, 3);
			for (Movie movie : movieList) {
				Long mid = movie.getId();
				if(markCountMap.get(mid) == null){
					markCountMap.put(mid, markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, mid));
				}
			}
			model.put("hotMovieList", movieList);
			model.put("curMarkCountMap",markCountMap);
			// 电影活动
			List<GewaCommend> activityList = commonService.getGewaCommendList(citycode,null,SignName.MOVIEINDEX_ACTIVITY, null,null,true,true, 0,3);
			commonService.initGewaCommendList("activityList", rh, activityList);
			model.put("activityList", activityList);
			model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		}else if(StringUtils.equals(tag, TagConstant.TAG_SPORT)){
			//每日推荐活动
			List<GewaCommend> hotActivityList = commonService.getGewaCommendList(citycode, tag+SignName.NEWS_ACTIVITY, null, null, true, 0, 3);
			List<Long> activityIdList = BeanUtil.getBeanPropertyList(hotActivityList, Long.class, "relatedid", true);
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityListByIds(activityIdList);
			if(code.isSuccess() && code.getRetval() != null){
				model.put("activityList", code.getRetval());
			}
		}else{
			List<GewaCommend> hotActivityList = commonService.getGewaCommendList(citycode, tag+SignName.NEWS_ACTIVITY, null, null, true, 0, 1);
			commonService.initGewaCommendList("hotActivityList", rh, hotActivityList);
			model.put("hotActivityList", hotActivityList);
		}
		if(!StringUtils.equals(tag, TagConstant.TAG_SPORT)){
			List<News> rightNewsList = newsService.getNewsListByTag(citycode, tag, "", null, DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -7), order, 0, 6);
			model.put("rightNewsList", rightNewsList);
			//右侧人物
			List<News> rightStarList = newsService.getNewsListByTag(citycode, tag, "5", null, curTimestamp, order, 0, 4);
			model.put("rightStarList", rightStarList);
			
			//哇啦
			List<Comment> commentList= commentService.getCommentListByTag(tag, 0, 20);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
			model.put("commentList", commentList);
		}
		//头部图片资讯
		//推荐部分的内容采用全国同步的方式 modified  by liuyunxin on 2012/10/25
		boolean isSportTag = StringUtils.equals(tag, TagConstant.TAG_SPORT);
		List<GewaCommend> topNewsListP = null;
		if (isSportTag) {
			topNewsListP = commonService.getGewaCommendList(citycode, tag+SignName.NEWS_INDEX, null, null, true, 0, 4);
		}
		// 如果分站没数据还是从上海站取数据
		if (CollectionUtils.isEmpty(topNewsListP)) {
			topNewsListP = commonService.getGewaCommendList("310000", tag+SignName.NEWS_INDEX, null, null, true, 0, 4);
		}
		commonService.initGewaCommendList("topNewsListP", rh, topNewsListP);
		model.put("topNewsListP", topNewsListP);
		//头部标题资讯
		//推荐部分的内容采用全国同步的方式 modified  by liuyunxin on 2012/10/25
		List<GewaCommend> titleNewsList = null;
		if (isSportTag) {
			titleNewsList = commonService.getGewaCommendList(citycode, tag+SignName.NEWS_TITLE, null, null, true, 0, 1);
		}
		// 如果分站没有数据还是从上海站取数据
		if (CollectionUtils.isEmpty(titleNewsList)) {
			titleNewsList = commonService.getGewaCommendList("310000", tag+SignName.NEWS_TITLE, null, null, true, 0, 1);
		}
		commonService.initGewaCommendList("titleNewsList", rh, titleNewsList);
		model.put("titleNewsList", titleNewsList);
	}
	
	@RequestMapping("/ajax/common/getNewsRightSpList.xhtml")
	public String newsRightSpList(String tag, ModelMap model){
		//右侧专题
		List<SpecialActivity> rightSpList = specialActivityService.getSpecialActivityList(Status.Y, tag, null, null, 0, 3);
		if(rightSpList != null){
			List<Map> jsonMapList = new ArrayList<Map>();
			for (SpecialActivity sp : rightSpList) {
				Map jsonMap = new HashMap();
				jsonMap.put("link", sp.getWebsite());
				GewaCommend gewaCommend_logo = daoService.getObject(GewaCommend.class, sp.getLogo());
				if(gewaCommend_logo != null) jsonMap.put("adpath", gewaCommend_logo.getLimg());
				else jsonMap.put("adpath", "img/default_head.png");
				jsonMap.put("title", sp.getActivityname());
				jsonMap.put("adtype", "picture");
				jsonMapList.add(jsonMap);
			}
			model.put("jsonMapList", jsonMapList);
		}
		return "common/adLoadFlash.vm";
	}
}
