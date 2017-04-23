package com.gewara.web.action.api2.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Province;
import com.gewara.web.action.api.BaseApiController;


/**
 * 地区 API 
 * @author taiqichao
 *
 */
@Controller
public class Api2AreaController extends BaseApiController {
	/**
	 * 城市列表API
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/area/allCityList.xhtml")
	public String countyList(ModelMap model){
		Map<Province, List<City>> proMap = new HashMap<Province, List<City>>();
		List<Province> proList = daoService.getObjectList(Province.class, "provincecode", true, 0, 5000);
		for(Province pro : proList){
			List<City> cityList = placeService.getCityByProvinceCode(pro.getProvincecode());
			proMap.put(pro, cityList);
		}
		model.put("proMap", proMap);
		return getXmlView(model, "api2/area/cityList.vm");
	}
	
	
	/**
	 * 行政区域列表
	 * @param citycode
	 * @param fields
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/area/countyList.xhtml")
	public String county(String countycodes, ModelMap model){
		if(countycodes==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数！");
		List<County> countyList = new ArrayList<County>();
		for(String countycode : countycodes.split(",")){
			County county = daoService.getObject(County.class, countycode);
			if(county!=null) countyList.add(county);
		}
		model.put("countyList", countyList);
		return getXmlView(model, "api2/area/countyList.vm");
	}
	/**
	 * 行政区域列表
	 * @param citycode
	 * @param fields
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/area/countyListByCitycode.xhtml")
	public String countyList(String citycode, String fields, ModelMap model){
		if(citycode==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数！");
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		return getXmlView(model, "api2/area/countyList.vm");
	}
	
	/**
	 * 地铁线路列表API
	 * @param citycode
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/area/subwayList.xhtml")
	public String subwayList(String citycode, ModelMap model){
		if(citycode==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数！");
		List subwaylineGroup = placeService.getPlaceGroupMapByCitySubwayline(citycode, TagConstant.TAG_CINEMA);
		model.put("subwaylineGroup", subwaylineGroup);
		return getXmlView(model, "api2/area/subwayList.vm");
	}
	
	@RequestMapping("/api2/area/indexarea.xhtml")
	public String indexarea(String countycode, ModelMap model){
		if(countycode==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数！");
		List<Indexarea> indexareaList = placeService.getIndexareaByCountyCode(countycode);
		model.put("indexareaList", indexareaList);
		return getXmlView(model, "api2/area/indexarea.vm");
	}
}
