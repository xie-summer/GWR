package com.gewara.web.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.filter.GenericFilterBean;

import com.gewara.constant.ApiConstant;
import com.gewara.model.acl.WebModule;
import com.gewara.model.api.ApiUser;
import com.gewara.service.DaoService;
import com.gewara.service.api.ApiSecureService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.support.RoleUrlMatchHelper;

public class ApiAuthenticationFilter extends GenericFilterBean {
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("apiSecureService")
	private ApiSecureService apiSecureService;
	private RoleUrlMatchHelper helper;
	private ApiFilterHelper apiFilterHelper;
	private static ThreadLocal<ApiAuth> ApiAuthLocal = new ThreadLocal<ApiAuth>();
	public static ApiAuth getApiAuth() {
		return ApiAuthLocal.get();
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		long cur = System.currentTimeMillis();
		// 开始处理 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		String urlRoles = helper.getFirstMatchUrlRoles(request);
		boolean success = true;
		if(StringUtils.isNotBlank(urlRoles)){
			String key = request.getParameter("key");
			ApiUser apiUser = daoService.getObjectByUkey(ApiUser.class, "partnerkey", key, true);
			ErrorCode result = checkRights(apiUser, request);
			if(result.isSuccess()){
				ApiAuthLocal.set(new ApiAuth(apiUser));
			}else{
				success = false;
				ApiFilterHelper.writeErrorResponse(response, result.getErrcode(), result.getMsg());
			}
		}else{//未匹配角色
			ApiFilterHelper.writeErrorResponse(response, ApiConstant.CODE_PARTNER_NORIGHTS, "没有权限");
			success = false;
		}
		try{
			if(success) {
				chain.doFilter(request, response);
			}
		}finally{//做清理
			ApiAuthLocal.set(null);
			apiFilterHelper.apiLog(request, cur, success);
		}
	}

	@Override
	protected void initFilterBean() throws ServletException {
		List<WebModule> moduleList = apiSecureService.getApiModuleList();
		helper = new RoleUrlMatchHelper(moduleList);
		apiFilterHelper = new ApiFilterHelper(monitorService);
	}
	
	public ErrorCode checkRights(ApiUser apiUser, HttpServletRequest request) {
		if(apiUser == null){
			return ErrorCode.getFailure(ApiConstant.CODE_PARTNER_NOT_EXISTS, "用户不存在或没权限");
		}
		if(!apiUser.isEnabled()){
			return ErrorCode.getFailure(ApiConstant.CODE_PARTNER_NORIGHTS, "没有权限");
		}
		boolean hasRights = helper.hasRights(apiUser.getRoles(), request);
		if(!hasRights){
			return ErrorCode.getFailure(ApiConstant.CODE_PARTNER_NORIGHTS, "没有权限");
		}
		return ErrorCode.SUCCESS;
	}
}
