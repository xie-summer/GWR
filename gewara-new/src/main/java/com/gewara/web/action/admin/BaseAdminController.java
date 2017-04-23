package com.gewara.web.action.admin;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.common.JsonData;
import com.gewara.model.content.GewaCommend;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public abstract class BaseAdminController extends AnnotationController {
	@Autowired@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	@Autowired@Qualifier("placeService")
	protected PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	
	@Autowired@Qualifier("commonService")
	protected CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("operationService")
	protected OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	protected String getAdminCitycode(HttpServletRequest request){
		Cookie cookie = WebUtils.getCookie(request, "admin_citycode");
		if(cookie==null) 
//			throw new IllegalArgumentException("admin_citycode 为空！");
			return AdminCityContant.CITYCODE_SH;
		String citycode = cookie.getValue();
		if(!WebUtils.isValidCitycode(citycode)) throw new IllegalArgumentException("admin_citycode 不合法！");
		return citycode;
	}
	protected String getDefaultCitycode(HttpServletRequest request){
		Cookie cookie = WebUtils.getCookie(request, "admin_citycode");
		String citycode = null;
		if(cookie==null) {
			citycode = StringUtils.split(getLogonUser().getCitycode(), ",")[0];
		}else{
			citycode = cookie.getValue();
		}
		if(!WebUtils.isValidCitycode(citycode)) throw new IllegalArgumentException("admin_citycode 不合法！");
		return citycode;
	}
	
	
	protected void setAdminCode(String citycode, HttpServletResponse response) {
		if (!WebUtils.isValidCitycode(citycode)) {
			throw new IllegalArgumentException("切换城市不合法！");
		}
		Cookie cookie = new Cookie("admin_citycode", citycode);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24 * 30);
		response.addCookie(cookie);
	}
	
	protected String getCommendList(RelatedHelper rh, String signname, Long parentid, boolean isInit, String url, HttpServletRequest request, ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request), signname, parentid,null, false,0, 200);
		if(isInit) commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("admincitycode", getAdminCitycode(request));
		model.put("gcList", gcList);
		model.put("signname", signname);
		return url;
	}
	protected final User getLogonUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null) return null;
		if(auth.isAuthenticated() && !auth.getName().equals("anonymous")){//登录
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			//refresh(user);
			if(user instanceof User) return (User)user;
		}
		return null;
	}
	protected Map<String, String> getAppSourceMap(){
		String key = JsonDataKey.KEY_MOBILE_APPSOURCE;
		JsonData jsonData = daoService.getObject(JsonData.class, key);
		Map<String, String> appSourcesMap = JsonUtils.readJsonToMap(jsonData.getData());
		return new TreeMap(appSourcesMap);
	}
}
