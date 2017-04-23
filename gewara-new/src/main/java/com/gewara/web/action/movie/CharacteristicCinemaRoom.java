package com.gewara.web.action.movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.CharacteristicType;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.PictureService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MarkHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class CharacteristicCinemaRoom  extends AnnotationController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	
	@RequestMapping("/cinema/imax.xhtml")
	public String characteristicRoom(Long cId,Long mId,String ctype,HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addSingleString("ctype", ctype);
			pageParams.addLong("mId", mId);
			pageParams.addLong("cId",cId);
			PageView pageView = pageCacheService.getPageView(request, "cinema/imax.xhtml", pageParams, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		Cinema cinema = null;
		if(cId != null){
			cinema = daoService.getObject(Cinema.class,cId);
		}
		List<String> ctypeList = null;
		if(cinema != null){
			citycode = cinema.getCitycode();
			WebUtils.setCitycode(request, citycode, response);
		}
		Map<String,String> map = mongoService.findOne(MongoData.NS_CITY_ROOM_CHARACTERISTIC, MongoData.SYSTEM_ID, citycode);
		if(map == null){
			ctypeList = new ArrayList();
		}else{
			ctypeList = Arrays.asList(StringUtils.split(map.get("characteristic"),","));
		}
		ctype = this.getDefaultCtype(ctypeList, ctype, model);
		if(StringUtils.isBlank(ctype)){
			return show404(model , "该城市下暂时还没该特色影厅！");
		}
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, mcpService.getCinemaIdListByRoomCharacteristic(ctype,citycode));
		if(cinema == null && !cinemaList.isEmpty()){
			cinema = cinemaList.get(0);
		}
		List<Picture> pictureList = new LinkedList<Picture>();
		if(cinema != null){
			List<Long> roomIdList = mcpService.getRoomIdListByCinemaAndCtype(cinema.getId(), ctype);
			if(!roomIdList.isEmpty()){
				model.put("cinemaRoom", this.daoService.getObject(CinemaRoom.class, roomIdList.get(0)));
			}
			for(Long id : roomIdList){
				pictureList.addAll(pictureService.getPictureListByRelatedid("characterroom", id, 0, 200));
			}
		}
		model.put("ctypePictureList",pictureService.getPictureListByRelatedid(ctype, 0L, 0, 200));
		model.put("roomPictureList",pictureList);
		model.put("ctype", ctype);
		model.put("cinema", cinema);
		sortHotCinemaListByMark(cinemaList);
		model.put("cinemaList",cinemaList);
		model.put("mId", mId);
		model.put("ctypeName", CharacteristicType.characteristicNameMap.get(ctype));
		model.put("cId", cId);
		return "movie/specialRoom.vm";
	}
	
	@RequestMapping("/ajax/movie/searchCharacteristicRoomOpi.xhtml")
	public String searchCharacteristicRoomOpi(Long mId,Long cId,String ctype,HttpServletRequest request,
			HttpServletResponse response,ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams pageParams = new PageParams();
			pageParams.addSingleString("ctype", ctype);
			pageParams.addLong("mId", mId);
			pageParams.addLong("cId",cId);
			PageView pageView = pageCacheService.getPageView(request, "ajax/movie/searchCharacteristicRoomOpi.xhtml", pageParams, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		if(cId == null){
			model.put("pageView", new PageView(DateUtil.addMinute(new Date(), pageCacheService.getCacheMin("ajax/movie/searchCharacteristicRoomOpi.xhtml")).getTime(), ""));
			return "pageView.vm";
		}
		List<OpenPlayItem> opiList = openPlayService.getOpiList(citycode, cId, mId, null, null, true);
		List<Long> roomIdList = mcpService.getRoomIdListByCinemaAndCtype(cId, ctype);
		Map<Date,Map<Long,List<OpenPlayItem>>> opiMap = new HashMap<Date,Map<Long,List<OpenPlayItem>>>();
		Set<Long> movieIdList = new HashSet<Long>();
		for(OpenPlayItem opi : opiList){
			if(roomIdList.contains(opi.getRoomid())){
				Date date = DateUtil.parseDate(DateUtil.formatDate(opi.getPlaytime()));
				Map<Long,List<OpenPlayItem>> tmpMap = opiMap.get(date);
				if(tmpMap == null){
					tmpMap = new HashMap<Long,List<OpenPlayItem>>();
					opiMap.put(date, tmpMap);
				}
				List<OpenPlayItem> tmpList = tmpMap.get(opi.getMovieid());
				if(tmpList == null){
					tmpList = new LinkedList<OpenPlayItem>();
					tmpMap.put(opi.getMovieid(), tmpList);
				}
				tmpList.add(opi);
				movieIdList.add(opi.getMovieid());
			}
		}
		List<Date> dateList = new ArrayList<Date>(opiMap.keySet());
		Collections.sort(dateList);
		Map dataMap = new HashMap();
		dataMap.put("dateList", BeanUtil.getSubList(dateList,0,5));
		dataMap.put("opiMap",opiMap);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
		dataMap.put("movieMap", BeanUtil.beanListToMap(movieList, "id"));
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		for(Long mid : movieIdList){
			markCountMap.put(mid, markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, mid));
		}
		dataMap.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		dataMap.put("markCountMap", markCountMap);
		String result = velocityTemplate.parseTemplate("movie/wide_ajax_ctype_opi.vm", dataMap);
		model.put("pageView", new PageView(DateUtil.addMinute(new Date(), pageCacheService.getCacheMin("ajax/movie/searchCharacteristicRoomOpi.xhtml")).getTime(), result));
		return "pageView.vm";
	}
	
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
	
	private String getDefaultCtype(List<String> ctypeList,String ctype,ModelMap model){
		List<String> list = new LinkedList<String>();
		for(String c : CharacteristicType.cTypeList){
			if(ctypeList.contains(c)){
				list.add(c);
			}
		}
		if(list.size() == 0){
			return null;
		}
		model.put("ctypeList", list);
		if(StringUtils.isBlank(ctype) || !list.contains(ctype)){
			ctype = list.get(0);
		}
		return ctype;
	}
}
