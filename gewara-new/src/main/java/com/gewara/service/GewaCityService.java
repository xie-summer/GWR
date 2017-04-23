package com.gewara.service;

import java.util.List;
import java.util.Map;

import com.gewara.model.common.GewaCity;

public interface GewaCityService{
	void initCityList();
	List<GewaCity> getHotCityList();
	Map<GewaCity, List<GewaCity>> getAdmCityMap();
	Map<GewaCity, List<GewaCity>> getIdxCityMap();
	Map<String, List<GewaCity>> getIdxProCityMap();
	Map<String, String> getPinyin2CitycodeMap();
	Map<String, String> getAllcityMap();
	Map<String, String> getPinyin2CitynameMap();
	Map<String, String> getCitycode2PinyinMap();
	Map<String, String> getCitycode2PyMap();
	Map<String, String[]> getMobileMap();
	List<GewaCity> getIdxList();
}
