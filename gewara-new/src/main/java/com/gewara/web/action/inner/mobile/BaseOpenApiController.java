package com.gewara.web.action.inner.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.api.GewaApiBbsHelper;
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.helper.api.GewaApiOrderHelper;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.content.News;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.MessageService;
import com.gewara.support.ErrorCode;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.web.action.api2mobile.ApiTicketBaseController;
import com.gewara.xmlbind.DataWrapper;

public class BaseOpenApiController extends ApiTicketBaseController{
	@Autowired@Qualifier("messageService")
	protected MessageService messageService;
	@Autowired@Qualifier("config")
	protected Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	protected String getMobilePath(){
		return config.getString("mobilePath");
	}
	protected ErrorCode<DataWrapper> getDataWrapper(HttpResult result){
		if(result == null || !result.isSuccess()) return ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "网络异常！");
		DataWrapper wrapper = (DataWrapper) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", DataWrapper.class), result.getResponse());
		if(wrapper==null) return ErrorCode.getFailure("解析错误！");
		if(!wrapper.isSuccess()) return ErrorCode.getFailure(wrapper.getError());
		return ErrorCode.getSuccessReturn(wrapper);
	}
	protected String getSuccessXmlView(ModelMap model){
		return getSingleResultXmlView(model, "success");
	}
	//查询请求参数中的fields
	protected void initField(ModelMap model, HttpServletRequest request){
		String fields = request.getParameter("fields");
		List<String> fieldList = new ArrayList();
		boolean hasField = false;
		if(StringUtils.isNotBlank(fields)){
			fieldList.addAll(Arrays.asList(fields.split(",")));
			hasField = true;
		}
		model.put("hasField", hasField);
		model.put("fieldList", fieldList);
	}
	protected void putDetail(Map<String, Object> resMap, ModelMap model, HttpServletRequest request){
		initField(model, request);
		model.put("resMap", resMap);
	}
	protected void putList(List<Map<String, Object>> resMapList, ModelMap model, HttpServletRequest request){
		initField(model, request);
		model.put("resMapList", resMapList);
	}
	protected String getOpenApiXmlDetail(ModelMap model){
		return getXmlView(model, "inner/partner/detail.vm");
	}
	protected String getOpenApiXmlList(ModelMap model){
		return getXmlView(model, "inner/partner/list.vm");
	}
	protected String getOpenApiXmlDetail(Map<String, Object> resMap, String root, ModelMap model, HttpServletRequest request){
		initField(model, request);
		model.put("root", root);
		model.put("resMap", resMap);
		return getXmlView(model, "inner/partner/detail.vm");
	}
	protected String getOpenApiXmlList(List<Map<String, Object>> resMapList, String nodes, ModelMap model, HttpServletRequest request){
		initField(model, request);
		if(StringUtils.isNotBlank(nodes)) {
			String[] node = StringUtils.split(nodes, ",");
			model.put("root", node[0]);
			model.put("nextroot", node[1]);
		}
		model.put("resMapList", resMapList);
		return getXmlView(model, "inner/partner/list.vm");
	}
	
	protected void putCommentNode(ModelMap model){
		model.put("root", "comment");
	}
	protected void putCommentListNode(ModelMap model){
		model.put("root", "commentList");
		model.put("nextroot", "comment");
	}
	
	protected void putDiaryNode(ModelMap model){
		model.put("root", "diary");
	}
	protected void putDiaryListNode(ModelMap model){
		model.put("root", "diaryList");
		model.put("nextroot", "diary");
	}
	
	protected void putPictureListNode(ModelMap model){
		model.put("root", "pictureList");
		model.put("nextroot", "picture");
	}
	

	protected void putActivityListNode(ModelMap model){
		model.put("root", "activityList");
		model.put("nextroot", "activity");
	}
	
	protected void putGoodsNode(ModelMap model){
		model.put("root", "goods");
	}
	protected void putGoodsListNode(ModelMap model){
		model.put("root", "goodsList");
		model.put("nextroot", "goods");
	}
	
	
	//影片的数据
	protected Map<String, Object> getMovieData(Movie movie, String logo, boolean hasMovieHighFields){
		Map<String, Object> params = GewaApiMovieHelper.getMovieData(movie, logo);
		//高级字段 只对部分合作商开放
		if(hasMovieHighFields){
			String gcedition = "";
			String edition = movie.getEdition();
			if(StringUtils.contains(edition, "4D")) gcedition = "4D";
			else if(StringUtils.contains(edition, "IMAX3D")) gcedition = "IMAX3D";
			else if(StringUtils.contains(edition, "IMAX")) gcedition = "IMAX";
			else if(StringUtils.contains(edition, "3D")) gcedition = "3D";
			params.put("generalmark", getMovieMark(movie));
			params.put("gcedition", gcedition);
			params.put("boughtcount", movie.getBoughtcount());
		}
		return params;
	}
	
	//------------------------------------------------------------------------------------------------------
	
	//影院数据
	protected Map<String, Object> getCinemaData(Cinema cinema, String logo, String firstpic, boolean hasCinemaHighFields){
		Map<String, Object> params = GewaApiMovieHelper.getCinemaData(cinema, logo, firstpic);
		//高级字段 只对部分合作商开放
		if(hasCinemaHighFields){
			Map<String, String> subwaylineMap = placeService.getSubwaylineMap(cinema.getCitycode());
			params.put("feature", cinema.getFeature());
			params.put("stationname", cinema.getStationname());
			params.put("exitnumber", cinema.getExitnumber());
			params.put("transport", cinema.getRTransport());
			params.put("generalmark", getPlaceGeneralmark(cinema));
			params.put("linename", cinema.getLineName(subwaylineMap));
			params.put("otherinfo", cinema.getOtherinfo());
			params.put("popcorn", StringUtils.equals(cinema.getPopcorn(), CinemaProfile.POPCORN_STATUS_Y)?1:0);
		}
		return params;
	}
	
	//------------------------------------------------------------------------------------------------------
	//电影订单数据
	protected void getTicketOrderMap(TicketOrder order, ModelMap model, HttpServletRequest request){
		if(order==null) return;
		Map<String, Object> resMap = GewaApiOrderHelper.getTicketOrderMap(order);
		putDetail(resMap, model, request);
	}
	protected void getTicketOrderListMap(List<TicketOrder> orderList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(TicketOrder order : orderList){
			Map<String, Object> params = GewaApiOrderHelper.getTicketOrderMap(order);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	
	protected Map<String, Object> getNewsMap(News news){
		String mobilePath = getMobilePath();
		Map<String, Object> params = GewaApiBbsHelper.getNews(news, mobilePath + news.getLimg(), mobilePath + news.getSmallLogo());
		return params;
	}
	protected void getNewsMap(News news, ModelMap model, HttpServletRequest request){
		Map<String, Object> params = getNewsMap(news);
		putDetail(params, model, request);
		model.put("root", "news");
	}
	protected void getNewsListMap(List<News> newsList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		String mobilePath = getMobilePath();
		for(News news : newsList){
			Map<String, Object> params = GewaApiBbsHelper.getNews(news, mobilePath + news.getLimg(), mobilePath + news.getSmallLogo());
			resMapList.add(params);
		}
		putList(resMapList, model, request);
		model.put("root", "newsList");
		model.put("nextroot", "news");
	}
	protected Map<String, Object> getMovieOrderMap(TicketOrder order, boolean isNewVersion) {
		Map<String, Object> resMap = GewaApiOrderHelper.getTicketOrderMap(order);
		String passmsg = "";
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(order.getId());
		if(order.isPaidSuccess()) passmsg= messageService.getOrderPassword(order, seatList);
		Cinema cinema = daoService.getObject(Cinema.class, order.getCinemaid());
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, order.getCinemaid());
		Movie movie = daoService.getObject(Movie.class, order.getMovieid());
		Long diaryid = null;
		if(profile!=null) diaryid = profile.getTopicid();
		int del = 0;
		resMap.put("playtime", order.getPlaytime());
		ErrorCode<String> code = GewaOrderHelper.validDelGewaOrder(order, order.getPlaytime());
		if(code.isSuccess()) del = 1;
		int definePaper = 0;
		if(order.isAllPaid() && profile != null && profile.hasDefinePaper()) definePaper = 1;
		int maxminute = 15;
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
		if(opi!=null) maxminute = opi.gainLockMinute();
		if(definePaper == 1 && order.getPartnerid().equals(PartnerConstant.IPHONE)){
			if(isNewVersion){
				definePaper = 1;
			}else {
				definePaper = 0;
			}
		}
		int mobileOrder = 0;
		if(order.getPartnerid()!=null && order.getPartnerid()>1){
			mobileOrder = 1;
		}
		int minutes = 90;
		if(movie.getVideolen()!=null){
			minutes = movie.getVideolen();
		}
		resMap.put("playendtime", DateUtil.addMinute(order.getPlaytime(), minutes));
		resMap.put("mobileOrder", mobileOrder);
		resMap.put("definePaper", definePaper);
		resMap.put("maxminute", maxminute);
		resMap.put("delenable", del);
		resMap.put("passmsg", passmsg);
		resMap.put("diaryid", diaryid);
		resMap.put("moviename", movie.getName());
		resMap.put("cinemaname", cinema.getName());
		resMap.put("movielogo", getMobilePath() + movie.getLimg());
		resMap.putAll(getBaseInfoMap(cinema));
		return resMap;
	}
	protected Map<String, Object> getBaseInfoMap(BaseInfo info) {
		Map<String, Object> resMap = new HashMap<String, Object>();
		Map<String, String> subMap = placeService.getSubwaylineMap(info.getCitycode());
		resMap.put("address", info.getAddress());
		resMap.put("pointx", info.getPointx());
		resMap.put("pointy", info.getPointy());
		resMap.put("transport", info.getRTransport());
		resMap.put("stationname", info.getStationname());
		resMap.put("exitnumber", info.getExitnumber());
		resMap.put("linename", info.getLineName(subMap));
		return resMap;
	}
}
