package com.gewara.web.action.user;

import java.util.HashMap;
import java.util.Map;

import com.gewara.Config;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

public class ProcessSendEmail extends Thread{
	private Map<String, String> params = new HashMap<String, String>();
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private String preUrl = "http://localhost:8080";
	public ProcessSendEmail(String emailListStr, String memberid, String commuid, String basePath){
		params.put("memberid", memberid);
		params.put("emailListStr", emailListStr);
		if(commuid != null) params.put("commuid", commuid);
		params.put("check", StringUtil.md5WithKey(memberid, 10));
		this.preUrl += basePath;
		this.setDaemon(true);
	}
	@Override
	public void run(){
		HttpResult result = HttpUtils.postUrlAsString(preUrl + "processSendEmail.xhtml", params);
		dbLogger.error(params.get("sendemail") + ":" + result.getResponse());
	}
}

