package com.gewara.web.action.inner.partner;

import java.util.ArrayList;
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
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.helper.ticket.CloseRuleOpiFilter;
import com.gewara.helper.ticket.OpiFilter;
import com.gewara.helper.ticket.PartnerPriceHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.service.bbs.BlogService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.filter.OpenApiPartnerAuthenticationFilter;
@Controller
public class OpenApiPartnerOpiController extends BaseOpenApiPartnerController{
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	//某个城市所有开放购票影院列表
	@RequestMapping("/openapi/partner/openCinemaListByCitycode.xhtml")
	public String openCinemaList(String citycode, ModelMap model, HttpServletRequest request) {
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<Cinema> cinemaList = mcpService.getBookingCinemaList(citycode);
		getCienmaListMap(cinemaList, auth.getUserExtra().hasCinemaHighFields(), model, request);
		putCinemaListNode(model);
		return getOpenApiXmlList(model);
	}
	//根据城市和影片获取购票影院列表
	@RequestMapping("/openapi/partner/opcList.xhtml")
	public String opcList(String citycode, Long movieid, Date playdate, ModelMap model, HttpServletRequest request) {
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<Cinema> cinemaList = partnerService.getOpenCinemaList(partner, citycode, movieid, playdate);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterCinema(cinemaList);
		getCienmaListMap(cinemaList, auth.getUserExtra().hasCinemaHighFields(), model, request);
		putCinemaListNode(model);
		return getOpenApiXmlList(model);
	}
	//根据城市获取购票影片列表
	@RequestMapping("/openapi/partner/opmList.xhtml")
	public String opmList(String citycode, Long cinemaid, Date playdate, ModelMap model, HttpServletRequest request) {
		ApiAuth auth = OpenApiPartnerAuthenticationFilter.getApiAuth();
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<Movie> movieList = partnerService.getOpenMovieList(partner, citycode, cinemaid, playdate);
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		CloseRuleOpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.filterMovie(movieList);
		mcpService.sortMoviesByMpiCount(citycode, movieList);
		getMovieListMap(movieList, auth.getUserExtra(), model, request);
		putMovieListNode(model);
		return getOpenApiXmlList(model);
	}
	
