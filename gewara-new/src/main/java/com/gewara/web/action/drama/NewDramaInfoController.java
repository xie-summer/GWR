package com.gewara.web.action.drama;

import java.io.Serializable;
import java.sql.Timestamp;
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

import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.PageView;
import com.gewara.model.bbs.Diary;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class NewDramaInfoController extends BaseDramaController{
	private static Map<String, Integer[]> dateMap = new HashMap();
	private static Map<String, String> orderMap = new HashMap<String, String>();
	static {
		//当前日期增加的时间
		dateMap.put("3", new Integer[]{0 ,7});
		dateMap.put("4", new Integer[]{0 ,30});
		dateMap.put("5", new Integer[]{0 ,90});
		orderMap.put("avggeneral", "avggeneral");
		orderMap.put("releasedate", "releasedate");
		orderMap.put("clickedtimes", "clickedtimes");
	}

	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	
	@RequestMapping("/drama/ajax/dramaPictureList.xhtml")
	public String dramaPictureList(ModelMap model, Long relatedid, Integer pageNo, String type){
		pictureComponent.pictureList(model, pageNo, TagConstant.TAG_DRAMA, relatedid, type, "/drama/ajax/dramaPictureList.xhtml");
		return "drama/ajax_dramaPictureList.vm";
	}

	//剧目列表
	@RequestMapping("/drama/dramaList.xhtml")
	public String dramaList_new(ModelMap model,String fyrq,String type,String order,String dramatype,String searchkey,Integer pageNo,HttpServletRequest request,HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(fyrq)) fyrq = "1"; //全部
		if(StringUtils.isBlank(order)) order = "clickedtimes";
		if(pageNo==null) pageNo=0;
		PageParams params = new PageParams();
		params.addSingleString("order", order);
		params.addSingleString("dramatype", dramatype);
		params.addSingleString("fyrq", fyrq);
		params.addInteger("pageNo", pageNo);
		params.addSingleString("searchkey", searchkey);
		if(pageCacheService.isUseCache(request)){
			PageView pageView = pageCacheService.getPageView(request, "/drama/dramaList.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		//前面是查看缓存是否存在,如果缓存不存在,页面生成从这里开始
		Integer rowsPerpage=10;
		Integer from = pageNo * rowsPerpage;
		List<Drama> dramaList = dramaService.getDramaList(citycode, fyrq, type, order, dramatype, searchkey, from, rowsPerpage);
		getDDList(dramaList,model);
		getReleasedDrama(dramaList,model);
		List<Long> dramaIdList = BeanUtil.getBeanPropertyList(dramaList, "id", true);
		getRecruitDiary(citycode,model);
		getActivity(citycode,model);
		getInterestDrama(citycode,model,dramaIdList);
		putDramaInfo(citycode, dramaIdList,model);
		getHotDiary(citycode,model);
		model.put("dramaList",dramaList);
		//获取演出类型
		model.put("dramaTypeList", dramaService.getDramaTypeList(citycode));
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
		//分页信息
		Integer count = dramaService.getDramaListCount(citycode, fyrq, type, order, dramatype, searchkey);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "drama/dramaList.xhtml", true, true);
		pageUtil.initPageInfo(params.getParams());
		model.put("pageUtil", pageUtil);
		model.put("dramacount", count);
		return "drama/wide_dramaList.vm";
	}
	
	//后台推荐追剧团获取
	private void getRecruitDiary(String citycode,ModelMap model){
		List<GewaCommend> recruitDiaryList = commonService.getGewaCommendList(citycode, SignName.SHOWINDEX_RECRUIT_DIARY, null, null, true, 0, 4);
		model.put("recruitDiaryList",recruitDiaryList);
	}
	//获取正在进行中的官方活动，按照报名结束日期由近到远排序
	private void getActivity(String citycode,ModelMap model){
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA,RemoteActivity.TIME_CURRENT,TagConstant.TAG_THEATRE,null, null,null,"duetime",0,3);
		if(code.isSuccess()){
			model.put("activityList",code.getRetval());
		}
	}
	//获取你可能喜欢的演出
	private void getInterestDrama(String citycode,ModelMap model,List<Long> dramaIdList){
		List<Long> bookingIdList = dramaPlayItemService.getCurDramaidList(citycode);
		bookingIdList = BeanUtil.getSubList(bookingIdList, 0, 4);
		dramaIdList.addAll(bookingIdList);
		List<Drama> interestDramaList = daoService.getObjectList(Drama.class, bookingIdList);
		model.put("interestDramaList",interestDramaList);
	}
	//获取热门评论
	private void getHotDiary(String citycode,ModelMap model){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp starttime = DateUtil.addDay(cur, -15);
		List<Diary> hotDiaryList = diaryService.getDiaryBySearchkeyAndOrder(citycode, null, starttime, cur, "sumnumed", 0, 10);
		List<Serializable> categoryIdList = BeanUtil.getBeanPropertyList(hotDiaryList, Serializable.class, "categoryid", true);
		RelatedHelper rh = new RelatedHelper(); 
		model.put("relatedHelper", rh);
		relateService.addRelatedObject(1, "categoryIdList", rh, TagConstant.TAG_DRAMA, categoryIdList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(hotDiaryList));
		model.put("diaryList",hotDiaryList);
	}
	//获取演出的导演和演员列表
	private void getDDList(List<Drama> dramaIds,ModelMap model){
		Map<Long, List<DramaStar>> dramaStarListMap= new HashMap<Long, List<DramaStar>>();
		Map<Long, List<DramaStar>> dramaDirectorListMap= new HashMap<Long, List<DramaStar>>();
		for(Drama drama:dramaIds){
			if(StringUtils.isNotBlank(drama.getActors())){
				List<Long> actorIdList = BeanUtil.getIdList(drama.getActors(), ",");
				List<DramaStar> actorsList = daoService.getObjectList(DramaStar.class, actorIdList);
				dramaStarListMap.put(drama.getId(), actorsList);
			}
			if(StringUtils.isNotBlank(drama.getDirector())){
				List<Long> directorIdList = BeanUtil.getIdList(drama.getDirector(), ",");
				List<DramaStar> directorsList = daoService.getObjectList(DramaStar.class, directorIdList);
				dramaDirectorListMap.put(drama.getId(), directorsList);
			}
		}
		model.put("dramaStarListMap", dramaStarListMap);
		model.put("dramaDirectorListMap", dramaDirectorListMap);
	}
	//获得上映中的演出列表和即将上映的演出列表
	private void getReleasedDrama(List<Drama> dramas,ModelMap model){
		Map<Long, Boolean> dramaReleased= new HashMap<Long, Boolean>();
		Map<Long, Boolean> dramaFuture= new HashMap<Long, Boolean>();
		Date now = DateUtil.currentTime();
		for(Drama drama:dramas){
			Long key = drama.getId();
			if(drama.getEnddate()!=null && drama.getReleasedate().after(now)){
				dramaFuture.put(key, true);
				dramaReleased.put(key,false);
			}else{
				dramaFuture.put(key, false);
				if(drama.getEnddate()!=null && drama.getEnddate().before(now))
					dramaReleased.put(key,false);
				else
					dramaReleased.put(key,true);
			}
				
		}
		model.put("dramaReleased", dramaReleased);
		model.put("dramaFuture", dramaFuture);
	}
	//统一获取演出的是否可选座,是否可售票,以及演出的价格
	private void putDramaInfo(String citycode, List<Long> dramaList,ModelMap model){
		Map<Long, List<Theatre>> theatreMap = new HashMap<Long, List<Theatre>>();
		Map<Long, List<Integer>> dramaPriceMap = new HashMap<Long, List<Integer>>();
		//TODO 有空再把循环里的查询拿出来 用下面的注释掉的方法代替
		for(Long dramaId : dramaList){
			theatreMap.put(dramaId, dramaPlayItemService.getTheatreList(citycode, dramaId, false, 2));//因为涉及到两个缓存,还需要讨论是否要从数据库中直接取,故暂时不改
			dramaPriceMap.put(dramaId, dramaPlayItemService.getPriceList(null, dramaId, DateUtil.getCurFullTimestamp(), null, false));
		}
		List<Long> openseatList = openDramaService.getCurDramaidList(citycode, OdiConstant.OPEN_TYPE_SEAT);
		List<Long> bookingList = openDramaService.getCurDramaidList(citycode);
		model.put("openseatList",openseatList);
		model.put("bookingList", bookingList);
		model.put("theatreMap", theatreMap);
		model.put("priceListMap",dramaPriceMap);
	}
}
