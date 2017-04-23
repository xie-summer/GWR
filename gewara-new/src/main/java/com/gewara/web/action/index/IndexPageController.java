package com.gewara.web.action.index;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.BaseObject;
import com.gewara.model.content.Advertising;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.content.News;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Festival;
import com.gewara.service.bbs.CommonPartService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.AdService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.UntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class IndexPageController extends AnnotationController{
	private static final String GEWA_INDEX_CHANGE_FLAG = "newidx";
	private static final String NEWIDX = "old";
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
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
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("sportService")
	protected SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService) {
		this.dramaPlayItemService = dramaPlayItemService;
	}
	@Autowired@Qualifier("commonPartService")
	private CommonPartService commonPartService;
	public void setCommonPartService(CommonPartService commonPartService) {
		this.commonPartService = commonPartService;
	}
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	public void setOpenTimeTableService(OpenTimeTableService openTimeTableService) {
		this.openTimeTableService = openTimeTableService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}

	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	
	@Autowired@Qualifier("adService")
	private AdService adService;
	
	@RequestMapping("/home/index.xhtml")
	public String homeIndex(ModelMap model, @CookieValue(required=false,value=GEWA_INDEX_CHANGE_FLAG) String flag,
			HttpServletRequest request, HttpServletResponse response) {
		return index(model, flag, request, response);
	}
	@RequestMapping("/changeCity.xhtml")
	public String index(String cityname, String path, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		String citycode = AdminCityContant.getCodeByPinyin(cityname);
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		WebUtils.setCitycode(request, citycode, response);
		if(StringUtils.isNotBlank(path)){
			return showRedirect(path, model);
		}
		return "redirect:/index.xhtml";
	}
	@RequestMapping("/defaultCity.xhtml")
	public String defaultCity(HttpServletRequest request, HttpServletResponse response, String rd, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		model.put("mycity", citycode);
		return showRedirect(rd, model);
	}
	@RequestMapping("/changeNew.xhtml")
	public String changeNew(HttpServletResponse response, String flag, ModelMap model){
		if(StringUtils.equals(flag, "old")){
			WebUtils.addCookie(response, GEWA_INDEX_CHANGE_FLAG, NEWIDX, "/", 60 * 60 * 24);
		}else{
			WebUtils.clearCookie(response, "/", GEWA_INDEX_CHANGE_FLAG);
		}
		return showRedirect("/index.xhtml", model);
	}
	
	public String newIndex(ModelMap model,String citycode){
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.putAll(getHeadData(citycode,SignName.INDEX_HEADINFO_NEW));//头部信息
		headBanner(model,citycode,SignName.INDEX_BANNER_NEWS);
		model.put("gcDiscountList", commonService.getGewaCommendList(citycode, null, SignName.INDEX_DISCOUNT_NEW, null, null, true, true, 0, 3));
		//推荐购票影片
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIELIST_NEW, null, null, true, 0, 8);
		commonService.initGewaCommendList("gcMovieList", rh, gcMovieList);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		List<Long> movieIdList = new ArrayList<Long>();
		for(GewaCommend gewaCommend :gcMovieList) {
			movieIdList.add(gewaCommend.getRelatedid());
			markCountMap.put(gewaCommend.getRelatedid(), markService.getMarkCountByTagRelatedid(gewaCommend.getTag(), gewaCommend.getRelatedid()));
		}
		model.put("gcMovieList", gcMovieList);
		model.put("movieIdList", movieIdList);
		model.put("markCountMap", markCountMap);
		model.put("openMovieList", mcpService.getOpenMovieList(citycode));//当前购票的影片列表
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		//新片推荐
		getFutureMovieInfo(model, rh, citycode);
		model.put("activityList",hotRecommendArea(citycode, SignName.INDEX_MOVIEACTIVITY_NEW, 4, rh,"activityList",true));
		List<GewaCommend> picMovieChosenList = this.chosenArea(model, "movieChosenList", citycode,SignName.INDEX_MOVIE_CHOSEN, 10, 5, 15);
		model.put("picMovieChosenList", picMovieChosenList);
		model.put("picChosePage",picMovieChosenList.size()/2 + (picMovieChosenList.size() == 0 ? 1 : 0) + (picMovieChosenList.size()%2 > 0 ? 1 : 0));
		
		//推荐购票剧目
		List<GewaCommend> gcDramaList = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMALIST_NEW, null, null, true, 0, 8);
		commonService.initGewaCommendList("gcDramaList", rh, gcDramaList);
		model.put("gcDramaList", gcDramaList);
		List<GewaCommend> featureDramaList = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMAAREA_NEW, null, null, true, 0, 5);
		commonService.initGewaCommendList("featureDramaList", rh, featureDramaList);
		model.put("featureDramaList",featureDramaList);
		Map<Long, List<Integer>> dramaPriceMap = new HashMap<Long, List<Integer>>();
		if(featureDramaList != null ) {
			for (GewaCommend gewaCommend : featureDramaList) {
				Drama drama = daoService.getObject(Drama.class, gewaCommend.getRelatedid());
				if(drama != null) {
					boolean isBooking = dramaPlayItemService.isBookingByDramaId(drama.getId());
					dramaPriceMap.put(drama.getId(), dramaPlayItemService.getPriceList(null, gewaCommend.getRelatedid(), new Timestamp(System.currentTimeMillis()), null, isBooking));
				}
			}
		}
		model.put("dramaPriceMap", dramaPriceMap);
		model.put("dramaActivityList",hotRecommendArea(citycode, SignName.INDEX_DRAMA_HOTACTIVITY, 4, rh,"dramaActivityList",false));
		List<GewaCommend> picDramaChosenList = this.chosenArea(model, "dramaChosenList", citycode,SignName.INDEX_DRAMA_CHOSEN, 2, 5, 7);
		model.put("picDramaChosenList", picDramaChosenList);
		List<GewaCommend> gcDramaDiary = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMADIARY_NEW, null, null, true, 0, 2);
		commonService.initGewaCommendList("gcDramaDiary", rh, gcDramaDiary);
		List<BaseObject> dList = rh.getGroupIndexList("gcDramaDiary", 1);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(dList));
		Map<Serializable, String> categoryMap = BeanUtil.getKeyValuePairMap(dList, "categoryid", "category");
		relateService.addRelatedObject(1, "categoryMap", rh, categoryMap);
		model.put("gcDramaDiary", gcDramaDiary);
		//运动区域
		model.put("sportNotice",commonService.getGewaCommendList(citycode, SignName.INDEX_SPORT_NOTICE, null, null, true, 0, 1));
		List<GewaCommend> gcSportList = commonService.getGewaCommendList(citycode, SignName.INDEX_NEW_SPORTLIST_NEW, null, null, true, 0, 8);
		commonService.initGewaCommendList("gcSportList", rh, gcSportList);
		model.put("gcSportList", gcSportList);
		Map<Long, Integer> openSportMap = new HashMap<Long, Integer>();
		for(GewaCommend gewa : gcSportList){
			List<Long>	sportIdList = openTimeTableService.getCurOttSportIdList(gewa.getRelatedid(), citycode);
			openSportMap.put(gewa.getRelatedid(), sportIdList.size());
		}
		model.put("openSportMap", openSportMap);
		List<GewaCommend> gcSportVenuesList = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORT_VENUES, null, null, true, 0,5);
		commonService.initGewaCommendList("gcSportVenuesList", rh, gcSportVenuesList);
		model.put("gcSportVenuesList", gcSportVenuesList);
		Map<Long, List<SportItem>> sportItemMap = new HashMap<Long, List<SportItem>>();
		Map<Long,Map<String,Integer>> priceMap = new HashMap();
		for (GewaCommend gc : gcSportVenuesList) {
			Map<String,Integer> priceList=sportService.getSportPrice(gc.getRelatedid(), null);
			priceMap.put(gc.getId(), priceList);
			List<SportItem> itemList = sportService.getSportItemListBySportId(gc.getRelatedid(), SportProfile.STATUS_OPEN);
			if(itemList != null) sportItemMap.put(gc.getId(), BeanUtil.getSubList(itemList, 0, 3));
		}
		model.put("sportActivityList",hotRecommendArea(citycode, SignName.INDEX_SPORTACTIVITY_NEW, 4, rh,"sportActivityList",false));
		List<GewaCommend> gcSportAreaList = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORTAREA_NEW, null, null, true, 0, 5);
		commonService.initGewaCommendList("gcSportAreaList", rh, gcSportAreaList);
		Map<Long,Sport> sportMap = new HashMap<Long,Sport>();
		for (GewaCommend gewaCommend : gcSportAreaList) {
			News news = daoService.getObject(News.class, gewaCommend.getRelatedid());
			if(news != null && news.getRelatedid()!=null && StringUtils.equals(news.getTag(), TagConstant.TAG_SPORT)) {
				sportMap.put(gewaCommend.getId(), daoService.getObject(Sport.class, news.getRelatedid()));
			}
		}
		model.put("gcSportAreaList", gcSportAreaList);
		model.put("sportMap",sportMap);
		
		model.put("priceMap", priceMap);
		model.put("sportItemMap",sportItemMap);
		//model.put("cinemaCount", openPlayService.getOpiCinemaidList(citycode,null).size());
		//Integer sportbookingcount = openTimeTableService.getbookingSportCount(DateUtil.parseDate(DateUtil.getCurDateStr(), "yyyy-MM-dd"), citycode);
		//model.put("sportCount", sportbookingcount);
		//model.put("dramaCount", dramaService.getDramaListCount(citycode, "1", null, "clickedtimes", null, null));
		//List<Long> theatreidList=dramaPlayItemService.getTheatreidList(citycode, null, true);
	//	model.put("theatrePlaceCount",theatreidList.size());
		//model.put("mpiMovieCount", mcpService.getCurMovieList(citycode).size());
		if(model.get("headInfo") == null){
			List<Advertising> ads = adService.getAdListByPid(citycode, "index_headinfo");
			if(!VmUtils.isEmptyList(ads)){
				model.put("headinfo_ad", ads.get(0));
			}
		}
		return "index/index_new.vm";
	}
	//首页
	@RequestMapping("/index.xhtml")
	public String index(ModelMap model, @CookieValue(required=false,value=GEWA_INDEX_CHANGE_FLAG) String flag, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(!StringUtils.equals(citycode, "310000")) {
			return "forward:/movie/city/index.xhtml";
		}
		
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams();
			if(StringUtils.equals(NEWIDX, flag)){
				params.addCookie(GEWA_INDEX_CHANGE_FLAG, "/", NEWIDX);
			}
			PageView pageView = pageCacheService.getPageView(request, "index.xhtml", params, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.putAll(getWebsiteNotice(citycode));//网站公告
		if(StringUtils.isBlank(flag)){
			return newIndex(model,citycode);
		}
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		model.putAll(getHeadData(citycode,SignName.INDEX_HEADINFO));//头部信息
		model.putAll(getMDSList(rh, citycode));
		model.putAll(getMovieArea(rh, citycode, model));
		model.putAll(getDramaArea(rh, citycode, model));
		model.putAll(getSportArea(rh, citycode));
		model.putAll(getCommunity(citycode, model));
		model.putAll(getBottomPic(citycode));
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		return "index/index.vm";
	}
	
	private Map getMDSList(RelatedHelper rh, String citycode) {
		Map model = new HashMap();
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIELIST, null, null, true, 0, 13);
		List<GewaCommend> gcDramaList = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMALIST, null, null, true, 0, 13);
		List<GewaCommend> newgcSportList = commonService.getGewaCommendList(citycode, SignName.INDEX_NEW_SPORTLIST, null, null, true, 0, 13);
		List<GewaCommend> gcMobile = commonService.getGewaCommendList(citycode, SignName.INDEX_MOBILE, null, null, true, 0, 10);
		List<GewaCommend> gcDiscountList = commonService.getGewaCommendList(citycode, null, SignName.INDEX_DISCOUNT, null, null, true, true, 0, 5);
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		Map<Long, String> editiomMap = new HashMap();
		commonService.initGewaCommendList("gcMovieList", rh, gcMovieList);
		for(GewaCommend gewaCommend :gcMovieList) {
			markCountMap.put(gewaCommend.getRelatedid(), markService.getMarkCountByTagRelatedid(gewaCommend.getTag(), gewaCommend.getRelatedid()));
		}
		commonService.initGewaCommendList("gcDramaList", rh, gcDramaList);
		commonService.initGewaCommendList("newgcSportList", rh, newgcSportList);
		Map<Long, Integer> openSportMap = new HashMap<Long, Integer>();
		for(GewaCommend gewa : newgcSportList){
			List<Long>	sportIdList = openTimeTableService.getCurOttSportIdList(gewa.getRelatedid(), citycode);
			openSportMap.put(gewa.getRelatedid(), sportIdList.size());
		}
		model.put("openSportMap", openSportMap);
		Integer cinemaCount = mcpService.getTicketCinemaCount(citycode, null, null, null);
		model.put("gcMovieList", gcMovieList);
		model.put("editiomMap", editiomMap);
		model.put("markCountMap", markCountMap);
		model.put("gcDramaList", gcDramaList);
		Map<Long, List<Integer>> dramaPriceMap = new HashMap<Long, List<Integer>>();
		if(gcDramaList != null ) {
			for (GewaCommend gewaCommend : gcDramaList) {
				Drama drama = daoService.getObject(Drama.class, gewaCommend.getRelatedid());
				if(drama != null) {
					boolean isBooking = dramaPlayItemService.isBookingByDramaId(drama.getId());
					dramaPriceMap.put(drama.getId(), dramaPlayItemService.getPriceList(null, gewaCommend.getRelatedid(), new Timestamp(System.currentTimeMillis()), null, isBooking));
				}
			}
		}
		//可以预定场馆的数量
		Integer sportbookingcount=openTimeTableService.getbookingSportCount(DateUtil.parseDate(DateUtil.getCurDateStr(), "yyyy-MM-dd"), citycode);
		Integer sportgoodscount = goodsService.getSportGoodsCount();
		model.put("dramaPriceMap", dramaPriceMap);
		model.put("newgcSportList", newgcSportList);
		model.put("cinemaCount", cinemaCount);
		model.put("dramaCount", dramaPlayItemService.getDramaOpenCount());
		model.put("sportCount", sportbookingcount + sportgoodscount);
		model.put("gcMobile", gcMobile);
		model.put("gcDiscountList", gcDiscountList);
		return model;
	}
	
	private Map getMovieArea(RelatedHelper rh, String citycode, ModelMap model) {
		Map dataMap = new HashMap();
		List<GewaCommend> gcMovieArea = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIEAREA, null, null, true, 0, 1);
		List<GewaCommend> gcMovieNewsSubj = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIESUBJECT, null, null, true, 0, 2);
		List<GewaCommend> gcNewsList = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIENEWS, null, null, true, 0, 2);
		List<GewaCommend> gcMovieWeek = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIEWEEK, null, null, true, 0, 1);
		List<GewaCommend> gcMovieDiary = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIEDIARY, null, null, true, 0, 2);
		List<GewaCommend> gcMovieAct = commonService.getGewaCommendList(citycode,  null, SignName.INDEX_MOVIEACTIVITY, null, null, true, true, 0, 13);
		List<GewaCommend> gcMovieMem = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIEMEMBER, null, null, true, 0, 1);
		commonService.initGewaCommendList("gcMovieDiary", rh, gcMovieDiary);
		commonService.initGewaCommendList("gcMovieAct", rh, gcMovieAct);
		commonService.initGewaCommendList("gcMovieMem", rh, gcMovieMem);
		
		List diaryList = rh.getGroupIndexList("gcMovieDiary", 1);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(diaryList);
		addCacheMember(model, memberidList);
		Map<Serializable, String> categoryMap = BeanUtil.getKeyValuePairMap(diaryList, "categoryid", "category");
		relateService.addRelatedObject(1, "categoryMap", rh, categoryMap);
		
		dataMap.put("gcMovieArea", gcMovieArea);
		dataMap.put("gcMovieNewsSubj", gcMovieNewsSubj);
		dataMap.put("gcMovieNews", gcNewsList);
		dataMap.put("gcMovieWeek", gcMovieWeek);
		dataMap.put("gcMovieDiary", gcMovieDiary);
		dataMap.put("gcMovieAct", gcMovieAct);
		dataMap.put("gcMovieMem", gcMovieMem);
		return dataMap;
	}
	
	private Map getDramaArea(RelatedHelper rh, String citycode, ModelMap model) {
		Map dataMap = new HashMap();
		List<GewaCommend> gcDramaArea = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMAAREA, null, null, true, 0, 5);
		List<GewaCommend> gcDramaNewsSubj = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMASUBJECT, null, null, true, 0, 1);
		List<GewaCommend> gcDramaNews = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMANEWS, null, null, true, 0, 2);
		List<GewaCommend> gcDramaDiary = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMADIARY, null, null, true, 0, 2);
		List<GewaCommend> gcDramaAct = commonService.getGewaCommendList(citycode, null, SignName.INDEX_DRAMAACTIVITY, null, null, true, true, 0, 13);
		List<GewaCommend> gcDramaMem = commonService.getGewaCommendList(citycode, SignName.INDEX_DRAMAMEMBER, null, null, true, 0, 1);
		commonService.initGewaCommendList("gcDramaArea", rh, gcDramaArea);
		Map<String,Object> dramaMap = rh.getGroupMap("gcDramaArea");
		if(!CollectionUtils.isEmpty(dramaMap)){
			Map<Long,List<DramaToStar>> starMap = new HashMap<Long, List<DramaToStar>>();
			model.put("starMap", starMap);
			for (Object object : dramaMap.values()) {
				String actors = (String) BeanUtil.get(object, "actors");
				if(StringUtils.isNotBlank(actors)){
					List<DramaToStar> starList = daoService.getObjectList(DramaToStar.class, BeanUtil.getIdList(actors, ","));
					starMap.put((Long)BeanUtil.get(object, "id"), starList);
				}
			}
		}
		commonService.initGewaCommendList("gcDramaDiary", rh, gcDramaDiary);
		commonService.initGewaCommendList("gcDramaAct", rh, gcDramaAct);
		commonService.initGewaCommendList("gcDramaMem", rh, gcDramaMem);
		List diaryList = rh.getGroupIndexList("gcDramaDiary", 1);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(diaryList);
		addCacheMember(model, memberidList);
		Map<Serializable, String> categoryMap = BeanUtil.getKeyValuePairMap(diaryList, "categoryid", "category");
		relateService.addRelatedObject(1, "categoryMap", rh, categoryMap);
		dataMap.put("gcDramaArea", gcDramaArea);
		dataMap.put("gcDramaNewsSubj", gcDramaNewsSubj);
		dataMap.put("gcDramaNews", gcDramaNews);
		dataMap.put("gcDramaDiary", gcDramaDiary);
		dataMap.put("gcDramaAct", gcDramaAct);
		dataMap.put("gcDramaMem", gcDramaMem);
		return dataMap;
	}
	
	private Map getSportArea(RelatedHelper rh, String citycode) {
		Map dataMap = new HashMap();
		List<GewaCommend> gcSportArea = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORTAREA, null, null, true, 0, 5);
		List<GewaCommend> gcSportSearch = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORT_SEARCH, null, null, true, 0, 5);
		List<GewaCommend> gcSportOpi = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORTOPI, null, null, true, 0, 5);
		List<GewaCommend> gcItemInfo = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORTPLACE, null, null, true, 0, 1);
		List<GewaCommend> gcSportAct = commonService.getGewaCommendList(citycode, null, SignName.INDEX_SPORTACTIVITY, null, null, true, true, 0, 13);
		List<GewaCommend> gcSportMem = commonService.getGewaCommendList(citycode, SignName.INDEX_SPORTMEMBER, null, null, true, 0, 1);
		commonService.initGewaCommendList("gcSportArea", rh, gcSportArea);
		commonService.initGewaCommendList("gcSportOpi", rh, gcSportOpi);
		commonService.initGewaCommendList("gcItemInfo", rh, gcItemInfo);
		commonService.initGewaCommendList("gcSportAct", rh, gcSportAct);
		commonService.initGewaCommendList("gcSportMem", rh, gcSportMem);
		Map<Long, List<SportItem>> itemMap = new HashMap();
		Map<Long, Sport> sportMap = new HashMap();
		//Map<Long,SportPriceTable> priceTableMap = new HashMap();
		Map<Long,Map<String,Integer>> priceMap = new HashMap();
		Map<Long, SportItem> sportItemMap = new HashMap<Long, SportItem>();
		for (GewaCommend gewaCommend : gcSportArea) {
			News news = daoService.getObject(News.class, gewaCommend.getRelatedid());
			if(news != null && news.getRelatedid()!=null && StringUtils.equals(news.getTag(), TagConstant.TAG_SPORT)) {
				sportMap.put(gewaCommend.getId(), daoService.getObject(Sport.class, news.getRelatedid()));
				itemMap.put(gewaCommend.getId(), sportService.getSportItemListBySportId(news.getRelatedid(), SportProfile.STATUS_OPEN));
			}
		}
		for (GewaCommend gc : gcSportOpi) {
			/*SportPriceTable priceTable = sportService.getSportPriceTable(gc.getRelatedid(), gc.getParentid());
			if(priceTable != null){
				priceTableMap.put(gc.getId(), priceTable);
			}*/
			Map<String,Integer> priceList=sportService.getSportPrice(gc.getRelatedid(), gc.getParentid());
			priceMap.put(gc.getId(), priceList);
			SportItem sportItem = daoService.getObject(SportItem.class, gc.getParentid());
			if(sportItem != null) sportItemMap.put(gc.getId(), sportItem);
		}
		dataMap.put("sportMap", sportMap);
		dataMap.put("sportitemMap", itemMap);
		//dataMap.put("priceTableMap", priceTableMap);
		dataMap.put("priceMap", priceMap);
		dataMap.put("gcSportArea", gcSportArea);
		dataMap.put("gcSportSearch", gcSportSearch);
		dataMap.put("gcSportOpi", gcSportOpi);
		dataMap.put("gcItemInfo", gcItemInfo);
		dataMap.put("sportItemMap", sportItemMap);
		Map<String, Integer> sportItemOpenCount = commonService.getSportItemSportCount();
		dataMap.put("sportItemOpenCount", sportItemOpenCount);
		Map<String, Integer> indexMovieActivityCountMap = commonService.getActivityCount();
		dataMap.put("indexMovieActivityCountMap", indexMovieActivityCountMap);
		Map<String, Integer> sportCommuCount = commonService.getCommuCount();
		dataMap.put("sportCommuCount", sportCommuCount);
		Map<String, Integer> sportNewsCount = commonService.getNewsCount();
		dataMap.put("sportNewsCount", sportNewsCount);
		
		Map<String, Integer> sportdiaryCount = commonService.getDiaryCount();
		dataMap.put("sportdiaryCount", sportdiaryCount);
		dataMap.put("gcSportAct", gcSportAct);
		dataMap.put("gcSportMem", gcSportMem);
		
		return dataMap;
	}
	private Map getCommunity(String citycode, ModelMap model) {
		Map dataMap = new HashMap();
		List<GewaCommend> gcCommunity = commonService.getGewaCommendList(citycode , SignName.INDEX_COMMUNITY, null, null, true, 0, 10);
		//多少人临红包、看电影等等
		dataMap.put("dataMap",commonService.getCurIndexDataSheet());
		//List<Comment> commentList = commentService.getCommentListByTag(null, 0, 10);
		List<Comment> commentList = commentService.getCommentListByTag("topic", 0, 10);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		dataMap.put("gcCommunity", gcCommunity);
		dataMap.put("commentList", commentList);
		Festival festival = commonPartService.getCurFestival(DateUtil.getCurDate());
		model.put("festival", festival);
		return dataMap;
	}
	
	private Map getBottomPic(String citycode) {
		Map model = new HashMap();
		List<GewaCommend> gcPictureList = commonService.getGewaCommendList(citycode , SignName.INDEX_BOTTOMPIC, null, null, true, 0, 3);
		model.put("gcPictureList", gcPictureList);
		return model;
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
	//头部数据
	private Map getHeadData(String citycode,String signName) {
		Map model = new HashMap();
		List<GewaCommend> gcHeadList = commonService.getGewaCommendList(citycode, null,signName, null, HeadInfo.TAG, true, true,true, 0, 1);
		HeadInfo headInfo = null;
		if(!gcHeadList.isEmpty()){
			headInfo =daoService.getObject(HeadInfo.class, gcHeadList.get(0).getRelatedid());
			model.put("headInfo",headInfo);
		}
		return model;
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
	
	//新版首页热门活动 或 热门推荐区域
	private List<GewaCommend> hotRecommendArea(String citycode,String signName,int maxnum,RelatedHelper rh,String group,boolean isActivity){
		List<GewaCommend> activityList = commonService.getGewaCommendList(citycode,null,signName, null,null,true,isActivity, 0, maxnum);
		commonService.initGewaCommendList(group, rh, activityList);
		return activityList;
	}
	//电影精选 话剧精选部分
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
	
	private static List<String> indexKeys = Arrays.asList("ticketCinemaCount","hotMovieCount","ticketMovieCount","futureMovieCount",
			"movieActivityCount","ticketDramaCount","ticketDramaPlaceCount","ticketSportCount");
	
	
	@RequestMapping("/ajax/loadIndexKeyNumber.xhtml")
	public String getIndexKeyNumber(String keys,HttpServletRequest request, HttpServletResponse response,ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		Map<String,Integer> jsonMap = new HashMap<String,Integer>();
		if(StringUtils.isNotBlank(keys)){
			Map markData = markService.getMarkdata(TagConstant.TAG_MOVIE);
			String[] keyArray = StringUtils.split(keys, ",");
			for(String key : keyArray){
				if(StringUtils.startsWith(key, "movieDetail_")){
					String[] values = StringUtils.split(key, "_");
					if(values.length == 2){
						String[] movieIds = StringUtils.split(values[1], "@");
						for(String id : movieIds){
							Movie movie = this.daoService.getObject(Movie.class, Long.parseLong(id));
							if(movie!=null){
								jsonMap.put("mark_" + movie.getId(),VmUtils.getLastMarkStar(movie, "general", markService.getMarkCountByTagRelatedid("movie", movie.getId()), markData));
								jsonMap.put("boughtcount_" + movie.getId(), movie.getBoughtcount() == null ? 0 : movie.getBoughtcount());
								jsonMap.put("xiangqu_" + movie.getId(), movie.getXiangqu() == null ? 0 : movie.getXiangqu());
								jsonMap.put("clickedtimes_" + movie.getId(), movie.getClickedtimes() == null ? 0 : movie.getClickedtimes());
								jsonMap.put("collectedtimes_" + movie.getId(), movie.getCollectedtimes() == null ? 0 : movie.getCollectedtimes());
							}
						}
					}
					continue;
				}
				if(!indexKeys.contains(key)){
					continue;
				}
				Integer count = untransService.getIndexKeyNumber(key,citycode);
				jsonMap.put(key, count);
			}
		}
		return this.showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/fromWap.xhtml")
	public String fromWap(String from, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		WebUtils.addCookie(response, "forceweb", "Y", "/", 12*60*60);
		model.putAll(request.getParameterMap());
		model.remove("from");
		if(StringUtils.indexOf(from, "?")>=0){
			from += "&forceweb=" + System.currentTimeMillis();
		}else{
			from += "?forceweb=" + System.currentTimeMillis();
		}
		return showRedirect(from, model);
	}
	
}
