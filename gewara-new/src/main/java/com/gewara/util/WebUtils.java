package com.gewara.util;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.model.common.GewaCity;
import com.gewara.support.ErrorCode;

public abstract class WebUtils extends BaseWebUtils{
	public static String getAndSetDefault(HttpServletRequest request, HttpServletResponse response) {
		String citypinyin = request.getParameter(AdminCityContant.CITYPINYINKEY);
		String citycode = AdminCityContant.getCodeByPinyin(citypinyin);
		if(StringUtils.isBlank(citycode)){ 
			Cookie cookie = getCookie(request, "citycode");
			if (cookie != null) {
				citycode = cookie.getValue();
				if (isValidCitycode(citycode)){
					return citycode;
				}
			}
			
			citycode = "310000";
			if(!isRobot(request.getHeader("User-Agent"))) {
				citycode = getCitycodeByIp(getRemoteIp(request));
			}
		}else{
			//强制设置citycode
			request.setAttribute(AdminCityContant.CITYCODE_KEY, citycode);
		}
		Cookie cookie = new Cookie("citycode", citycode);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24);// 24 hour
		response.addCookie(cookie);
		return citycode;
	}
	
	public static String getCitycodeByIp(String ip){
		String citycode = "310000";
		try {
			String address = IPUtil.getAddress(ip);
			if (StringUtils.isNotBlank(address)) {
				Map<String, List<GewaCity>> proMap = AdminCityContant.getProMap();
				for (String proName : proMap.keySet()) {
					if (StringUtils.contains(address, proName)){
						boolean isBreak = true;
						List<GewaCity> cityList = proMap.get(proName);
						for (GewaCity gewaCity : cityList) {
							if(StringUtils.contains(address, gewaCity.getCityname())){
								citycode = gewaCity.getCitycode();
								isBreak = false;
								break;
							}
						}
						if(isBreak){
							citycode = cityList.get(0).getCitycode();
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			dbLogger.error("", e);
			dbLogger.warn("获取城市代码错误!");
		}
		return citycode;
	}
	public static ErrorCode getAndSetDefaultWap(HttpServletRequest request, HttpServletResponse response) {
		String citycode = "";
		Cookie cookie = getCookie(request, "citycode");
		if (cookie != null) {
			citycode = cookie.getValue();
			if (!isValidCitycode(citycode))
				return ErrorCode.getFailure("城市未开通！");
			return ErrorCode.getSuccess(citycode);
		}
		if (StringUtils.isBlank(citycode))
			citycode = "310000";
		cookie = new Cookie("citycode", citycode);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24);// 24 hour
		response.addCookie(cookie);
		return ErrorCode.getSuccess(citycode);
	}

	public static boolean isValidCitycode(String citycode) {
		Map<String, String> cityMap = AdminCityContant.allcityMap;
		return cityMap.containsKey(citycode);
	}

	public static void setCitycode(HttpServletRequest request, String citycode, HttpServletResponse response) {
		//强制设置citycode
		request.setAttribute(AdminCityContant.CITYCODE_KEY, citycode);
		Cookie cookie = new Cookie("citycode", citycode);
		cookie.setPath("/");
		cookie.setMaxAge(60 * 60 * 24);// 24 hour
		response.addCookie(cookie);
	}
	
	public static String urlDecoder(String str){
		return urlDecoder(str, "utf-8");
	}
	
	public static String urlDecoder(String str, String encode){
		String tmp = "";
		try {
			tmp = URLDecoder.decode(str, encode);
		} catch (Exception e) {
		}
		return tmp;
	}
	
	public static String getIpAndPort(String ip, HttpServletRequest request){
		if(StringUtils.isBlank(ip)) return null;
		String port = request.getHeader("x-client-port");
		if(StringUtils.isBlank(port)) return ip;
		String result = ip + ":" + port;
		return result;
	}

	public static boolean isAjaxRequest(HttpServletRequest request) {
		boolean result = StringUtils.isNotBlank(request.getHeader("X-Requested-With"));
		return result;
	}
}