	//购票日期
	@RequestMapping("/openapi/partner/playdateList.xhtml")
	public String opendateList(Long cinemaid, Long movieid, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影院不存在");
		}
		ApiUser partner = OpenApiPartnerAuthenticationFilter.getApiAuth().getApiUser();
		if(!partner.supportsCity(cinema.getCitycode())) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不支持城市" + cinema.getCitycode());
		List<Date> playdateList = openPlayService.getCinemaAndMovieOpenDateList(cinemaid, movieid);
		model.put("playdateList", playdateList);
		return getXmlView(model, "inner/partner/playdateList.vm");
	}
	
	//通过影院ID、影片ID与播放日期检索购票场次
	@RequestMapping("/openapi/partner/opiList.xhtml")
	public String opendateList(String citycode, Long cinemaid, Long movieid, Date playdate, ModelMap model, HttpServletRequest request) {
		ApiUser partner = OpenApiPartnerAuthenticationFilter.getApiAuth().getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<OpenPlayItem> opiList = partnerService.getPartnerOpiList(partner, citycode, cinemaid, movieid, playdate);
		PartnerPriceHelper priceHelper = new PartnerPriceHelper();
		List<PartnerCloseRule> pcrList = partnerService.getCloseRuleList();
		OpiFilter filter = new CloseRuleOpiFilter(partner, pcrList);
		filter.applyFilter(opiList);
		Long partnerid = partner.getId();
		List<OpenPlayItem> opiList2 = new ArrayList<OpenPlayItem>();
		for(OpenPlayItem opi : opiList){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, partnerid);
			if(goodsGift==null){
				opiList2.add(opi);
			}
		}
		model.put("priceHelper", priceHelper);
		model.put("opiList", opiList2);
		getOpiListMap(opiList2, model, request);
		model.put("root", "opiList");
		model.put("nextroot", "opi");
		return getOpenApiXmlList(model);
	}
	
	//场次座位信息
	@RequestMapping("/openapi/partner/opiSeatInfo.xhtml")
	public String opiSeatInfo(Long mpid, ModelMap model, HttpServletRequest request) {
		ApiUser partner = OpenApiPartnerAuthenticationFilter.getApiAuth().getApiUser();
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		ErrorCode code = addOpiSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		getOpiMap(opi, model, request);
		return getXmlView(model, "inner/partner/opiSeatInfo.vm");
	}
	/**
	 * 根据场次id查询场次信息
	 */
	@RequestMapping("/openapi/partner/opiDetail.xhtml")
	public String opiDetail(Long mpid, ModelMap model, HttpServletRequest request) {
		OpenPlayItem opi=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if (opi == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "数据不存在！");
		Map<String, Object> resMap = GewaApiMovieHelper.getOpiData(opi);
		resMap.put("cinemaname", opi.getCinemaname());
		resMap.put("moviename", opi.getMoviename());
		resMap.put("remark", opi.getRemark());
		model.put("resMap", resMap);
		model.put("root", "openPlayItem");
		initField(model, request);
		return getOpenApiXmlDetail(model);
	}
	//场次锁定的座位信息
	@RequestMapping("/openapi/partner/opiLockSeatInfo.xhtml")
	public String opiLockedSeat(Long mpid, ModelMap model, HttpServletRequest request) {
		ApiUser partner = OpenApiPartnerAuthenticationFilter.getApiAuth().getApiUser();
		if(mpid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "参数不正确！");
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在或已删除！");
		ErrorCode code = addOpiLockedSeatListData(opi, partner, model);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		getOpiMap(opi, model, request);
		return getXmlView(model, "inner/partner/opiLockedSeat.vm");
	}
	
	//查询最近更新的影厅列表
	@RequestMapping("/openapi/partner/updateRoomList.xhtml")
	public String roomList(String citycode, Date updatedate, Long cinemaid, ModelMap model) {
		ApiUser partner = OpenApiPartnerAuthenticationFilter.getApiAuth().getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getCitycode();
		}
		List<CinemaRoom> roomList = null;
		if(cinemaid!=null){
			roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", new Long(cinemaid));
		}else{
			roomList = openPlayService.updateRoomList(citycode, updatedate);
		}
		model.put("roomList", roomList);
		return getXmlView(model, "inner/partner/roomList.vm");
	}
		
	//通过影厅ID查询座位信息
	@RequestMapping("/openapi/partner/roomSeatInfo.xhtml")
	public String roomList(Long roomid, ModelMap model) {
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		List<RoomSeat> seatList = openPlayService.getSeatListByRoomId(roomid);
		Map<Integer, String> lineMap = new HashMap<Integer, String>();
		Map<String, RoomSeat> seatMap = BeanUtil.beanListToMap(seatList, "position");
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		RoomSeat seat = null; String status;
		for(int i=1; i<= room.getLinenum(); i++){
			List<String> seatRankList = new ArrayList<String>();
			for(int j=1; j<= room.getRanknum(); j++){
				seat = seatMap.get(i + ":" + j);
				if(seat == null){
					status = "ZL"; //走廊
				}else{
					status = seat.getSeatrank();
					rowMap.put(i, seat.getSeatline());
				}
				seatRankList.add(status);
			}
			lineMap.put(i, StringUtils.join(seatRankList, ","));
		}
		model.put("lineMap", lineMap);
		model.put("room", room);
		model.put("rowMap", rowMap);
		return getXmlView(model, "inner/partner/roomSeatInfo.vm");
	}
	
	/**
	 * 获取影院取票帮助
	 */
	@RequestMapping("/openapi/partner/ticketHelp.xhtml")
	public String getTicketHelp(Long cinemaid, ModelMap model){
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cinemaid);
		if(profile.getTopicid()==null){
			 return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有取票帮助相关信息");
		}
		String diaryContent = blogService.getDiaryBody(profile.getTopicid());
		return getSingleResultXmlView(model, diaryContent);
	}
}
