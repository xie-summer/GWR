package com.gewara.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.Status;
import com.gewara.model.common.GewaCity;
import com.gewara.service.GewaCityService;
import com.gewara.util.BeanUtil;
@Service("gewaCityService")
public class GewaCityServiceImpl  extends BaseServiceImpl implements GewaCityService, InitializingBean{
	private Map<String, String> allcityMap = new HashMap<String, String>();
	private Map<String, String> pinyin2CitycodeMap = new HashMap<String, String>();
	private Map<String, String> citycode2PinyinMap = new HashMap<String, String>();
	private Map<String, String> citycode2PyMap = new HashMap<String, String>();
	private Map<String, String> pinyin2CitynameMap = new HashMap<String, String>();
	private Map<String, String> citycode2CitynameMap = new HashMap<String, String>();
	private Map<String, String[]> citycodeBPointMap = new HashMap<String, String[]>();
	//热门城市
	private List<GewaCity> hotcityList = new ArrayList<GewaCity>();
	//后台省份及城市
	private Map<GewaCity, List<GewaCity>> admMap = new HashMap<GewaCity, List<GewaCity>>();
	//手机
	private Map<GewaCity, List<GewaCity>> mobileMap = new HashMap<GewaCity, List<GewaCity>>();
	//前台省份及城市
	private Map<GewaCity, List<GewaCity>> idxMap = new HashMap<GewaCity, List<GewaCity>>();
	//根据provincecode得到省份下的城市
	private Map<String, List<GewaCity>> idxProMap = new HashMap<String, List<GewaCity>>();
	//前台所有城市
	private List<GewaCity> idxList = new ArrayList<GewaCity>(); 
	
	@Override
	public List<GewaCity> getHotCityList(){
		return hotcityList;
	}
	@Override
	public Map<GewaCity, List<GewaCity>> getAdmCityMap(){
		return admMap;
	}
	@Override
	public Map<GewaCity, List<GewaCity>> getIdxCityMap(){
		return idxMap;
	}
	@Override
	public Map<String, List<GewaCity>> getIdxProCityMap(){
		return idxProMap;
	}
	
	@Override
	public Map<String, String> getPinyin2CitycodeMap(){
		return pinyin2CitycodeMap;
	}
	
	@Override
	public Map<String, String> getAllcityMap(){
		return allcityMap;
	}
	
