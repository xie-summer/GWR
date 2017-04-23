package com.gewara.web.action.api;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.model.common.County;

@Controller
public class ApiCommonController extends BaseApiController{
	@RequestMapping("/api/403.xhtml")
	public String error(ModelMap model){
		return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在或无权限");
	}
	@RequestMapping("/api/common/cityList.xhtml")
	public String cityList(String key, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return getXmlView(model, "api/info/common/cityList.vm");
	}
	@RequestMapping("/api/common/county.xhtml")
	public String county(String key, String citycode, String encryptCode, ModelMap model){
		ApiAuth auth = checkRights(encryptCode, key);
		if(!auth.isChecked()) return getErrorXmlView(model, auth.getCode(), auth.getMsg());
		if(citycode==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		return getXmlView(model, "api/info/common/county.vm");
	}
}
