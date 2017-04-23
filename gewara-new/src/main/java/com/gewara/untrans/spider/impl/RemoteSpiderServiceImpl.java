package com.gewara.untrans.spider.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.commons.sign.Sign;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.spider.RemoteSpiderService;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.xmlbind.ticket.SynchPlayItem;
import com.gewara.xmlbind.ticket.SynchPlayItemList;

@Service("remoteSpiderService")
public class RemoteSpiderServiceImpl extends AbstractSynchBaseService implements RemoteSpiderService, InitializingBean{
	
	public static final String playItemUrl = "/inner/getPlayItem.xhtml";
	public static final String cinemaPlayItemUrl = "/inner/getCinemaPlayItem.xhtml";
	
	private int TICKET_TIMEOUT = 120000;
	private String spiderApiUrl;
	@Value("${ticket.appkey}")
	private String appkey;
	@Value("${ticket.secretCode}")
	private String secretCode;

	@Override
	public void afterPropertiesSet() throws Exception {
		spiderApiUrl = config.getString("spiderApiUrl");
	}
	
	@Override
	protected HttpResult getRequestResult(String url, Map<String, String> params, int timeount) {
		params.put("appkey", appkey);
		params.put("sign", Sign.signMD5(params, secretCode));
		HttpResult result = super.getRequestResult(url, params, TICKET_TIMEOUT);
		if(Config.isDebugEnabled()){
			dbLogger.warn("url:" + url + ", params:" + params + ", return:" + result.getResponse());
		}
		return result;
	}
	
	@Override
	public ErrorCode<List<SynchPlayItem>> getRemotePlayItemListByUpdatetime(Timestamp updatetime, Long cinemaid){
		String url = spiderApiUrl + playItemUrl;
		Map<String, String> params = new HashMap<String,String>();
		params.put("updatetime", DateUtil.format(updatetime, "yyyy-MM-dd HH:mm:ss"));
		if(cinemaid != null) params.put("cinemaid", String.valueOf(cinemaid));
		ErrorCode<List<SynchPlayItem>> code = getObjectList(SynchPlayItemList.class, url, params, TICKET_TIMEOUT);
		return code;
	}
}
