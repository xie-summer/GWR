package com.gewara.web.action.inner.place;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.common.County;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class PlaceController extends BaseApiController{
	@RequestMapping("/inner/place/getCountyList.xhtml")
	public String getCountyList(String citycode, ModelMap model){
		List<County> countyList = new ArrayList<County>();
		if(StringUtils.isNotBlank(citycode)){
			countyList = placeService.getCountyByCityCode(citycode);
		}else {
			countyList = daoService.getAllObjects(County.class);
		}
		model.put("countyList", countyList);
		return getXmlView(model, "inner/place/countyList.vm");
	}
}
