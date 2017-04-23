package com.gewara.web.action.partner;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;

public class PartnerUtil {
	public static String getParamStr(HttpServletRequest request, String... pnames){
		Map<String, String[]> params = new HashMap<String, String[]>();
		if(pnames==null || pnames.length==0){
			params.putAll(request.getParameterMap());
		} else {
			for(String pn: pnames) params.put(pn, request.getParameterValues(pn));
		}
		Long validtime = System.currentTimeMillis() + DateUtil.m_minute * 30;
		params.put("SYS_VALIDTIME", new String[]{""+validtime});
		String qryStr = JsonUtils.writeMapToJson(WebUtils.flatRequestMap(params, ","));
		return PKCoderUtil.encryptString(qryStr, "partner");
	}
	public static String getParamStr(Map<String, String> params){
		Long validtime = System.currentTimeMillis() + DateUtil.m_minute * 30;
		params.put("SYS_VALIDTIME",""+validtime);
		String qryStr = JsonUtils.writeMapToJson(params);
		return PKCoderUtil.encryptString(qryStr, "partner");
	}
	public static String addParamStr(String encQryStr, String name, String value){
		Map<String, String> params = getParamMap(encQryStr);
			params.put(name, value);
		String qryStr = WebUtils.getQueryStr(params, "utf-8");
		return PKCoderUtil.encryptString(qryStr, "partner");
	}
	public static String addParamStr(String encQryStr, String[] names, String[] values){
		Map<String, String> params = getParamMap(encQryStr);
		for(int i=0;i<names.length;i++){
			params.put(names[i], values[i]);
		}
		String qryStr = WebUtils.getQueryStr(params, "utf-8");
		return PKCoderUtil.encryptString(qryStr, "partner");
	}
	public static Map<String, String> getParamMap(String encQryStr) throws IllegalArgumentException{
		try{
			String qryStr = PKCoderUtil.decryptString(encQryStr, "partner");
			Map<String, String> result = VmUtils.readJsonToMap(qryStr);
			Long validtime = new Long(result.get("SYS_VALIDTIME"));
			if(validtime < System.currentTimeMillis()) {
				result = new HashMap<String, String>();
				result.put("ERROR_MSG", "链接超时，请返回来源处重新打开");
			}
			return result;
		}catch(Exception e){
			Map<String, String> result = new HashMap<String, String>();
			result.put("ERROR_MSG", "链接验证错误，请返回来源处重新打开");
			return result;
		}
	}
	public static String setUkCookie(HttpServletResponse response, String path){
		String ukey = StringUtil.getRandomString(30);
		Cookie cookie = new Cookie("ukey", ukey);
		cookie.setPath(path);
		cookie.setMaxAge(60 * 60 * 12);//12 hour
		response.addCookie(cookie);
		return ukey;
	}
	public static String getPartnerCookieValue(String cookie){
		String result = PKCoderUtil.decryptString(cookie, "partner");
		return result;
	}
	public static void setPartnerCookie(HttpServletResponse response, String path, String cookieValue){
		Cookie cookie = new Cookie("pkey", PKCoderUtil.encryptString(cookieValue, "partner"));
		cookie.setPath(path);
		cookie.setMaxAge(60 * 60 * 1);//1 hour
		response.addCookie(cookie);
	}

}
