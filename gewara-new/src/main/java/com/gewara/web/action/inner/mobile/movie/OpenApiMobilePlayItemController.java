package com.gewara.web.action.inner.mobile.movie;

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
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.helper.ticket.PartnerPriceHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.monitor.MemberMonitorService;
import com.gewara.untrans.monitor.MemberMonitorService.CountType;
import com.gewara.untrans.ticket.TicketQueueService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteActivityMpi;
@Controller
public class OpenApiMobilePlayItemController extends BaseOpenApiMobileMovieController{
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("ticketQueueService")
	private TicketQueueService ticketQueueService;
	@Autowired@Qualifier("memberMonitorService")
	private MemberMonitorService memberMonitorService;
	/**
	 * 当前影院播放该影片的排片日期列表(影院排片日期)
	 */
	@RequestMapping("/openapi/mobile/playItem/openDateList.xhtml")
	public String getMovieOfCinemaOpenDateList(Long cinemaid, Long movieid, String citycode, ModelMap model) {
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		if (StringUtils.isNotBlank(citycode)) {
			if (!partner.supportsCity(citycode))
				return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		} else {
			citycode = partner.getDefaultCity();
		}
		if(movieid==null && cinemaid==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请选择影院或影片！");
		}
		List<Date> dateList = null;
		if(cinemaid == null){
			dateList = mcpService.getCurMoviePlayDate2(citycode, movieid);
		}else{
			dateList = openPlayService.getMovieOfCinemaOpenDateList(cinemaid, movieid);
		}
		model.put("openDateList", dateList);
		return getXmlView(model, "inner/mobile/openDateList.vm");
	}
	/**
	 * 根据区、电影，获取排片日期
	 * @return
	 */
	@RequestMapping(value = "/openapi/mobile/playItem/getPlayDateInfoByCountyMovie.xhtml")
	public String getPlayDateInfoByCountyMovie(String countycode, Long movieid, ModelMap model){
		List<Date> playdateList = openPlayService.getMovieOpenDateListByCounycode(countycode, movieid);
		model.put("openDateList", playdateList);
		return getXmlView(model, "inner/mobile/openDateList.vm");
	}
	/**
	 * 根据场次id查询场次信息
	 */
	@RequestMapping("/openapi/mobile/playItem/mpiDetail.xhtml")
	public String getCurMpiList(Long mpid, ModelMap model, HttpServletRequest request) {
		if (mpid == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传入参数有误！");
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, mpid);
		if (mpi == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在！");
		OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
		Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi, opi);
		resMap.put("cinemaname", opi.getCinemaname());
		resMap.put("moviename", opi.getMoviename());
		resMap.put("remark", opi.getRemark());
		resMap.put("seatStatus", opi.isOrder()?1:0);
		resMap.put("ticketstatus", opi.getSeatStatus());
		resMap.put("unbookingReason", OpiConstant.getUnbookingReason(opi));
		model.put("resMap", resMap);
		putPlayItemNode(model);
		initField(model, request);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 根据场次id查询场次信息
	 */
	@RequestMapping("/openapi/mobile/playItem/opiDetail.xhtml")
	public String opiDetail(Long mpid, ModelMap model, HttpServletRequest request) {
		OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if (opi == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在！");
		Map<String, Object> resMap = GewaApiMovieHelper.getOpiData(opi);
		resMap.put("cinemaname", opi.getCinemaname());
		resMap.put("moviename", opi.getMoviename());
		resMap.put("seatStatus", opi.isOrder()?1:0);
		resMap.put("ticketstatus", opi.getSeatStatus());
		resMap.put("unbookingReason", OpiConstant.getUnbookingReason(opi));
		model.put("resMap", resMap);
		model.put("root", "openPlayItem");
		initField(model, request);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 根据影院影片和日期获取排片
	 */
	@RequestMapping("/openapi/mobile/playItem/curMpiList.xhtml")
	public String getCurMpiList(Long cinemaid, Long movieid, Date playdate, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request) {
		if (cinemaid == null || movieid == null)
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传入参数有误！");
		if(from==null)from=0;
		if (playdate == null) playdate = new Date();
		if(maxnum==null) maxnum = 50;
		if (maxnum != null && maxnum >50) maxnum = 50;
		List<MoviePlayItem> playItemList = mcpService.getCurMpiList(cinemaid, movieid, playdate);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		if (!VmUtils.isEmptyList(playItemList)) {
			playItemList = MoviePlayItem.getCurrent(playdate, playItemList);
			if (from != null && maxnum != null){
				playItemList = BeanUtil.getSubList(playItemList, from, maxnum);
			}
			ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
			List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
			OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
			for (MoviePlayItem mpi : playItemList) {
				if(mpi.isUnShowToGewa()) continue;
				OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
				Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi, opi);
				int seatStatus = 0;
				String moviename = null;
				String cinemaname = null;
				if(opi!=null) {
					if(opi.getCostprice()!=null && !filter.excludeOpi(opi)){
						resMap.put("unbookingReason",  OpiConstant.getUnbookingReason(opi));
						if(opi.isOrder()) seatStatus = 1;
					}
					// 剩余座位信息，0没票；1少量票；2很多票
					resMap.put("seatAmountStatus", opi.seatAmountStatus());
					resMap.put("ticketstatus", opi.getSeatStatus());
					resMap.put("remark", opi.getRemark());
					cinemaname = opi.getCinemaname();
					moviename = opi.getMoviename();
				}else {
					Movie movie = daoService.getObject(Movie.class, mpi.getMovieid());
					Cinema cinema = daoService.getObject(Cinema.class, mpi.getCinemaid());
					cinemaname = cinema.getName();
					moviename = movie.getName();
					resMap.put("unbookingReason",  "该场次未开放售票");
				}
				resMap.put("moviename", moviename);
				resMap.put("cinemaname", cinemaname);
				resMap.put("seatStatus", seatStatus);
				resMapList.add(resMap);
			}
		}
		model.put("resMapList", resMapList);
		putPlayItemListNode(model);
		initField(model, request);
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 获取场次座位信息(场次座位信息)
	 */
	@RequestMapping("/openapi/mobile/playItem/opiSeatInfo.xhtml")
	public String getOpiSeatList(Long mpid, String appVersion, ModelMap model, HttpServletRequest request){
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		Map<String,String> otherInfo = JsonUtils.readJsonToMap(opi.getOtherinfo());
		String mealoption = otherInfo.get(OpiConstant.MEALOPTION);
		if(!StringUtils.equals(mealoption, "notuse")){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, auth.getApiUser().getId());
			if(goodsGift!=null) {
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goodsGift", goodsGift);
				model.put("bindGoods", goods);
			}else {
				List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, opi.getCinemaid(), 0, 5);
				GoodsFilterHelper.goodsFilter(goodsList, auth.getApiUser().getId());
				model.put("optionalGoods", goodsList);
			}
		}
		ErrorCode code = addOpiSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		Member member = auth.getMember();
		if(member!=null){
			memberMonitorService.increament(member.getId(), CountType.getSeat, request);
		}
		boolean newseat=false;
		if(appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_6)>=0){
			newseat = true;
		}
		model.put("newseat", newseat);
		return getXmlView(model, "inner/mobile/opiSeatInfo.vm");
	}
	
