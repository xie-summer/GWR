package com.gewara.web.action.drama;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.bbs.Diary;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;

@Controller
public class DramaStarIndexController extends BaseDramaController {

	/**
	 *  明星首页(列表页)
	 */
	@RequestMapping("/drama/star/index.xhtml")
	public String starindex(ModelMap model, Integer pageNo, String type, String startype, String order, String searchkey, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(StringUtils.isBlank(type)){
			type = DramaStar.TYPE_TROUPE;
			startype = DramaStar.TYPE_TROUPE;
			if(StringUtils.isBlank(order)){
				order = "avggeneral";
			}
		}
		//分页显示当前明星
		int starcount = 0;	// 只查询填充完整的明星(修改:查询所有明星)
		if (pageNo == null){
			pageNo = 0;
		}
		int rowsPerpage = 10;
		int firstRow = pageNo * rowsPerpage;
		List<DramaStar> starList = null;
		if(!StringUtils.equals(type, DramaStar.TYPE_TROUPE)){
			if(StringUtils.isBlank(order)){
				order = "clickedtimes";
			}
			starList = dramaStarService.getDramaStarList(order, startype, searchkey, null, firstRow, rowsPerpage, "logo", "content");	// 按粉丝排序
			starcount = dramaStarService.getStarCount(startype, searchkey, "logo", "content");
		}else{
			starList = dramaStarService.getDramaStarList(order, startype, searchkey, null, firstRow, rowsPerpage);
			starcount = dramaStarService.getStarCount(startype, searchkey);
		}
		initStarList(starList, model);
		model.put("starList", starList);
		model.put("starcount", starcount);
		PageUtil pageUtil = new PageUtil(starcount, rowsPerpage, pageNo, "drama/star", true, true);
		Map params = new HashMap();
		params.put("type", type);
		params.put("startype", startype);
		params.put("searchkey", searchkey);
		params.put("order", order);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("type", type);
		RelatedHelper rh = new RelatedHelper(); 
		model.put("relatedHelper", rh);
		rightData(rh, citycode, model);
		if(StringUtils.equals(type, DramaStar.TYPE_TROUPE)){
			return "drama/star/wide_troupeList.vm";
		}
		return "drama/star/wide_dramaStarList.vm";
	}
	
	private void initStarList(List<DramaStar> starList, ModelMap model){
		Map<Long,List<Map<String,String>>> historyDramaListMap = new HashMap<Long, List<Map<String,String>>>();
		Map<Long,List<DramaStar>> dramaStarMap = new HashMap<Long, List<DramaStar>>();
		Map<Long, Integer> dramaStarCountMap = new HashMap<Long, Integer>();
		for(DramaStar star : starList){
			List<Map<String, String>> tmp = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, star.getRepresentativeRelate());
			historyDramaListMap.put(star.getId(), tmp);
			List<DramaStar> dramaStarList = dramaStarService.getStarListByTroupid(star.getId(), "clickedtimes", false, 0, 5);
			dramaStarMap.put(star.getId(), dramaStarList);
			Integer count = dramaStarService.getStarCountByTroupid(star.getId());
			dramaStarCountMap.put(star.getId(), count);
		}
		model.put("historyDramaListMap", historyDramaListMap);
		model.put("dramaStarMap", dramaStarMap);
		model.put("dramaStarCountMap", dramaStarCountMap);
	}
	
	private void rightData(RelatedHelper rh, String citycode, ModelMap model){
		List<Long> openseatList = openDramaService.getCurDramaidList(citycode, OdiConstant.OPEN_TYPE_SEAT);
		List<Long> bookingList = openDramaService.getCurDramaidList(citycode);
		model.put("openseatList",openseatList);
		model.put("bookingList", bookingList);
		List<Drama> dramaList = daoService.getObjectList(Drama.class, bookingList);
		Collections.sort(dramaList,new MultiPropertyComparator(new String[]{"boughtcount", "clickedtimes"}, new boolean[]{false, false}));
		dramaList = BeanUtil.getSubList(dramaList, 0, 5);
		model.put("hotDramaList", dramaList);
		Map<Long, List<Theatre>> theatreMap = new HashMap<Long, List<Theatre>>();
		Map<Long, List<Integer>> priceListMap = new HashMap<Long, List<Integer>>();
		for(Drama curDrama : dramaList){
			theatreMap.put(curDrama.getId(), dramaPlayItemService.getTheatreList(citycode, curDrama.getId(), true, 2));
			priceListMap.put(curDrama.getId(), dramaPlayItemService.getPriceList(null, curDrama.getId(), null, null, false));
		}
		model.put("theatreMap", theatreMap);
		model.put("priceListMap", priceListMap);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		Timestamp starttime = DateUtil.getBeginTimestamp(DateUtil.addDay(cur, -30));
		List<Diary> diaryList = diaryService.getDiaryBySearchkeyAndOrder(citycode, null, starttime, cur, "sumnumed", 0, 10);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(diaryList);
		addCacheMember(model, memberidList);
		List<Serializable> categoryIdList = BeanUtil.getBeanPropertyList(diaryList, Serializable.class, "categoryid", true);
		relateService.addRelatedObject(1, "categoryIdList", rh, TagConstant.TAG_DRAMA, categoryIdList);
		model.put("diaryList", diaryList);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
	}
}
