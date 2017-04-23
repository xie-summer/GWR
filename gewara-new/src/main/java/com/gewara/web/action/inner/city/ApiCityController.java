package com.gewara.web.action.inner.city;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.common.GewaCity;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class ApiCityController extends BaseApiController{
	
	@RequestMapping("/inner/city/getGewaCityAll.xhtml")
	public String getGewaCityAll(ModelMap model){
		List<GewaCity> cityList = daoService.getAllObjects(GewaCity.class);
		model.put("gewaCityList", cityList);
		return getXmlView(model, "inner/city/gewaCityList.vm");
	}
	@RequestMapping("/subject/proxy/city/getGewaCityAll.xhtml")
	public String getGewaCity2All(ModelMap model){
		List<GewaCity> cityList = daoService.getAllObjects(GewaCity.class);
		Map jsonMap = new HashMap();
		jsonMap.put("success", true);
		jsonMap.put("retval", JsonUtils.writeObjectToJson(cityList));
		model.put("jsonMap", jsonMap);
		model.put("jsname", "data");
		return "common/json.vm";
	}
}
