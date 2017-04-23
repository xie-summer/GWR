package com.gewara.web.filter;

import java.io.IOException;

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

import com.gewara.commons.api.ApiSysParamConstants;
import com.gewara.constant.ApiConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.service.api.ApiMobileService;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.ApiAuth;

/**
 * API2.0身份过滤器
 * 
 * @author taiqichao
 * 
 */
public class OpenApiPartnerAuthenticationFilter extends GenericFilterBean  {
	@Autowired@Qualifier("apiMobileService")
	private ApiMobileService apiMobileService;
	
	private static ThreadLocal<ApiAuth> apiAuthLocal = new ThreadLocal<ApiAuth>();
	private String[] innerIpList;
	public static ApiAuth getApiAuth() {
		return apiAuthLocal.get();
	}
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String remoteIp = WebUtils.getRemoteIp(request);
		if(!isInnerIp(remoteIp)) {
			response.sendError(403, "只能内部调用！");
			return;
		}
		String appkey = request.getParameter(ApiSysParamConstants.APPKEY);
		ApiUser apiUser = apiMobileService.getApiUserByAppkey(appkey);
		if (apiUser == null) {
			ApiFilterHelper.writeErrorResponse(response, ApiConstant.CODE_PARTNER_NOT_EXISTS,"用户不存在");
			return;
		}
		String partnerIp = request.getHeader("GEWA-REMOTE-IP");

		try{
			ApiUserExtra userExtra = apiMobileService.getApiUserExtraById(apiUser.getId());
			apiAuthLocal.set(new ApiAuth(apiUser, userExtra, partnerIp));
			chain.doFilter(request, response);
		}finally{
			apiAuthLocal.set(null);
		}
	}

	private boolean isInnerIp(String remoteIp){
		for(String ipPre: innerIpList){
			if(StringUtils.startsWith(remoteIp, ipPre)) return true;
		}
		return false;
	}

	@Override
	public void initFilterBean() throws ServletException {
		innerIpList = new String[]{"172.22.1.", "192.168.", "180.153.146.1", "114.80.171.2", "127.0.0.1"};
	}

}
