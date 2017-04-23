package com.gewara.service.api.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gewara.model.acl.WebModule;
import com.gewara.service.api.ApiSecureService;
import com.gewara.service.impl.BaseServiceImpl;
@Service("apiSecureService")
public class ApiSecureServiceImpl extends BaseServiceImpl implements ApiSecureService {
	@Override
	public List<WebModule> getApiModuleList(){
		String query = "from WebModule where moduleurl is not null and tag = ? order by matchorder";
		List<WebModule> result = hibernateTemplate.find(query, WebModule.TAG_API);
		return result;
	}

}
