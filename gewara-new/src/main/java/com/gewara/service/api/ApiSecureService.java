package com.gewara.service.api;

import java.util.List;

import com.gewara.model.acl.WebModule;


public interface ApiSecureService {

	List<WebModule> getApiModuleList();

}
