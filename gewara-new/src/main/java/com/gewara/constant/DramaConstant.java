package com.gewara.constant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

public class DramaConstant {
	public static final String TYPE_DRAMA = "drama";
	public static final String TYPE_CONCERT = "concert";				//演唱会
	public static final String TYPE_MUSICALE = "musicale";				//音乐会
	public static final String TYPE_MUSICALPLAY = "musicalplay";		//音乐剧
	public static final String TYPE_DANCE = "dance";					//舞蹈
	public static final String TYPE_ACROBATICS = "acrobatics";			//曲艺杂技
	public static final String TYPE_RACE = "race";						//体育赛事
	public static final String TYPE_SHOW = "show";						//展览
	public static final String TYPE_OTHER = "other";					//其他
	
	public static final String PRETYPE_ENTRUST = "E"; 					//委托代售
	public static final String PRETYPE_MANAGE = "M";					//自主经营
	
	//话剧、演唱会、音乐剧、舞蹈、展会、曲艺杂技、体育赛事、儿童亲子、景点门票	
	public static final String TYPE_CHILDREN = "children"; 				//儿童亲子
	public static final String TYPE_ATTRACTICKET = "attracticket"; 		//景点门票
	public static final Map<String, String> dramaTypeMap;
	public static final Map<String, String> dramaSaleCycleMap;
	public static final Map<String, String> pretypeMap;
	static{
		Map<String, String> tmpMap = new LinkedHashMap<String, String>();
		tmpMap.put(TYPE_DRAMA, "话剧");
		tmpMap.put(TYPE_CONCERT, "演唱会");
		tmpMap.put(TYPE_MUSICALE, "音乐会");
		tmpMap.put(TYPE_MUSICALPLAY, "音乐剧");
		tmpMap.put(TYPE_DANCE, "舞蹈");
		tmpMap.put(TYPE_ACROBATICS, "曲艺杂技");
		tmpMap.put(TYPE_RACE, "体育赛事");
		tmpMap.put(TYPE_SHOW, "展会");
		tmpMap.put(TYPE_CHILDREN, "儿童亲子");
		tmpMap.put(TYPE_ATTRACTICKET, "景点门票");
		tmpMap.put(TYPE_OTHER, "其他");
		dramaTypeMap = UnmodifiableMap.decorate(tmpMap);
		Map<String, String> cycleTmpMap = new LinkedHashMap<String, String>();
		cycleTmpMap.put("1", "第一周期");
		cycleTmpMap.put("2", "第二周期");
		cycleTmpMap.put("3", "第三周期");
		cycleTmpMap.put("4", "第四周期");
		cycleTmpMap.put("5", "第五周期");
		dramaSaleCycleMap = UnmodifiableMap.decorate(cycleTmpMap);
		Map<String, String> tmpPretypeMap = new LinkedHashMap<String, String>();
		tmpPretypeMap.put(PRETYPE_MANAGE, "格瓦拉");
		tmpPretypeMap.put(PRETYPE_ENTRUST, "合作方");
		pretypeMap = UnmodifiableMap.decorate(tmpPretypeMap);
	}
	
	public static String getDramaTypeText(String type){
		String tmp = dramaTypeMap.get(type);
		if(StringUtils.isBlank(tmp)) return "";
		return tmp;
	}
}
