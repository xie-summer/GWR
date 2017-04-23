package com.gewara.untrans.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.untrans.SysManageService;
import com.gewara.untrans.monitor.ZookeeperService;
@Service("sysManageService")
public class SysManageServiceImpl implements SysManageService{
	@Autowired@Qualifier("keeperService")
	private ZookeeperService keeperService;
	@Override
	public Map<String, String> getRegisterServers() {
		Map<String, String> serverData = keeperService.getChildrenData("/server/" + Config.SYSTEMID);
		Map<String, String> result = new HashMap<String, String>();
		for(String server: serverData.values()){
			String[] tmps = StringUtils.split(server,"|");
			result.put(tmps[0], tmps[2]);
		}
		return result;
	}
}
