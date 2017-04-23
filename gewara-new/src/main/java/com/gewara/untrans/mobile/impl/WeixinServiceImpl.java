package com.gewara.untrans.mobile.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.mobile.WeixinService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WeixinUtil;

@Service("weixinService")
public class WeixinServiceImpl implements WeixinService{
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Value("${weixin_token}")
	private String token;
	
	@Value("${weixin_appid}")
	private String appid;
	
	@Value("${weixin_appsecret}")
	private String appsecret;
	
	@Value("${weixin_touser}")
	private String touser;
	
	@Override
	public  boolean verify(String signature, String timestamp, String nonce) {
		String[] arr = {token, timestamp, nonce };
		Arrays.sort(arr);
		String tmpStr = StringUtils.join(arr);
		String str = StringUtil.sha(tmpStr, "UTF-8");
		return StringUtils.equalsIgnoreCase(str, signature);
	}
	@Override
	public ErrorCode<Map<String, String>> getAccessToken() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "client_credential");
		params.put("appid", appid);
		params.put("secret", appsecret);
		HttpResult result = HttpUtils.getUrlAsString(WeixinUtil.GETTOKEN_URL, params);
		String response = result.getResponse();
		dbLogger.warn(result.getResponse() + "," + result.getMsg());
		if(!result.isSuccess()){
			return ErrorCode.getFailure(result.getMsg());
		}
		Map<String, String> map = JsonUtils.readJsonToMap(response);
		if(map.containsKey("errmsg")){
			return ErrorCode.getFailure(map.get("errmsg"));
		}
		return ErrorCode.getSuccessReturn(map);
	}
	@Override
	public ErrorCode<String> validReq(String tms, String wxs){
		String res = StringUtil.md5(tms+appsecret, 8);
		if(!StringUtils.equalsIgnoreCase(wxs, res)){
			return ErrorCode.getFailure("验证不通过！");
		}
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Timestamp time = DateUtil.parseTimestamp(tms, "yyyyMMddHHmmss");
		if(time.before(curtime)){
			return ErrorCode.getFailure("链接地址已超时，请重新获取！");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public String getWxs(String tms){
		return StringUtil.md5(tms + appsecret, 8);
	}
	@Override
	public String getWxToUser(){
		return touser;
	}
}