	/**
	 * 获取场次座位信息
	 */
	@RequestMapping("/openapi/mobile/playItem/opiLockedSeat.xhtml")
	public String getOpiLockedSeatList(Long mpid, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		if(StringUtils.contains(opi.getOtherinfo(), OpiConstant.ADDRESS) && partner.getId() >= PartnerConstant.GEWA_CLIENT){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		}
		GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, partner.getId());
		if(goodsGift!=null){
			if(partner.getId() < PartnerConstant.GEWA_CLIENT){//Gewara商户
				model.put("goodsGift", goodsGift);
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goods", goods);
			}else{
				return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
			}
		}
		ErrorCode code = addOpiLockedSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg()); 
		return getXmlView(model, "inner/mobile/opiLockedSeat.vm");
	}
	/**
	 * 电影活动中的场次
	 */
	@RequestMapping("/openapi/mobile/playItem/activityOpiList.xhtml")
	public String activityList(Long activityid, Long cinemaid, ModelMap model, HttpServletRequest request){
		if(activityid==null || cinemaid==null)return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(activityid);
		if(!code.isSuccess())  return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		ErrorCode<List<RemoteActivityMpi>> code2 = synchActivityService.getRemoteActiviyMpiList(activityid);
		List<RemoteActivityMpi> ampiList = new ArrayList<RemoteActivityMpi>();
		if(code2.isSuccess()) ampiList = code2.getRetval();
		Map<Long, String> guestMap = new HashMap<Long, String>();
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		List<MoviePlayItem> playItemList = new ArrayList<MoviePlayItem>();
		for(RemoteActivityMpi ampi : ampiList){
			MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, ampi.getMpid());
			if(mpi!=null && cinemaid.equals(mpi.getCinemaid())){
				OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), false);
				if(opi!=null && !opi.isExpired() && opi.isOpenToPartner()) {
					if(filter.excludeOpi(opi)) opi = null;
					playItemList.add(mpi);
				}
			}
			guestMap.put(ampi.getMpid(), ampi.getGuest());
		}
		Collections.sort(playItemList, new MultiPropertyComparator(new String[]{"playdate" ,"playtime"}, new boolean[]{true, true}));
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for (MoviePlayItem mpi : playItemList) {
			OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
			Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi, opi);
			int seatStatus = 0;
			String moviename = null;
			String cinemaname = null;
			if(opi!=null) {
				if(opi.getCostprice()!=null && !filter.excludeOpi(opi)){
					resMap.put("unbookingReason",  OpiConstant.getUnbookingReason(opi));
					if(opi.isOrder()) seatStatus = 1;
				}
				resMap.put("ticketstatus", opi.getSeatStatus());
				resMap.put("remark", opi.getRemark());
				cinemaname = opi.getCinemaname();
				moviename = opi.getMoviename();
			}else {
				Movie movie = daoService.getObject(Movie.class, mpi.getMovieid());
				Cinema cinema = daoService.getObject(Cinema.class, mpi.getCinemaid());
				cinemaname = cinema.getName();
				moviename = movie.getName();
				resMap.put("unbookingReason",  "该场次未开放售票");
			}
			resMap.put("moviename", moviename);
			resMap.put("cinemaname", cinemaname);
			resMap.put("guest", guestMap.get(opi.getMpid()));
			resMap.put("seatStatus", seatStatus);
			resMapList.add(resMap);
		}
		model.put("resMapList", resMapList);
		putPlayItemListNode(model);
		initField(model, request);
		return getOpenApiXmlList(model);
	}
	/**
	 * 午夜场次
	 */
	@RequestMapping("/openapi/mobile/playItem/midnightOpiList.xhtml")
	public String midnightOpiList(Long movieid, Long cinemaid, Date playdate, ModelMap model, HttpServletRequest request){
		playdate = DateUtil.addDay(playdate, 1);
		List<MoviePlayItem> mpiList = mcpService.getCurMpiList(cinemaid, movieid, playdate);
		String midNight = "06:00";
		ApiUser partner = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getApiUser();
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		for(MoviePlayItem mpi : mpiList){
			if(mpi.getPlaytime().compareTo(midNight)<=0){
				OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
				Map<String, Object> resMap = GewaApiMovieHelper.getMpiData(mpi, opi);
				int seatStatus = 0;
				if(opi!=null && opi.getCostprice()!=null && !filter.excludeOpi(opi)) {
					resMap.put("unbookingReason",  OpiConstant.getUnbookingReason(opi));
					if(opi.isOrder()) seatStatus = 1;
					resMap.put("ticketstatus", opi.getSeatStatus());
					resMap.put("remark", opi.getRemark());
					resMap.put("cinemaname", opi.getCinemaname());
					resMap.put("moviename", opi.getMoviename());
				}else {
					resMap.put("unbookingReason",  "该场次未开放售票");
				}
				resMap.put("seatStatus", seatStatus);
				resMapList.add(resMap);
			}
		}
		model.put("resMapList", resMapList);
		putPlayItemListNode(model);
		initField(model, request);
		return getOpenApiXmlList(model);
	}
	
	
	@RequestMapping("/openapi/mobile/playItem/cacheSeat.xhtml")
	public String cacheSeat(Long mpid, ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		if(!opi.isOpenToPartner()) return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, "本场不接受（或暂停）预订！");
		Map<String,String> otherInfo = JsonUtils.readJsonToMap(opi.getOtherinfo());
		String mealoption = otherInfo.get(OpiConstant.MEALOPTION);
		if(!StringUtils.equals(mealoption, "notuse")){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, auth.getApiUser().getId());
			if(goodsGift!=null) {
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				model.put("goodsGift", goodsGift);
				model.put("bindGoods", goods);
			}else {
				List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, opi.getCinemaid(), 0, 5);
				GoodsFilterHelper.goodsFilter(goodsList, auth.getApiUser().getId());
				model.put("optionalGoods", goodsList);
			}
		}

		ErrorCode booking = ticketOrderService.checkPauseBooking(opi);
		if(!booking.isSuccess()){
			return getErrorXmlView(model, ApiConstant.CODE_OPI_CLOSED, booking.getMsg());
		}
		ErrorCode result = ticketQueueService.isPartnerAllowed(partner.getId(), opi.getCinemaid());
		if(!result.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, booking.getMsg());
		model.put("partner", partner);
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		model.put("priceHelper", priceHelper);
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		String[] seatMap = openPlayService.getOpiSeatMap(mpid);
		if(seatMap==null) {
			model.put("seatMap", room.getSeatmap());
		}else{
			model.put("seatMap", seatMap[0]);
		}
		model.put("cacheSeat", true);
		model.put("opi", opi);
		model.put("room", room);
		return getXmlView(model, "inner/mobile/opiSeatInfo.vm");
	}
	
	/**
	 * 验证选择座位的合法性
	 */
	@RequestMapping("/openapi/mobile/playItem/validseatPostion.xhtml")
	public String validseatPostion(Long mpid, String seatLabel, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi==null) return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR,"场次不存在");
		if(StringUtils.isBlank(seatLabel)) return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR, "座位为空");
		ErrorCode<List<String>> lockCode = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, false);
		if(!lockCode.isSuccess()) return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR, lockCode.getMsg());
		ErrorCode<String> code = ticketOrderService.isValidateSeatPosition(opi, seatLabel, lockCode.getRetval());
		if(!code.isSuccess()) return getErrorXmlView(model,ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		return getSuccessXmlView(model);
	}
}
