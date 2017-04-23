package com.gewara.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Flag;
import com.gewara.model.BaseObject;
import com.gewara.model.agency.Agency;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Province;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.sport.Sport;
import com.gewara.util.BeanUtil;

public class ServiceHelper {
	private static Map<String, Class<? extends BaseObject>> placeLocationMap = new HashMap<String, Class<? extends BaseObject>>();
	private static final Map<String, Boolean> tagMap = new HashMap<String, Boolean>();
	private static final Map<String, String> categoryMap = new HashMap<String, String>();
	private static Map<String, Class<? extends BaseInfo>> placeClassMap = new HashMap<String, Class<? extends BaseInfo>>();
	private static final Map<String, String> reasonMap = new HashMap<String, String>();
	private static final List<String> otherinfoList = new ArrayList<String>();
	private static final List<String> robotcityList = new ArrayList<String>();
	private static final List<String> gymcourseList = new ArrayList<String>();
	static{
		placeLocationMap.put("province", Province.class);
		placeLocationMap.put("city", City.class);
		placeLocationMap.put("county", County.class);
		placeLocationMap.put("indexarea", Indexarea.class);
		
		placeClassMap.put("cinema", Cinema.class);
		placeClassMap.put("sport", Sport.class);
		placeClassMap.put("theatre",Theatre.class);
		placeClassMap.put("agency",Agency.class);
		
		tagMap.put("cinema", true);
		tagMap.put("ktv", true);
		tagMap.put("sport", true);
		tagMap.put("bar", true);
		tagMap.put("gym", true);
		tagMap.put("activity", true);
		tagMap.put("theatre",true);
		
		
		categoryMap.put("gymcourse", "gym");
		categoryMap.put("gymcoach", "gym");
		categoryMap.put("gymcurriculum", "gym");//健身课程
		categoryMap.put("gymcard", "gym");//健身卡
		categoryMap.put("sportservice", "sport");
		categoryMap.put("movie", "cinema");
		categoryMap.put("drama", "theatre");
		categoryMap.put("dramastar", "theatre");
		
		
		
		reasonMap.put("1", "广告信息");
		reasonMap.put("2", "政治敏感话题");
		reasonMap.put("3", "情、色、暴力等不健康信息");
		reasonMap.put("4", "非法买卖等违法信息");
		
		otherinfoList.add(Flag.SERVICE_WEBCOMMENT);
		otherinfoList.add(Flag.SERVICE_PAIRSEAT);
		otherinfoList.add(Flag.SERVICE_PARK); 
		otherinfoList.add(Flag.SERVICE_VISACARD); 
		otherinfoList.add(Flag.SERVICE_PLAYGROUND); 
		otherinfoList.add(Flag.SERVICE_3D); 
		otherinfoList.add(Flag.SERVICE_SALE); 
		otherinfoList.add(Flag.SERVICE_FOOD); 
		otherinfoList.add(Flag.SERVICE_RESTREGION); 
		otherinfoList.add(Flag.SERVICE_IMAX); 
		otherinfoList.add(Flag.SERVICE_CHILD); 
		otherinfoList.add(Flag.SERVICE_CUPBOARD); 
		otherinfoList.add(Flag.SERVICE_BATHE); 
		otherinfoList.add(Flag.SERVICE_INDOOR); 
		otherinfoList.add(Flag.SERVICE_OUTDOOR);	
		otherinfoList.add(Flag.SERVICE_SITECOUNT);	
		otherinfoList.add(Flag.SERVICE_TRAIN); 
		otherinfoList.add(Flag.SERVICE_MEAL);		
		otherinfoList.add(Flag.SERVICE_HEIGHTVENUE); 
		otherinfoList.add(Flag.SERVICE_FLOORING); 
		otherinfoList.add(Flag.SERVICE_LEASE); 
		otherinfoList.add(Flag.SERVICE_MAINTAIN);
		otherinfoList.add(Flag.SERVICE_OPENINFO);
		otherinfoList.add(Flag.SERVICE_SEOTITLE);
		otherinfoList.add(Flag.SERVICE_SEODESCRIPTION);
		otherinfoList.add(Flag.SERVICE_MEMBERCARD);
		
		otherinfoList.add(Flag.SERVICE_PARK_RECOMMEND);
		otherinfoList.add(Flag.SERVICE_VISACARD_RECOMMEND);
		otherinfoList.add(Flag.SERVICE_RESTREGION_RECOMMEND);
		otherinfoList.add(Flag.SERVICE_SALE_RECOMMEND);
		otherinfoList.add(Flag.SERVICE_CUPBOARD_RECOMMEND);
		otherinfoList.add(Flag.SERVICE_BATHE_RECOMMEND);
		otherinfoList.add(Flag.SERVICE_MEAL_RECOMMENDL);
		otherinfoList.add(Flag.SERVICE_TRAIN_RECOMMENDL);
		otherinfoList.add(Flag.SERVICE_LEASE_RECOMMENDL);
		otherinfoList.add(Flag.SERVICE_MAINTAIN_RECOMMENDL);
		otherinfoList.add(Flag.SERVICE_MEMBERCARD_RECOMMENDL);
		
		gymcourseList.add(Flag.APPLE_PEOPLE);
		gymcourseList.add(Flag.CONSUMPTION_LEVEL);
		gymcourseList.add(Flag.DIFFICULT_EASY);
		gymcourseList.add(Flag.ESSENTIALEQUIPMENT);
		
		robotcityList.add("440300");
	}
	public static boolean isRobotAllow(String citycode){
		return robotcityList.contains(citycode);
	}
	public static List<String> getGymcourselist() {
		return gymcourseList;
	}

