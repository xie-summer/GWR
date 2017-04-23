package com.gewara.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author acerge(acerge@163.com)
 * @since 9:36:50 AM Sep 15, 2009
 */
public class GewaSearchKey implements Serializable{
	private static final long serialVersionUID = 4914995483381697551L;
	public static List<Integer> charList = new ArrayList<Integer>();
	private String keycode;
	private String name;
	private String tag;//¿‡–Õ±Í«©
	private Long relatedid;
	private String category;
	private String skey;
	private Integer sksort;
	private String status;
	private Long timenum;
	private Object relatedObj;   
	private String citycode;	
	private static final Map<String, Integer> category_sortMap = new HashMap<String, Integer>();
	static{
		category_sortMap.put("movie", 100);
		category_sortMap.put("cinema", 95);
		category_sortMap.put("drama", 90);
		category_sortMap.put("theatre", 85);
		category_sortMap.put("sportservice", 80);
		category_sortMap.put("sport", 75);
		category_sortMap.put("gymcourse", 70);
		category_sortMap.put("gym", 65);
		category_sortMap.put("ktv", 60);
		category_sortMap.put("bar", 55);
		category_sortMap.put("activity", 50);
		category_sortMap.put("commu", 45);
		category_sortMap.put("dramastar", 40);
		category_sortMap.put("gymcoach", 35);
		category_sortMap.put("barsinger", 30);
		category_sortMap.put("video", 25);
		category_sortMap.put("news", 20);
		category_sortMap.put("gewaquestion", 15);
		category_sortMap.put("diary", 5);
	}
	public static Integer getSortByCategory(String category){
		return category_sortMap.get(category);
	}
	
	public GewaSearchKey(String keycode, String name, String tag, Long relatedid, String category, String skey, Integer sksort, String status, Long timenum) {
		this.keycode = keycode;
		this.name = name;
		this.tag = tag;
		this.relatedid = relatedid;
		this.category = category;
		this.skey = skey;
		this.sksort = sksort;
		this.status = status;
		this.timenum = timenum;
	}
	
	public GewaSearchKey(String keycode, String name, String tag, Long relatedid, String category, String skey, Integer sksort, String status, Long timenum,String citycode) {
		this.keycode = keycode;
		this.name = name;
		this.tag = tag;
		this.relatedid = relatedid;
		this.category = category;
		this.skey = skey;
		this.sksort = sksort;
		this.status = status;
		this.timenum = timenum;
		this.citycode=citycode;
	}

	public static List<Integer> getCharList(String str){
		List<Integer> list = new ArrayList<Integer>();
		String ss[] = str.split("-");
		int start = Integer.valueOf(ss[0]);
		int end = Integer.valueOf(ss[1]);
		for(int i= start; i<=end;i++){
			list.add(i);
		}
		return list;
	}
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}

	public GewaSearchKey() {
	}
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	
	public Object getRelatedObj() {
		return relatedObj;
	}
	public void setRelatedObj(Object relatedObj) {
		this.relatedObj = relatedObj;
	}
	public Integer getSksort() {
		return sksort;
	}
	public void setSksort(Integer sksort) {
		this.sksort = sksort;
	}
	public Long getTimenum() {
		return timenum;
	}
	public void setTimenum(Long timenum) {
		this.timenum = timenum;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public String getKeycode() {
		return keycode;
	}
	public void setKeycode(String keycode) {
		this.keycode = keycode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	
}
