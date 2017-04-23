package com.gewara.web.support;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.Config;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.support.GewaLoadEventListener;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.untrans.monitor.ConfigTrigger;
import com.gewara.untrans.monitor.ZookeeperService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.web.listener.StartupListener;

public class ShWebAppPostProcessor implements WebAppPostProcessor{
	private GewaLogger logger = LoggerUtils.getLogger(StartupListener.class, Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("keeperService")
	private ZookeeperService keeperService;
	public void setKeeperService(ZookeeperService keeperService) {
		this.keeperService = keeperService;
	}
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	public void setConfigCenter(ConfigCenter configCenter) {
		this.configCenter = configCenter;
	}
	@Override
	public void init() {
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_DISABLE_2LCACHE, new ConfigTrigger(){
			@Override
			public void refreshCurrent(String newConfig) {
				if(StringUtils.startsWith(newConfig, "Enable:")){
					GewaLoadEventListener.enableAll2ndCache();
				}else{
					String[] clazzList = StringUtils.substring(newConfig, newConfig.indexOf(':')+1).split(",");
					for(String clazz: clazzList){
						GewaLoadEventListener.addDisabledEntity(clazz);
					}
				}
			}
		});
		try{
			configCenter.reloadCurrent(Config.SYSTEMID, ConfigTag.KEY_DISABLE_2LCACHE);
		}catch(Exception e){
		}
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				String serverPath = "/server/" + Config.SYSTEMID + "/s";
				try {
					keeperService.registerNode(serverPath, Config.getServerIp() + "|" + Config.getHostname() + "|" + DateUtil.formatTimestamp(System.currentTimeMillis()));
					logger.warn("register server on node:" + Config.getHostname()+ "---->" + Config.getServerIp());
				} catch (Exception e) {
					logger.warn("", e);
				}

				try{
					String path = "/OPENAPI/PARTNER";
					if(!keeperService.exist(path)){
						keeperService.addPresistentNode(path, System.currentTimeMillis()+"");
					}
					String data = "http://" + Config.getServerIp() + ":2000" /*+ config.getBasePath()*/;
					logger.warn("zookeeper添加临时节点 ： " + path + " 节点内容为：" + data);
					keeperService.registerNode(path + "/s", data);
					
					path = "/OPENAPI/MOBILE";
					if(!keeperService.exist(path)){
						keeperService.addPresistentNode(path, System.currentTimeMillis()+"");
					}
					logger.warn("zookeeper添加临时节点 ： " + path + " 节点内容为：" + data);
					keeperService.registerNode(path + "/s", data);
				}catch(Exception ex){
					logger.error("", ex);
				}
			}
			
		}, 1000 * 15);
	}
	
}
