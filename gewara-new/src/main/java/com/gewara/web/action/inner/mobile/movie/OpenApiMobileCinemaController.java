package com.gewara.web.action.inner.mobile.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchCinemaCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.Flag;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.member.TreasureService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.LongitudeAndLatitude;
import com.gewara.util.MarkHelper;
import com.gewara.util.OuterSorter;
import com.gewara.util.VmUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.activity.RemoteActivity;
@Controller
public class OpenApiMobileCinemaController extends BaseOpenApiMobileMovieController{
	private static final String CINEMA_ALL = "all";// 所有影院
	private static final String CINEMA_REGION = "region";// 区域影院
	private static final String CINEMA_MYORDER = "order";// 我购票的影院
	private static final String CINEMA_NEAR = "near";// 附近影院
	private static final String CINEMA_SUBWAY = "subway";// 地铁
	private static final String CINEMA_BOOK = "book";// 购票影院
	
	private static final String CINEMA_ORDER_MARK = "mark";// 评分
	private static final String CINEMA_ORDER_DISTANCE = "distance";// 距离
	private static final String CINEMA_ORDER_CLICKEDTIMES = "clickedtimes";// 关注度
	
	@Autowired@Qualifier("moviePriceService")
	private MoviePriceService moviePriceService;
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	/**
	 * 根据影院id获取影院
	 */
	@RequestMapping("/openapi/mobile/cinema/cinemaDetail.xhtml")
	public String cinemaDetail(Long cinemaid, String memberEncode, ModelMap model, HttpServletRequest request) {
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影院不存在！");
		}
		Treasure treasure = null;
		if (StringUtils.isNotBlank(memberEncode)) {
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_CINEMA, member.getId(), cinemaid, Treasure.ACTION_COLLECT);
		}
		Map<String, Object> resMap = getCinemaData(cinema);
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(cinema.getOtherinfo());
		CinemaProfile cinemaProfile = daoService.getObject(CinemaProfile.class, cinema.getId());
		if(cinemaProfile!=null){
			if(Status.Y.equals(cinemaProfile.getIsRefund())){
				otherinfoMap.put("refund", "放映前4小时可退票");
			}
			resMap.put("diaryid", cinemaProfile.getTopicid());
		}
		//推荐放映厅
		String rooms = otherinfoMap.get("roomList");
		String characteristic = "";
		if(StringUtils.isNotBlank(rooms)){
			String roomids[] = StringUtils.split(rooms, ",");
			for(String roomid : roomids){
				CinemaRoom room = daoService.getObject(CinemaRoom.class, Long.parseLong(roomid));
				if(room!=null) {
					characteristic = characteristic + room.getRoomname()+":"+room.getContent();
				}
			}
		}
		String otherinfo = JsonUtils.writeMapToJson(otherinfoMap);
		int booking = 0;
		if(StringUtils.equals(cinema.getBooking(), CinemaProfile.STATUS_OPEN)) booking = 1;
		resMap.put("booking", booking);
		resMap.put("iscollect", treasure == null ? 0 : 1);
		resMap.put("otherinfo", otherinfo);
		resMap.put("characteristic", characteristic);
		initField(model, request);
		putCinemaNode(model);
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	private List<Cinema> getMyCinemaList(Member member, String citycode, String orderField, Double pointx, Double pointy, Integer maxnum){
		if(maxnum==null) maxnum = 4;
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		List<Cinema> myCinemaList = orderQueryService.getMemberOrderCinemaList(member.getId(), maxnum);//我去过的影院
		for(Cinema cinema : myCinemaList){
			if(StringUtils.equals(cinema.getCitycode(), citycode)) {
				cinemaList.add(cinema);
			}
		}
		if(StringUtils.isNotBlank(orderField) && member!=null && myCinemaList.size() > 0 && pointx !=null && pointy != null) {
			OuterSorter sorter = new OuterSorter<Cinema>(false);
			for(Cinema cinema : cinemaList){
				if(StringUtils.isNotBlank(cinema.getPointx()) && StringUtils.isNotBlank(cinema.getPointy())){
					long value = Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(cinema.getPointx()), Double.parseDouble(cinema.getPointy())));
					sorter.addBean(value, cinema);
				}else{
					sorter.addBean(null, cinema);
				}
			}
			return sorter.getAscResult();

		}
		return cinemaList;
	}
	/**
	 * 当前电影购票影院
	 * @param subwayid:地铁线路
	 * @param orderField:排序字段
	 * @param playdate:播放日期
	 * @param specialfield：特色筛选
	 * @param movieid: 电影id （空的情况，是app首页中的影院）
	 * TODO: movieid为空和非空，逻辑分开
	 */
	@RequestMapping("/openapi/mobile/cinema/cinemaList.xhtml")
	public String movieOrderCinema(String type, String pointx, String pointy, Long movieid, 
			String memberEncode, String countycode, String citycode, Long subwayid, String orderField,
			Integer from, Integer maxnum, Date playdate, String specialfield, ModelMap model, HttpServletRequest request) {
		ApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 100;
		if (maxnum != null && maxnum > 100) maxnum = 100;
		ApiUser partner = auth.getApiUser();
		if (StringUtils.isNotBlank(citycode)) {
			if (!partner.supportsCity(citycode))
				return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		} else {
			citycode = partner.getDefaultCity();
		}
		if(playdate == null) playdate = DateUtil.getCurDate();
		boolean isToday = true;
		if(movieid==null){
			Date curDate = DateUtil.currentTime();
			String playtime=DateUtil.format(curDate, "HH:mm");
			if(playtime.compareTo("22:00")>=0){
				isToday = false;
				curDate = DateUtil.addDay(curDate, 1);
				curDate = DateUtil.getBeginTimestamp(curDate);
				playdate  = curDate;
			}
		}
		
		Member member = null;
		if (StringUtils.isNotBlank(memberEncode)){
			member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		}
		
		Map<String,List<Cinema>> cinemasMap = null;
		//某部电影能购票的影院id结合
		List<Long> opiCinemaIdList = openPlayService.getOpiCinemaidList(citycode, movieid);
		
		if(StringUtils.equalsIgnoreCase(pointx, "null")) pointx = "";
		if(StringUtils.endsWithIgnoreCase(pointy, "null")) pointy = "";
		Double pointxx = null;
		Double pointyy = null;
		if(StringUtils.isNotBlank(pointx)) pointxx = Double.valueOf(pointx);
		if(StringUtils.isNotBlank(pointy)) pointyy = Double.valueOf(pointy);
		List<Cinema> myCinemaList = null;
		List<Cinema> cinemaList = null;
		if(StringUtils.isBlank(type)){//默认 我去过的+全部影院（按距离）
			if (pointx == null || pointy == null){
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "坐标参数传入有误！");
			}
			cinemaList = mcpService.getCinemaListByCitycode(citycode, 0, 200);
			cinemasMap = new HashMap<String,List<Cinema>>();
			List<Cinema> curOpen = new ArrayList<Cinema>();
			List<Cinema> curClose = new ArrayList<Cinema>();
			for(Cinema cinema : cinemaList){
				if(opiCinemaIdList.contains(cinema.getId())){
					curOpen.add(cinema);
				}else{
					curClose.add(cinema);
				}
			}
			if(movieid != null && StringUtils.isBlank(orderField)){
				orderField = CINEMA_ORDER_DISTANCE;
			}
			cinemasMap.put("curOpen", curOpen);
			cinemasMap.put("curClose", curClose);
			
			//购票
			if(member != null){
				myCinemaList = getMyCinemaList(member, citycode, orderField, pointxx, pointyy, 3);
				for (Cinema cinema : myCinemaList) {
					if(!cinemaList.contains(cinema)){
						cinemaList.add(cinema);
					}
				}
			}
			filterPopcorn(cinemaList, specialfield);
		}else if (CINEMA_NEAR.equals(type)) { // 附近影院(按距离排序)
			if (pointx == null || pointy == null) {
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "坐标参数传入有误！");
			}

			cinemasMap = new HashMap<String,List<Cinema>>();
			List<Cinema> curOpen = new ArrayList<Cinema>();
			List<Cinema> curClose = new ArrayList<Cinema>();

			cinemaList = mcpService.getNearCinemaList(pointxx, pointyy, 5000, movieid, citycode, playdate);
			for(Cinema cinema: cinemaList){
				if(opiCinemaIdList.contains(cinema.getId())){
					curOpen.add(cinema);
				}else{
					curClose.add(cinema);
				}
			}
			
			filterPopcorn(cinemaList, specialfield);
			if(movieid != null && StringUtils.isBlank(orderField)){
				orderField = CINEMA_ORDER_DISTANCE;
			}
			cinemasMap.put("curOpen", curOpen);
			cinemasMap.put("curClose", curClose);
		} else if (CINEMA_MYORDER.equals(type)) {// 我的购票影院
			if(member==null){
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
			}
			cinemaList = getMyCinemaList(member, citycode, orderField, pointxx, pointyy, 50);
			filterPopcorn(cinemaList, specialfield);
		} else if (CINEMA_ALL.equals(type)) {// 全部影院
			List<Long> cinemaIdList = null;
			if(movieid != null){
				cinemaIdList = mcpService.getPlayCinemaIdList(citycode, movieid, playdate);
			}else{
				SearchCinemaCommand cmd = searchCinemaCommand(specialfield);
				cinemaIdList = mcpService.getCinemaIdListBySearchCmd(cmd, citycode);
			}
			ErrorCode<Map<String,List<Cinema>>> code = getAllBookCinemaList(cinemaIdList, opiCinemaIdList, specialfield, orderField, pointxx, pointyy);
			if (!code.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
			}
			cinemasMap = code.getRetval();
		} else if (CINEMA_REGION.equals(type)) {// 按区域
			if (countycode == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传入参数有误！");
			List<Long> cinemaIdList = null;
			if(movieid != null){
				cinemaIdList = mcpService.getPlayCinemaIdListByCountycode(countycode, movieid, playdate);
			}else{
				SearchCinemaCommand cmd = new SearchCinemaCommand();
				cmd.setCountycode(countycode);
				cinemaIdList = mcpService.getCinemaIdListBySearchCmd(cmd, citycode);
			}
			ErrorCode<Map<String,List<Cinema>>> code = getAllBookCinemaList(cinemaIdList, opiCinemaIdList, specialfield, orderField, pointxx, pointyy);
			if (!code.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
			}
			cinemasMap = code.getRetval();
		} else if (CINEMA_SUBWAY.equals(type)) { // 地铁线路
			if (subwayid == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传入参数有误！");
			SearchCinemaCommand cmd = new SearchCinemaCommand();
			cmd.setLineId(subwayid);
			List<Long> cinemaIdList = mcpService.getCinemaIdListBySearchCmd(cmd, citycode);
			if(movieid != null){
				cinemaIdList = new ArrayList<Long>(CollectionUtils.intersection(cinemaIdList, mcpService.getPlayCinemaIdList(citycode, movieid, playdate)));
			}
			ErrorCode<Map<String,List<Cinema>>> code = getAllBookCinemaList(cinemaIdList, opiCinemaIdList, specialfield, orderField, pointxx, pointyy);
			if (!code.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
			}
			cinemasMap = code.getRetval();
		} else if (CINEMA_BOOK.equals(type)) {// 购票影院
			if(StringUtils.isBlank(orderField)){
				orderField = CINEMA_ORDER_CLICKEDTIMES;
			}
			ErrorCode<Map<String,List<Cinema>>> code = getAllBookCinemaList(opiCinemaIdList, opiCinemaIdList, specialfield, orderField, pointxx, pointyy);
			if (!code.isSuccess()){
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
			}
			cinemasMap = code.getRetval();
		}
		Map<Long, Integer> countMap = new HashMap<Long, Integer>();//剩余场次数
		List<Cinema> cinemas = null;
		if(CINEMA_MYORDER.equals(type)){
			cinemas = sortAndSpecialfield(cinemaList, specialfield, movieid, playdate, orderField, countMap, pointxx, pointyy, partner);
		}else{
			cinemas = sortAndSpecialfield(cinemasMap.get("curOpen"), specialfield, movieid, playdate, orderField, countMap, pointxx, pointyy, partner);
			if(cinemas.size() < from + maxnum){//数量不够才加
				if(cinemasMap.get("curClose") != null){
					cinemas.addAll(sortAndSpecialfield(cinemasMap.get("curClose"), specialfield, movieid, playdate, orderField, countMap, pointxx, pointyy, partner));
				}
			}
			myCinemaList = getSortCinemaList(orderField, myCinemaList, pointxx, pointyy);
			//购票的影院排序 order=1
			if(myCinemaList != null && member!= null){
				int x = 0;
				for (Cinema cinema : myCinemaList) {
					if(cinemas.contains(cinema)){
						cinemas.remove(cinema);
						cinemas.add(x, cinema);
						x++;
					}
				}
			}
		}
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterCinema(cinemas);
		//TODO:上面分页优化后此处不分页
		cinemas = BeanUtil.getSubList(cinemas, from, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();

		for(Cinema cinema : cinemas){
			Map<String, Object> params = getCinemaData(cinema);
			Integer playitemcount = countMap.get(cinema.getId());
			Integer playmoviecount = mcpService.getCinemaMovieCountByDate(cinema.getId(), playdate);
			if(playitemcount==null) playitemcount = 0;
			if(playmoviecount==null) playmoviecount = 0;
			params.put("playitemcount", playitemcount);
			params.put("playmoviecount", playmoviecount);
			int booking = 0;
			if(opiCinemaIdList.contains(cinema.getId())) booking = 1;
			params.put("booking", booking);
			int myCinema = 0;
			if(myCinemaList!=null && myCinemaList.contains(cinema)) myCinema = 1;
			params.put("myCinema", myCinema);
			String countdes = "";
			//价格区间
			Map priceInfo = null;
			if(movieid!=null){
				if(playitemcount>0){
					countdes = "剩余" + playitemcount + "场";
				}
				priceInfo = moviePriceService.getPlacePriceFromCache("cinema", cinema.getId(), "movie", movieid);
			}else {
				priceInfo = moviePriceService.getMinMaxPlacePrice("cinema", cinema.getId());
				if(playmoviecount>0){
					if(isToday){
						countdes = "今天放映"+playmoviecount+"部余"+playitemcount+"场";
					}else {
						countdes = "明天放映"+playmoviecount+"部余"+playitemcount+"场";
					}
				}else {
					countdes = "排片尚未公布";
				}
			}
			
			if(priceInfo!=null){
				params.put("priceinfo", priceInfo.get("minprice")+"-" + priceInfo.get("maxprice"));
				countdes = countdes + " ￥" + priceInfo.get("minprice") + "-" + priceInfo.get("maxprice");
			}
			params.put("countdes", countdes);
			resMapList.add(params);
		}
		model.put("resMapList", resMapList);
		initField(model, request);
		putCinemaListNode(model);
		return getOpenApiXmlList(model);
	}
	private void filterPopcorn(List<Cinema> cinemaList, String specialfield){
		if(!StringUtils.equals("popcorn", specialfield) || cinemaList==null) return;
		List<Cinema> tmpList = new ArrayList<Cinema>();
		for(Cinema cinema : cinemaList){
			if(!StringUtils.equals(cinema.getPopcorn(), CinemaProfile.POPCORN_STATUS_Y)){
				tmpList.add(cinema);
			}
		}
		cinemaList.removeAll(tmpList);
	}
	private List<Cinema> getSortCinemaList(String orderField, List<Cinema> cinemaList, Double pointx, Double pointy){
		if(StringUtils.isBlank(orderField) || cinemaList==null || cinemaList.size()==0) return cinemaList;
		if(StringUtils.equals(orderField, CINEMA_ORDER_MARK) || StringUtils.equals(orderField, CINEMA_ORDER_CLICKEDTIMES)){
			return null;
		}
		List<String> orderList = Arrays.asList(CINEMA_ORDER_DISTANCE);
		OuterSorter sorter = new OuterSorter<Cinema>(false);
		if(orderList.contains(orderField)) {
			for(Cinema cinema : cinemaList){
				if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_MARK)){
					Double point = Double.valueOf(getPlaceGeneralmark(cinema));
					sorter.addBean(point, cinema);
				}else if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_CLICKEDTIMES)){
					sorter.addBean(cinema.getClickedtimes(), cinema);
				}else if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_DISTANCE) && pointx!=null && pointy!=null){
					if(StringUtils.isNotBlank(cinema.getPointx()) && StringUtils.isNotBlank(cinema.getPointy())) {
						long value = Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(cinema.getPointx()), Double.parseDouble(cinema.getPointy())));
						sorter.addBean(value, cinema);
					}else{
						sorter.addBean(null, cinema);
					}
				}
			}
			if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_DISTANCE))  {
				return sorter.getAscResult();
			}
			return sorter.getDescResult();
		}
		return cinemaList;
	}
	
	private List<Cinema> sortAndSpecialfield(List<Cinema> cinemaList, String specialfield, Long movieid, Date playdate,
			String orderField, Map<Long,Integer> countMap, Double pointx, Double pointy, ApiUser partner){
		//TODO:对外直接分页，分页外的附加数据不查询
		OuterSorter sorter = new OuterSorter<Cinema>(false);
		
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterCinema(cinemaList);
		
		for(Cinema cinema:cinemaList){
			boolean add = false;
			//特色筛选
			if(StringUtils.equals(specialfield,"all") || isRig(specialfield, cinema)){
				if(movieid != null){
					//剩余场次数
					int overMap = mcpService.getCinemaMpiCount(cinema.getId(), movieid, playdate);
					countMap.put(cinema.getId(), overMap);
					if(overMap > 0) add = true;
				}else{
					int cinemaMpiCount = mcpService.getCinemaMpiCount(cinema.getId(), null, playdate);
					countMap.put(cinema.getId(), cinemaMpiCount);
					add = true;
				}
			}
			if(add){
				Comparable sortValue = null;
				if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_MARK)){
					Double point = Double.valueOf(getPlaceGeneralmark(cinema));
					sortValue = point;
				}else if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_CLICKEDTIMES)){
					sortValue = cinema.getClickedtimes();
				}else if(StringUtils.equalsIgnoreCase(orderField, CINEMA_ORDER_DISTANCE) && pointx!=null && pointy!=null){
					if(StringUtils.isNotBlank(cinema.getPointx()) && StringUtils.isNotBlank(cinema.getPointy())){
						sortValue = Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(cinema.getPointx()), Double.parseDouble(cinema.getPointy())));
					}
				}else{
					sortValue = countMap.get(cinema.getId());
				}
				sorter.addBean(sortValue, cinema);
			}
		}
		if(CINEMA_ORDER_DISTANCE.equals(orderField)) return sorter.getAscResult();
		return sorter.getDescResult();
	}
	private SearchCinemaCommand searchCinemaCommand(String specialfield){
		SearchCinemaCommand cmd = new SearchCinemaCommand();
		if(Flag.SERVICE_PARK.equals(specialfield)){
			cmd.setPark(Flag.SERVICE_PARK);
		}else if(Flag.SERVICE_VISACARD.equals(specialfield)){
			cmd.setVisacard(Flag.SERVICE_VISACARD);
		}else if(Flag.SERVICE_PAIRSEAT.equals(specialfield)){
			cmd.setPairseat(Flag.SERVICE_PAIRSEAT);
		}else if(Flag.SERVICE_IMAX.equals(specialfield)){
			cmd.setImax(Flag.SERVICE_IMAX);
		}else if(Flag.SERVICE_CHILD.equals(specialfield)){
			cmd.setChild(Flag.SERVICE_CHILD);
		}else if(Flag.SERVICE_POPCORN.equals(specialfield)){
			cmd.setPopcorn(Status.Y);
		}
		return cmd;
	}
	private boolean isRig(String specialfield, Cinema cinema){
		boolean isRig = false;
		if(StringUtils.isBlank(specialfield) || (cinema.getOtherinfo() != null && cinema.getOtherinfo().indexOf(specialfield) != -1) || (StringUtils.equals(specialfield, "popcorn") && StringUtils.equals(cinema.getPopcorn(), CinemaProfile.POPCORN_STATUS_Y))){
			isRig = true;
		}
		return isRig;
	}
	private ErrorCode<List<Cinema>> setOrderFiled(List<Cinema> cinemaList,String orderField, Double pointx, Double pointy,String specialfield) {
		OuterSorter sorter = new OuterSorter<Cinema>(false);
		List<Cinema> cinemas = null;
		if (CINEMA_ORDER_CLICKEDTIMES.equals(orderField)) {
			for(Cinema cinema : cinemaList){
				if(isRig(specialfield, cinema)){
					sorter.addBean(cinema.getClickedtimes(), cinema);
				}
			}
			cinemas = sorter.getDescResult();
		} else if (CINEMA_ORDER_DISTANCE.equals(orderField)) {
			if (pointx == null || pointy == null)
				return ErrorCode.getFailure("参数有误！");
			for (Cinema cinema : cinemaList) {
				if(isRig(specialfield, cinema)){
					if (StringUtils.isNotBlank(cinema.getPointx()) && StringUtils.isNotBlank(cinema.getPointy())) {
						long value = Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(cinema.getPointx()), Double.parseDouble(cinema.getPointy())));
						sorter.addBean(value, cinema);
					}else{
						sorter.addBean(null, cinema);
					}
				}
			}
			cinemas = sorter.getAscResult();
		} else if (CINEMA_ORDER_MARK.equals(orderField)) {
			for (Cinema cinema : cinemaList) {
				if(isRig(specialfield, cinema)){
					Integer generalmark = MarkHelper.getSingleMarkStar(cinema, "general");
					sorter.addBean(generalmark, cinema);
				}
			}
			cinemas = sorter.getDescResult();
		}else {
			cinemas = new ArrayList<Cinema>();
			for(Cinema cinema : cinemaList){
				if(isRig(specialfield, cinema)){
					cinemas.add(cinema);
				}
			}
		}
		return ErrorCode.getSuccessReturn(cinemas);
	}
	
	private ErrorCode<Map<String,List<Cinema>>> getAllBookCinemaList(List<Long> cinemaIdList, List<Long> opiCinemaIdList,String specialfield,String orderField,Double pointx, Double pointy) {
		List<Long> cinemasIdList = new ArrayList<Long>(cinemaIdList);
		List<Long> curOpenList = new ArrayList<Long>(CollectionUtils.intersection(cinemasIdList, opiCinemaIdList));
		List<Cinema> curOpenCinama = daoService.getObjectList(Cinema.class, curOpenList);
		cinemasIdList.removeAll(curOpenList);
		List<Cinema> curClostList  = daoService.getObjectList(Cinema.class, cinemasIdList);
		ErrorCode<List<Cinema>> code = this.setOrderFiled(curOpenCinama, orderField, pointx, pointy, specialfield);
		if(!code.isSuccess()){
			return ErrorCode.getFailure("参数有误！");
		}
		curOpenCinama = code.getRetval();
		ErrorCode<List<Cinema>> closeCode = this.setOrderFiled(curClostList, orderField, pointx, pointy, specialfield);
		if(!closeCode.isSuccess()){
			return ErrorCode.getFailure("参数有误！");
		}
		curClostList = closeCode.getRetval();
		
		Map<String,List<Cinema>> cinemasMap = new HashMap<String,List<Cinema>>();
		cinemasMap.put("curOpen", curOpenCinama);
		cinemasMap.put("curClose", curClostList);
		return ErrorCode.getSuccessReturn(cinemasMap);
	}
	
	/**
	 * 获取影院取票帮助
	 */
	@RequestMapping("/openapi/mobile/cinema/ticketHelp.xhtml")
	public String getTicketHelp(Long diaryid, ModelMap model){
		if(diaryid == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR,"diaryid不能为空");
		}
		String diaryContent = blogService.getDiaryBody(diaryid);
		if(diaryContent.indexOf("http://img") != -1){
			diaryContent = StringUtils.replace(diaryContent, "/userfiles", "/sw300h300/userfiles");
		}else{
			diaryContent = StringUtils.replace(diaryContent, "/userfiles", config.getString("picPath") + "sw300h300/userfiles");
		}
		diaryContent = StringUtils.replace(diaryContent, "src=\"/sw300h300", "src=\"" + config.getString("picPath") + "sw300h300");
		diaryContent = diaryContent.replace("style", "css");
		return getSingleResultXmlView(model, diaryContent);
	}
	
	/**
	 * 电影活动关联的场次，场次关联影院
	 */
	@RequestMapping("/openapi/mobile/cinema/cinemaListByActivityOpi.xhtml")
	public String cinemaListByActivityOpi(Long activityid, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(activityid+""))return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "activityId不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess())  return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		List<String> mpidList = synchActivityService.getActivityMpidList(activityid);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for(String mpid : mpidList){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", Long.valueOf(mpid));
			if(opi!=null && !opi.isExpired() && opi.isOpenToPartner()) opiList.add(opi);
		}
		List<Long> cinemaidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "cinemaid", true);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
		getCienmaListMap(cinemaList, model, request);
		putCinemaListNode(model);
		return getOpenApiXmlList(model);
	}
	/**
	 * 能预定套餐的影院
	 */
	@RequestMapping("/openapi/mobile/cinema/cinemaListByBookingMeal.xhtml")
	public String cinemaListByBookingBmh(String citycode, String memberEncode, ModelMap model, HttpServletRequest request){
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		SearchCinemaCommand cmd = new SearchCinemaCommand();
		cmd.setPopcorn(Status.Y);
		List<Cinema> cinemaList = mcpService.getCinemaListBySearchCmd(cmd, citycode, 0, 200);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Cinema cinema : cinemaList){
			Map<String, Object> resMap = getCinemaData(cinema);
			if(StringUtils.isNotBlank(memberEncode)){
				Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
				Treasure treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_CINEMA, member.getId(), cinema.getId(), Treasure.ACTION_COLLECT);
				resMap.put("iscollect", treasure == null ? 0 : 1);
			}
			resMapList.add(resMap);
			List<Goods> goodsList = goodsService.getGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, cinema.getId(), true, true, true, "goodssort", true, false);
			GoodsFilterHelper.goodsFilter(goodsList, partner.getId());
			resMap.put("mealCount", goodsList.size());
			resMapList.add(resMap);
		}
		initField(model, request);
		putCinemaListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 返回我常去的影院列表(我去过的+我关注的)
	 */
	@RequestMapping("/openapi/mobile/cinema/cinemaListByMy.xhtml")
	public String cinemaListByMy(String pointx, String pointy, String citycode, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 10;
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<Long> cinemaidList = blogService.getTreasureCinemaidList(citycode, member.getId(), Treasure.ACTION_COLLECT);
		List<Cinema> myCinemaList = getMyCinemaList(member, citycode, null, null, null, 100);
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		cinemaList.addAll(myCinemaList);
		
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Long cinemaid : cinemaidList){
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			if(!cinemaList.contains(cinema) && StringUtils.equals(citycode, cinema.getCitycode())){
				cinemaList.add(cinema);
			}
		}
		if(StringUtils.isNotBlank(pointx) && !StringUtils.equals(pointx, "null")){
			cinemaList = mcpService.getCinemaListByNearOrder(Double.parseDouble(pointx), Double.parseDouble(pointy), cinemaList, 5000, false);
		}
		cinemaList = BeanUtil.getSubList(cinemaList, from, maxnum);
		for(Cinema cinema : cinemaList){
			Map<String, Object> resMap = getCinemaData(cinema);
			resMap.put("iscollect", !cinemaidList.contains(cinema.getId()) ? 0 : 1);
			int booking = 0;
			if(StringUtils.equals(cinema.getBooking(), CinemaProfile.STATUS_OPEN)) booking = 1;
			int myCinema = 0;
			if(myCinemaList!=null && myCinemaList.contains(cinema)) myCinema = 1;
			resMap.put("myCinema", myCinema);
			resMap.put("booking", booking);
			resMapList.add(resMap);
		}
		initField(model, request);
		putCinemaListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 根据区、放映日期、电影，获取电影院
	 * @return
	 */
	@RequestMapping(value = "/openapi/mobile/cinema/getOpenCinemaListByCountycode.xhtml")
	public String getCinemaList(String countycode, Date playdate, Long movieid, ModelMap model, HttpServletRequest request){
		List<Cinema> cinemaList = mcpService.getPlayCinemaListByCountycode(countycode, movieid, playdate);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Cinema cinema : cinemaList){
			Map<String, Object> resMap = getCinemaData(cinema);
			resMapList.add(resMap);
		}
		initField(model, request);
		putCinemaListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	@RequestMapping("/openapi/mobile/cinema/searchCinema.xhtml")
	public String seatchCinemaList(String pointx, String pointy, String citycode, String name, int from, int maxnum, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(citycode) && StringUtils.isBlank(name)){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "citcode和name不能同时为空");
		}
		if(maxnum>200){
			maxnum = 200;
		}
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		if(StringUtils.isNotBlank(citycode)){
			query.add(Restrictions.eq("citycode", citycode));
		}
		if(StringUtils.isNotBlank(name)){
			query.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		List<Cinema> cinemaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		if(StringUtils.isNotBlank(pointx) && !StringUtils.equals(pointx, "null")){
			mcpService.getCinemaListByNearOrder(Double.parseDouble(pointx), Double.parseDouble(pointy), cinemaList, 5000, false);
		}
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Cinema cinema : cinemaList){
			Map<String, Object> resMap = getCinemaData(cinema);
			int booking = 0;
			if(StringUtils.equals(cinema.getBooking(), CinemaProfile.STATUS_OPEN)) booking = 1;
			resMap.put("booking", booking);
			getCinemaDesc(cinema, resMap);
			resMapList.add(resMap);
		}
		return getOpenApiXmlList(resMapList, "cinemaList,cinema", model, request);
	}
	private void getCinemaDesc(Cinema cinema, Map<String, Object> resMap){
		Date curdate = DateUtil.getCurDate();
		Integer playitemcount = mcpService.getCinemaMpiCount(cinema.getId(), null, curdate);
		Integer playmoviecount = mcpService.getCinemaMovieCountByDate(cinema.getId(), curdate);
		if(playitemcount==null) playitemcount = 0;
		if(playmoviecount==null) playmoviecount = 0;
		String countdes = "";
		if(playmoviecount>0){
			countdes = "今天放映"+playmoviecount+"部余"+playitemcount+"场";
		}else {
			countdes = "排片尚未公布";
		}
		Map map = moviePriceService.getMinMaxPlacePrice("cinema", cinema.getId());
		if(map!=null){
			resMap.put("priceinfo", map.get("minprice")+"-"+map.get("maxprice"));
			countdes = countdes + " ￥" + map.get("minprice") + "-" + map.get("maxprice");
		}
		resMap.put("playitemcount", playitemcount);
		resMap.put("playmoviecount", playmoviecount);
		resMap.put("countdes", countdes);
	}
}
