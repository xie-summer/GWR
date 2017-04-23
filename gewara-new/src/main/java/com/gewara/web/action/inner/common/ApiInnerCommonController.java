package com.gewara.web.action.inner.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.content.News;
import com.gewara.service.content.NewsService;
import com.gewara.service.member.TreasureService;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiInnerCommonController extends BaseApiController {
	
	@Autowired@Qualifier("commonService")
	private CommonService commonService;

	
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;

	@RequestMapping("/inner/common/list/newsByTag.xhtml")	
	public String getNewsListTag(String citycode, String tag, Long relatedid, String order, String asc, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(tag) || relatedid == null || from == null || maxnum ==null || from < 0 || maxnum <=0) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		List<News> newsList = newsService.getNewsList(citycode, tag, relatedid, null, null, order, Boolean.parseBoolean(asc), from, maxnum);
		model.put("newsList", newsList);
		return getXmlView(model, "inner/common/newsList.vm");
	}
	@RequestMapping("/inner/common/count/newsByTag.xhtml")	
	public String getNewsCountTag(String citycode, String tag, Long relatedid, ModelMap model){
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(tag) || relatedid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		int result = newsService.getNewsCount(citycode, tag, null, relatedid, null);
		return getSingleResultXmlView(model, result);
	}
	
	
	@RequestMapping("/inner/common/count/groupByTag.xhtml")
	public String gymCounts(String tag, String ids, ModelMap model){
		List<Long> IdList = BeanUtil.getIdList(ids, ","); 
		if(StringUtils.isBlank(tag) || IdList.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		Map<String, Integer> pictureCountMap = commonService.getPictureCount();//图片数量
		Map<String, Integer> videoCountMap = commonService.getVideoCount();//视频数量
		Map<String, Integer> commentCountMap = commonService.getCommentCount();//哇啦数量
		model.put("pictureCountMap", pictureCountMap); 
		model.put("videoCountMap", videoCountMap);
		model.put("commentCountMap", commentCountMap);
		model.put("idList", IdList);
		model.put("tag", tag);
		return getXmlView(model, "inner/common/countByGroupTag.vm");
	}
	
	@RequestMapping("/inner/common/list/treasureByTag.xhtml")
	public String getTreasureMemberList(String tag, Long relatedid, String action, String order, String asc, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid == null || from == null || maxnum ==null || from < 0 || maxnum <=0) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		List<Long> memberIdList = treasureService.getTreasureMemberList(action, tag, relatedid, order, Boolean.parseBoolean(asc), from, maxnum);
		addCacheMember(model, memberIdList);
		model.put("memberIdList", memberIdList);
		return getXmlView(model, "inner/common/cacheMemberList.vm");
	}
}
