package com.gewara.web.action.sport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.CommentCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.TimeItemHelper;
import com.gewara.json.PageView;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.common.Indexarea;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.content.News;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportProfile;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.OpenTimeSaleService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;


@Controller
public class SportIndexController extends BaseSportController {
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	@Autowired@Qualifier("openTimeSaleService")
	private OpenTimeSaleService openTimeSaleService;
	
	@RequestMapping("/sport/getConstSport.xhtml")
	public String getSportKey(String citycode,  ModelMap model, Long itemid) throws Exception{
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode , SignName.SPORT_ORDER, null, true, true,0, -1);
		// 20110505 加入判断(如果该城市没有区域, 生成固定JS)
		if(gcList.size() == 0){
			GewaCommend gewaCommend = new GewaCommend("");
			gewaCommend.setRelatedid(000000L);// 表示无区域
			gewaCommend.setTitle("近郊");
			gcList.add(gewaCommend);
		}
		List<Map> countyListMap = BeanUtil.getBeanMapList(gcList, "relatedid", "title", "ordernum");
		model.put("countyListMap", countyListMap);
		List<Long> idlist=sportService.getBookingSportIdList(itemid, citycode);
		List<Sport> sportList = daoService.getObjectList(Sport.class, idlist);
		Map<String, List<Sport>> sportMap = BeanUtil.groupBeanList(sportList, "countycode");
		model.put("sportMap", sportMap);
		model.put("itemid",itemid);
		return "sport/innerSoport.vm";
	}
	
	private static final String TAG_SPORT = "sport";
	// 首页上层的搜索框 + 热门搜索
	@RequestMapping("/sport/commonSearchData.xhtml")
	public String getSportCommonData(String countycode, String indexareacode, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Indexarea> indexarealist = placeService.getUsefulIndexareaList(countycode,TAG_SPORT);
		model.put("countycode", countycode);
		model.put("indexareacode", indexareacode);
		model.put("indexarealist", indexarealist);
		// 根据区号, 返回商圈列表 + 场馆列表
		List<Sport> sports = null;
		if(StringUtils.isNotBlank(indexareacode)) sports = placeService.getPlaceListByIndexareaCode(Sport.class, indexareacode, "clickedtimes", false, 0, 20);
		else if(StringUtils.isNotBlank(countycode)) sports = placeService.getPlaceListByCountyCode(Sport.class, countycode, "clickedtimes", false, 0, 20);
		else sports = placeService.getPlaceList(citycode, Sport.class, "clickedtimes", false, 0, 20);

		model.put("g_allCanbookingSports", sports);
		return "sport/searchMenu.vm";
	}
	
	// 根据场馆ID, 查询子项目
	@RequestMapping("/sport/searchsportitem.xhtml")
	public String getSearchKey(String skey, Long sportid, ModelMap model){
		List<SportItem> itemList = null;
		if(sportid == null){
			itemList = sportService.getSportlistLikeItemname(skey);
		}else{
			itemList = sportService.getSportItemListBySportId(sportid, SportProfile.STATUS_OPEN);
		}
		List<Map> itemMapList = BeanUtil.getBeanMapList(itemList, "id", "itemname");
		Map jsonMap = new HashMap();
		jsonMap.put("result", itemMapList);
		return showJsonSuccess(model, jsonMap);
	}
	
	//新版运动首页
	@RequestMapping("/sport/index.xhtml")
	public String index(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			PageView pageView = pageCacheService.getPageView(request, "sport/index.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//左侧导航
		this.setheadData(citycode, model);
		//套头
		List<GewaCommend> gcHeadList = commonService.getGewaCommendList(citycode, null, SignName.SPORT_HEADINFO, null, HeadInfo.TAG, true, true, 0, 1);
		HeadInfo headInfo = null;
		if(!gcHeadList.isEmpty()){
			headInfo = daoService.getObject(HeadInfo.class, gcHeadList.get(0).getRelatedid());
		}
		model.put("headInfo",headInfo);
		//运动首页推荐广告
		List<GewaCommend> infoList = commonService.getGewaCommendList(citycode, SignName.SPORTEINDEX_NEWS, null, null, true, 0, 10);
		model.put("infoList", infoList);
		//热门会员卡
		List<GewaCommend> gcMemberCardList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_MEMBERCARD, null, null, true, 0, 6);
		if(gcMemberCardList != null && !gcMemberCardList.isEmpty()){
			List<Long> mcIdList = BeanUtil.getBeanPropertyList(gcMemberCardList, Long.class, "relatedid", true);
			List<MemberCardType> memberCardList = daoService.getObjectList(MemberCardType.class, mcIdList);
			Map<Long, String> memberCardSportMap = new HashMap<Long, String>();
			for (MemberCardType mc : memberCardList) {
				String sportname =  memberCardService.getFitPlace(mc.getBelongVenue());
				memberCardSportMap.put(mc.getId(), sportname);
			}
			model.put("memberCardList", memberCardList);
			model.put("memberCardSportMap", memberCardSportMap);
		}
		//推荐项目
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_SPORTITEM, null, null, true, 0, 16);
		List<SportItem> itemList = new ArrayList<SportItem>();
		Map<Long, Integer> itemSportBookingMap = new HashMap<Long, Integer>();
		if(!gcList.isEmpty()){
			for(GewaCommend gc : gcList){
				SportItem sportItem = daoService.getObject(SportItem.class, gc.getRelatedid());
				if(sportItem != null){
					List<Long> sportIdList = openTimeTableService.getCurOttSportIdList(sportItem.getId(), citycode);
					Integer bookingCount = 0;
					if(sportIdList != null && !sportIdList.isEmpty()){
						bookingCount = sportIdList.size();
					} 
					itemSportBookingMap.put(gc.getRelatedid(), bookingCount);
					itemList.add(sportItem);
				}
			}
			GewaCommend itemGc = gcList.get(0);
			List<GewaCommend> sportGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_OPEN_SPORT, itemGc.getId(), null, true, 0, 2);
			Date playDate = DateUtil.addDay(DateUtil.getCurDate(), 1);
			Map<Long, GewaCommend> sportWalaMap = new HashMap<Long, GewaCommend>();
			Map<Long, List<String>> playHourListMap = new HashMap<Long, List<String>>();
			List<Sport> openSportList = new ArrayList<Sport>(); 
			Map<Long, Map> otiMap= new HashMap<Long, Map>();
			for (GewaCommend sportGc : sportGcList) {
				Sport sport = daoService.getObject(Sport.class, sportGc.getRelatedid());
				if(sport != null){
					openSportList.add(sport);
					List<OpenTimeTable> ottList = openTimeTableService.getOpenTimeTableList(sport.getId(), itemGc.getRelatedid(), playDate, null, 0, 1);
					if(!ottList.isEmpty()){
						OpenTimeTable ott = ottList.get(0);
						if(ott.hasField()){
							List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
							TimeItemHelper itemHelper = new TimeItemHelper(otiList);
							List<String> playHourList = itemHelper.getPlayHourList();
							Collections.sort(playHourList);
							playHourListMap.put(sport.getId(), playHourList);
							otiMap.put(sport.getId(), BeanUtil.beanListToMap(otiList, "hour"));
						}
					}
					//测评
					List<GewaCommend> cepingList = commonService.getGewaCommendList(citycode, SignName.SPORT_DETAIL_CEPING, sport.getId(), null, false, 0, 1);
					if(!cepingList.isEmpty()) {
						sportWalaMap.put(sport.getId(), cepingList.get(0));
					}
					
				}
			}
			model.put("openSportList", openSportList);
			model.put("playHourListMap", playHourListMap);
			model.put("otiMap", otiMap);
			model.put("sportWalaMap", sportWalaMap);
		}
		model.put("itemList", itemList);
		model.put("itemSportBookingMap", itemSportBookingMap);
		//视频秀
		List<GewaCommend> videoList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, SignName.SPORTINDEX_VIDEO, null, null, true, 0, 4);
		model.put("videoList", videoList);
		//场馆测评
		List<GewaCommend> cepingGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_CEPING, null, null, true, 0, 5);
		Map<Long, News> cepingNewsMap = new HashMap<Long, News>();
		Map<Long, Integer> newsCountMap = new HashMap<Long, Integer>();
		for(GewaCommend gc : cepingGcList){
			News news = daoService.getObject(News.class, gc.getRelatedid());
			if(news != null){
				Integer commentCount = commentService.getCommentCountByRelatedId("news", gc.getRelatedid());
				newsCountMap.put(gc.getRelatedid(), commentCount);
				cepingNewsMap.put(news.getId(), news);
			}
		}
		model.put("cepingList", cepingGcList);
		model.put("newsCountMap", newsCountMap);
		model.put("cepingNewsMap", cepingNewsMap);
		//运动圈子
		List<GewaCommend> sportCommuGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_SPORTCOMMU, null, null, true, 0, 5);
		List<Long> commuIdList = BeanUtil.getBeanPropertyList(sportCommuGcList, Long.class, "relatedid", true);
		List<Commu> sportCommuList = daoService.getObjectList(Commu.class, commuIdList);
		model.put("sportCommuList", sportCommuList);
		//热门活动
		List<GewaCommend> sportactivityList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_SPORTACTIVITY, null, null, true, 0, 6);
		if(sportactivityList != null && !sportactivityList.isEmpty()){
			List<Long> activityIdList = BeanUtil.getBeanPropertyList(sportactivityList, Long.class, "relatedid", true);
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityListByIds(activityIdList);
			if(code.isSuccess() && code.getRetval() != null){
				model.put("sportActivityList", code.getRetval());
			}
		}
		//瓦友精彩哇啦
		List<GewaCommend> commentPicGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_WALA_PICTURE, null, null, true, 0, 1);
		if(!commentPicGcList.isEmpty()){
			model.put("walaPicture", commentPicGcList.get(0));
		}
		//优惠活动
		List<GewaCommend> adList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_AD, null, null, true, 0, 6);
		model.put("adList", adList);
		//热门培训课程
		List<GewaCommend> traningGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_TRAINING, null, null, true, 0, 4);
		List<TrainingGoods> trainingList = new ArrayList<TrainingGoods>();
		List<DramaToStar> tempList = new ArrayList<DramaToStar>();
		Map<Long,List<DramaToStar>> tcDtsListMap = new HashMap<Long,List<DramaToStar>>();
		Map<Long, Agency> agencyMap = new HashMap<Long, Agency>();
		for(GewaCommend gc : traningGcList){
			TrainingGoods training = daoService.getObject(TrainingGoods.class, gc.getRelatedid());
			if(training != null){
				trainingList.add(training);
				//教练
				List<DramaToStar> tcDtsList = dramaToStarService.getDramaToStarListByDramaid(GoodsConstant.GOODS_TYPE_TRAINING, training.getId(), false);
				tcDtsListMap.put(training.getId(), tcDtsList);
				tempList.addAll(tcDtsList);
				//机构
				Agency agency = daoService.getObject(Agency.class, training.getRelatedid());
				agencyMap.put(agency.getId(), agency);
			}
		}
		List<Long> starIdList = BeanUtil.getBeanPropertyList(tempList, Long.class, "starid", true);
		Map<Long,DramaStar> starMap = daoService.getObjectMap(DramaStar.class, starIdList);
		model.put("trainingList", trainingList);
		model.put("trainingStarMap", tcDtsListMap);
		model.put("starMap", starMap);
		model.put("agencyMap", agencyMap);
		//运动资讯体育新闻
		List<GewaCommend> sportNewsGcList = commonService.getGewaCommendList(citycode, SignName.NEWS_INDEXSPORT, null, null, true, 0, 5);
		List<Long> newsIdList = BeanUtil.getBeanPropertyList(sportNewsGcList, Long.class, "relatedid", true);
		Map sportNewsMap = daoService.getObjectMap(News.class, newsIdList);
		model.put("sportNewsMap", sportNewsMap);
		model.put("sportNewsGcList", sportNewsGcList);
		//热门场馆
		List<GewaCommend> sportGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_SPORT, null, null, true, 0, 5);
		List<Sport> sportList = new ArrayList<Sport>();
		Map<Long, Integer> sportOtiCountMap = new HashMap<Long, Integer>(); 
		Map<Long, SportItem> siMap = new HashMap<Long, SportItem>();
		for (GewaCommend gc : sportGcList) {
			Sport sport = daoService.getObject(Sport.class, gc.getRelatedid());
			if(sport != null){
				List<GewaCommend> sportitemGcList = commonService.getGewaCommendList(citycode, SignName.SPORTINDEX_SPORT_SPORTSERVICE, gc.getId(), null, true, 0, 1);
				if(sportitemGcList != null && !sportitemGcList.isEmpty()){
					Long itemid = Long.parseLong(sportitemGcList.get(0).getRelatedid()+"");
					SportItem sItem = daoService.getObject(SportItem.class, itemid);
					if(sItem != null){
						List<Map> dataList = openTimeTableService.getOpenTimeCountByItemid(sItem.getId(), sport.getId());
						if(dataList != null && !dataList.isEmpty()){
							sportOtiCountMap.put(sport.getId(), Integer.parseInt(dataList.get(0).get("remain")+""));
							siMap.put(sport.getId(), sItem);
						}
					}
				}
				sportList.add(sport);
			}
		}
		model.put("siMap", siMap);
		model.put("sportOtiCountMap", sportOtiCountMap);
		model.put("sportList", sportList);
		return "sport/wide_index.vm";
	}
	
	//竞价场地
	@RequestMapping("/sport/ajax/getOpenSale.xhtml")
	public String getOpenSale(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<OpenTimeSale> otiList = openTimeSaleService.getOpenOtsList(citycode, null, false, 0, 1);
		if(!otiList.isEmpty()){
			OpenTimeSale ots = otiList.get(0);
			if(ots != null){
				OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ots.getOttid());
				if(ott != null && ott.isBooking()){
					model.put("otsSport", daoService.getObject(Sport.class, ots.getSportid()));
					model.put("otsSportitem", daoService.getObject(SportItem.class, ots.getItemid()));
					model.put("opentimesale", ots);
				}
			}
		}
		return "sport/wide_jingpai.vm";
	}
	//哇啦
	@RequestMapping("/sport/ajax/getIndexWalaList.xhtml")
	public String getIndexWalaList(String title, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			params.addSingleString("title", title);
			PageView pageView = pageCacheService.getPageView(request, "sport/ajax/getIndexWalaList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		List<Comment> commentList = new ArrayList<Comment>();
		commentList.addAll(commentService.searchCommentList(title, CommentCommand.TYPE_MODERATOR, 0, 7));
		if(commentList.size() < 7){
			commentList.addAll(commentService.getCommentListByRelatedId(TagConstant.TAG_SPORT, null, null, null, 0, 7-commentList.size()));
		}
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		return "sport/wide_indexWala.vm";
	}
}
