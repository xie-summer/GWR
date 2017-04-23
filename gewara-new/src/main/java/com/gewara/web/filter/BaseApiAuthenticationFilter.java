package com.gewara.web.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.filter.GenericFilterBean;

import com.gewara.commons.api.ApiSysParamConstants;
import com.gewara.commons.sign.Sign;
import com.gewara.constant.ApiConstant;
import com.gewara.model.acl.WebModule;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.service.DaoService;
import com.gewara.service.api.ApiMobileService;
import com.gewara.service.api.ApiSecureService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.support.GewaMultipartResolver;
import com.gewara.web.support.RoleUrlMatchHelper;

/**
 * API2.0身份过滤器
 * 
 * @author taiqichao
 * 
 */
public abstract class BaseApiAuthenticationFilter extends GenericFilterBean {
	private static ThreadLocal<ApiAuth> apiAuthLocal = new ThreadLocal<ApiAuth>();
	protected ApiFilterHelper apiFilterHelper;
	protected RoleUrlMatchHelper rightsHelper;
	
	@Autowired@Qualifier("daoService")
	protected DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;

	@Autowired@Qualifier("apiSecureService")
	private ApiSecureService apiSecureService;
	@Autowired@Qualifier("apiMobileService")
	private ApiMobileService apiMobileService;
	public static ApiAuth getApiAuth() {
		return apiAuthLocal.get();
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		Long cur = System.currentTimeMillis();
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		if(ServletFileUpload.isMultipartContent(request)){//包装文件上传请求
			request=new GewaMultipartResolver().resolveMultipart(request);
		}
		
		String appkey = request.getParameter(ApiSysParamConstants.APPKEY);
		//用户身份校验
		ApiUser apiUser = apiMobileService.getApiUserByAppkey(appkey);
		if (apiUser == null) {
			ApiFilterHelper.writeErrorResponse(response, ApiConstant.CODE_PARTNER_NOT_EXISTS,"用户不存在");
			apiFilterHelper.apiLog(request, cur, false);//记录失败日志
			return;
		}
		String sign = request.getParameter(ApiSysParamConstants.SIGN);
		String privateKey = getPrivateKey(apiUser, request);
		//签名校验
		String signData=Sign.signMD5(ApiFilterHelper.getTreeMap(request), privateKey);
		if (!StringUtils.equalsIgnoreCase(sign, signData)) {
			ApiFilterHelper.writeErrorResponse(response, ApiConstant.CODE_PARTNER_NORIGHTS,"校验签名错误!");
			apiFilterHelper.apiLog(request, cur, false);//记录失败日志
			return;
		}
		
		//权限校验
		boolean hasRights = checkRights(apiUser, request);
		if(!hasRights){
			ApiFilterHelper.writeErrorResponse(response, ApiConstant.CODE_PARTNER_NORIGHTS,"没有权限");
			apiFilterHelper.apiLog(request, cur, false);//记录失败日志
			return;
		}
		
		//综上条件校验通过
		try{
			//保存当前授权用户
			ApiUserExtra userExtra = apiMobileService.getApiUserExtraById(apiUser.getId());
			apiAuthLocal.set(new ApiAuth(apiUser, userExtra));
			//执行下面方法链
			chain.doFilter(request, response);
		}finally{
			//清除当前授权用户
			apiAuthLocal.set(null);
			//记录成功日志
			apiFilterHelper.apiLog(request, cur, true);
		}
		
	}
	protected boolean checkRights(ApiUser user, HttpServletRequest request){
		if(user == null){
			return false;
		}
		if(!user.isEnabled()){
			return false;
		}
		return rightsHelper.hasRights(user.getRoles(), request);
	}
	protected abstract String getPrivateKey(ApiUser apiUser, HttpServletRequest request);


	@Override
	protected void initFilterBean() throws ServletException {
		List<WebModule> moduleList = apiSecureService.getApiModuleList();
		rightsHelper = new RoleUrlMatchHelper(moduleList);
		apiFilterHelper = new ApiFilterHelper(monitorService);
	}

}
