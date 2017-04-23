package com.gewara.constant.app;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

/**
 * 应用类型字典
 * 
 * @author taiqichao
 * 
 */
public class AppType {

	public static final String APP_TYPE_CINAME = "cinema";
	public static final String APP_TYPE_SPORT = "sport";
	public static final String APP_TYPE_BAR = "bar";
	public static final String APP_TYPE_CINAME_CMCC="cinema_cmcc";

	public static final Map<String, String> APPTYPE_MAP;

	static {
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put(APP_TYPE_CINAME, "电影");
		tmp.put(APP_TYPE_BAR, "酒吧");
		tmp.put(APP_TYPE_SPORT, "运动");
		tmp.put(APP_TYPE_CINAME_CMCC, "移动android客户端");
		APPTYPE_MAP = UnmodifiableMap.decorate(tmp);
	}
}
