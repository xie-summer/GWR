package com.gewara.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.common.GewaCity;

public class AdminCityContant {
	public static final String CITYCODE_SH = "310000";
	public static final String CITYCODE_ALL = "000000"; // citycode 全国为 000000, 新闻/活动需要
	public static final List<String> zxsList = Arrays.asList("110000","120000", "500000", "310000");
	public static final List<String> zxsNameList = Arrays.asList("上海","北京", "天津市");
	public static final String CITYPINYINKEY = "cityPinyinKey";//系统切换时的城市拼音参数
	public static final String CITYCODE_KEY = "syscitycodeKey";//系统切换时的城市代码

	//key:citycode value:城市名 所有
	public static Map<String, String> allcityMap = new HashMap<String, String>();
	//key:拼音 value:citycode
	public static Map<String, String> pinyinMap = new HashMap<String, String>();
	//key:citycode value:拼音
	public static Map<String, String> citycode2PinyinMap = new HashMap<String, String>();
	//key:citycode value:城市
	public static Map<String, GewaCity> citycode2GewaCity = new HashMap<String, GewaCity>();
	//key:拼音 value:城市名
	public static Map<String, String> pinyin2CitynameMap = new HashMap<String, String>();
	//key:省份名 value:cityList
	public static Map<String, List<GewaCity>> proMap = new HashMap<String, List<GewaCity>>();
	//key:citycode value:城市名	对外展示
	public static Map<String, String> citycode2CitynameMap = new HashMap<String, String>();
	//key:citycode value:point 城市百度坐标
	public static Map<String, String[]> citycodeBPointMap = new HashMap<String, String[]>();
	public static Map<GewaCity, List<GewaCity>> admMap = new HashMap<GewaCity, List<GewaCity>>();
	
	public static Map<GewaCity, List<GewaCity>> getAdmMap() {
		return admMap;
	}

	public static void setAdmMap(Map<GewaCity, List<GewaCity>> admMap) {
		AdminCityContant.admMap = admMap;
	}

	public static Map<String, String> getAllcityMap() {
		return allcityMap;
	}

	public static void setAllcityMap(Map<String, String> allcityMap) {
		AdminCityContant.allcityMap = allcityMap;
	}

	public static Map<String, String> getPinyinMap() {
		return pinyinMap;
	}

	public static void setPinyinMap(Map<String, String> pinyinMap) {
		AdminCityContant.pinyinMap = pinyinMap;
	}
	
	public static String getCodeByPinyin(String citypy) {
		if(StringUtils.isBlank(citypy)) return null;
		return pinyinMap.get(citypy);
	}
	public static String getCityNameByCode(String citycode) {
		return allcityMap.get(citycode);
	}
	
	public static Map<String, String> getCitycode2PinyinMap() {
		return citycode2PinyinMap;
	}

	public static void setCitycode2PinyinMap(Map<String, String> citycode2PinyinMap) {
		AdminCityContant.citycode2PinyinMap = citycode2PinyinMap;
	}

	public static Map<String, GewaCity> getCitycode2GewaCity() {
		return citycode2GewaCity;
	}

	public static void setCitycode2GewaCity(Map<String, GewaCity> citycode2GewaCity) {
		AdminCityContant.citycode2GewaCity = citycode2GewaCity;
	}

	public static Map<String, String> getPinyin2CitynameMap() {
		return pinyin2CitynameMap;
	}

	public static void setPinyin2CitynameMap(Map<String, String> pinyin2CitynameMap) {
		AdminCityContant.pinyin2CitynameMap = pinyin2CitynameMap;
	}

	public static Map<String, List<GewaCity>> getProMap() {
		return proMap;
	}

	public static void setProMap(Map<String, List<GewaCity>> proMap) {
		AdminCityContant.proMap = proMap;
	}

	public static Map<String, String> getCitycode2CitynameMap() {
		return citycode2CitynameMap;
	}

	public static void setCitycode2CitynameMap(Map<String, String> citycode2CitynameMap) {
		AdminCityContant.citycode2CitynameMap = citycode2CitynameMap;
	}

	public static Map<String, String[]> getCitycodeBPointMap() {
		return citycodeBPointMap;
	}

	public static void setCitycodeBPointMap(Map<String, String[]> citycodeBPointMap) {
		AdminCityContant.citycodeBPointMap = citycodeBPointMap;
	}
	
}
