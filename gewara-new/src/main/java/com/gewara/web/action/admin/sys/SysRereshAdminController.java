package com.gewara.web.action.admin.sys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.camel.CamelContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.model.common.GewaConfig;
import com.gewara.service.GewaCityService;
import com.gewara.service.MessageService;
import com.gewara.service.api.ApiMobileService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheConfigure;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.SysManageService;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.untrans.monitor.ConfigTrigger;
import com.gewara.untrans.monitor.ZookeeperService;
import com.gewara.untrans.ticket.TicketQueueService;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class SysRereshAdminController extends BaseAdminController implements InitializingBean{
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("keeperService")
	private ZookeeperService keeperService;
	public void setKeeperService(ZookeeperService keeperService) {
		this.keeperService = keeperService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	
	@Autowired@Qualifier("sysManageService")
	private SysManageService sysManageService;
	
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	
	@Autowired@Qualifier("ticketQueueService")
	private TicketQueueService ticketQueueService;
	public void setTicketQueueService(TicketQueueService ticketQueueService) {
		this.ticketQueueService = ticketQueueService;
	}
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService) {
		this.pageCacheService = pageCacheService;
	}
	@Autowired
	private CacheConfigure cacheConfigure;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	@Autowired@Qualifier("scalperService")
	private ScalperService scalperService;

	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	@Autowired@Qualifier("apiMobileService")
	private ApiMobileService apiMobileService;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_ALLFILTERKEYS, blogService);
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_TICKETQUEUE, ticketQueueService);
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_SMSCHANNEL, messageService);
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_SCALPER, scalperService);
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_IMGSERVER, new ConfigTrigger() {
			@Override
			public void refreshCurrent(String newConfig) {
				initImg();
			}
		});
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_CACHE_VERSION, new ConfigTrigger() {
			@Override
			public void refreshCurrent(String newConfig) {
				cacheService.refreshVersion(cacheConfigure.getRegionVersion());
			}
		});
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_PAGEKEY_VERSION, new ConfigTrigger() {
			@Override
			public void refreshCurrent(String newConfig) {
				pageCacheService.refreshKeyVersion();
			}
		});
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_PAYLIMIT, new ConfigTrigger(){
			@Override
			public void refreshCurrent(String newConfig) {
				paymentService.reInitLimitPayList();				
			}
		});

		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_PAGETOOL, new ConfigTrigger(){
			@Override
			public void refreshCurrent(String newConfig) {
				Map<String, String> map = JsonUtils.readJsonToMap(newConfig);
				for(String key: map.keySet()){
					config.replacePageTool(key, map.get(key));
				}
			}
		});
		
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_GEWACITY, new ConfigTrigger(){
			@Override
			public void refreshCurrent(String newConfig) {
				gewaCityService.initCityList();				
			}
		});
		
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_APIUSER, new ConfigTrigger(){
			@Override
			public void refreshCurrent(String newConfig) {
				apiMobileService.initApiUserList();				
			}
		});
	}


	@RequestMapping("/admin/sysmgr/ajax/clearCache.xhtml")
	public String clearCache(String classOrRegionName, String type, String clearType, ModelMap model) {
		String msg = "";
		if("class".equals(type)){
			String[] classList = StringUtils.split(classOrRegionName, ",");
			List<String> successList = new ArrayList<String>();
			for(String clazz: classList){
				ErrorCode code = daoService.clearCache(clazz, type);
				if(!code.isSuccess()){
					msg += code.getMsg();
				}else{
					successList.add(clazz);
				}
			}
			if("disable".equals(clearType) && !successList.isEmpty()){
				configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_DISABLE_2LCACHE, System.currentTimeMillis() + ":" + StringUtils.join(successList, ","));
			}
		}else{
			ErrorCode code = daoService.clearCache(classOrRegionName, type);
			if(!code.isSuccess()) msg += code.getMsg();
		}
		if("enable".equals(clearType)){
			configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_DISABLE_2LCACHE, "Enable:" + System.currentTimeMillis());
		}
		if(StringUtils.isNotBlank(msg)){
			return showJsonError(model, msg);
		}else{
			return showJsonSuccess(model);
		}
	}
	@RequestMapping("/admin/sysmgr/refreshConfig.xhtml")
	public String refreshConfig(ModelMap model, String key){
		configCenter.refresh(Config.SYSTEMID, key);
		return forwardMessage(model, "success!");
	}
	@RequestMapping("/admin/sysmgr/rebuildFilterKeys.xhtml")
	public String rebuildFilterKeys(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_ALLFILTERKEYS);
		return forwardMessage(model, "success!");
	}
	@RequestMapping("/admin/sysmgr/refreshQueueConfig.xhtml")
	@ResponseBody
	public String refreshAllQuery(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_TICKETQUEUE);
		return forwardMessage(model, "success!");
	}
	
	@RequestMapping("/admin/sysmgr/batchInit.xhtml")
	public String initImgPath2(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_IMGSERVER);
		return forwardMessage(model, "success!");
	}
	@RequestMapping("/admin/sysmgr/refreshCacheVersion.xhtml")
	public String refreshCacheVersion(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_CACHE_VERSION);
		return forwardMessage(model, "success!");
	}
	@RequestMapping("/admin/sysmgr/refreshPageKey.xhtml")
	public String refreshPageKey(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_PAGEKEY_VERSION);
		return forwardMessage(model, "success!");
		
	}
	@RequestMapping("/admin/sysmgr/refreshAllLimitPay.xhtml")
	public String refreshAllLimitPay(ModelMap model){
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_PAYLIMIT);
		return forwardMessage(model, "success!");
	}
	@RequestMapping("/admin/sysmgr/changeConfig.xhtml")
	public String changeConfig(ModelMap model, String name, String value){
		Map<String, String> map = new HashMap<String, String>();
		map.put(name, value);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_PAGETOOL, JsonUtils.writeMapToJson(map));
		return showJsonError(model, "result:" + name + "---->" + value);
	}
	private void initImg(){
		GewaConfig gc = daoService.getObject(GewaConfig.class, config.getLong("imgServer"));
		VmUtils utils = new VmUtils();
		utils.initImg(gc, config.getString("picPath"));
		config.replacePageTool("VmUtils", utils);
	}


	@RequestMapping("/getAllTicketQueueStats.xhtml")
	public String getAllTicketQueueStats(ModelMap model){
		Map resultMap = new HashMap();
		Map txtMap = new HashMap();
		String errorServer = "";
		Map<String/*ip*/, String/*starttime*/> serverMap = sysManageService.getRegisterServers();
		for(String server: serverMap.keySet()){
			String url = "http://" + server + ":8080" + config.getBasePath() + "getTicketQueueStatistics.xhtml";
			HttpResult tmp = HttpUtils.postUrlAsString(url, null);
			if(tmp.isSuccess()){
				Map sr = JsonUtils.readJsonToMap(tmp.getResponse());
				resultMap.put(server, sr);
				txtMap.put(server, tmp.getResponse());
			}else{
				errorServer += server;
			}
		}
		model.put("errorServer", errorServer);
		model.put("resultMap", resultMap);
		model.put("txtMap", txtMap);
		return "admin/sysmgr/ticketQueue.vm";
	}

	@RequestMapping("/getTicketQueueStatistics.xhtml")
	@ResponseBody
	public String getTicketQueueStatistics(String clear, HttpServletRequest request){
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		String result = JsonUtils.writeMapToJson(ticketQueueService.getStatistics());
		if(StringUtils.isNotBlank(clear)) ticketQueueService.clearData();
		return result;
	}
	@RequestMapping("/admin/sysmgr/clearIndex.xhtml")
	public String clearIndex(HttpServletRequest request,String pageUrl,String citycode,ModelMap model){
		if(StringUtils.isBlank(citycode)){
			citycode = "310000";
		}
		if(StringUtils.isBlank(pageUrl)){
			pageUrl = "/index.xhtml";
		}
		PageParams params = new PageParams();
		if(StringUtils.indexOf(pageUrl, "movieDetail.xhtml")==-1){
			params.addString(request, "fyrq");
			params.addString(request, "movieid");
		}else{
			params.addString(request, "mid");
		}
		pageCacheService.clearPageView(pageUrl, params, citycode);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sysmgr/saveAllLimitPay.xhtml")
	public String saveAllLimitPay(String content, ModelMap model){
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PAYLIMIT);
		cfg.setContent(content);
		daoService.saveObject(cfg);
		refreshAllLimitPay(model);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/sysmgr/modMethod.xhtml")
	public String modMethod(String method, ModelMap model){
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PAYLIMIT);
		List<String> limitList = new ArrayList<String>();
		String[] strs  = StringUtils.split(cfg.getContent(), ",");
		if(strs!=null) limitList = Arrays.asList(strs);
		List<String> newList = new ArrayList<String>(limitList);
		if(newList.contains(method)){
			newList.remove(method);
		}else {
			newList.add(method);
		}
		cfg.setContent(StringUtils.join(newList, ","));
		daoService.saveObject(cfg);
		refreshAllLimitPay(model);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sysmgr/servers.xhtml")
	@ResponseBody
	public String getServerList(){
		return sysManageService.getRegisterServers().toString();
	}
	
	@RequestMapping("/stopCamel.xhtml")
	@ResponseBody
	public String stopCamel(HttpServletRequest request) throws Exception{
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		String msg = "stop CamelContext:" + new Date();
		dbLogger.warn(msg);
		CamelContext context = applicationContext.getBean("camel", CamelContext.class);
		if(context!=null)	{
			context.stop();
			return "stop camel success!";
		}else{
			return "camel not exists!";
		}
	}
	@RequestMapping("/beforeUpdate.xhtml")
	@ResponseBody
	public String beforeUpdate(HttpServletRequest request) throws Exception{
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		String msg = "";
		try{
			CamelContext context = applicationContext.getBean("camel", CamelContext.class);
			context.stop();
			msg += "stop camel success!";
		}catch(NoSuchBeanDefinitionException e){
			msg += "camel not exists!";
		}
		keeperService.destroy();
		msg += "zookeeper disconnect!";
		return msg;
	}
	@RequestMapping("/startCamel.xhtml")
	public String startCame(HttpServletRequest request, ModelMap model) throws Exception{
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		String msg = "start CamelContext:" + new Date();
		dbLogger.warn(msg);
		CamelContext context = applicationContext.getBean("camel", CamelContext.class);
		context.start();
		return forwardMessage(model, msg);
	}
	private String[] ips = new String[]{"114.80.171.2", "180.153.146.1", "172.22.1.", "127.0.0.1", "192.168."};
	public void checkIp(String ip){
		if(WebUtils.isLocalIp(ip)) return;
		for(String pre:ips){
			if(StringUtils.startsWith(ip, pre)) return;
		}
		throw new  IllegalArgumentException("IP禁止访问");
	}
	
	@RequestMapping("/admin/sysmgr/pageCacheUrl.xhtml")
	public String pageCacheUrl(ModelMap model){
		Map<String, Integer> cahceMinMap = pageCacheService.getCacheMinMap();
		model.put("cahceMinMap", cahceMinMap);
		return "admin/sysmgr/pageCacheUrl.vm";
	}
	@RequestMapping("/admin/sysmgr/refreashCacheMin.xhtml")
	@ResponseBody
	public String refreashCacheMin(String pageUrl, Integer minute, HttpServletRequest request){
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		if(StringUtils.isBlank(pageUrl) || minute == null ||minute <=0) return "fail";
		pageCacheService.refreashCacheMin(pageUrl, minute);
		return "success";
	}

	@RequestMapping("/admin/sysmgr/clearCacheManageIndex.xhtml")
	public String clearCacheManageIndex(ModelMap model){
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/sysmgr/clearCacheManageIndex.vm";
	}
	
	
	/**
	 * 
	 * 清楚排版缓存数据
	 * 
	 * */
	@RequestMapping("/admin/sysmgr/removeCacheDetail.xhtml")
	public String removeCacheDetail(HttpServletRequest request,String regionName,String key,ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		checkIp(ip);
		if(StringUtils.isBlank(key)){
			return showJsonError(model,"选择模块为空，不可操作！");
		}
		if(StringUtils.isBlank(regionName)){
			return showJsonError(model,"缓存区域选择为空，请选择后操作！");
		}
		cacheService.remove(regionName, key);
		return showJsonSuccess(model);
	}
}
