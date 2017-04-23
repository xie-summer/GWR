package com.gewara.web.action.inner.activity;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.common.BaseInfo;
import com.gewara.support.ServiceHelper;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class ApiActivityVenueInfoController extends BaseApiController{
	@RequestMapping("/inner/activity/getVenueInfo.xhtml")
	public String getVenueInfo(String tag, Long relatedid, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid==null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		BaseInfo venueInfo = daoService.getObject(ServiceHelper.getPalceClazz(tag), relatedid);
		if(venueInfo==null) return getErrorXmlView(model,  ApiConstant.CODE_NOT_EXISTS, "数据不存在！");
		model.put("info", venueInfo);
		model.put("tag", tag);
		return getXmlView(model, "inner/activity/venueInfo.vm");
	}
	@RequestMapping("/inner/activity/getItemInfo.xhtml")
	public String getItemInfo(String category, Long categoryid, ModelMap model){
		if(StringUtils.isBlank(category) || categoryid==null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Object itemInfo = relateService.getRelatedObject(category, categoryid);
		if(itemInfo==null) return getErrorXmlView(model,  ApiConstant.CODE_NOT_EXISTS, "数据不存在！");
		model.put("info", itemInfo);
		return getXmlView(model, "inner/activity/itemInfo.vm");
	}
	@RequestMapping("/inner/activity/getMarkdata.xhtml")
	public String getItemInfo(String tag, ModelMap model){
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Map markData = markService.getMarkdata(tag);
		model.put("markData", markData);
		return getXmlView(model, "inner/activity/markData.vm");
	}
}
