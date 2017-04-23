package com.gewara.web.action.movie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchCinemaCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Flag;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.common.GewaCity;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MarkHelper;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class NewMovieIndexPageController extends AnnotationController {
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;

	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}

	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;

	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}

	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;

	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}

	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;

	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;

	@RequestMapping("/movie/index.xhtml")
	public String newIndex(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (!StringUtils.equals(citycode, "310000")){
			GewaCity gewaCity = AdminCityContant.citycode2GewaCity.get(citycode);
			if(gewaCity.hasAuto() || GewaCity.SINGLE_SERVICE_TYPE.equals(gewaCity.getServiceType())){
				return "forward:/movie/city/index.xhtml";
			}else{
				return "forward:/movie/city/movieIndex.xhtml";
			}
		}
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			PageView pageView = pageCacheService.getPageView(request, "movie/index.xhtml", params, citycode);
			if (pageView != null) {
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
		// 即将上映
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		List<GewaCommend> videoList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_VIDEO, null, null, true, 0,4);
		commonService.initGewaCommendList("videoList", rh, videoList);
		model.put("videoList", videoList);
		// 影评
		Map<Long, Map> diaryPointMap = new HashMap<Long, Map>();
		Map<Long, Integer> moiveDiaryCountMap = new HashMap<Long, Integer>();
		List<GewaCommend> diaryList = commonService.getGewaCommendList(citycode, SignName.DIARY_MOVIEINDEX, null, null, true, 0, 5);
		commonService.initGewaCommendList("diaryList", rh, diaryList);
		List<BaseObject> dList = rh.getGroupIndexList("diaryList", 1);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(dList));
		Map<Serializable, String> categoryMap = BeanUtil.getKeyValuePairMap(dList, "categoryid", "category");
		relateService.addRelatedObject(1, "categoryMap", rh, categoryMap);
		for(GewaCommend diary : diaryList){
			Long mId = ((Diary)rh.getR1("diaryList",diary.getId())).getCategoryid();
			if(mId != null && moiveDiaryCountMap.get(mId) == null){
				moiveDiaryCountMap.put(mId,diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", mId));
			}
		}
		model.put("moiveDiaryCountMap", moiveDiaryCountMap);
		model.put("diaryList", diaryList);
		model.put("diaryPointMap", diaryPointMap);
		// 电影资讯
		List<GewaCommend> movieNewsList = commonService.getGewaCommendList(citycode, SignName.NEWS_MOVIE, null, null, true, 0, 4);
		commonService.initGewaCommendList("movieNewsList", rh, movieNewsList);
		model.put("movieNewsList", movieNewsList);
		// 电影活动
		List<GewaCommend> activityList = commonService.getGewaCommendList(citycode,null,SignName.MOVIEINDEX_ACTIVITY, null,null,true,true, 0, 4);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
		model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		// 电影论坛
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_HOT_DIARY, null, "", true, 0, 5);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		// 待解决的电影问题
		List<GewaCommend> qnList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_QUERSTION, null, null, true, 0, 5);
		commonService.initGewaCommendList("qnList", rh, qnList);
		model.put("qnList", qnList);
		//电影优惠
		List<GewaCommend> discountList = commonService.getGewaCommendList(citycode,null,SignName.MOVIE_INDEX_DISCOUNT, null, null, true,true, 0, 5);
		model.put("discountList", discountList);
		// 评分统计
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		// 首页信息
		List<GewaCommend> infoList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_NEWS, null, null, true, 0, 20);
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
		Integer count = mcpService.getCurMovieCount(citycode);
		int rowsPerPage = 10;
		Integer realPageCount = (count - 1) / rowsPerPage + 1;
		model.put("realPageCount", realPageCount);
		model.putAll(getHotMovie(rh, citycode));
		model.put("citycode", citycode);
		model.put("cinemaCount",openPlayService.getOpiCinemaidList(citycode,null).size());
		getHotSaleList(model);
		model.put("movieIndex", true);
		List<String> ctypeList = new ArrayList();
		Map<String,String> map = mongoService.findOne(MongoData.NS_CITY_ROOM_CHARACTERISTIC, MongoData.SYSTEM_ID, citycode);
		if(map != null){
			ctypeList = Arrays.asList(StringUtils.split(map.get("characteristic"),","));
		}
		model.put("roomCtypeList", ctypeList);
		model.put("roomFeatureCinemas",mcpService.getRoomFeatureCinema(citycode));
		//model.put("featureCinemas", mcpService.getFeatureCinema(citycode,Flag.SERVICE_PARK,Flag.SERVICE_SALE,Flag.SERVICE_IMAX,Flag.SERVICE_CHILD,Flag.SERVICE_PAIRSEAT));
		model.put("movieIdList", new ArrayList<Long>());
		return "movie/wide_index.vm";
	}
	
	
	public void getHotSaleList(ModelMap model){
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
		model.put("saleMovieList", BeanUtil.getSubList(saleMovieList, 0, 5));
		model.put("saleMovieMap", saleMovieMap);
	}

	
	@RequestMapping("/movie/loadMovieIndexList.xhtml")
	public String commonData(ModelMap model, Integer pageNo,String movieIds, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			params.addInteger("pageNo", pageNo);
			params.addSingleString("movieIds", movieIds);
			PageView pageView = pageCacheService.getPageView(request, "movie/loadMovieIndexList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		if(StringUtils.isNotBlank(movieIds)){
			model.put("markData",markService.getMarkdata(TagConstant.TAG_MOVIE));
			List<Movie> movies = new LinkedList<Movie>();
			Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
			String[] mIds = StringUtils.split(movieIds, ",");
			for(String mId : mIds){
				if(ValidateUtil.isNumber(mId)){
					Movie movie = this.daoService.getObject(Movie.class,Long.parseLong(mId));
					movies.add(movie);
					markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
				}
			}
			model.put("markCountMap", markCountMap);
			model.put("movieIds", movieIds);
			model.put("movies", movies);
			model.put("pageNo", pageNo);
			model.put("commentCountMap", commonService.getCommentCount());
			model.put("opiMovieList", openPlayService.getOpiMovieidList(citycode, null));
			model.put("videoCountMap", commonService.getVideoCount());
			model.put("citycode", citycode);
			model.put("movieIdList", new ArrayList<Long>());
		}
		return "include/movie/ajax_wide_movieIndex.vm";
	}
	/**
	 * 首页默认热门影院列表
	 * @param citycode
	 * @param cmd
	 * @param member
	 * @param model
	 * @return
	 */
	private List<Cinema> getCommendCinemaList(String citycode,SearchCinemaCommand cmd,Member member,ModelMap model){
		cmd.setHotcinema("true");
		List<Cinema> cinemaList = mcpService.getCinemaListBySearchCmd(cmd, citycode, 0, 300);
		model.put("cinemaCount", cinemaList.size());
		cinemaList = BeanUtil.getSubList(cinemaList, 0, 5);
		if (member != null) {
			List<Cinema> cinemas = orderQueryService.getMemberOrderCinemaList(member.getId(), 10);
			List<Cinema> otherList = new LinkedList<Cinema>();
			for(Cinema cinema : cinemas){
				if(otherList.size()  > 1){
					break;
				}
				if(StringUtils.equals(citycode,cinema.getCitycode())){
					otherList.add(cinema);
				}
			}
			List<Long> orderCinemaIds = BeanUtil.getBeanPropertyList(otherList, Long.class, "id", true);
			model.put("orderCinemas",orderCinemaIds);
			if(orderCinemaIds != null && !orderCinemaIds.isEmpty()){
				Iterator<Cinema> it = cinemaList.iterator();
				while(it.hasNext()){
					Cinema cinema = it.next();
					if(orderCinemaIds.contains(cinema.getId())){
						it.remove();
					}
				}
				otherList.addAll(BeanUtil.getSubList(cinemaList, 0, 5 - otherList.size()));
				cinemaList = otherList;
			}
		}
		return cinemaList;
	}
	
	private final List<String> hotFlag = Arrays.asList(new String[]{Flag.SERVICE_PARK,Flag.SERVICE_PAIRSEAT,"popcorn","refund","activity"});
	
	private void sortHotCinemaListByMark(List<Cinema> cinemaList){
		Collections.sort(cinemaList,new Comparator<Cinema>(){
			@Override
			public int compare(Cinema o1, Cinema o2) {
				int result = 0;
				if(o1!=null && o2==null){
					result = 1;
				}else if(o1==null && o2!=null){
					result = -1;
				}else if(o1!=null && o2!=null){
					int mark1 = MarkHelper.getSingleMarkStar(o1, "general");
					int mark2 = MarkHelper.getSingleMarkStar(o2, "general");
					if(mark1 > mark2){
						result = -1;
					}else if(mark1 == mark2){
						result = 0;
					}else{
						result = 1;
					}
				}
				return result;
			}
		});
	}
	@RequestMapping("/movie/ajax/loadHotCinemaList.xhtml")
	public String getHotCinema(@CookieValue(value = LOGIN_COOKIE_NAME, required = false) String sessid,
			HttpServletRequest request,String flag, HttpServletResponse response, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		boolean isCacheFlag = hotFlag.contains(flag);
		if (pageCacheService.isUseCache(request) && (isCacheFlag || (!isCacheFlag && member == null))) {// 先使用缓存
			PageParams params = new PageParams();
			if(isCacheFlag){
				params.addSingleString("flag", flag);
			}
			PageView pageView = pageCacheService.getPageView(request, "movie/ajax/loadHotCinemaList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		SearchCinemaCommand cmd = new SearchCinemaCommand();
		//cmd.setBooking(Cinema.BOOKING_OPEN);
		List<Cinema> cinemaList = null;
		if(StringUtils.equals(Flag.SERVICE_PARK, flag)){
			cmd.setPark("freePark");
			cmd.setOrder("generalmark");
			cinemaList = mcpService.getCinemaListBySearchCmd(cmd, citycode, 0,300);
			sortHotCinemaListByMark(cinemaList);
			model.put("cinemaCount", cinemaList.size());
			cinemaList = BeanUtil.getSubList(cinemaList, 0, 5);
		}else if(StringUtils.equals(Flag.SERVICE_PAIRSEAT, flag)){
			cmd.setPairseat(Flag.SERVICE_PAIRSEAT);
			cmd.setOrder("generalmark");
			cinemaList = mcpService.getCinemaListBySearchCmd(cmd, citycode, 0,300);
			sortHotCinemaListByMark(cinemaList);
			model.put("cinemaCount", cinemaList.size());
			cinemaList = BeanUtil.getSubList(cinemaList, 0, 5);
		}else if(StringUtils.equals("popcorn", flag)){
			List<Long> cinemaIdList = mcpService.getCinemaIdListByGoods(citycode);
			model.put("cinemaCount", cinemaIdList.size());
			cinemaList = BeanUtil.getSubList(daoService.getObjectList(Cinema.class, cinemaIdList), 0, 5);
		}else if(StringUtils.equals("refund", flag)){
			cmd.setRefund("Y");
			cmd.setOrder("generalmark");
			cinemaList = mcpService.getCinemaListBySearchCmd(cmd, citycode, 0,300);
			sortHotCinemaListByMark(cinemaList);
			model.put("cinemaCount", cinemaList.size());
			cinemaList = BeanUtil.getSubList(cinemaList, 0, 5);
		}else if(StringUtils.equals("activity", flag)){
			List<Long> cinemaIdList = synchActivityService.getActivityRelatedidByTag(citycode, 3, "cinema");
			if(VmUtils.isEmptyList(cinemaIdList)){
				cinemaList = new ArrayList<Cinema>();
				model.put("cinemaCount", cinemaList.size());
			}else{
				cinemaList = daoService.getObjectList(Cinema.class, cinemaIdList);
				model.put("cinemaCount", cinemaList.size());
				Collections.sort(cinemaList,new MultiPropertyComparator(new String[] {"clickedtimes"}, new boolean[] {false}));
				cinemaList = BeanUtil.getSubList(cinemaList, 0, 5);
			}
		}else{
			cinemaList = getCommendCinemaList(citycode, cmd, member, model);
		}
		Map<Long,Integer> playMovieCount = new HashMap<Long,Integer>();
		Map<Long,Integer> playItemCount = new HashMap<Long,Integer>();
		Map<Long,String> playItemTimeArea = new HashMap<Long,String>();
		Map<Long,Integer> commentCountMap = new HashMap<Long,Integer>();
		Date fyrq = DateUtil.getBeginningTimeOfDay(new Date());
		Map<String, Integer> commonCountMap = commonService.getCommentCount();
		Map<Long,List<Goods>> cinemaGoodsMap = new HashMap<Long,List<Goods>>();
		Map<Long,List<RemoteActivity>> cinemaActivityMap = new HashMap<Long,List<RemoteActivity>>();
		for(Cinema cinema : cinemaList){
			playMovieCount.put(cinema.getId(),mcpService.getCinemaMovieCountByDate(cinema.getId(), fyrq));
			List<MoviePlayItem> mpiList = mcpService.getCinemaMpiList(cinema.getId(), null,fyrq);
			int size = mpiList.size();
			playItemCount.put(cinema.getId(),size);
			if(size > 0){
				playItemTimeArea.put(cinema.getId(),mpiList.get(0).getPlaytime() + "~" + mpiList.get(size - 1).getPlaytime());
			}
			commentCountMap.put(cinema.getId(), commonCountMap.get(cinema.getId() + "cinema"));
			if(StringUtils.equals("popcorn", flag)){
				cinemaGoodsMap.put(cinema.getId(),BeanUtil.getSubList(goodsService.getGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH,
						cinema.getId(),true, true, true, "goodssort", true, false),0,2));//影院套餐
			}else if(StringUtils.equals("activity", flag)){
				ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, cinema.getId(), null,null,null, 0, 2);
				if(code.isSuccess()){
					List<RemoteActivity> activityList = code.getRetval();
					cinemaActivityMap.put(cinema.getId(), activityList);
				}
			}
		}
		model.put("playMovieCount",playMovieCount);
		model.put("playItemCount",playItemCount);
		model.put("playItemTimeArea", playItemTimeArea);
		model.put("commentCountMap", commentCountMap);
		model.put("cinemaList",cinemaList);
		model.put("cinemaGoodsMap", cinemaGoodsMap);
		model.put("cinemaActivityMap", cinemaActivityMap);
		model.put("flag",flag);
		return "movie/wide_index_hotcinema.vm";
	}
	/**
	 *电影首页热映电影，处理下一页热映电影的id 
	 * @param dataMap
	 * @param gcMovieList
	 * @param cityCode
	 */
	private void nextHotMovieIds(Map dataMap,List<GewaCommend> gcMovieList,String cityCode){
		List<Movie> movieList = mcpService.getCurMovieList(cityCode);
		mcpService.sortMoviesByMpiCount(cityCode, movieList);
		dataMap.put("overHotMovieCount", movieList.size() > 6 ? movieList.size() - 6 : 0);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		int gcMovieSize = gcMovieList.size();
		List<Movie> movies = new LinkedList<Movie>();
		List<Long> movieIds = null;
		if(gcMovieSize <= 6){
			movieIds = BeanUtil.getBeanPropertyList(movieList, Long.class,"id", true);
			for(GewaCommend gewaCommend : gcMovieList){
				Movie movie = daoService.getObject(Movie.class, gewaCommend.getRelatedid());
				markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
				movies.add(movie);
			}
			for(int index = 0 ;index < 6 - gcMovieSize;index++){
				Movie movie = movieList.get(index);
				markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
				movieIds.remove(movie.getId());
				movies.add(movie);
			}
		}else{
			movieIds = new LinkedList<Long>();
			for(int index = 0 ;index < gcMovieSize;index++){
				if(index >= 6){
					movieIds.add(gcMovieList.get(index).getRelatedid());
					Movie movie = daoService.getObject(Movie.class, gcMovieList.get(index).getRelatedid());
					movieList.remove(movie);
				}else{
					Movie movie = daoService.getObject(Movie.class, gcMovieList.get(index).getRelatedid());
					markCountMap.put(movie.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
					movies.add(movie);
					movieList.remove(movie);
				}
			}
			movieIds.addAll(BeanUtil.getBeanPropertyList(movieList, Long.class,"id", true));
		}
		dataMap.put("markCountMap", markCountMap);
		dataMap.put("movieIds", movieIds);
		dataMap.put("pageCount", movieIds.size()/6 + 1 + (movieIds.size()%6 > 0 ? 1 : 0));
		dataMap.put("movies", movies);
		dataMap.put("pageNo", 0);
	}
	
	private Map getHotMovie(RelatedHelper rh,String cityCode){
		Map dataMap = new HashMap();
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(cityCode, SignName.INDEX_MOVIELIST_NEW, null, null, true, 0, 8);
		commonService.initGewaCommendList("movieIndexList", rh, gcMovieList);
		nextHotMovieIds(dataMap, gcMovieList, cityCode);
		Map<String, Integer> commentCountMap = commonService.getCommentCount();
		dataMap.put("commentCountMap", commentCountMap);
		List<Long> opiMovieIdList = openPlayService.getOpiMovieidList(cityCode, null);
		dataMap.put("opiMovieList", opiMovieIdList);
		dataMap.put("curMovieListCount", opiMovieIdList.size());
		List<Movie> fetureMovieList = mcpService.getFutureMovieList(0, 200, null);
		for(Movie movie : fetureMovieList){
			if(opiMovieIdList.contains(movie.getId())){
				dataMap.put("fetureNew",true);
				break;
			}
		}
		dataMap.put("fetureMovieCount",fetureMovieList.size());
		dataMap.put("fetureMovieList",BeanUtil.getSubList(fetureMovieList, 0, 5));
		dataMap.put("videoCountMap", commonService.getVideoCount());
		dataMap.put("citycode", cityCode);
		return dataMap;
	}
	
	@RequestMapping("/movie/loadFetureMovieList.xhtml")
	public String loadFetureMovieList(ModelMap model,String movieIds,Integer pageNo, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			params.addInteger("pageNo", pageNo);
			params.addSingleString("movieIds", movieIds);
			PageView pageView = pageCacheService.getPageView(request, "movie/loadFetureMovieList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		List<Movie> movies = null;
		if(StringUtils.isBlank(movieIds)){
			List<Movie> movieList = mcpService.getFutureMovieList(0, 200, null);
			int size = movieList.size();
			model.put("pageCount", size/6  + (size%6 > 0 ? 1 : 0));
			movies = BeanUtil.getSubList(movieList, pageNo * 6, 6);
			List<Long> movieIdList = BeanUtil.getBeanPropertyList(movieList, Long.class,"id", true);
			model.put("movieIds",BeanUtil.getSubList(movieIdList,6,movieIdList.size() - 6));
		}else{
			movies = new LinkedList<Movie>();
			String[] mIds = StringUtils.split(movieIds, ",");
			for(String mId : mIds){
				if(ValidateUtil.isNumber(mId)){
					Movie movie = this.daoService.getObject(Movie.class,Long.parseLong(mId));
					movies.add(movie);
				}
			}
		}
		model.put("movieList",movies);
		model.put("pageNo",pageNo);
		model.put("opiMovieList", openPlayService.getOpiMovieidList(citycode, null));
		model.put("pictureCountMap",commonService.getPictureCount());
		model.put("videoCountMap", commonService.getVideoCount());
		model.put("movieIdList", new ArrayList<Long>());
		return "include/movie/ajax_wide_futureMovieIndex.vm";
	}
}
