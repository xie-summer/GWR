package com.gewara.web.action.inner.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.sport.Sport;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class ApiActivitySportController extends BaseApiController{
	/**
	 * 获取话剧列表
	 * @param movieids
	 * @return
	 */
	@RequestMapping("/inner/activity/sport/getSportByIds.xhtml")
	public String getMovieByIds(String sportids, ModelMap model){
		if(StringUtils.isBlank(sportids)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> sportidList = Arrays.asList(StringUtils.split(sportids, ","));
		List<Long> sportIds = new ArrayList<Long>();
		for (String string : sportidList) {
			sportIds.add(Long.parseLong(string));
		}
		List<Sport> sportList = daoService.getObjectList(Sport.class, sportIds);
		model.put("sportList", sportList);
		return getXmlView(model, "inner/activity/sportList.vm");
	}
	/**
	 * 获取运动信息
	 * @param movieids
	 * @return
	 */
	@RequestMapping("/inner/activity/sport/getSport.xhtml")
	public String getsportById(Long sportid, ModelMap model){
		if(sportid == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "数据不存在！");
		model.put("sport", sport);
		return getXmlView(model, "inner/activity/sportDetail.vm");
	}
}
