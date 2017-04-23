package com.gewara.web.action.inner.activity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.drama.Drama;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiActivityDramaController extends BaseApiController{
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	/**
	 * 获取话剧列表
	 * @param movieids
	 * @return
	 */
	@RequestMapping("/inner/activity/drama/getDramaByIds.xhtml")
	public String getMovieByIds(String dramaids, ModelMap model){
		if(StringUtils.isBlank(dramaids)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> dramaidList = Arrays.asList(StringUtils.split(dramaids, ","));
		List<Long> dramaIds = new ArrayList<Long>();
		for (String string : dramaidList) {
			dramaIds.add(Long.parseLong(string));
		}
		List<Drama> dramaList = daoService.getObjectList(Drama.class, dramaIds);
		model.put("dramaList", dramaList);
		return getXmlView(model, "inner/activity/dramaList.vm");
	}
	/**
	 * 获取电影信息
	 * @param movieids
	 * @return
	 */
	@RequestMapping("/inner/activity/drama/getDrama.xhtml")
	public String getDramaById(Long dramaid, ModelMap model){
		if(dramaid == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "数据不存在！");
		model.put("drama", drama);
		return getXmlView(model, "inner/activity/dramaDetail.vm");
	}
	/**
	 * 获取电影信息
	 * @param movieids
	 * @return
	 */
	@RequestMapping("/inner/activity/drama/isBooking.xhtml")
	public String isBooking(Long dramaid, ModelMap model){
		if(dramaid == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "数据不存在！");
		boolean isBooking = openDramaService.isSupportBooking(null, null, dramaid, DateUtil.getMillTimestamp(), null);
		String result = isBooking?"open":"close";
		return getSingleResultXmlView(model, result);
	}
	@RequestMapping("/inner/activity/drama/getPriceList.xhtml")
	public String isBooking(Long theatreid, Long dramaid, Timestamp starttime, Timestamp endtime, String isBooking, ModelMap model){
		boolean booking = StringUtils.equalsIgnoreCase("true", isBooking)?true:false;
		List<Integer> priceList = dramaPlayItemService.getPriceList(theatreid, dramaid, starttime, endtime, booking);
		return getSingleResultXmlView(model, StringUtils.join(priceList, ","));
	}
}
