package com.gewara.web.action.api2;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.gewara.Config;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.mobile.MobileAdvertisingYouMi;
import com.gewara.mongo.MongoService;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;


/**
 * 爱普动力接口助手类
 * 
 * @author taiqichao
 * 
 */
@Component
public class AppDriverUtils {
	
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	@Autowired
	@Qualifier("mongoService")
	protected MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	/**广告主app的唯一ID**/
	public final static String APPDRIVER_SITE_ID="333";
	/**广告主app的签名key**/
	public final static String APPDRIVER_SITE_KEY="703ee3edbb6c3f17d185c6fa59d6194f";
	/**广告主的推广广告ID**/
	public final static String APPDRIVER_CAMPAIGN_ID="408";
	/**广告主的推广参数**/
	public final static String APPDRIVER_ADVERTISEMENT="install";
	
	
	public void notifyAppDriver(MobileAdvertisingYouMi aym, String deviceid) {
		dbLogger.warn("爱普动力推广，mac:"+deviceid);
		if (aym != null) {
			StringBuilder url = new StringBuilder("http://appdriver.cn/6.0."+APPDRIVER_SITE_ID+"ca");
			url.append("?campaign_id="+APPDRIVER_CAMPAIGN_ID+"&advertisement="+APPDRIVER_ADVERTISEMENT);
			String digest=StringUtil.md5(APPDRIVER_ADVERTISEMENT+","+APPDRIVER_CAMPAIGN_ID+","+aym.getMsource()+","+aym.getUinfo()+","+APPDRIVER_SITE_KEY);
			url.append("&uinfo="+aym.getUinfo()+"&msource="+aym.getMsource()+"&digest="+digest.toLowerCase());
			HttpResult code = HttpUtils.getUrlAsString(url.toString());
			if (code.isSuccess()) {
				Map resultMap=JsonUtils.readJsonToMap(code.getResponse());
				Integer success=(Integer) resultMap.get("success");
				if(success==1){//回调成功
					aym.setYmRecord(code.getResponse());
					aym.setOpenUDID(deviceid);
					dbLogger.warn("爱普动力推广，回调成功,mac:"+deviceid);
				}else if(success==0){
					aym.setYmRecord(code.getResponse());
					dbLogger.warn("爱普动力推广，失败:"+code.getResponse());
				}
				mongoService.saveOrUpdateObject(aym, MongoData.DEFAULT_ID_NAME);
			}
		}
	}
	
	
	
	
}
