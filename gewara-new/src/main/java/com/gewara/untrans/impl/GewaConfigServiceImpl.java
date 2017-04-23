	package com.gewara.untrans.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.model.common.GewaConfig;
import com.gewara.service.DaoService;
import com.gewara.untrans.CacheConfigure;
import com.gewara.util.JsonUtils;
@Service("gewaConfigService")
public class GewaConfigServiceImpl extends CacheConfigure{
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	@Autowired@Qualifier("config")
	private Config config;
	@Override
	public Map<String, String> getRegionVersion() {
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, config.getLong("memcacheVersion"));
		return JsonUtils.readJsonToMap(gewaConfig.getContent());
	}
}
