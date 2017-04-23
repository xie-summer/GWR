package com.gewara.web.action.admin.mobile;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.JsonDataKey;
import com.gewara.model.common.JsonData;
import com.gewara.model.mobile.AsConfig;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class MobileAsAdminController extends BaseAdminController{
	@RequestMapping("/admin/mobile/appSourceList.xhtml")
	public String appSourceList(ModelMap model) {
		String key = JsonDataKey.KEY_MOBILE_APPSOURCE;
		JsonData jsonData = daoService.getObject(JsonData.class, key);
		if(jsonData==null){
			jsonData = new JsonData(key, "{}");
			jsonData.setTag(key);
			jsonData.setValidtime(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), 360*10));
			daoService.saveObject(jsonData);
		}
		Map<String, String> appSourcesMap = JsonUtils.readJsonToMap(jsonData.getData());
		model.put("appSourcesMap", new TreeMap(appSourcesMap));
		return "admin/mobile/appsourceList.vm";
	}
	
	@RequestMapping("/admin/mobile/saveAppsource.xhtml")
	public String appSource(String appsouce, String name, ModelMap model) {
		if(StringUtils.isBlank(appsouce) || StringUtils.isBlank(name)){
			return showJsonError(model, "数据不能为空！");
		}
		String key = JsonDataKey.KEY_MOBILE_APPSOURCE;
		JsonData jsonData = daoService.getObject(JsonData.class, key);
		Map<String, String> appSourcesMap = JsonUtils.readJsonToMap(jsonData.getData());
		appSourcesMap.put(appsouce, name);
		jsonData.setData(JsonUtils.writeMapToJson(appSourcesMap));
		daoService.saveObject(jsonData);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/batchSaveAppsource.xhtml")
	public String appSource(String content) {
		if(StringUtils.isBlank(content)){
			return "admin/mobile/appsourceText.vm";
		}
		String key = JsonDataKey.KEY_MOBILE_APPSOURCE;
		JsonData jsonData = daoService.getObject(JsonData.class, key);
		Map<String, String> appSourcesMap = JsonUtils.readJsonToMap(jsonData.getData());
		String[] strs = StringUtils.split(content, ",");
		for(String str : strs){
			String[] s = str.split("\\:");
			appSourcesMap.put(s[0], s[1]);
		}
		jsonData.setData(JsonUtils.writeMapToJson(appSourcesMap));
		daoService.saveObject(jsonData);
		return "redirect:/admin/mobile/appSourceList.xhtml";
	}
	@RequestMapping("/admin/mobile/asConfigList.xhtml")
	public String asConfigList(ModelMap model){
		String key = JsonDataKey.KEY_MOBILE_APPSOURCE;
		JsonData jsonData = daoService.getObject(JsonData.class, key);
		Map<String, String> appSourcesMap = JsonUtils.readJsonToMap(jsonData.getData());
		List<AsConfig> asList = daoService.getObjectList(AsConfig.class, "partnerid", true, 0, 300);
		Map<Long, List<AsConfig>> asMap = BeanUtil.groupBeanList(asList, "partnerid");
		model.put("asList", asList);
		model.put("asMap", asMap);
		model.put("appSourcesMap", appSourcesMap);
		return "admin/mobile/asConfigList.vm";
	}
	@RequestMapping("/admin/mobile/saveAsConfig.xhtml")
	public String saveAsConfig(Long id, ModelMap model, HttpServletRequest request) {
		AsConfig as = new AsConfig();
		if(id!=null){
			as = daoService.getObject(AsConfig.class, id);
		}
		BindUtils.bindData(as, request.getParameterMap());
		daoService.saveObject(as);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/getAsConfig.xhtml")
	public String getAsConfig(Long id, ModelMap model) {
		AsConfig as = daoService.getObject(AsConfig.class, id);
		return showJsonSuccess(model, BeanUtil.getBeanMap(as));
	}
	@RequestMapping("/admin/mobile/delAsConfig.xhtml")
	public String delAsConfig(Long id, ModelMap model) {
		AsConfig as = daoService.getObject(AsConfig.class, id);
		daoService.removeObject(as);
		return showJsonSuccess(model);
	}
}
