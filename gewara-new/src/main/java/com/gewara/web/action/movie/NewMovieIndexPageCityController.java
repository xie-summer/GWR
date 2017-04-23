package com.gewara.web.action.movie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.CommentCommand;
import com.gewara.command.SearchCinemaCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.helper.TimeItemHelper;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.common.GewaCity;
import com.gewara.model.content.Advertising;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.AdService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MarkHelper;
import com.gewara.util.RelatedHelper;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class NewMovieIndexPageCityController extends AnnotationController {
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}

	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}	
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	
	@Autowired@Qualifier("adService")
	private AdService adService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@RequestMapping("/movie/city/orderGuide.xhtml")
	public String orderGuide(String notAny, HttpServletResponse response, ModelMap model){
		Cookie cookie = new Cookie("orderGuide", "show");
		cookie.setPath("/movie/");
		if(StringUtils.equals(notAny, "yes")){
			cookie.setMaxAge(60 * 60 * 24 * 30);//30 day
		}else {
			cookie.setMaxAge(60 * 60 * 24); //24 hour
		}
		response.addCookie(cookie);
		return showJsonSuccess(model);
	}
	//其他城市多业务电影首页
	@RequestMapping("/movie/city/movieIndex.xhtml")
	public String otherCityMovieIndex(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams(); 
			PageView pageView = pageCacheService.getPageView(request, "movie/city/movieIndex.xhtml", params, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//头部信息
		List<GewaCommend> gcHeadList = commonService.getGewaCommendList(citycode, null, SignName.MOVIE_HEADINFO, null, HeadInfo.TAG, true, true, 0, 1);
		HeadInfo headInfo = null;
		if(!gcHeadList.isEmpty()){
			headInfo = daoService.getObject(HeadInfo.class, gcHeadList.get(0).getRelatedid());
			model.put("headInfo",BeanUtil.getBeanMapWithKey(headInfo, "css", "logosmall", "logobig", "link"));
		}
		Date playdate = DateUtil.getBeginningTimeOfDay(new Date());
		hotMovieList(model, playdate, citycode, SignName.MOVIEINDEX_MOVIE,true);
		List<GewaCommend> discountList = commonService.getGewaCommendList(citycode,null,SignName.MOVIE_INDEX_DISCOUNT, null, null, true,true, 0, 3);
		model.put("fontLinkList", discountList);
		model.put("moreServiceMovieIndex", true);
		model.put("movieIdList", new ArrayList<Long>());
		return singleOtherCityIndex(model,citycode,true);
	}
	//其他城市电影首页
	@RequestMapping("/movie/city/index.xhtml")
	public String otherCityIndex(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.equals(citycode, "310000")){
			citycode = "330100";
			WebUtils.setCitycode(request, "330100", response);
		}
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams(); 
			PageView pageView = pageCacheService.getPageView(request, "movie/city/index.xhtml", params, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		GewaCity gewaCity = daoService.getObject(GewaCity.class, citycode);
		//头部信息
		List<GewaCommend> gcHeadList = null;
		if(gewaCity.hasAuto()){
			gcHeadList = commonService.getGewaCommendList("310000", null,SignName.INDEX_HEADINFO_AUTO, null, HeadInfo.TAG, true, true, 0, 1);
		}else{
			gcHeadList = commonService.getGewaCommendList(citycode, null,SignName.INDEX_HEADINFO, null, HeadInfo.TAG, true, true, 0, 1);
		}
		HeadInfo headInfo = null;
		if(!gcHeadList.isEmpty()){
			headInfo = daoService.getObject(HeadInfo.class, gcHeadList.get(0).getRelatedid());
			model.put("headInfo",BeanUtil.getBeanMapWithKey(headInfo, "css", "logosmall", "logobig", "link"));
		}
		if(headInfo == null){
			List<Advertising> ads = adService.getAdListByPid(citycode, "index_headinfo");
			if(!VmUtils.isEmptyList(ads)){
				model.put("headinfo_ad", ads.get(0));
			}
		}
		//网站公告
		model.putAll(getWebsiteNotice(citycode));
		model.put("movieIdList", new ArrayList<Long>());
		if(gewaCity.hasAuto()){//是否无人管理的分站
			return autoCityIndex(model,citycode);
		}else if(GewaCity.SINGLE_SERVICE_TYPE.equals(gewaCity.getServiceType())){
			Date playdate = DateUtil.getBeginningTimeOfDay(new Date());
			this.hotMovieList(model, playdate, citycode, SignName.INDEX_MOVIELIST,true);
			List<GewaCommend> discountList = commonService.getGewaCommendList(citycode,null,SignName.INDEX_DISCOUNT, null, null, true,true, 0, 3);
			model.put("fontLinkList", discountList);
			return singleOtherCityIndex(model,citycode,false);
		}else{
			return otherCityIndex(model, citycode);
		}
	}
	/**
	 * 无人运营分站首页
	 * @param model
	 * @param citycode
	 * @return
	 */
	private String autoCityIndex(ModelMap model,String citycode){
		this.menuLeft(model, citycode);
		List<Movie> fetureMovieList = mcpService.getFutureMovieList(0, 200, "clickedtimes");
		model.put("fetureMovieCount",fetureMovieList.size());
		Date playdate = DateUtil.getBeginningTimeOfDay(new Date());
		this.hotMovieList(model, playdate, citycode, SignName.AUTO_MOVIEINDEX_MOVIE,false);
		Integer cinemaCount = mcpService.getTicketCinemaCount(citycode, null, null, null);
		model.put("ticketCinemaCount",cinemaCount);
		List<Cinema> cinemaList = mcpService.getCinemaListBySearchCmd(new SearchCinemaCommand(), citycode, 0, 5);
		model.put("cinemaList", cinemaList);
		model.put("fetureMovieList",BeanUtil.getSubList(fetureMovieList, 0, 5));
		model.put("videoCountMap", commonService.getVideoCount());// 预告片
		model.put("commentCountMap", commonService.getCommentCount());//哇啦
		// 电影资讯
		RelatedHelper rh = new RelatedHelper();
		movieNews(model,rh);
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTimetype("000000", RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, null, "cinema", null, null, null, 0, 8);
		if(code.isSuccess()){
			activityList = code.getRetval();
		}
		// 电影活动
		List<GewaCommend> autoActivityList = commonService.getGewaCommendList(citycode,null,SignName.AUTO_MOVIEINDEX_ACTIVITY, null,null,true,true, 0, 4);
		commonService.initGewaCommendList("activityList", rh, autoActivityList);
		List playActivityList = new LinkedList();
		for(GewaCommend c : autoActivityList){
			Object obj = rh.getR1("activityList",c.getId());
			if(obj != null){
				playActivityList.add(obj);
			}
		}
		if(playActivityList.isEmpty()){
			playActivityList = BeanUtil.getSubList(activityList, 0, 4);
			model.put("activityList",BeanUtil.getSubList(activityList, 4, 4));
		}else{
			activityList.removeAll(playActivityList);
			model.put("activityList",BeanUtil.getSubList(activityList, 0, 4));
		}
		model.put("playActivityList",playActivityList);
		model.put("relatedHelper", rh);
		movieVideos(model,rh);
		movieDiary(model, rh, citycode,true);
		getHotSaleList(model);
		Advertising ad = adService.getFirstAdByPostionTag("movieAutoIndex");
		model.put("ad", ad);
		model.put("movieIndex", true);
		List<String> ctypeList = new ArrayList();
		Map<String,String> map = mongoService.findOne(MongoData.NS_CITY_ROOM_CHARACTERISTIC, MongoData.SYSTEM_ID, citycode);
		if(map != null){
			ctypeList = Arrays.asList(StringUtils.split(map.get("characteristic"),","));
		}
		model.put("roomCtypeList", ctypeList);
		model.put("roomFeatureCinemas",mcpService.getRoomFeatureCinema(citycode));
		return "movie/fenzhan/index_spe_s.vm";
	}
	
	private String singleOtherCityIndex(ModelMap model,String citycode,boolean isMovieIndex){
		this.menuLeft(model, citycode);
		List<Movie> fetureMovieList = mcpService.getFutureMovieList(0, 200, "clickedtimes");
		model.put("fetureMovieCount",fetureMovieList.size());
		model.put("fetureMovieList",BeanUtil.getSubList(fetureMovieList, 0, 5));
		headBanner(model,citycode,SignName.MOVIEINDEX_NEWS);
		Integer cinemaCount = mcpService.getTicketCinemaCount(citycode, null, null, null);
		model.put("ticketCinemaCount",cinemaCount);
		// 电影资讯
		RelatedHelper rh = new RelatedHelper();
		movieNews(model,rh);
		model.put("relatedHelper", rh);
		// 电影活动
		String activitySignName = SignName.INDEX_MOVIEACTIVITY;
		if(isMovieIndex){
			activitySignName = SignName.MOVIEINDEX_ACTIVITY;
		}
		List<GewaCommend> autoActivityList = commonService.getGewaCommendList(citycode,null,activitySignName, null,null,true,true, 0, 8);
		commonService.initGewaCommendList("activityList", rh, autoActivityList);
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		List playActivityList = new LinkedList();
		for(GewaCommend c : autoActivityList){
			Object obj = rh.getR1("activityList",c.getId());
			if(obj != null){
				playActivityList.add(obj);
			}
		}
		if(playActivityList.size() < 8){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTimetype(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, null, "cinema", null, null, null, 0, 8);
			if(code.isSuccess()){
				activityList = code.getRetval();
			}
		}
		if(playActivityList.isEmpty()){
			playActivityList = BeanUtil.getSubList(activityList, 0, 4);
			model.put("activityList",BeanUtil.getSubList(activityList, 4, 4));
		}else{
			if(playActivityList.size() > 4){
				List tmpList = BeanUtil.getSubList(playActivityList, 4, playActivityList.size());
				playActivityList = BeanUtil.getSubList(playActivityList, 0,4);
				if(tmpList.size() < 4){
					activityList.removeAll(playActivityList);
					activityList.removeAll(tmpList);
					tmpList.addAll(BeanUtil.getSubList(activityList, 0, 4 - tmpList.size()));
				}
				model.put("activityList",tmpList);
			}else{
				activityList.removeAll(playActivityList);
				model.put("activityList",BeanUtil.getSubList(activityList, 0, 4));
			}
		}
		model.put("playActivityList",playActivityList);
		movieVideos(model,rh);
		getHotSaleList(model);
		List<Cinema> cinemaList =  mcpService.getCinemaListBySearchCmd(new SearchCinemaCommand(), citycode,0,5);
		model.put("cinemaList", cinemaList);
		model.put("movieIndex", true);
		this.movieDiary(model, rh, citycode,true);
		List<GewaCommend> gcPictureList = commonService.getGewaCommendList(citycode , SignName.INDEX_RIGHTPIC, null, null, true, 0, 1);
		model.put("ad", gcPictureList.isEmpty() ? null : gcPictureList.get(0));
		List<GewaCommend> weiboList = commonService.getGewaCommendList(citycode,null,SignName.INDEX_GUANGZHU_WEIBO, null,null,true,false, 0, 1);
		model.put("weibo",weiboList.isEmpty() ? null : weiboList.get(0));
		List<String> ctypeList = new ArrayList();
		Map<String,String> map = mongoService.findOne(MongoData.NS_CITY_ROOM_CHARACTERISTIC, MongoData.SYSTEM_ID, citycode);
		if(map != null){
			ctypeList = Arrays.asList(StringUtils.split(map.get("characteristic"),","));
		}
		model.put("roomCtypeList", ctypeList);
		model.put("roomFeatureCinemas",mcpService.getRoomFeatureCinema(citycode));
		return "movie/fenzhan/index_spe.vm";
	}
	private String otherCityIndex(ModelMap model,String citycode){
		headBanner(model,citycode,SignName.INDEX_BANNER_NEWS);
		RelatedHelper rh = new RelatedHelper();
		getMDSList(model, rh, citycode);
		getFutureMovieInfo(model, rh, citycode);
		/*List<GewaCommend> movieNewsList = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIENEWS, null, null, true, 0, 4);
		commonService.initGewaCommendList("movieNewsList", rh, movieNewsList);
		model.put("movieNewsList", movieNewsList);*/
		List<GewaCommend> picMovieChosenList = chosenArea(model, "movieChosenList", citycode,SignName.INDEX_MOVIE_CHOSEN, 10, 5, 15);
		model.put("picMovieChosenList", picMovieChosenList);
		model.put("picChosePage",picMovieChosenList.size()/2 + (picMovieChosenList.size() == 0 ? 1 : 0) + (picMovieChosenList.size()%2 > 0 ? 1 : 0));
		
		List<GewaCommend> activityList = commonService.getGewaCommendList(citycode,null,SignName.INDEX_MOVIEACTIVITY, null,null,true,true, 0, 4);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
		//movieDiary(model, rh, citycode,false);
		getSportArea(model,citycode,rh);
		model.put("relatedHelper", rh);
		List<Long> opiCinemaIdList = openPlayService.getOpiCinemaidList(citycode,null);
		model.put("cinemaCount", VmUtils.isEmptyList(opiCinemaIdList) ? 0 : opiCinemaIdList.size());
		Integer sportbookingcount = openTimeTableService.getbookingSportCount(DateUtil.parseDate(DateUtil.getCurDateStr(), "yyyy-MM-dd"), citycode);
		model.put("sportCount", sportbookingcount);
		List<GewaCommend> weiboList = commonService.getGewaCommendList(citycode,null,SignName.INDEX_GUANGZHU_WEIBO, null,null,true,false, 0, 1);
		model.put("weibo",weiboList.isEmpty() ? null : weiboList.get(0));
		return "movie/fenzhan/index_mix.vm";
	}
	//多业务分站热门资讯
	private List<GewaCommend> chosenArea(ModelMap model,String chosenType,String citycode,String signName,int pictureMax,int titleMax,int maxnum){
		List<GewaCommend> chosenList = commonService.getGewaCommendList(citycode,null,signName, null,null,true,false, 0, maxnum);
		List<GewaCommend> picMovieChosenList = new LinkedList<GewaCommend>();
		for(GewaCommend gc : chosenList){
			if(StringUtils.isNotBlank(gc.getLogo())){
				picMovieChosenList.add(gc);
			}
			if(picMovieChosenList.size() == pictureMax){
				break;
			}
		}
		chosenList.removeAll(picMovieChosenList);
		model.put(chosenType, BeanUtil.getSubList(chosenList, 0, titleMax));
		return picMovieChosenList;
	}
	/**
	 * 多业务首页 运动区域
	 * @param model
	 * @param citycode
	 * @param rh
	 */
	private void getSportArea(ModelMap model,String citycode,RelatedHelper rh){
		List<GewaCommend> gcItemInfo = commonService.getGewaCommendList(citycode, SignName.INDEX_NEW_SPORTLIST, null, null, true, 0, 3);
		commonService.initGewaCommendList("gcItemInfo", rh, gcItemInfo);
		Map<Long, Integer> openSportMap = new HashMap<Long, Integer>();
		for(GewaCommend gewa : gcItemInfo){
			List<Long>	sportIdList = openTimeTableService.getCurOttSportIdList(gewa.getRelatedid(), citycode);
			openSportMap.put(gewa.getRelatedid(), sportIdList.size());
		}
		model.put("openSportMap", openSportMap);
		List<Long> sportIdList = new LinkedList<Long>();
		List<GewaCommend> cIdCommendList = commonService.getGewaCommendList(citycode,null,SignName.INDEX_SPORTAREA, null,"sport",true,false, 0, 3);
		for(GewaCommend c : cIdCommendList){
			if(c.getRelatedid() != null){
				sportIdList.add(c.getRelatedid());
			}
		}
		if(gcItemInfo.size() > 0){
			GewaCommend firstItemInfo = BeanUtil.getSubList(gcItemInfo, 0, 1).get(0);
			Date cur =  DateUtil.getCurDate();
			if(sportIdList.size() < 3){
				List<Map<Long,Long>> mapList = openTimeTableService.getOpenTimeTableSportList(citycode,firstItemInfo.getRelatedid(), cur, DateUtil.addDay(cur, 1));
				for(Map<Long,Long> map : mapList){
					if(sportIdList.size() >= 3){
						break;
					}
					if(!sportIdList.contains(map.get("sportid"))){
						sportIdList.add(map.get("sportid"));
					}
				}
			}
			model.put("sportList",daoService.getObjectList(Sport.class, sportIdList));
			if(!VmUtils.isEmptyList(sportIdList)){
				getSportOpenTimeTableList(sportIdList.get(0), firstItemInfo.getRelatedid(), model);
				model.put("ottListSportId", sportIdList.get(0));
			}
			model.put("firstItemInfo", firstItemInfo);
			model.put("gcItemInfo", BeanUtil.getSubList(gcItemInfo, 1, 3));
		}
		model.put("sportNotice",commonService.getGewaCommendList(citycode, SignName.INDEX_SPORT_NOTICE, null, null, true, 0, 1));
	}
	/**
	 * 首页头部banner
	 * @param model
	 * @param citycode
	 */
	private void headBanner(ModelMap model, String citycode,String signName){
		// 首页信息
		List<GewaCommend> infoList = commonService.getGewaCommendList(citycode, signName, null, null, true, 0, 8);
		model.put("infoList", infoList);
		Map<Long, String> subTitleMap = new HashMap<Long, String>();
		for (GewaCommend gc : infoList) {
			List<GewaCommend> list = commonService.getGewaCommendList(citycode, SignName.NEWS_SUBTITLE, gc.getId(), null, true, 0, 1);
			if (list.size() > 0)
				subTitleMap.put(gc.getId(), list.get(0).getTitle());
			else {
				subTitleMap.put(gc.getId(), "");
			}
		}
		model.put("subTitleMap", subTitleMap);
	}
	//电影推荐
	private void getMDSList(ModelMap model, RelatedHelper rh, String citycode) {
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIELIST, null, null, true, 0, 13);
		commonService.initGewaCommendList("gcMovieList", rh, gcMovieList);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		List<Diary> diaryList = new ArrayList<Diary>();
		for(GewaCommend gewaCommend :gcMovieList) {
			markCountMap.put(gewaCommend.getRelatedid(), markService.getMarkCountByTagRelatedid(gewaCommend.getTag(), gewaCommend.getRelatedid()));
			diaryList.addAll(diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", gewaCommend.getRelatedid(), 0, 50, "poohnum"));
		}
		model.put("gcMovieList", gcMovieList);
		/*List<GewaCommend> gcMobile = commonService.getGewaCommendList(citycode, SignName.INDEX_MOBILE, null, null, true, 0, 10);
		model.put("gcMobile", gcMobile);*/
		model.put("markCountMap", markCountMap);
		model.put("curMovieIdList", mcpService.getCurMovieIdList(citycode));//当前排片的影片列表
		model.put("openMovieList", mcpService.getOpenMovieList(citycode));//当前购票的影片列表
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		model.put("gcDiscountList", commonService.getGewaCommendList(citycode, null, SignName.INDEX_DISCOUNT, null, null, true, true, 0, 3));
		Collections.sort(diaryList, new MultiPropertyComparator(new String[] {"flag","sumnum","addtime"}, new boolean[] {true,false,false}));
		diaryList = BeanUtil.getSubList(diaryList, 0, 5);
		Map<Long, Integer> moiveDiaryCountMap = new HashMap<Long, Integer>();
		Map<Long,Movie> diaryMovieMap = new HashMap<Long,Movie>();
		for(Diary diary : diaryList){
			Long mId = diary.getCategoryid();
			if(mId != null && moiveDiaryCountMap.get(mId) == null){
				moiveDiaryCountMap.put(mId,diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mId));
			}
			diaryMovieMap.put(mId, daoService.getObject(Movie.class, mId));
		}
		model.put("diaryMovieMap", diaryMovieMap);
		model.put("moiveDiaryCountMap", moiveDiaryCountMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		model.put("diaryList", diaryList);
	}
	
	//即将上映
	private void getFutureMovieInfo(ModelMap model, RelatedHelper rh, String citycode){
		Map<Long, Integer> pictureCountMap = new HashMap<Long, Integer>();
		Map<Long, Integer> videoCountMap = new HashMap<Long, Integer>();
		List<GewaCommend> futureMovieList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_FUTUREMOVIE, null, null, true, 0, 5);
		commonService.initGewaCommendList("futureMovieList", rh, futureMovieList);
		Map<Long, MarkCountData> markCountMap = (Map<Long, MarkCountData>)model.get("markCountMap");
		if (futureMovieList.size() > 0) {
			for (GewaCommend futureMovie : futureMovieList) {
				pictureCountMap.put(futureMovie.getId(), pictureService.getPictureCountByRelatedid("movie", futureMovie.getRelatedid()));
				videoCountMap.put(futureMovie.getId(), videoService.getVideoCountByTag("movie", futureMovie.getRelatedid()));
				markCountMap.put(futureMovie.getRelatedid(), markService.getMarkCountByTagRelatedid(futureMovie.getTag(), futureMovie.getRelatedid()));
			}
		}
		model.put("pictureCountMap", pictureCountMap);
		model.put("videoCountMap", videoCountMap);
		model.put("futureMovieList", futureMovieList);
	}
	
	
	// 网站公告
	private Map getWebsiteNotice(String citycode){
		Map model = new HashMap();
		List<GewaCommend> gcCommuList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_ALL,null, SignName.INDEX_WEBSITE_NOTICE, null, null, true, true, 0, 1);
		if(!gcCommuList.isEmpty()&&StringUtils.isNotBlank(gcCommuList.get(0).getSummary())) {
			model.put("publicNotice", gcCommuList.get(0).getSummary());
		}else{
			gcCommuList = commonService.getGewaCommendList(citycode,null, SignName.INDEX_WEBSITE_NOTICE, null, null, true, true, 0, 1);
			if(!gcCommuList.isEmpty()&&StringUtils.isNotBlank(gcCommuList.get(0).getSummary())) {
				model.put("publicNotice", gcCommuList.get(0).getSummary());
			}
		}
		return model;
	}
	//购票排行
	private void getHotSaleList(ModelMap model){
		Map<Long, MarkCountData> markCountMap = (Map<Long, MarkCountData>)model.get("markCountMap");
		List<Map> saleMovie = nosqlService.getBuyTicketRanking();
		List<Movie> saleMovieList = new LinkedList<Movie>();
		Map<Long,Map> saleMovieMap = new HashMap<Long,Map>();
		for(Map map : saleMovie){
			Movie movie = this.daoService.getObject(Movie.class,(Long)map.get("movieId"));
			saleMovieList.add(movie);
			saleMovieMap.put(movie.getId(),map);
			if(markCountMap.get(movie.getId()) == null){
				markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE,movie.getId()));
			}
		}
		model.put("saleMovieList",BeanUtil.getSubList(saleMovieList, 0, 5) );
		model.put("saleMovieMap", saleMovieMap);
	}
	
	/**
	 * 电影资讯
	 * @param model
	 * @param rh
	 */
	private void movieNews(ModelMap model,RelatedHelper rh){
		List<GewaCommend> movieNewsList = commonService.getGewaCommendList("310000", SignName.NEWS_MOVIE, null, null, true, 0, 4);
		commonService.initGewaCommendList("movieNewsList", rh, movieNewsList);
		model.put("movieNewsList", movieNewsList);
	}
	
	/**
	 * 新片预告
	 * @param model
	 * @param rh
	 */
	private void movieVideos(ModelMap model,RelatedHelper rh){
		List<GewaCommend> videoList = commonService.getGewaCommendList("310000", SignName.MOVIEINDEX_VIDEO, null, null, true, 0,4);
		commonService.initGewaCommendList("videoList", rh, videoList);
		model.put("videoList", videoList);
	}
	
	private void getSportOpenTimeTableList(long sportId,long itemId,ModelMap model){
		Sport sport = daoService.getObject(Sport.class, sportId);
		Map<Date,List<String>> playHourListMap = new HashMap<Date,List<String>>();
		Map<Date,Map> otiMap = new HashMap<Date,Map>();
		Date curDate = DateUtil.currentTime();
		if(sport != null){
			List<OpenTimeTable> ottList = openTimeTableService.getOpenTimeTableList(sport.getId(), itemId, curDate,null,OpenTimeTableConstant.OPEN_TYPE_FIELD,true,0,2);
			if(!ottList.isEmpty()){
				for(OpenTimeTable ott : ottList){
					List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
					TimeItemHelper itemHelper = new TimeItemHelper(otiList);
					List<String> playHourList = itemHelper.getPlayHourList();
					Collections.sort(playHourList);
					playHourListMap.put(ott.getPlaydate(), playHourList);
					otiMap.put(ott.getPlaydate(), BeanUtil.beanListToMap(otiList, "hour"));
				}
			}
		}
		Object[] dates = playHourListMap.keySet().toArray();
		Arrays.sort(dates);
		model.put("playDates", dates);
		model.put("playHourListMap", playHourListMap);
		model.put("otiMap", otiMap);
		model.put("curHour", DateUtil.format(curDate, "HH:mm"));
	}
	/**
	 * 运动场次加载
	 * @param sportId
	 * @param itemId
	 * @param model
	 * @return
	 */
	@RequestMapping("/ajax/loadSportOttItem.xhtml")
	public String loadSportOttItem(long sportId,long itemId,ModelMap model){
		this.getSportOpenTimeTableList(sportId, itemId, model);
		return "movie/fenzhan/ajax_sportOttItem.vm";
	}
	/**
	 * ajax加载运动心得
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/ajax/loadSportDiary.xhtml")
	public String loadSportDiary(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams();
			PageView pageView = pageCacheService.getPageView(request, "ajax/loadSportDiary.xhtml", params, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//List<GewaCommend> gcItemInfo = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORTPLACE, null, null, true, 0, 3);
		List<Diary> diaryList = new ArrayList<Diary>();
		//for(GewaCommend gc : gcItemInfo){
		diaryList.addAll(diaryService.getDiaryList(Diary.class, null, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_SPORTITEM, /*gc.getRelatedid()*/null, 0, 200, "flowernum"));
		//}
		Collections.sort(diaryList, new MultiPropertyComparator(new String[] {"flowernum","addtime"}, new boolean[] {false,false}));
		model.put("diaryList",diaryList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		return "movie/fenzhan/ajax_sportDiaryList.vm";
	}
	
	@RequestMapping("/ajax/loadLeftMenuAd.xhtml")
	public String loadLeftMenuAd(String tag,ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams();
			params.addSingleString("tag", tag);
			PageView pageView = pageCacheService.getPageView(request, "ajax/loadLeftMenuAd.xhtml", params, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		String signName = SignName.INDEX_MOVIE_AD;
		if(StringUtils.equals(tag, "sport")){
			signName = SignName.INDEX_SPORT_AD;
		}else if(StringUtils.equals(tag, "drama")){
			signName = SignName.INDEX_DRAMA_AD;
		}
		List<GewaCommend> commendList = commonService.getGewaCommendList(citycode,signName, null, null, true, 0,5);
		model.put("adList", commendList);
		model.put("tag", tag);
		return "movie/fenzhan/ajax_leftMenuAd.vm";
	}
	
	@RequestMapping("/ajax/common/qryAutoIndexPageComment.xhtml")
	public String qryAutoIndexPageComment(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			HttpServletRequest request,HttpServletResponse response,CommentCommand command, ModelMap model){
		List<Comment> commentList = new ArrayList<Comment>();
		String citycode = WebUtils.getAndSetDefault(request, response);
		// 推荐的热映电影
		List<Long> movieIdList = new LinkedList<Long>();
		GewaCity gewaCity = daoService.getObject(GewaCity.class, citycode);
		String signName =  SignName.INDEX_MOVIELIST;
		if(gewaCity.hasAuto()){
			signName = SignName.AUTO_MOVIEINDEX_MOVIE;
		}
		List<GewaCommend> autoMovieList = commonService.getGewaCommendList(citycode,null,signName, null,"movie",true,false, 0, 5);
		for(GewaCommend c : autoMovieList){
			if(c.getRelatedid() != null){
				movieIdList.add(c.getRelatedid());
			}
		}
		if(movieIdList.size() < 5){
			movieIdList.addAll(mcpService.getCurMovieIdList(citycode,DateUtil.getBeginningTimeOfDay(new Date())));
		}
		movieIdList = BeanUtil.getSubList(movieIdList, 0, 5);
		List<Comment> allList = new ArrayList<Comment>();//防止某些城市影片不够5部取不到5条热门哇啦
		for(Long id : movieIdList){
			List<Comment> tmpList = commentService.getHotCommentListByRelatedId(command.tag,"", id, null, null, 0, 20);
			Collections.sort(tmpList, new MultiPropertyComparator(new String[] {"replycount"}, new boolean[] {false}));
			allList.addAll(tmpList);
			commentList.addAll(BeanUtil.getSubList(tmpList, 0, 1));
		}
		if(commentList.size() < 5){
			allList.removeAll(commentList);
			Collections.sort(allList, new MultiPropertyComparator(new String[] {"replycount"}, new boolean[] {false}));
			commentList.addAll(BeanUtil.getSubList(allList, 0, 5 - commentList.size()));
		}
		Collections.sort(commentList, new MultiPropertyComparator(new String[] {"replycount"}, new boolean[] {false}));
		model.put("commentList", BeanUtil.getSubList(commentList, 0, 5));
		List<Long> cIds = BeanUtil.getBeanPropertyList(commentList, Long.class, "transferid", true);
		if(!cIds.isEmpty()){
			List<Comment> tranferCommentList = commentService.getCommentByIdList(cIds);
			Map<Long, Comment> tranferCommentMap = BeanUtil.beanListToMap(tranferCommentList, "id");
			model.put("tranferCommentMap", tranferCommentMap);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(tranferCommentMap.values()));
		}
		if(command.hasMarks()){
			model.put("markHelper", new MarkHelper());
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			addCacheMember(model, member.getId());
			model.put("logonMember", member);
		}
		model.put("command", command);
		model.put("commentCount",0);
		return "wala/wide_wala.vm";
	}
	private void menuLeft(ModelMap model,String citycode){
		List<Long> opiCinemaList = openPlayService.getOpiCinemaidList(citycode,null);
		model.put("cinemaCount",VmUtils.isEmptyList(opiCinemaList) ? 0 : opiCinemaList.size());
		model.put("opiCinemaList",opiCinemaList);
		model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		List<Long> opiMovieIdList = openPlayService.getOpiMovieidList(citycode, null);//购票影片
		model.put("curMovieListCount", opiMovieIdList.size());
		model.put("opiMovieList", opiMovieIdList);
	}
	/**
	 * 推荐的热映电影
	 * @param model
	 * @param playdate
	 * @param citycode
	 * @param signName
	 */
	private void hotMovieList(ModelMap model,Date playdate,String citycode,String signName,boolean isSign){
		List<Long> movieIdList = mcpService.getCurMovieIdList(citycode,playdate);
		model.put("ticketMovieCount", movieIdList.size());
		// 推荐的热映电影
		List<Long> mIds = new LinkedList<Long>();
		List<GewaCommend> autoMovieList = commonService.getGewaCommendList(citycode,null,signName, null,"movie",true,false, 0, 5);
		for(GewaCommend c : autoMovieList){
			if(c.getRelatedid() != null){
				mIds.add(c.getRelatedid());
			}
		}
		List<Movie> movieList = daoService.getObjectList(Movie.class, mIds);
		movieIdList.removeAll(mIds);
		List<Long> subMovieIdList = BeanUtil.getSubList(movieIdList, 0, 7 - mIds.size());
		movieList.addAll(daoService.getObjectList(Movie.class, subMovieIdList));
		// 评分统计
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		for(Movie m : movieList){
			markCountMap.put(m.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, m.getId()));
		}
		model.put("markCountMap", markCountMap);
		Movie movie = null;
		if(!movieList.isEmpty()){
			movie = movieList.remove(0);
			model.put("movie", movie);
		}
		model.put("movieList", movieList);
		Map<Cinema,List<MoviePlayItem>> cMPIMap = new HashMap<Cinema,List<MoviePlayItem>>();
		List<Long> openMpiList = new ArrayList<Long>();
		if(movie != null){
			List<Long> cidList = new ArrayList<Long>();
			if(isSign){
				List<GewaCommend> cIdCommendList = commonService.getGewaCommendList(citycode,null,SignName.INDEX_MOVIE_MPICINEMA, null,"cinema",true,false, 0, 2);
				for(GewaCommend c : cIdCommendList){
					if(c.getRelatedid() != null){
						cidList.add(c.getRelatedid());
					}
				}
			}
			List<Cinema> cinemaList = daoService.getObjectList(Cinema.class,cidList);
			List<Date> dateList = mcpService.getCurMoviePlayDate2(citycode, movie.getId());
			if (!dateList.isEmpty() && playdate.before(dateList.get(0))) {
				playdate = dateList.get(0);
			}
			if(!dateList.isEmpty() && !dateList.contains(playdate)){
				playdate = dateList.get(0);
			}
			if(cinemaList.size() < 2){
				List<Long> tmpCinemaIdList = openPlayService.getOpiCinemaidList(citycode, movie.getId());
				List<Cinema> opiCinemaList = new ArrayList<Cinema>();
				if(!VmUtils.isEmptyList(tmpCinemaIdList)){
					opiCinemaList = daoService.getObjectList(Cinema.class,tmpCinemaIdList);
					final Map<Long, Integer> cinemaOpiCountMap = new HashMap<Long, Integer>();
					for(Long cId : tmpCinemaIdList){
						cinemaOpiCountMap.put(cId, openPlayService.getOpiCount(citycode, cId, movie.getId(), null, null, true));
					}
					Collections.sort(opiCinemaList,new Comparator<Cinema>(){
						@Override
						public int compare(Cinema o1, Cinema o2) {
							int result = 0;
							if(o1!=null && o2==null){
								result = 1;
							}else if(o1==null && o2!=null){
								result = -1;
							}else if(o1!=null && o2!=null){
								int opi1 = cinemaOpiCountMap.get(o1.getId());
								int opi2 = cinemaOpiCountMap.get(o2.getId());
								if(opi1 > opi1){
									result = 1;
								}else if(opi1 == opi2){
									result = 0;
								}else{
									result = -1;
								}
							}
							return result;
						}
					});
					if(!opiCinemaList.isEmpty()){
						opiCinemaList.removeAll(cinemaList);
					}
				}
				if(opiCinemaList.isEmpty() || opiCinemaList.size() < 2){
					List<Cinema> tmpCinemaList = daoService.getObjectList(Cinema.class, mcpService.getCurCinemaIdList(citycode, movie.getId(),playdate));
					Collections.sort(tmpCinemaList, new MultiPropertyComparator(new String[] {"clickedtimes"}, new boolean[] {false}));
					opiCinemaList.addAll(tmpCinemaList);
				}
				for(Cinema c : opiCinemaList){
					if(cinemaList.size() >= 2){
						break;
					}
					if(!cinemaList.contains(c)){
						cinemaList.add(c);
					}
				}
			}
			for(Cinema c : cinemaList){
				List<MoviePlayItem> mpiList = mcpService.getCinemaMpiList(c.getId(), movie.getId(), playdate);
				mpiList = BeanUtil.getSubList(mpiList, 0, 15);
				cMPIMap.put(c,mpiList);
				for(MoviePlayItem mpi : mpiList){
					OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
					if (opi != null && !opi.isClosed()) {
						openMpiList.add(mpi.getId());
					}
				}
			}
			model.put("mpiCinemaList",cinemaList);
		}
		model.put("cMPIMap", cMPIMap);
		model.put("openMpiList",openMpiList);
	}
	/**
	 * 精彩影评
	 * @param model
	 * @param rh
	 * @param citycode
	 */
	private void movieDiary(ModelMap model,RelatedHelper rh,String citycode,boolean loadSh){
		String city = citycode;
		String signName = SignName.INDEX_MOVIEDIARY;
		if(loadSh){
			city = "310000";
			signName = SignName.DIARY_MOVIEINDEX;
		}
		List<GewaCommend> diaryList = commonService.getGewaCommendList(city, signName, null, null, true, 0, 5);
		commonService.initGewaCommendList("diaryList", rh, diaryList);
		Map<Long, Integer> moiveDiaryCountMap = new HashMap<Long, Integer>();
		for(GewaCommend diary : diaryList){
			Long mId = ((Diary)rh.getR1("diaryList",diary.getId())).getCategoryid();
			if(mId != null && moiveDiaryCountMap.get(mId) == null){
				moiveDiaryCountMap.put(mId,diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mId));
			}
		}
		List<BaseObject> dList = rh.getGroupIndexList("diaryList", 1);
		Map<Serializable, String> categoryMap = BeanUtil.getKeyValuePairMap(dList, "categoryid", "category");
		relateService.addRelatedObject(1, "categoryMap", rh, categoryMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(dList));
		model.put("moiveDiaryCountMap", moiveDiaryCountMap);
		model.put("diaryList", diaryList);
	}
}