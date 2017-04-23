package com.gewara.web.action.admin.partner;

import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.gewara.Config;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.helper.GewaAppHelper;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserBusiness;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.api.CooperUser;
import com.gewara.service.api.ApiService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class ApiAdminController extends BaseAdminController{
	@Autowired@Qualifier("apiService")
	private ApiService apiService;
	public void setApiService(ApiService apiService) {
		this.apiService = apiService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@RequestMapping("/admin/api/apiUserList.xhtml")
	public String apiUser(String status, ModelMap model){
		if(StringUtils.isBlank(status)){
			status = ApiUser.STATUS_OPEN;
		}
		List<ApiUser> apiUserList = apiService.getApiUserList(status);
		model.put("apiUserList", apiUserList);
		model.put("status", status);
		return "admin/api/apiUserList.vm";
		
	}
	@RequestMapping("/admin/api/cooperUserList.xhtml")
	public String cooperUserList(ModelMap model){
		List<CooperUser> cooperUserList = daoService.getObjectList(CooperUser.class, "id", true, 0, 5000);
		model.put("cooperUserList", cooperUserList);
		return "admin/api/cooperUserList.vm";
		
	}
	@RequestMapping("/admin/api/modifyApiUser.xhtml")
	public String modifyApiUser(Long uid, ModelMap model){
		if(uid==null) return forwardMessage(model, "只能修改账户！！");
		ApiUser user = daoService.getObject(ApiUser.class, uid);
		model.put("apiUser", user);
		return "admin/api/modifyApiUser.vm";
	}
	@RequestMapping("/admin/api/modifyCooperUser.xhtml")
	public String modifyCooperUser(Long uid, ModelMap model){
		CooperUser user = null;
		if(uid!=null) {
			user = daoService.getObject(CooperUser.class, uid);
		}else {
			user = new CooperUser();
		}
		model.put("apiUser", user);
		model.put("appSourcesMap", getAppSourceMap());
		return "admin/api/modifyCooperUser.vm";
	}
	@RequestMapping("/admin/api/apiUserExtra.xhtml")
	public String apiUserExtra(Long uid, ModelMap model){
		ApiUser user = daoService.getObject(ApiUser.class, uid);
		model.put("apiUser", user);
		if(uid!=null){
			ApiUserExtra userExtra = daoService.getObject(ApiUserExtra.class, uid);
			model.put("userExtra", userExtra);
		}
		model.put("paytextMap", GewaAppHelper.textMap);
		return "admin/api/apiUserExtra.vm";
	}
	@RequestMapping("/admin/api/saveApiUserExtra.xhtml")
	public String saveApiUserExtra(Long uid, HttpServletRequest request, ModelMap model){
		ApiUserExtra userExtra = daoService.getObject(ApiUserExtra.class, uid);
		if(userExtra==null) userExtra = new ApiUserExtra(uid);
		BindUtils.bindData(userExtra, request.getParameterMap());
		daoService.saveObject(userExtra);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_APIUSER);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/api/saveApiUser.xhtml")
	public String saveApiUser(Long uid, String otherinfo, HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		ApiUser apiUser = daoService.getObject(ApiUser.class, uid);
		ChangeEntry entry = new ChangeEntry(apiUser);
		BindUtils.bindData(apiUser, request.getParameterMap());
		if(VmUtils.readJsonToMap(otherinfo).isEmpty()){
			apiUser.setOtherinfo(null);
		}else{
			apiUser.setOtherinfo(otherinfo);
		}
		apiUser.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		apiUser.setClerk(user.getId());
		daoService.saveObject(apiUser);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_APIUSER);
		cacheService.cleanUkey(ApiUser.class, "partnerkey", apiUser.getPartnerkey());
		monitorService.saveChangeLog(user.getId(), ApiUser.class, apiUser.getId(), entry.getChangeMap(apiUser));
		return  apiUser(ApiUser.STATUS_OPEN, model);
	}
	
	@RequestMapping("/admin/api/saveCooperUser.xhtml")
	public String saveApiUser(Long uid, HttpServletRequest request, ModelMap model){
		CooperUser cooperUser = null;
		if(uid!=null){
			cooperUser = daoService.getObject(CooperUser.class, uid);
		}else {
			cooperUser = new CooperUser();
		}
		boolean isAdd = false;
		if(cooperUser==null){
			cooperUser = new CooperUser();
			cooperUser.setId(uid);
			isAdd = true;
		}
		ChangeEntry entry = new ChangeEntry(cooperUser);
		BindUtils.bindData(cooperUser, request.getParameterMap());
		if(isAdd){
			cooperUser.setLoginpass(StringUtil.md5(request.getParameter("loginpass")));
		}
		daoService.saveObject(cooperUser);
		monitorService.saveChangeLog(cooperUser.getId(), CooperUser.class, cooperUser.getId(), entry.getChangeMap(cooperUser));
		return  cooperUserList(model);
	}
	
	@RequestMapping("/admin/api/modifyUserBusiness.xhtml")
	public String modifyUserBusiness(Long uid, ModelMap model){
		ApiUserBusiness bus = null;
		if(uid!=null) {
			bus = daoService.getObject(ApiUserBusiness.class, uid);
		}else {
			bus = new ApiUserBusiness();
		}
		model.put("apiUser", bus);
		return "admin/api/modifyUserBusiness.vm";
	}
	
	@RequestMapping("/admin/api/saveUserBusiness.xhtml")
	public String saveUserBusiness(Long uid, HttpServletRequest request, ModelMap model){
		ApiUserBusiness bus = daoService.getObject(ApiUserBusiness.class, uid);
		if(bus==null) {
			bus = new ApiUserBusiness();
			bus.setId(uid);
		}
		ChangeEntry entry = new ChangeEntry(bus);
		BindUtils.bindData(bus, request.getParameterMap());
		daoService.saveObject(bus);
		monitorService.saveChangeLog(bus.getId(), ApiUserBusiness.class, bus.getId(), entry.getChangeMap(bus));
		model.put("uid", uid);
		return  "redirect:/admin/api/modifyUserBusiness.xhtml";
	}
}
