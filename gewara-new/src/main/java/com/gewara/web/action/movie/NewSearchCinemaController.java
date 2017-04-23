package com.gewara.web.action.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchCinemaCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CharacteristicType;
import com.gewara.constant.Flag;
import com.gewara.constant.PayConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.common.Subwayline;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.mongo.MongoService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.action.partner.ObjectSpdiscountFilter;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
@Controller
public class NewSearchCinemaController extends AnnotationController {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;

	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	
	
	private static Map<String, Order> orderMap = new HashMap<String, Order>();
	static{
		orderMap.put("name", Order.asc("name"));
		orderMap.put("clickedtimes", Order.desc("clickedtimes"));
		orderMap.put("generalmark", Order.desc("avggeneral"));
		orderMap.put("environmentmark", Order.desc("avgenvironment"));
		orderMap.put("servicemark", Order.desc("avgservice"));
		orderMap.put("pricemark", Order.desc("avgprice"));
		orderMap.put("audiomark", Order.desc("avgaudio"));
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
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
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	//@RequestMapping("/movie/searchCinema.xhtml")
	/*public String newCinemaList11(SearchCinemaCommand cmd, ModelMap model,
			HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		int pageNo = cmd.getPageNo();
		PageParams pparams = new PageParams();
		pparams.addSingleString("cinemaname",  cmd.getCinemaname());
		pparams.addSingleString("order",  cmd.order);
		pparams.addSingleString("countycode",  cmd.countycode);
		pparams.addSingleString("indexareacode",  cmd.indexareacode);
		pparams.addSingleString("coupon",  cmd.getCoupon());
		if(cmd.lineId!=null){
			pparams.addLong("lineId", cmd.lineId);
			if(cmd.stationid!=null) pparams.addLong("stationid",  cmd.stationid);
		}
		pparams.addSingleString("park",  cmd.getPark());
		pparams.addSingleString("playground",  cmd.getPlayground());
		pparams.addSingleString("visacard",  cmd.getVisacard());
		pparams.addSingleString("popcorn",  cmd.getPopcorn());
		pparams.addSingleString("pairseat",  cmd.getPairseat());
		pparams.addSingleString("imax",  cmd.getImax());
		pparams.addSingleString("child",  cmd.getChild());
		pparams.addSingleString("booking",  cmd.getBooking());
		pparams.addInteger("pageNo", pageNo);
		if(StringUtils.isBlank(cmd.getCinemaname()) && pageCacheService.isUseCache(request)){
			PageView pageView = pageCacheService.getPageView(request, "movie/searchCinema.xhtml", pparams, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		int firstRow = pageNo * cmd.rowsPerpage;
		String url = "cinemalist";
		if(!StringUtils.isBlank(cmd.getCountycode())) url = "movie/searchCinema.xhtml";
		Integer rowsCount = new Integer(readOnlyTemplate.findByCriteria(getCinemaQuery(cmd, citycode).setProjection(Projections.rowCount())).get(0)+"");
		PageUtil pageUtil = new PageUtil(rowsCount, cmd.rowsPerpage, pageNo, url, true, true);
		pageUtil.initPageInfo(pparams.getParams());
		model.put("pageUtil", pageUtil);
		model.put("rowsCount", rowsCount);
		County curCounty = null;
		Indexarea curIndexarea = null;
		List subwaylineGroup = new ArrayList();
		//Frequented 常去的影院
		if(StringUtils.isNotBlank(cmd.countycode) && !StringUtils.equals("Frequented", cmd.countycode)) {
			curCounty = daoService.getObject(County.class, cmd.countycode);
		}else if(StringUtils.isNotBlank(cmd.indexareacode)){
			curIndexarea = daoService.getObject(Indexarea.class, cmd.indexareacode);
			if(curIndexarea != null) curCounty = curIndexarea.getCounty();
		}
		//区域
		List<Map> countyGroup = placeService.getPlaceCountyCountMap(Cinema.class, citycode);
		model.put("countyGroup",countyGroup);
		//商圈
		List indexareaGroup = new ArrayList();
		if(curCounty != null){
			indexareaGroup = placeService.getPlaceIndexareaCountMap(Cinema.class, curCounty.getCountycode());
			model.put("curCounty", curCounty);
		}
		//地铁线路
		subwaylineGroup = placeService.getPlaceGroupMapByCitySubwayline(citycode, "cinema");
		if(subwaylineGroup != null && !subwaylineGroup.isEmpty()){
			for (Iterator iterator = subwaylineGroup.iterator(); iterator.hasNext();) {
				Map<String, Object> map2 = (Map<String, Object>) iterator.next();
				Subwayline line = daoService.getObject(Subwayline.class, Long.parseLong(map2.get("lineid")+""));
				if(!StringUtils.equals(line.getCitycode(), citycode)){
					iterator.remove();
				}
			}
		}
		model.put("indexareaGroup", indexareaGroup);
		model.put("subwaylineGroup", subwaylineGroup);
		if(cmd.lineId!=null){
			List<Map> subwaystationList = placeService.getSubwaystationList(citycode, "cinema", cmd.lineId);
			model.put("subwaystationList", subwaystationList);
		}
		//电影院
		List<Cinema> cinemaList = readOnlyTemplate.findByCriteria(getCinemaQuery(cmd, citycode), 0,300);
		List<Long> opiCinemaList = openPlayService.getOpiCinemaidList(citycode, null);
		cinemaList = sortCinemaList(cinemaList,firstRow,cmd.getRowsPerpage(),opiCinemaList);
		model.put("cinemaList", cinemaList);
		Map<Long, CinemaProfile> popcornMap=new HashMap<Long, CinemaProfile>();
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		Map<Long,Integer> playMovieCount = new HashMap<Long,Integer>();
		Map<Long,Integer> playItemCount = new HashMap<Long,Integer>();
		for(Cinema cinema : cinemaList){
			CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinema.getId());
			if(cp!=null) popcornMap.put(cinema.getId(), cp);
			playMovieCount.put(cinema.getId(),mcpService.getCinemaMovieCountByDate(cinema.getId(), null));
			playItemCount.put(cinema.getId(),mcpService.getCinemaMpiCount(cinema.getId(), null,null));
		}
		model.put("playMovieCount",playMovieCount);
		model.put("playItemCount",playItemCount);
		model.put("popcornMap", popcornMap);
		
		//热门活动
		ErrorCode<List<RemoteActivity>> result = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null, null, null, "duetime", 0, 4);
		if(result.isSuccess()) model.put("activityList", result.getRetval());
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		// 热门影院
		List<GewaCommend> hotCinemaList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_HOTCINEMA, null, null, true, 0, 6);
		commonService.initGewaCommendList("hotCinemaList", rh, hotCinemaList);
		model.put("hotCinemaList", hotCinemaList);
		//正在热映
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
		//评分统计
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		//有购票的影院
		model.put("opiCinemaList", opiCinemaList);
		model.put("subwaylineMap", placeService.getSubwaylineMap(citycode));
		model.put("curMarkCountMap",markCountMap);
		model.put("cinemaCount",opiCinemaList.size());
		model.put("fetureMovieCount",mcpService.getFutureMovieList(0, 200, null).size());
		model.put("activityCount",synchActivityService.getActivityCount(citycode, null,RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null).getRetval());
		getHotSaleList(model);
		model.put("searchCinema", true);
		model.put("movieIdList", new ArrayList<Long>());
		return "movie/wide_movieCinemaList.vm";
	}*/
	
	@RequestMapping("/movie/searchCinema.xhtml")
	public String newCinemaList(SearchCinemaCommand cmd, ModelMap model,
			HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		int pageNo = cmd.getPageNo();
		PageParams pparams = new PageParams();
		pparams.addSingleString("cinemaids",  cmd.getCinemaids());
		pparams.addSingleString("characteristic",  cmd.getCharacteristic());
		pparams.addSingleString("ctype",  cmd.getCtype());
		pparams.addSingleString("countycode",  cmd.getCountycode());
		pparams.addSingleString("lineall",  cmd.getLineall());
		if(cmd.lineId!=null){
			pparams.addLong("lineId", cmd.lineId);
		}
		pparams.addSingleString("hotcinema",  cmd.getHotcinema());
		pparams.addSingleString("pairseat",  cmd.getPairseat());
		pparams.addSingleString("popcorn",  cmd.getPopcorn());
		pparams.addSingleString("park",  cmd.getPark());
		pparams.addSingleString("refund",  cmd.getRefund());
		pparams.addSingleString("acthas",  cmd.getActhas());
		pparams.addSingleString("cinemaname",  cmd.getCinemaname());
		pparams.addSingleString("order",  cmd.order);
		pparams.addInteger("pageNo", pageNo);
		if(StringUtils.isBlank(cmd.getCinemaname()) && StringUtils.isBlank(cmd.getCinemaids()) && pageCacheService.isUseCache(request)){
			PageView pageView = pageCacheService.getPageView(request, "movie/searchCinema.xhtml", pparams, citycode);
			if(pageView!=null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		int firstRow = pageNo * cmd.rowsPerpage;
		String url = AdminCityContant.citycode2PinyinMap.get(citycode) + "/cinemalist";
		if(!StringUtils.isBlank(cmd.getCountycode())) url = AdminCityContant.citycode2PinyinMap.get(citycode) + "/movie/searchCinema.xhtml";
		
		Integer rowsCount = 0;
		List<Long> goodsCinemaIdList = mcpService.getCinemaIdListByGoods(citycode);
		List<Long> activityCinemaIdList = synchActivityService.getActivityRelatedidByTag(citycode, 3, "cinema");
		model.put("goodsCinemaIdList", goodsCinemaIdList);
		model.put("activityCinemaIdList", activityCinemaIdList);
		
		List<Long> relatedidList = new ArrayList<Long>();
		if(StringUtils.isNotBlank(cmd.getActhas())){
			relatedidList = activityCinemaIdList;
			if(relatedidList != null && !relatedidList.isEmpty()){
				rowsCount = new Integer(readOnlyTemplate.findByCriteria(getCinemaQuery(cmd, citycode, relatedidList).setProjection(Projections.rowCount())).get(0)+"");
			}
		}else if(StringUtils.isNotBlank(cmd.getPopcorn())){//是否有线上卖品
			relatedidList = goodsCinemaIdList;
			if(relatedidList != null && !relatedidList.isEmpty()){
				rowsCount = new Integer(readOnlyTemplate.findByCriteria(getCinemaQuery(cmd, citycode, relatedidList).setProjection(Projections.rowCount())).get(0)+"");
			}
		}else{
			rowsCount = new Integer(readOnlyTemplate.findByCriteria(getCinemaQuery(cmd, citycode, null).setProjection(Projections.rowCount())).get(0)+"");
		}
		PageUtil pageUtil = new PageUtil(rowsCount, cmd.rowsPerpage, pageNo, url, true, true);
		pageUtil.initPageInfo(pparams.getParams());
		model.put("pageUtil", pageUtil);
		model.put("rowsCount", rowsCount);
		
		Map<Long, Map> paytimeMap = new HashMap<Long, Map>();
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		List<Long> opiCinemaList = new ArrayList<Long>();
		Map<Long,Integer> playMovieCount = new HashMap<Long,Integer>();
		Map<Long,Integer> playItemCount = new HashMap<Long,Integer>();
		Map<Long,String> playItemTimeArea = new HashMap<Long,String>();
		Map<Long, CinemaRoom> roomMap = new HashMap<Long, CinemaRoom>();
		Map<Long, Map<Long, OpenPlayItem>> cinemaOpiMap = new HashMap<Long, Map<Long,OpenPlayItem>>();
		Map<Long, CinemaProfile> cprofileMap = new HashMap<Long, CinemaProfile>();
		if(rowsCount > 0){
			Date fyrq = DateUtil.getBeginningTimeOfDay(new Date());
			cinemaList = readOnlyTemplate.findByCriteria(getCinemaQuery(cmd, citycode, relatedidList), 0, 300);
			opiCinemaList = openPlayService.getOpiCinemaidList(citycode, null);
			cinemaList = sortCinemaList(cinemaList,firstRow, cmd.getRowsPerpage(), opiCinemaList);
			for (Cinema cinema : cinemaList) {
				List<Map> payMemberList = untransService.getPayMemberListByTagAndId(TagConstant.TAG_CINEMA, cinema.getId(), 0, 1);
				if(payMemberList != null && !payMemberList.isEmpty()){
					paytimeMap.put(cinema.getId(), payMemberList.get(0));
				}
				playMovieCount.put(cinema.getId(),mcpService.getCinemaMovieCountByDate(cinema.getId(), fyrq));
				List<MoviePlayItem> mpiList = mcpService.getCinemaMpiList(cinema.getId(), null,fyrq);
				int size = mpiList.size();
				playItemCount.put(cinema.getId(),size);
				if(size > 0){
					playItemTimeArea.put(cinema.getId(),mpiList.get(0).getPlaytime() + "~" + mpiList.get(size - 1).getPlaytime());
				}
				List<Long> roomIdList = mcpService.getRoomIdListByOpi(cinema.getId());
				if(roomIdList != null && !roomIdList.isEmpty()){
					int i = 0;
					Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
					for (Long roomid : roomIdList) {
						if(i >= 2) break;
						CinemaRoom cRoom = daoService.getObject(CinemaRoom.class, roomid);
						if(CharacteristicType.cTypeList.contains(cRoom.getCharacteristic())){
							List<OpenPlayItem> opiList = openPlayService.getOpiListByRoomId(roomid, 0, 1);
							if(opiList != null && !opiList.isEmpty()){
								roomMap.put(roomid, cRoom);
								opiMap.put(roomid, opiList.get(0));
								i++;
							}
						}
					}
					cinemaOpiMap.put(cinema.getId(), opiMap);
				}
				cprofileMap.put(cinema.getId(), daoService.getObject(CinemaProfile.class, cinema.getId()));
			}	
		}
		model.put("cprofileMap", cprofileMap);
		model.put("roomMap", roomMap);
		model.put("cinemaOpiMap", cinemaOpiMap);
		model.put("playMovieCount",playMovieCount);
		model.put("playItemCount",playItemCount);
		model.put("playItemTimeArea", playItemTimeArea);
		model.put("paytimeMap", paytimeMap);
		model.put("opiCinemaList", opiCinemaList);
		model.put("cinemaList", cinemaList);
		Map<String, Integer> commentMap = commonService.getCommentCount();
		model.put("commentMap", commentMap);
		
		//特效厅
		List<String> characteristicList = new ArrayList<String>();
		Map map = mongoService.findOne(MongoData.NS_CITY_ROOM_CHARACTERISTIC, MongoData.SYSTEM_ID, citycode);
		if(map != null){
			if(map.get("characteristic") != null){
				String characteristic = map.get("characteristic") + "";
				characteristicList = Arrays.asList(StringUtils.split(characteristic, ","));
			}
		}
		model.put("characteristicList", characteristicList);
		//行政区域
		List<Map> countyGroup = placeService.getPlaceCountyCountMap(Cinema.class, citycode);
		model.put("countyGroup",countyGroup);
		//地铁沿线
		List<Map<String, Object>> subwaylineGroup = placeService.getPlaceGroupMapByCitySubwayline(citycode, "cinema");
		if(subwaylineGroup != null && !subwaylineGroup.isEmpty()){
			for (Iterator iterator = subwaylineGroup.iterator(); iterator.hasNext();) {
				Map<String, Object> map2 = (Map<String, Object>) iterator.next();
				Subwayline line = daoService.getObject(Subwayline.class, Long.parseLong(map2.get("lineid")+""));
				if(!StringUtils.equals(line.getCitycode(), citycode)){
					iterator.remove();
				}
			}
		}
		model.put("subwaylineGroup", subwaylineGroup);
		
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		
		//正在热映
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
		// 最受关注的影院		
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		List<GewaCommend> hotCinemaList = commonService.getGewaCommendList(citycode, SignName.MOVIEINDEX_HOTCINEMA, null, null, true, 0, 5);
		commonService.initGewaCommendList("hotCinemaList", rh, hotCinemaList);
		model.put("hotCinemaList", hotCinemaList);
		//热门活动
		ErrorCode<List<RemoteActivity>> result = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_CURRENT, TagConstant.TAG_CINEMA, null, null, null, "duetime", 0, 3);
		if(result.isSuccess()) model.put("activityList", result.getRetval());
		model.put("curMarkCountMap",markCountMap);
		//购票排行榜
		getHotSaleList(model);
		
		//影院数量
		model.put("cinemaSumCount", mcpService.getCinemaCountByCitycode(citycode));
		model.put("searchCinema", true);
		return "movie/wide_movieCinemaList.vm";
	}
	@RequestMapping("/movie/ajax/getFrequentedCinemaIds.xhtml")
	public String getFrequentedCinemaIds(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
													String sessid, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		String result = "";
		if(member != null){
			List<Treasure> tList = blogService.getTreasureListByMemberId(member.getId(), new String[]{"cinema"} ,null, null, 0, 100, Treasure.ACTION_COLLECT);
			if(tList != null && !tList.isEmpty()){
				List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(tList, Long.class, "relatedid", true);
				result = StringUtils.join(cinemaIdList, ",");
			}
		}
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/movie/getFrequentedCinema.xhtml")
	public String getFrequentedCinema(SearchCinemaCommand cmd,String cinemaIdList,ModelMap model,
			HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(cinemaIdList)){
			return "movie/wide_ajax_movieCinemaList.vm";
		}
		String[] idList = StringUtils.split(cinemaIdList, ",");
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		for(String id : idList){
			if(ValidateUtil.isNumber(id)){
				cinemaList.add(daoService.getObject(Cinema.class, Long.valueOf(id)));
			}
		}
		String order = "clickedtimes";
		if("generalmark".equals(cmd.order)){
			order = "avggeneral";
		}else if(StringUtils.isNotBlank(cmd.order)){
			order = cmd.order;
		}
		Collections.sort(cinemaList, new PropertyComparator(order, false, false));
		List<Cinema> cinemas = new LinkedList<Cinema>();
		for(Cinema cinema : cinemaList){
			if(StringUtils.isNotBlank(cmd.getPark())){
				if(StringUtils.isNotBlank(cinema.getOtherinfo()) && cinema.getOtherinfo().contains("park") &&  cinema.getOtherinfo().contains("\"" + Flag.SERVICE_PARK_RECOMMEND + "\":\"free")){
					if(StringUtils.isNotBlank(cmd.getCinemaname())){
						if(StringUtils.indexOf(cinema.getName(), cmd.getCinemaname()) != -1){
							cinemas.add(cinema);
						}
					}else{
						cinemas.add(cinema);
					}
				}
			}else if(StringUtils.isNotBlank(cmd.getPopcorn())){
				CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinema.getId());
				if(cp!=null && StringUtils.equals(cp.getPopcorn(), "Y")) {
					if(StringUtils.isNotBlank(cmd.getCinemaname())){
						if(StringUtils.indexOf(cinema.getName(), cmd.getCinemaname()) != -1){
							cinemas.add(cinema);
						}
					}else{
						cinemas.add(cinema);
					}
				}
			}else if(StringUtils.isNotBlank(cmd.getPairseat()) && cmd.getPairseat()!=null ){
				if(StringUtils.isNotBlank(cinema.getOtherinfo()) && cinema.getOtherinfo().contains("pairseat")){
					if(StringUtils.isNotBlank(cmd.getCinemaname())){
						if(StringUtils.indexOf(cinema.getName(), cmd.getCinemaname()) != -1){
							cinemas.add(cinema);
						}
					}else{
						cinemas.add(cinema);
					}
				}
			}else if(StringUtils.isNotBlank(cmd.getImax()) && cmd.getImax() != null){
				if(StringUtils.isNotBlank(cinema.getOtherinfo()) && cinema.getOtherinfo().contains("imax")){
					if(StringUtils.isNotBlank(cmd.getCinemaname())){
						if(StringUtils.indexOf(cinema.getName(), cmd.getCinemaname()) != -1){
							cinemas.add(cinema);
						}
					}else{
						cinemas.add(cinema);
					}
				}
			}else if(StringUtils.isNotBlank(cmd.getChild()) && cmd.getChild() != null){
				if(StringUtils.isNotBlank(cinema.getOtherinfo()) && cinema.getOtherinfo().contains("child")){
					if(StringUtils.isNotBlank(cmd.getCinemaname())){
						if(StringUtils.indexOf(cinema.getName(), cmd.getCinemaname()) != -1){
							cinemas.add(cinema);
						}
					}else{
						cinemas.add(cinema);
					}
				}
			}else{
				cinemas.add(cinema);
			}
		}
		model.put("cinemaList",cinemas);
		List<Long> opiCinemaList = openPlayService.getOpiCinemaidList(citycode, null);
		cinemaList = sortCinemaList(cinemaList,0,cinemas.size(),opiCinemaList);
		//评分统计
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		Map<Long, CinemaProfile> popcornMap=new HashMap<Long, CinemaProfile>();
		Map<Long,Integer> playMovieCount = new HashMap<Long,Integer>();
		Map<Long,Integer> playItemCount = new HashMap<Long,Integer>();
		List<SpecialDiscount> spdiscountList  = paymentService.getSpecialDiscountList(PayConstant.APPLY_TAG_MOVIE, SpecialDiscount.OPENTYPE_GEWA);
		Map<Long, List<SpecialDiscount>> spdiscountMap = new HashMap<Long, List<SpecialDiscount>>();
		for(Cinema cinema : cinemaList){
			CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinema.getId());
			if(cp!=null) popcornMap.put(cinema.getId(), cp);
			playMovieCount.put(cinema.getId(),mcpService.getCinemaMovieCountByDate(cinema.getId(), null));
			playItemCount.put(cinema.getId(),mcpService.getCinemaMpiCount(cinema.getId(), null,null));
			spdiscountMap.put(cinema.getId(),this.spdiscountList(new ArrayList(spdiscountList), citycode, cinema.getId()));
		}
		model.put("playMovieCount",playMovieCount);
		model.put("playItemCount",playItemCount);
		model.put("popcornMap", popcornMap);
		model.put("spdiscountMap",spdiscountMap);
		model.put("subwaylineMap", placeService.getSubwaylineMap(citycode));
		return "movie/wide_ajax_movieCinemaList.vm";
	}
	
