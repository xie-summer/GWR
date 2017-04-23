/** 
 */
package com.gewara.untrans.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.PlayItemRefreshService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;


/**
 * @author Zhicheng.Peng   Johnny.Resurgam@Gmail.com
 *  
 *  Jun 3, 2013  4:22:55 PM
 */
@Service("playItemRefreshService")
public class PlayItemRefreshServiceImpl implements PlayItemRefreshService, InitializingBean {
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	
	private Map<String, String[]> urlMapping = new HashMap<String, String[]>();
	
	@Override
	public void clearOrderedPageCache(Map<String, String> params, String citycode) {
		for (String key : urlMapping.keySet()){
			String pageUrl = key;
			PageParams pageParams = getSpecialParamsForPage(pageUrl, params); 
			if (pageParams == null){
				return;
			}
			pageCacheService.clearPageView(pageUrl, pageParams, citycode);
		}
	}
	private PageParams getSpecialParamsForPage(String pageUrl, Map<String, String> params){
		String[] keys = urlMapping.get(pageUrl);
		if (keys == null)
			return null;
		
		PageParams p = new PageParams();
		for (int i = 0; i < keys.length; i++){
			String v = params.get(keys[i]);
			p.addSingleString(keys[i], v);
		}
		return p;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		urlMapping.put("cinema/ajax/getCinemaPlayItem.xhtml", new String[]{"cid","fyrq"});
		urlMapping.put("movie/ajax/getOpiItemPage.xhtml", new String[]{"fyrq","cid", "movieid"});
		urlMapping.put("movie/ajax/getSearchOpiItem.xhtml", new String[]{"fyrq","movieid","cid"});
	}
}
