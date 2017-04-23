package com.gewara.web.action.inner.mobile.area;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Province;
import com.gewara.model.movie.Cinema;
import com.gewara.service.GewaCityService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileAreaController extends BaseOpenApiController{
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	private static final Map<String, String[]> proviceMap;
	private static final Map<String, String[]> sportproviceMap;
	private static final Map<String, String[]> dramaproviceMap;
	static {
		proviceMap = new LinkedHashMap<String, String[]>();
		proviceMap.put("直辖市", new String[] { "310000", "110000", "500000" });
		proviceMap.put("浙江省", new String[] { "330100", "330200", "330400", "330600", "330500", "331000" });
		proviceMap.put("江苏省", new String[] { "320100", "320200", "320400", "320500", "320600" });
		proviceMap.put("广东省", new String[] { "440100", "440300" });
		proviceMap.put("四川省", new String[] { "510100" });
		proviceMap.put("湖北省", new String[] { "420100" });
		
		sportproviceMap = new LinkedHashMap<String, String[]>();
		sportproviceMap.put("直辖市", new String[] { "310000", "110000"});
		sportproviceMap.put("浙江省", new String[] { "330100"});
		sportproviceMap.put("江苏省", new String[] { "320100"});
		sportproviceMap.put("广东省", new String[] { "440100", "440300"});
		
		dramaproviceMap = new LinkedHashMap<String, String[]>();
		dramaproviceMap.put("直辖市", new String[] { "310000"});
	}
	@RequestMapping("/openapi/mobile/area/provinceList.xhtml")
	public String county(ModelMap model, HttpServletRequest request) {
		List<Province> provinceList = daoService.getObjectList(Province.class, "provincecode", true, 0, 5000);
		List<Map> resMapList = BeanUtil.getBeanMapList(provinceList, "provincecode",  "provincename");
		model.put("resMapList", resMapList);
		initField(model, request);
		model.put("root", "provinceList");
		model.put("nextroot", "province");
		return getOpenApiXmlList(model);
	}
	
	@RequestMapping("/openapi/mobile/area/cityList.xhtml")
	public String county(String provincecode, ModelMap model, HttpServletRequest request) {
		List<City> provinceList = placeService.getCityByProvinceCode(provincecode);
		List<Map> resMapList = BeanUtil.getBeanMapList(provinceList, "citycode",  "cityname");
		model.put("resMapList", resMapList);
		initField(model, request);
		model.put("root", "cityList");
		model.put("nextroot", "city");
		return getOpenApiXmlList(model);
	}
	@RequestMapping("/openapi/mobile/area/countyList.xhtml")
	public String county(String citycode, ModelMap model) {
		if (citycode == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		return getXmlView(model, "inner/partner/countyList.vm");
	}
	@RequestMapping("/openapi/mobile/area/countyListByCountycodes.xhtml")
	public String countyListByCountycodes(String countycodes, ModelMap model) {
		List<County> countyList = daoService.getObjectList(County.class, Arrays.asList(StringUtils.split(countycodes, ",")));
		model.put("countyList", countyList);
		return getXmlView(model, "inner/partner/countyList.vm");
	}
	
	/**
	 * 当前可用城市列表
	 * @author bob.hu
	 * @date 2011-06-02 17:56:11
	 * 参数都必传
	 */
	@RequestMapping("/openapi/mobile/area/getOpenCitys.xhtml")
	public String getUsableCitys(String apptype, String showHot, ModelMap model) {
		Map<String, String> citynameMap = AdminCityContant.getCitycode2CitynameMap();
		Map<String, String> codeMap = AdminCityContant.getCitycode2PinyinMap();
		model.put("citynameMap", citynameMap);
		model.put("codeMap", codeMap);
		if(StringUtils.equals(apptype, TagConstant.TAG_SPORT)){
			model.put("proviceMap", sportproviceMap);
		}if(StringUtils.equals(apptype, TagConstant.TAG_DRAMA)){
			model.put("proviceMap", dramaproviceMap);
		}else {
			model.put("proviceMap", gewaCityService.getMobileMap());
			if(StringUtils.equals(showHot, Status.Y)){
				model.put("hotcityList", gewaCityService.getHotCityList());
			}
			model.put("pyMap", gewaCityService.getCitycode2PyMap());
		}
		return getXmlView(model, "api/mobile/usableCitys.vm");
	}
	/**
	 * 地铁线路API
	 */
	@RequestMapping("/openapi/mobile/area/subwayList.xhtml")
	public String subwayList(String citycode,ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ApiUser partner = auth.getApiUser();
		if(StringUtils.isNotBlank(citycode)){
			if(!partner.supportsCity(citycode)) return getErrorXmlView(model, ApiConstant.CODE_PARTNER_NORIGHTS, "不支持城市" + citycode);
		}else{
			citycode = partner.getDefaultCity();
		}
		List subwaylineGroup = placeService.getPlaceGroupMapByCitySubwayline(citycode, TagConstant.TAG_CINEMA);
		model.put("subwaylineGroup", subwaylineGroup);
		return getXmlView(model, "api/mobile/subwayList.vm");
	}
	
	/** 
	 * 根据影片、所在城市、获取有购票的区域
	 * @return
	 */
	@RequestMapping(value = "/openapi/mobile/area/getCountyByOpenCinema.xhtml")
	public String getCountyByOpenCinema(String citycode, Date playdate, Long movieid, ModelMap model){
		Set<County> countyList = new HashSet<County>();
		List<Long> cinemaidList = mcpService.getCurCinemaIdList(citycode, movieid, playdate);
		for(Long cinemaid : cinemaidList){
			Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
			County county = daoService.getObject(County.class, cinema.getCountycode());
			countyList.add(county);
		}
		model.put("countyList", countyList);
		return getXmlView(model, "inner/partner/countyList.vm");
	}
}
