package com.gewara.web.action.admin.city;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.model.acl.User;
import com.gewara.model.common.City;
import com.gewara.model.common.GewaCity;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.util.BeanUtil;
import com.gewara.util.PinYinUtils;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class AdminCityController extends BaseAdminController{
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@RequestMapping("/admin/city/cityList.xhtml")
	public String cityList(ModelMap model){
		List<GewaCity> cityList = daoService.getAllObjects(GewaCity.class);
		Map<String, List<GewaCity>> proMap = BeanUtil.groupBeanList(cityList, "provincecode");
		Map<String, List<GewaCity>> proMap2 = new HashMap<String, List<GewaCity>>();
		List<GewaCity> proList = new ArrayList<GewaCity>();
		for(String key : proMap.keySet()){
			List<GewaCity> tmpList = proMap.get(key);
			if(tmpList.size()>0){
				proList.add(tmpList.get(0));
			}
			Collections.sort(tmpList, new PropertyComparator("citySort", true, true));
			proMap2.put(key, tmpList);
		}
		Collections.sort(proList, new PropertyComparator("provinceSort", true, true));
		model.put("proList", proList);
		model.put("proMap", proMap2);
		return "admin/city/gewaCityList.vm";
	}
	@RequestMapping("/admin/city/pinyin.xhtml")
	public String pinyin(){
		List<GewaCity> cityList = daoService.getAllObjects(GewaCity.class);
		for(GewaCity city : cityList){
			city.setPinyin(PinYinUtils.getPinyin(city.getCityname()));
			city.setPy(PinYinUtils.getFirstSpell(city.getCityname()));
		}
		daoService.saveObjectList(cityList);
		return "redirect:/admin/city/cityList.xhtml";
	}
	@RequestMapping("/admin/city/proSort.xhtml")
	public String proSort(Integer sortnum, String provincecode, ModelMap model){
		User user = getLogonUser();
		List<GewaCity> cityList = daoService.getObjectListByField(GewaCity.class, "provincecode", provincecode);
		for(GewaCity city : cityList){
			city.setProvinceSort(sortnum);
		}
		daoService.saveObjectList(cityList);
		dbLogger.warn("用户修改省份排序：" + user.getId() + ", sortnum:" + sortnum + "," + provincecode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/citySort.xhtml")
	public String citySort(Integer sortnum, String citycode, ModelMap model){
		User user = getLogonUser();
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		city.setCitySort(sortnum);
		daoService.saveObject(city);
		dbLogger.warn("用户修改城市排序：" + user.getId() + ", sortnum:" + sortnum + "," + citycode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/showIdx.xhtml")
	public String citySort(String showIdx, String citycode, ModelMap model){
		User user = getLogonUser();
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		city.setShowIdx(showIdx);
		daoService.saveObject(city);
		dbLogger.warn("用户修改城市显示：" + user.getId() + ", showIdx:" + showIdx + "," + citycode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/showAdm.xhtml")
	public String showAdm(String showAdm, String citycode, ModelMap model){
		User user = getLogonUser();
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		city.setShowAdm(showAdm);
		daoService.saveObject(city);
		dbLogger.warn("用户修改城市显示：" + user.getId() + ", showAdm:" + showAdm + "," + citycode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/showHot.xhtml")
	public String showHot(String showHot, String citycode, ModelMap model){
		User user = getLogonUser();
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		city.setShowHot(showHot);
		daoService.saveObject(city);
		dbLogger.warn("用户修改热门城市：" + user.getId() + ", showHot:" + showHot + "," + citycode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/manmethod.xhtml")
	public String manmethod(String manmethod, String citycode, ModelMap model){
		User user = getLogonUser();
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		if(StringUtils.equals(city.getShowIdx(), Status.N)) return showJsonError(model, "该分站没有对外展示，不能设置管理方式！");
		city.setManmethod(manmethod);
		daoService.saveObject(city);
		dbLogger.warn("用户修改城市管理方式：" + user.getId() + ", manmethod:" + manmethod + "," + citycode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/changeServiceType.xhtml")
	public String changeServiceType(String serviceType, String citycode, ModelMap model){
		User user = getLogonUser();
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		if(StringUtils.equals(city.getShowIdx(), Status.N)) return showJsonError(model, "该分站没有对外展示，不能设置业务模式！");
		city.setServiceType(serviceType);
		daoService.saveObject(city);
		dbLogger.warn("用户修改城市业务模式：" + user.getId() + ", serviceType:" + serviceType + "," + citycode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/city/refreshGewaCity.xhtml")
	public String refreshGewaCity(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_GEWACITY);
		return forwardMessage(model, "success!");
	}
	//地图
	@RequestMapping("/admin/city/mapbpoint.xhtml")
	public String mapbpoint(String citycode, ModelMap model) {
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		model.put("object", city);
		model.put("citycode", citycode);
		return "admin/common/cityMapBaiduPoint.vm";
	}
	//保存
	@RequestMapping("/admin/city/saveBMap.xhtml")
	public String saveBMap(String citycode, String bpointx, String bpointy, ModelMap model) {
		GewaCity city = daoService.getObject(GewaCity.class, citycode);
		if(city != null){
			city.setBpointx(bpointx);
			city.setBpointy(bpointy);
			daoService.saveObject(city);
		}
		return showJsonSuccess(model);
	}
	//查询
	@RequestMapping("/admin/city/addGewaCity.xhtml")
	public String addGewaCity() {
		return "admin/city/addGewaCity.vm";
	}
	//查询
	@RequestMapping("/admin/city/getCityByName.xhtml")
	public String getCityByName(String cityname, ModelMap model) {
		if(StringUtils.isBlank(cityname)){
			return "admin/common/qryCityList.vm";
		}
		String hql = "from City c where c.cityname like ?";
		List<City> cityList = hibernateTemplate.find(hql, "%"+cityname+"%");
		if(cityList.size()==0){
			return showJsonError(model, "城市不存在，请联系技术人员！");
		}
		if(cityList.size()>1){
			return showJsonError(model, "查询城市数量大于1，请输入详细名称");
		}
		Map<String, String> pinyinMap = new HashMap<String, String>();
		Map<String, String> pyMap = new HashMap<String, String>();
		City city = cityList.get(0);
		pinyinMap.put(city.getCitycode(), PinYinUtils.getPinyin(city.getCityname()));
		pyMap.put(city.getCitycode(), PinYinUtils.getFirstSpell(city.getCityname()));
		model.put("pinyinMap", pinyinMap);
		model.put("pyMap", pyMap);
		model.put("city", city);
		return "admin/city/qryCityList.vm";
	}
	
	//增加
	@RequestMapping("/admin/city/saveGewaCity.xhtml")
	public String getCityByName(String citycode, String cityname, String pinyin, String py, ModelMap model) {
		GewaCity gewacity = daoService.getObjectByUkey(GewaCity.class, "citycode", citycode);
		if(gewacity!=null){
			return showJsonError(model, "城市已经添加，请不要重复添加！");
		}
		City city = daoService.getObject(City.class, citycode);
		List<GewaCity> gcList = daoService.getObjectListByField(GewaCity.class, "provincecode", city.getProvince().getProvincecode());
		GewaCity gc = gcList.get(0);
		gewacity = new GewaCity();
		gewacity.setCitycode(citycode);
		gewacity.setCityname(cityname);
		gewacity.setPinyin(pinyin);
		gewacity.setPy(py);
		gewacity.setCitySort(100);
		gewacity.setHotSort(100);
		gewacity.setManmethod("auto");
		gewacity.setProvincecode(gc.getProvincecode());
		gewacity.setProvincename(gc.getProvincename());
		gewacity.setProvinceSort(gc.getProvinceSort());
		gewacity.setShowAdm(Status.N);
		gewacity.setShowHot(Status.N);
		gewacity.setShowIdx(Status.N);
		daoService.saveObject(gewacity);
		return showJsonSuccess(model);
	}
}
