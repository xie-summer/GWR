package com.gewara.web.action.inner.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class ApiActivityCinemaController extends BaseApiController{
	/**
	 * 获取电影院列表
	 * @param cinemaids
	 * @return
	 */
	@RequestMapping("/inner/activity/cinema/getCinemas.xhtml")
	public String  getCinemas(String cinemaids, ModelMap model){
		if(cinemaids == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> memberidList = Arrays.asList(StringUtils.split(cinemaids, ","));
		List<Long> memberIds = new ArrayList<Long>();
		for (String string : memberidList) {
			memberIds.add(Long.parseLong(string));
		}
		List<Cinema> cinemaList = null;
		if(memberIds.size() > 0)
			cinemaList= daoService.getObjectList(Cinema.class, memberIds);
		model.put("cinemaList", cinemaList);
		return getXmlView(model, "inner/activity/cinemaList.vm");
	}
	/**
	 * 获取电影院列表
	 * @param cinemaids
	 * @return
	 */
	@RequestMapping("/inner/activity/cinema/getCinema.xhtml")
	public String  getCinemas(Long cinemaid, ModelMap model){
		if(cinemaid == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		if(cinema == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "数据不存在！");
		model.put("cinema", cinema);
		return getXmlView(model, "inner/activity/cinemaDetail.vm");
	}
}
