package com.gewara.web.action.drama;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.json.PageView;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.service.content.VideoService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class DramaStarInfoController extends BaseDramaController {
	private static final String TAG_DRAMASTAR = DramaStar.TAG_DRAMASTAR;
	
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	
	@Autowired
	private VideoService videoService;
	@Autowired
	private PageCacheService pageCacheService;
	
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;

	/**
	 *  明星详细 / 剧团详细
	 */
	@RequestMapping("/drama/star/stardetail.xhtml")
	public String baseStarDetail(ModelMap model, Long starid, HttpServletRequest request, HttpServletResponse response){
		DramaStar dramaStar  = daoService.getObject(DramaStar.class, starid);
		if(dramaStar==null) return show404(model, "明星/剧团不存在或已经删除！");
		cacheDataService.getAndSetIdsFromCachePool(DramaStar.class, starid);
		cacheDataService.getAndSetClazzKeyCount(DramaStar.class, starid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){
			PageParams params = new PageParams();
			params.addLong("starid", starid);
			PageView pageView = pageCacheService.getPageView(request, "drama/star/stardetail.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("dramaStar", dramaStar);
		headerInfo(model, citycode, dramaStar);
		rightDramaStarData(model, dramaStar, citycode);
		if(DramaStar.TYPE_TROUPE.equals(dramaStar.getStartype())){
			model.put("type", DramaStar.TYPE_TROUPE);
			return "drama/star/wide_troupeDetail.vm";
		}
		return "drama/star/wide_dramaStarDetail.vm";
	}

	private void headerInfo(ModelMap model, String citycode, DramaStar dramaStar){
		Integer pictureCount = commonService.getPictureCount().get(dramaStar.getId()+TAG_DRAMASTAR);
		model.put("pictureCount", pictureCount);
		Integer walaCount = commonService.getCommentCount().get(dramaStar.getId()+TAG_DRAMASTAR);
		model.put("walaCount", walaCount);
		model.put("troupecount", dramaStarService.getStarCountByTroupid(dramaStar.getId()));
		// 成员
		if(DramaStar.TYPE_TROUPE.equals(dramaStar.getStartype())){
			List<DramaStar> headTroupeList = dramaStarService.getStarListByTroupid(dramaStar.getId(), "clickedtimes", false, 0, 20);
			model.put("headTroupeList", headTroupeList);
		}else{
			DramaStar troupe = daoService.getObject(DramaStar.class, dramaStar.getTroupe());
			model.put("troupe", troupe);
		}
		List<Drama> curDramaList = openDramaService.getDramaByStarid(dramaStar.getId(), 0, 2);
		relatedDramasByStar(citycode, curDramaList, model);
		
		List<Map<String, String>> historyDramaList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
		model.put("historyDramaList", historyDramaList);
		List<Comment> commentList = commentService.getCommentListByRelatedId(TAG_DRAMASTAR, null, dramaStar.getId(), null, 0, 6);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("commentList", commentList);
	}
	private void relatedDramasByStar(String citycode, List<Drama> curDramaList, ModelMap model){
		Map<Long, List<Theatre>> theatreMap = new HashMap<Long, List<Theatre>>();
		Map<Long, List<Integer>> priceListMap = new HashMap<Long, List<Integer>>();
		if(curDramaList != null && curDramaList.size() > 0){
			for(Drama drama : curDramaList){
				List<Theatre> listTheatre = dramaPlayItemService.getTheatreList(citycode, drama.getId(), true, 2);
				List<Integer> listInteger = dramaPlayItemService.getPriceList(null, drama.getId(), null, null, true);
				if(null != listTheatre)theatreMap.put(drama.getId(), listTheatre);
				if(null != listInteger)priceListMap.put(drama.getId(), listInteger);
			}
			model.put("curDramaList", curDramaList);
			model.put("theatreMap", theatreMap);
			model.put("priceListMap", priceListMap);
		}
	}
	private void rightDramaStarData(ModelMap model, DramaStar dramaStar, String citycode){
		if(StringUtils.equals(dramaStar.getStartype(), DramaStar.TYPE_TROUPE)){
			rightTroupeData(model, dramaStar, citycode);
		}else{
			rightStarData(model, dramaStar);
		}
		List<Long> bookingList = openDramaService.getCurDramaidList(citycode);
		model.put("bookingList", bookingList);
		List<Long> openSeatList = openDramaService.getCurDramaidList(citycode, OdiConstant.OPEN_TYPE_SEAT); 
		model.put("openseatList", openSeatList);
		List<Drama> hotDramaList = daoService.getObjectList(Drama.class, bookingList);
		Collections.sort(hotDramaList, new MultiPropertyComparator(new String[]{"boughtcount"}, new boolean[]{false}));
		hotDramaList = BeanUtil.getSubList(hotDramaList, 0, 5);
		model.put("hotDramaList", hotDramaList);
		Map<Long, List<Theatre>> theatreMap = new HashMap<Long, List<Theatre>>();
		Map<Long, List<Integer>> priceListMap = new HashMap<Long, List<Integer>>();
		for(Drama curDrama : hotDramaList){
			theatreMap.put(curDrama.getId(), dramaPlayItemService.getTheatreList(citycode, curDrama.getId(), true, 2));
			priceListMap.put(curDrama.getId(), dramaPlayItemService.getPriceList(null, curDrama.getId(), null, null, false));
		}
		model.put("theatreMap", theatreMap);
		model.put("priceListMap", priceListMap);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
	}
	
	private void rightStarData(ModelMap model, DramaStar dramaStar){
		List<DramaStar> troupeList = dramaStarService.getStarListByTroupid(dramaStar.getTroupe(), "avggeneral", false, 0, 3);
		if(!troupeList.isEmpty()){
			model.put("troupeList", troupeList);
			Map<Long, List<Map<String,String>>> troupeMap = new HashMap<Long, List<Map<String,String>>>();
			model.put("troupeMap", troupeMap);
			for (DramaStar dramaStar2 : troupeList) {
				List<Map<String, String>> historyMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar2.getRepresentativeRelate());
				troupeMap.put(dramaStar2.getId(), historyMapList);
			}
		}
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TAG_DRAMASTAR, dramaStar.getId(), 0, -1);
		model.put("pictureList", pictureList);
	}
	
	private void rightTroupeData(ModelMap model, DramaStar dramaStar, String citycode){
		List<DramaStar> troupeList = dramaStarService.getStarListByTroupid(dramaStar.getId(), "avggeneral", false, 0, 3);
		if(!troupeList.isEmpty()){
			model.put("troupeList", troupeList);
			Map<Long, List<Map<String,String>>> troupeMap = new HashMap<Long, List<Map<String,String>>>();
			model.put("troupeMap", troupeMap);
			for (DramaStar dramaStar2 : troupeList) {
				List<Map<String, String>> historyMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar2.getRepresentativeRelate());
				troupeMap.put(dramaStar2.getId(), historyMapList);
			}
		}
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA,RemoteActivity.TIME_CURRENT,TagConstant.TAG_THEATRE, null, null,null,"duetime",0,3);
		if(code.isSuccess()){
			model.put("activityList", code.getRetval());
		}
	}
	
	@RequestMapping("/drama/ajax/starDetail.xhtml")
	public String starDetail(ModelMap model, Long starid, HttpServletRequest request, HttpServletResponse response){
		DramaStar dramaStar  = daoService.getObject(DramaStar.class, starid);
		if(dramaStar==null) return showJsonError(model, "明星/剧团不存在或已经删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){
			PageParams params = new PageParams();
			params.addLong("starid", starid);
			PageView pageView = pageCacheService.getPageView(request, "drama/ajax/starDetail.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("dramaStar", dramaStar);
		List<Map<String, String>> historyDramaList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
		model.put("historyDramaList", historyDramaList);
		Map<String, Drama> dramaMap = new HashMap<String, Drama>();
		model.put("dramaMap", dramaMap);
		if(!CollectionUtils.isEmpty(historyDramaList)){
			for (Map<String, String> dataMap : historyDramaList) {
				String id = dataMap.get("id");
				Drama drama = daoService.getObject(Drama.class, Long.valueOf(id));
				if(drama != null){
					dramaMap.put(id, drama);
				}
			}
		}
		Integer pictureCount = commonService.getPictureCount().get(dramaStar.getId()+ TAG_DRAMASTAR);
		model.put("pictureCount", pictureCount);
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TAG_DRAMASTAR, starid, 0, 5);
		model.put("pictureList", pictureList);
		return "include/drama/mod_troupeTicket.vm";
	}
	
	@RequestMapping("/drama/ajax/starTroupe.xhtml")
	public String starTroupe(ModelMap model, Long starid, HttpServletRequest request, HttpServletResponse response){
		DramaStar dramaStar  = daoService.getObject(DramaStar.class, starid);
		if(dramaStar==null) return showJsonError(model, "明星/剧团不存在或已经删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){
			PageParams params = new PageParams();
			params.addLong("starid", starid);
			PageView pageView = pageCacheService.getPageView(request, "drama/ajax/starTroupe.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("dramaStar", dramaStar);
		List<DramaStar> troupeList = dramaStarService.getStarListByTroupid(dramaStar.getId(), "avggeneral", false, 0, -1);
		if(!troupeList.isEmpty()){
			model.put("troupeList", troupeList);
			Map<Long, List<Map<String,String>>> troupeMap = new HashMap<Long, List<Map<String,String>>>();
			model.put("troupeMap", troupeMap);
			for (DramaStar dramaStar2 : troupeList) {
				List<Map<String, String>> historyMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar2.getRepresentativeRelate());
				troupeMap.put(dramaStar2.getId(), historyMapList);
			}
		}
		
		return "include/drama/mod_troupeMember.vm";
	}
	
	@RequestMapping("/drama/ajax/starWala.xhtml")
	public String starWala(ModelMap model, Long starid, HttpServletRequest request, HttpServletResponse response){
		DramaStar dramaStar  = daoService.getObject(DramaStar.class, starid);
		if(dramaStar==null) return showJsonError(model, "明星/剧团不存在或已经删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){
			PageParams params = new PageParams();
			params.addLong("starid", starid);
			PageView pageView = pageCacheService.getPageView(request, "drama/ajax/starWala.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		return "include/drama/mod_troupeWala.vm";
	}
	@RequestMapping("/drama/ajax/starPictureList.xhtml")
	public String starPictureList(ModelMap model, Long starid, HttpServletRequest request, HttpServletResponse response){
		DramaStar dramaStar  = daoService.getObject(DramaStar.class, starid);
		if(dramaStar==null) return showJsonError(model, "明星/剧团不存在或已经删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){
			PageParams params = new PageParams();
			params.addLong("starid", starid);
			PageView pageView = pageCacheService.getPageView(request, "drama/ajax/starPictureList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("dramaStar", dramaStar);
		Integer pictureCount = commonService.getPictureCount().get(dramaStar.getId()+TAG_DRAMASTAR);
		model.put("pictureCount", pictureCount);
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TAG_DRAMASTAR, + dramaStar.getId(), 0, -1);
		model.put("pictureList", pictureList);
		List<Video> videoList = videoService.getVideoListByTag(TAG_DRAMASTAR, + dramaStar.getId(), 0, -1);
		model.put("videoList", videoList);
		return "include/drama/mod_troupe_videoPhoto.vm";
	}
	
	@RequestMapping("/drama/ajax/starDramaPlayItem.xhtml")
	public String starDramaPlayItem(Long starid, Long dramaid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		DramaStar dramaStar  = daoService.getObject(DramaStar.class, starid);
		if(dramaStar==null) return showJsonError(model, "明星/剧团不存在或已经删除！");
		String citycode = WebUtils.getAndSetDefault(request, response);
		model.put("dramaStar", dramaStar);
		final String viewPage = "drama/star/dramaPlayItem.vm";
		List<Long> dramaIdList = dramaPlayItemService.getDramaStarDramaIdList(citycode, dramaStar.getId());
		if(dramaIdList.isEmpty()) return viewPage;
		if(dramaid == null){
			dramaid = dramaIdList.get(0);
		}else if(!dramaIdList.contains(dramaid)){
			dramaid = dramaIdList.get(0);
		}
		if(pageCacheService.isUseCache(request)){
			PageParams params = new PageParams();
			params.addLong("starid", starid);
			params.addLong("dramaid", dramaid);
			PageView pageView = pageCacheService.getPageView(request, "drama/ajax/starDramaPlayItem.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		List<Drama> dramaList = daoService.getObjectList(Drama.class, dramaIdList);
		model.put("dramaList", dramaList);
		Drama curDrama = daoService.getObject(Drama.class, dramaid);
		model.put("curDrama", curDrama);
		if(!dramaList.isEmpty()){
			List<DramaPlayItem> dpiList = dramaPlayItemService.getDramaPlayItemList(citycode, null, dramaid, dramaStar.getId(), DateUtil.getCurFullTimestamp(), null, null);
			Collections.sort(dpiList, new MultiPropertyComparator(new String[]{"sortnum","playtime"}, new boolean[]{true,true}));
			model.put("dpiList", dpiList);
			Map<Long, OpenDramaItem> odiMap = new HashMap<Long, OpenDramaItem>();
			for (DramaPlayItem item : dpiList) {
				odiMap.put(item.getId(), daoService.getObjectByUkey(OpenDramaItem.class, "dpid", item.getId()));
			}
			model.put("odiMap", odiMap);
		}
		return viewPage;
	}
	
	@RequestMapping("/drama/ajax/star/detail.xhtml")
	public String starDetail(Long starid, ModelMap model){
		DramaStar star = daoService.getObject(DramaStar.class, starid);
		if(star == null) return showJsonError(model, "明星不存在！");
		List<Map<String, String>> historyMapList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, star.getRepresentativeRelate());
		Map dataMap = new HashMap();
		dataMap.put("star", star);
		DramaStar troupe = daoService.getObject(DramaStar.class, star.getTroupe());
		dataMap.put("troupe", troupe);
		dataMap.put("historyMapList", historyMapList);
		String result = velocityTemplate.parseTemplate("drama/starInfo.vm", dataMap);
		return showJsonSuccess(model, result);
	}
}
