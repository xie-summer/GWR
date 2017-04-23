package com.gewara.pay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gewara.Config;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;


public class PayBoxPayUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(PayBoxPayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static final String PARTNER_ID = "10531000078320001";
	private static final String KEY = "37A0C99931994134B00F2006DA68A074";
	
	public static String getBoxKey(){
		return KEY;
	}
	
	public static String getCheckBoxLogin(ApiUser partner, String token,String iboxUserId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("parterId",PARTNER_ID);
		params.put("token", token);
		params.put("iboxUserId", iboxUserId);
		params.put("signType", "1");
		List<String> keyList = new ArrayList(params.keySet());
		Collections.sort(keyList);
		StringBuilder sb = new StringBuilder();
		for (String key : keyList) {
			sb.append(key).append("=").append(params.get(key)).append("&");
		}
		sb.append("key=").append(KEY);
		dbLogger.warn(sb.toString() + ":md5:"  + StringUtil.md5(sb.toString()).toUpperCase());
		try {
			params.put("signMsg",StringUtil.md5(URLEncoder.encode(sb.toString(), "UTF-8")).toUpperCase());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "fail";
		}
		HttpResult code = HttpUtils.getUrlAsString(VmUtils.readJsonToMap(partner.getOtherinfo()).get("partnerLoginUrl"), params);
		if(code.isSuccess()){
			dbLogger.warn(code.getResponse());
			return code.getResponse();
		}
		return "fail";
	}
	
	public static String bindUser(String token,String memeberId,String iboxUserId,ApiUser partner){
		Map<String, String> params = new HashMap<String, String>();
		params.put("parterId",PARTNER_ID);
		params.put("partnerUserId", memeberId);
		params.put("token", token);
		params.put("iboxUserId", iboxUserId);
		params.put("signType", "1");
		List<String> keyList = new ArrayList(params.keySet());
		Collections.sort(keyList);
		StringBuilder sb = new StringBuilder();
		for (String key : keyList) {
			sb.append(key).append("=").append(params.get(key)).append("&");
		}
		sb.append("key=").append(KEY);
		dbLogger.warn(sb.toString() );
		try {
			params.put("signMsg",StringUtil.md5(URLEncoder.encode(sb.toString(), "UTF-8")).toUpperCase());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "fail";
		}
		HttpResult code = HttpUtils.getUrlAsString(VmUtils.readJsonToMap(partner.getOtherinfo()).get("bindUserUrl"), params);
		if(code.isSuccess()){
			dbLogger.warn(code.getResponse());
			return code.getResponse();
		}
		return "fail";
	}
	
	public static String queryOrder(String orderNo,String orderSerial, ApiUser partner){
		Map<String, String> params = new HashMap<String, String>();
		params.put("parterId",PARTNER_ID);
		params.put("orderNo", orderNo);
		params.put("orderSerial",orderSerial);
		params.put("signType", "1");
		List<String> keyList = new ArrayList(params.keySet());
		Collections.sort(keyList);
		StringBuilder sb = new StringBuilder();
		for (String key : keyList) {
			sb.append(key).append("=").append(params.get(key)).append("&");
		}
		sb.append("key=").append(KEY);
		try {
			String signMsg = sb.toString();
			String md5SignMsg = StringUtil.md5(URLEncoder.encode(signMsg, "UTF-8")).toUpperCase();
			dbLogger.warn(signMsg + "[md5]" +  md5SignMsg);
			params.put("signMsg",md5SignMsg);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "fail";
		}
		HttpResult code = HttpUtils.postUrlAsString(partner.getQryurl(), params);
		if(code.isSuccess()){
			dbLogger.warn(code.getResponse());
			return code.getResponse();
		}
		return "fail";
	}
	
	public static String saveOrder(GewaOrder order, ApiUser partner,String iboxUserId){
		Map<String, String> params = new HashMap<String, String>();
		params.put("parterId",PARTNER_ID);
		params.put("goodsName", "格瓦拉电影票");
		params.put("bizType", "1");
		params.put("orderNo", order.getTradeNo());
		params.put("orderTime", DateUtil.format(order.getAddtime(), "yyyyMMddHHmmss"));
		params.put("orderAmount", (order.getDue() * 100) + "");
		params.put("callbackUrl", partner.getNotifyurl());
		params.put("cutOffTime", DateUtil.format(order.getValidtime(), "yyyyMMddHHmmss"));
		params.put("signType", "1");
		params.put("iboxUserId", iboxUserId);
		List<String> keyList = new ArrayList(params.keySet());
		Collections.sort(keyList);
		StringBuilder sb = new StringBuilder();
		for (String key : keyList) {
			sb.append(key).append("=").append(params.get(key)).append("&");
		}
		sb.append("key=").append(KEY);
		try {
			String signMsg = sb.toString();
			String md5SignMsg = StringUtil.md5(URLEncoder.encode(signMsg, "UTF-8")).toUpperCase();
			dbLogger.warn(signMsg + "[md5]" +  md5SignMsg);
			params.put("signMsg",md5SignMsg);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "fail";
		}
		HttpResult code = HttpUtils.postUrlAsString(partner.getAddOrderUrl(), params);
		if(code.isSuccess()){
			dbLogger.warn(code.getResponse());
			return code.getResponse();
		}
		return "fail";
	}
}
