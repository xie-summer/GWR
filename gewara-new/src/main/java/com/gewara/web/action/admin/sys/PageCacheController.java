package com.gewara.web.action.admin.sys;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class PageCacheController extends BaseAdminController implements InitializingBean{
	private String keyPre;
	@Autowired@Qualifier("memcachedClient")
	private MemcachedClient memcachedClient;
	@RequestMapping("/admin/sysmgr/ajax/clearPage.xhtml")
	public String clearPage(ModelMap model, String citycode, String page){
		boolean success1 = memcachedClient.delete(getExistsKey(page, citycode)).getStatus().isSuccess();
		boolean success2 = memcachedClient.delete(getContentKey(page, citycode)).getStatus().isSuccess();
		return showJsonSuccess(model, "result:"+success1 + "," + success2);
	}
	private String getExistsKey(String pageUrl, String citycode){
		String key = pageUrl;
		int idx = pageUrl.indexOf("?");
		if(idx>0) key=pageUrl.substring(0,idx) + pageUrl.substring(idx).hashCode();
		String result = keyPre + "KEY" + key + "CITYCODE" + citycode;
		StringUtils.deleteWhitespace(result);
		return result;
	}
	private String getContentKey(String pageUrl, String citycode){
		String key = pageUrl;
		int idx = pageUrl.indexOf("?");
		if(idx>0) key=pageUrl.substring(0,idx) + pageUrl.substring(idx).hashCode();
		String result = keyPre + "PAGE" + key + "CITYCODE" + citycode;
		StringUtils.deleteWhitespace(result);
		return result;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		GewaConfig config = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PAGECACHE_VERSION);
		keyPre = config.getContent();
	}
}