	public static List<String> getOtherInfoList(){
		return otherinfoList;
	}
	
	public  static String assignID(String tag){
		return tag + System.currentTimeMillis();
	}
	
	public static boolean isTag(String tag) {
		if(StringUtils.isBlank(tag)) return false;
		return tagMap.get(tag) != null;
	}

	public static boolean isCategory(String tag) {
		if(StringUtils.isBlank(tag)) return false;
		return categoryMap.get(tag) != null;
	}
	
	public static String getReason(String reason) {
		if(StringUtils.isBlank(reason)) return "";
		return reasonMap.get(reason);
	}
	
	public static Class<? extends BaseInfo> getPalceClazz(String tag) {
		return placeClassMap.get(tag);
	}
	public static Class<? extends BaseObject> getPlaceLocationClazz(String tag) {
		return placeLocationMap.get(tag);
	}

	public static String getTag(String category){
		if(StringUtils.isBlank(category)) return null;
		if(isTag(category)) return category;
		return categoryMap.get(category);
	}
	
	
	/**
	 * 将 request.getParameterMap().get(key) 方法重写 
	 */
	public static String get(Map<String, String[]> map, String key){
		if(map != null && map.size() > 0){
			if(map.get(key) != null){
				return map.get(key)[0];
			}
			return "";
		}
		return "";
	}
	public static Map requestMap(HttpServletRequest request){
		Map<String, String[]> map = request.getParameterMap();
		Map<String, String> requestMap = new HashMap<String, String>();
		for(String key : map.keySet()){
			requestMap.put(key, map.get(key)[0]);
		}
		return requestMap;
	}

	/**
	 * 返回clazz的顶层superclass：如果clazz的superClass 是 BaseObject，则直接返回clazz
	 * @param clazz
	 * @return
	 */
	public static <T extends BaseObject> Class  getBaseRootClazz(Class<T> clazz){
		Class tmp = clazz;
		for(int i=0;i<4;i++){
			if(tmp.getSuperclass().equals(BaseObject.class)) return tmp;
			else if(tmp.getSuperclass().equals(Object.class)) return clazz;
			tmp = tmp.getSuperclass();
		}
		return clazz;
	}
	
	/** 全局Memberid List */
	public static List<Long> getMemberIdListFromBeanList(final Collection beanList){
		return BeanUtil.getBeanPropertyList(beanList, Long.class, "memberid", true);
	}
}
