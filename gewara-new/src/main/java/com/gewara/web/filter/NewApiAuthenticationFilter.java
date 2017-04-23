package com.gewara.web.filter;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.gewara.model.api.ApiUser;

/**
 * API2.0身份过滤器
 * 
 * @author taiqichao
 * 
 */
public class NewApiAuthenticationFilter extends BaseApiAuthenticationFilter {
	//TODO 将来移除
	//由于权限没有规划，暂时支持内部使用
	private List<Long> partnerids = Arrays.asList(50000010L,50000011L,50000020L,50000030L,50000035L,50000040L,50000070L,50000080L,
			50000081L,50000090L,50000091L,50000099L,50000100L,50000161L,50000240L,50000301L,50000302L,50000420L,50000521L,50000680L,
			50000701L,50000702L,50000705L,50000706L,50000760L,50000774L,50000778L,50000779L,50000891L,50000960L,50000986L);
	@Override
	protected boolean checkRights(ApiUser apiUser, HttpServletRequest request) {
		if(partnerids.contains(apiUser.getId())){
			return true;
		}
		return rightsHelper.hasRights(apiUser.getRoles(), request);
	}

	@Override
	protected String getPrivateKey(ApiUser apiUser, HttpServletRequest request) {
		return apiUser.getPrivatekey();
	}
	
	
}
