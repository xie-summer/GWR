package com.gewara.web.action.inner.activity;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiActivityPlaceController extends BaseApiController{
	
	/**
	 * 获取地铁信息
	 * @param cinemaids
	 * @return
	 */
	@RequestMapping("/inner/activity/place/getSubwaylineMap.xhtml")
	public String  getCinemas(String citycode, ModelMap model){
		if(StringUtils.isBlank(citycode)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		Map<String, String> subwayMap = placeService.getSubwaylineMap(citycode);
		model.put("subwayMap", subwayMap);
		return getXmlView(model, "inner/activity/subwayList.vm");
	}
}
