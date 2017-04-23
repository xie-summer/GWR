package com.gewara.web.filter;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;

/**
 * API2.0Éí·Ý¹ýÂËÆ÷
 * 
 * @author taiqichao
 * 
 */
public class MacApiAuthenticationFilter extends BaseApiAuthenticationFilter {
	private List<Long> partneridList = Arrays.asList(new Long[]{PartnerConstant.PARTNER_CHANGTU, PartnerConstant.PARTNER_MACBUY, PartnerConstant.PARTNER_CUS});
	@Override
	protected boolean checkRights(ApiUser apiUser, HttpServletRequest request) {
		if(partneridList.contains(apiUser.getId()))  return true;
		return rightsHelper.hasRights(apiUser.getRoles(), request);
	}

	@Override
	protected String getPrivateKey(ApiUser apiUser, HttpServletRequest request) {
		return apiUser.getPrivatekey();
	}
	
	
}
