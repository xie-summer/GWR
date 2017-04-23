package com.gewara.web.action.admin.sys;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.support.magent.MessageCommandCenter;
import com.gewara.support.magent.RequestStatsGroup;
import com.gewara.support.magent.SmackMonitorAgentClient;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class MonitorAgentController extends BaseAdminController implements InitializingBean{
	private SmackMonitorAgentClient client;
	private MessageCommandCenter mcc;
	@Autowired@Qualifier("config")
	private Config config;
	
	@RequestMapping("/admin/sysmgr/getStatsInfo.xhtml")
	public String getStatsInfo(String cmd, ModelMap model){
		if(StringUtils.isBlank(cmd)) {
			cmd="help";
		}
		String result = mcc.execCommand(cmd);
		model.put("result", result);
		return "admin/sysmgr/statsInfo.vm"; 
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String username = StringUtils.lowerCase(Config.SYSTEMID + "_" + Config.getHostname());
		String host = config.getString("openfire.host");
		mcc = MessageCommandCenter.getDefaultInstance();
		mcc.registerGroup(new RequestStatsGroup());
		//FIXME:用户名、密码从数据库中查询
		client = new SmackMonitorAgentClient(host, username, Config.SYSTEMID + "pass", mcc);
		client.init();
	}
}
