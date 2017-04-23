package com.gewara.pay;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.WebUtils;

public class AlipayUtil{
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(AlipayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	public static final String check(String notifyid) {
		String inputLine = "";
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("notify_id", notifyid);
			HttpResult result = HttpUtils.getUrlAsString(PayOtherUtil.getAlipayCheckUrl(), params);
			if(result.isSuccess()) {
				dbLogger.warn("checkAlipaySuccess!!!!" + result.getResponse());
				return result.getResponse();
			}else{
				dbLogger.warn("checkFailure!!!!" +  result.getResponse());
			}
		} catch (Exception e) {
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, "", e);
		}
		return inputLine;
	}
	public static final String sign(Map<String, String[]> requestMap){
		Map<String, String> params = new HashMap<String, String>();
		for (Iterator iter = requestMap.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = requestMap.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		params.remove("sign");
		params.remove("sign_type");
		String content = getSignData(params);
		return content;
	}
	
	public static final boolean rsaSign(String userId,String userType,String sign){
		Map<String, String> params = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		sb.append("user_id=").append(userId).append("&userType=").append(userType);	
		params.put("content", sb.toString());
		params.put("sign", sign);
		HttpResult result = HttpUtils.getUrlAsString(PayOtherUtil.getAlipayRSACheckSignUrl(), params);
		if(result.isSuccess()){
			dbLogger.warn("sign response: " + result.getResponse());
			if(StringUtils.equals(result.getResponse(), "true")){
				return true;
			}
			return false;
		}
		dbLogger.warn("sign msg: " + result.getMsg());
		return false;
	}
	
	public static final String getRASSign(String signData){
		Map<String, String> params = new HashMap<String, String>();
		params.put("signData", signData);
		dbLogger.warn("signData:" + signData);
		HttpResult result = HttpUtils.getUrlAsString(PayOtherUtil.getAlipayRSASignUrl(), params);
		if(result.isSuccess()){
			dbLogger.warn("sign response: " + result.getResponse());
			return result.getResponse();
		}
		dbLogger.warn("sign msg: " + result.getMsg());
		return result.getMsg();
	}
	
	public static final String sign(String signData){
		return getSignByApi(signData);
	}
	public static final String getSignData(Map<String, String> params) {
		String content =  WebUtils.joinParams(params, false);
		dbLogger.warn("params content:" + content);
		return getSignByApi(content);
	}
	public static final String getSignByApi(String signData){
		Map<String, String> params = new HashMap<String, String>();
		params.put("signData", signData);
		dbLogger.warn("signData:" + signData);
		HttpResult result = HttpUtils.getUrlAsString(PayOtherUtil.getAlipaySignUrl(), params);
		if(result.isSuccess()){
			dbLogger.warn("sign response: " + result.getResponse());
			return result.getResponse();
		}
		dbLogger.warn("sign msg: " + result.getMsg());
		return result.getMsg();
	}
	public static String getShareUrl(String returnUrl){
		Map<String, String> params = new HashMap<String, String>();
		params.put("return_url", returnUrl);
		HttpResult result = HttpUtils.getUrlAsString(PayOtherUtil.getAlipayShareUrl(), params);
		if(result.isSuccess()){
			dbLogger.warn("aliShareUrl response: " + result.getResponse());
			return result.getResponse();
		}
		dbLogger.warn("aliShareUrl msg: " + result.getMsg());
		return result.getMsg();
	}
}