	private List<SpecialDiscount> spdiscountList(List<SpecialDiscount> spdiscountList,String citycode ,Long cId){
		ObjectSpdiscountFilter osf = new ObjectSpdiscountFilter(citycode,"cinema", cId, DateUtil.getCurFullTimestamp());
		osf.applyFilter(spdiscountList);
		Collections.sort(spdiscountList, new PropertyComparator("sortnum", false, false));
		return spdiscountList;
	}
	
	public void getHotSaleList(ModelMap model){
		Map<Long, MarkCountData> markCountMap = (Map<Long, MarkCountData>)model.get("curMarkCountMap");
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
		model.put("markCountMap", markCountMap);
	}

	private DetachedCriteria getCinemaQuery(SearchCinemaCommand cmd, String citycode, List<Long> relatedidList){
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "c");
		if(StringUtils.isNotBlank(cmd.getCinemaids())){
			List cidList = BeanUtil.getIdList(cmd.getCinemaids(), ",");
			if(cidList.size() == 1){
				query.add(Restrictions.eq("id", cidList.get(0)));
			}else{
				query.add(Restrictions.in("id", cidList));
			}
		}else if(StringUtils.isNotBlank(cmd.getCtype())){
			DetachedCriteria subquery = DetachedCriteria.forClass(CinemaRoom.class, "r");
			subquery.add(Restrictions.eqProperty("r.cinemaid", "c.id"));
			subquery.add(Restrictions.eq("r.characteristic", cmd.getCtype()));
			subquery.setProjection(Projections.property("r.cinemaid"));
			query.add(Subqueries.exists(subquery));
		}else if(StringUtils.isNotBlank(cmd.getCharacteristic())){
			DetachedCriteria subquery = DetachedCriteria.forClass(CinemaRoom.class, "r");
			subquery.add(Restrictions.eqProperty("r.cinemaid", "c.id"));
			subquery.add(Restrictions.isNotNull("r.characteristic"));
			subquery.setProjection(Projections.property("r.cinemaid"));
			query.add(Subqueries.exists(subquery));
		}
		if(StringUtils.isNotBlank(cmd.indexareacode)){//indexareacode 和 countycode 只要一个
			query.add(Restrictions.eq("c.indexareacode", cmd.indexareacode));
		}else if(StringUtils.isNotBlank(cmd.countycode)){
			query.add(Restrictions.eq("c.countycode", cmd.countycode));
		}else{
			query.add(Restrictions.eq("c.citycode", citycode));
			if(cmd.lineId!=null){
				query.add(Restrictions.like("c.lineidlist", String.valueOf(cmd.lineId), MatchMode.ANYWHERE));
				if(cmd.stationid!=null){
					query.add(Restrictions.eq("c.stationid", new Long(cmd.stationid)));
				}
			}else if(StringUtils.isNotBlank(cmd.getLineall())){
				query.add(Restrictions.isNotNull("c.stationid"));
			}
		}
		if(StringUtils.isNotBlank(cmd.getHotcinema())){
			query.add(Restrictions.gt("c.hotvalue", 0));
		}
		if(StringUtils.isNotBlank(cmd.getCinemaname())){
			query.add(Restrictions.ilike("c.name", cmd.getCinemaname(), MatchMode.ANYWHERE));
		}
		if(cmd.lineId!=null){
			query.add(Restrictions.like("c.lineidlist", String.valueOf(cmd.lineId), MatchMode.ANYWHERE));
		}
		//免费停车位
		if(StringUtils.isNotBlank(cmd.getPark())){
			query.add(Restrictions.like("c.otherinfo","\"" + Flag.SERVICE_PARK_RECOMMEND + "\":\"free", MatchMode.ANYWHERE));
		}else	if(StringUtils.isNotBlank(cmd.getVisacard()) && cmd.getVisacard()!=null){	//刷卡
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_VISACARD, MatchMode.ANYWHERE));
		}else if(StringUtils.isNotBlank(cmd.getRefund())){//是否可退款
			DetachedCriteria subquery = DetachedCriteria.forClass(CinemaProfile.class, "p");
			subquery.add(Restrictions.eqProperty("p.id", "c.id"));
			subquery.add(Restrictions.eq("p.isRefund", "Y"));
			subquery.setProjection(Projections.property("p.id"));
			query.add(Subqueries.exists(subquery));
		}else	if(StringUtils.isNotBlank(cmd.getPairseat()) && cmd.getPairseat()!=null){//情侣座
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_PAIRSEAT, MatchMode.ANYWHERE));
		}
		if(relatedidList != null && !relatedidList.isEmpty()){
			if(relatedidList.size() == 1){
				query.add(Restrictions.eq("id", relatedidList.get(0)));
			}else{
				query.add(Restrictions.in("id", relatedidList));
			}
		}
		//IMAX
		if(StringUtils.isNotBlank(cmd.getImax()) && cmd.getImax() != null){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_IMAX, MatchMode.ANYWHERE));
		}
		//儿童套餐
		if(StringUtils.isNotBlank(cmd.getChild()) && cmd.getChild() != null){
			query.add(Restrictions.like("c.otherinfo", Flag.SERVICE_CHILD, MatchMode.ANYWHERE));
		}
		
		if(StringUtils.equals(cmd.getBooking(), Cinema.BOOKING_OPEN)){
			if(StringUtils.equals("310000", citycode)){
				query.add(Restrictions.eq("booking", Cinema.BOOKING_OPEN));
			}else {
				DetachedCriteria subQry = DetachedCriteria.forClass(MoviePlayItem.class, "m");
				subQry.add(Restrictions.ge("m.playdate", DateUtil.addDay(new Date(), -1)));
				subQry.add(Restrictions.eqProperty("m.cinemaid", "c.id"));
				subQry.setProjection(Projections.property("m.cinemaid"));
				query.add(Subqueries.exists(subQry));
			}
		}
		if(StringUtils.isNotBlank(cmd.order)){
			query.addOrder(orderMap.get(cmd.order));
		}else{
			query.addOrder(Order.desc("c.hotvalue"));
		}
		query.addOrder(Order.desc("c.clickedtimes"));
		query.addOrder(Order.asc("c.id"));
		return query;
	}
	
	private List<Cinema> sortCinemaList(List<Cinema> cinemaList,int firstRow,int maxnum,List<Long> opiCinemaIdsList){
		if(opiCinemaIdsList.isEmpty()){
			return BeanUtil.getSubList(cinemaList, firstRow, maxnum);
		}
		List<Cinema> opiCinemaList = new LinkedList<Cinema>();
		for(Cinema cinema : cinemaList){
			if(opiCinemaIdsList.contains(cinema.getId())){
				opiCinemaList.add(cinema);
			}
		}
		cinemaList.removeAll(opiCinemaList);
		opiCinemaList.addAll(cinemaList);
		return BeanUtil.getSubList(opiCinemaList, firstRow, maxnum);
	}
}