	@Override
	public Map<String, String> getPinyin2CitynameMap() {
		return pinyin2CitynameMap;
	}
	@Override
	public Map<String, String> getCitycode2PinyinMap() {
		return citycode2PinyinMap;
	}
	@Override
	public Map<String, String> getCitycode2PyMap() {
		return citycode2PyMap;
	}
	@Override
	public Map<String, String[]> getMobileMap() {
		Map<String, String[]> resMap = new LinkedHashMap<String, String[]>();
		resMap.put("直辖市", new String[] { "310000", "110000", "500000", "120000"});
		for(GewaCity pcity : mobileMap.keySet()){
			List<GewaCity> cityList = mobileMap.get(pcity);
			List<String> citycodeList = BeanUtil.getBeanPropertyList(cityList, "citycode", false);
			if(citycodeList.size()>00){
				String[] a = new String[citycodeList.size()];
				resMap.put(pcity.getProvincename(), citycodeList.toArray(a));
			}
		}
		return resMap;
	}
	@Override
	public void initCityList() {
		List<GewaCity> allCityList =  baseDao.getAllObjects(GewaCity.class);
		Map<String, GewaCity> proMap = new HashMap<String, GewaCity>();
		Map<String, List<GewaCity>> proCityMap = BeanUtil.groupBeanList(allCityList, "provincecode");
		//热门城市
		List<GewaCity> tmphotcityList = new ArrayList<GewaCity>();
		List<GewaCity> tempCityList = new ArrayList<GewaCity>(); 
		for(GewaCity city : allCityList){
			if(StringUtils.equals(city.getShowIdx(), Status.Y)){
				if(StringUtils.equals(city.getShowHot(), Status.Y)){
					tmphotcityList.add(city);
				}
				citycode2CitynameMap.put(city.getCitycode(), city.getCityname());
				tempCityList.add(city);
				pinyin2CitycodeMap.put(city.getPinyin(), city.getCitycode());
			}
			if(!proMap.containsKey(city.getProvincecode())){
				proMap.put(city.getProvincecode(), city);
			}
			allcityMap.put(city.getCitycode(), city.getCityname());
			citycode2PinyinMap.put(city.getCitycode(), city.getPinyin());
			citycode2PyMap.put(city.getCitycode(), city.getPy());
			pinyin2CitynameMap.put(city.getPinyin(), city.getCityname());
		}
		AdminCityContant.setAllcityMap(allcityMap);
		AdminCityContant.setPinyinMap(pinyin2CitycodeMap);
		AdminCityContant.setCitycode2GewaCity(BeanUtil.beanListToMap(allCityList, "citycode"));
		AdminCityContant.setCitycode2PinyinMap(citycode2PinyinMap);
		AdminCityContant.setPinyin2CitynameMap(pinyin2CitynameMap);
		AdminCityContant.setCitycode2CitynameMap(citycode2CitynameMap);
		Map<GewaCity, List<GewaCity>> tmpadmMap = new TreeMap<GewaCity, List<GewaCity>>(new ProSort());
		Map<GewaCity, List<GewaCity>> tmpidxMap = new TreeMap<GewaCity, List<GewaCity>>(new ProSort());
		Map<GewaCity, List<GewaCity>> tmpMobMap = new TreeMap<GewaCity, List<GewaCity>>(new ProSort());
		Map<String, List<GewaCity>> tmpidxProMap = new TreeMap<String, List<GewaCity>>();
		Map<String, List<GewaCity>> tmpProMap = new TreeMap<String, List<GewaCity>>();
		Map<String, String[]> tempBPointMap = new HashMap<String, String[]>();
		for(String key : proCityMap.keySet()){
			List<GewaCity> cyList = proCityMap.get(key);
			Collections.sort(cyList, new PropertyComparator("citySort", true, true));
			GewaCity proCity = proMap.get(key);
			tmpadmMap.put(proCity, cyList);
			List<GewaCity> tmpcyList = new ArrayList<GewaCity>();
			List<GewaCity> tmpmobList = new ArrayList<GewaCity>();
			for(GewaCity cy : cyList){
				if(StringUtils.equals(cy.getShowIdx(), Status.Y)){
					tmpcyList.add(cy);
					if(StringUtils.isNotBlank(cy.getBpointx()) && StringUtils.isNotBlank(cy.getBpointy())){
						String[] tmpPoint = new String[]{cy.getBpointx(), cy.getBpointy()};
						tempBPointMap.put(cy.getCitycode(), tmpPoint);
					}
				}
				if(!AdminCityContant.zxsList.contains(cy.getCitycode())){
					if(StringUtils.equals(cy.getShowAdm(), Status.Y)){
						tmpmobList.add(cy);
					}
				}
			}
			if(!tmpcyList.isEmpty()){
				tmpidxMap.put(proCity, tmpcyList);
				tmpidxProMap.put(proCity.getProvincecode(), tmpcyList);
				tmpProMap.put(proCity.getProvincename(), tmpcyList);
			}
			if(!tmpmobList.isEmpty()){
				tmpMobMap.put(proCity, tmpmobList);
			}
		}
		Collections.sort(tmphotcityList, new PropertyComparator("citySort", true, true));
		hotcityList = tmphotcityList;
		Collections.sort(tempCityList, new PropertyComparator("citySort", true, true));
		idxList = tempCityList;
		admMap = tmpadmMap;
		idxMap = tmpidxMap;
		idxProMap = tmpidxProMap;
		mobileMap = tmpMobMap;
		citycodeBPointMap = tempBPointMap;
		AdminCityContant.setProMap(tmpProMap);
		AdminCityContant.setCitycodeBPointMap(tempBPointMap);
		AdminCityContant.setAdmMap(admMap);
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		initCityList();
	}
	class ProSort implements Comparator<GewaCity>{
		@Override
		public int compare(GewaCity o1, GewaCity o2) {
			int r = o1.getProvinceSort() - o2.getProvinceSort();
			if(r==0) return o1.getCitycode().compareTo(o2.getCitycode());
			return r;
		}
		
	}
	public Map<String, String[]> getCitycodeBPointMap() {
		return citycodeBPointMap;
	}
	@Override
	public List<GewaCity> getIdxList() {
		return idxList;
	}

}
