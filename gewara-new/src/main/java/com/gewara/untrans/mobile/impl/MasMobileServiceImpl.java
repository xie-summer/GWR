/**
 * 
 */
package com.gewara.untrans.mobile.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.gewara.model.pay.SMSRecord;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.mobile.MobileService;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;

@Service("masMobileService")
public class MasMobileServiceImpl implements MobileService {
	private static final String url = "http://mas.gewala.net:3456/callcenter/mas/sendMessage.xhtml";
	private static final String key = "MASSYS";
	private static final String encryptCode = "84c75120440e13c7eadbf18b4e84e152";
	
	@Override
	public ErrorCode sendMessage(SMSRecord sms) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", key);
		params.put("encryptCode", encryptCode);
		params.put("id", sms.getId() + "");
		params.put("mobile", sms.getContact());
		params.put("content", sms.getContent());
		HttpResult code = HttpUtils.getUrlAsString(url, params, 15*1000);
		String retval = code.getResponse();
		if (StringUtils.contains(retval, "success")) return ErrorCode.SUCCESS;
		else return ErrorCode.getFailure("ERROR");
	}
}
