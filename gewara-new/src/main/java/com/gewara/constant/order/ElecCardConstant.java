package com.gewara.constant.order;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

public abstract class ElecCardConstant implements Serializable {

	private static final long serialVersionUID = 8398429592873502608L;
	
	//数据属性：历史、正在使用
	public static final String DATA_HIS = "data_his";
	public static final String DATA_NOW = "data_now";
	public static final String DATA_ALL = "data";
	
	public static final String STATUS_NEW = "N";	//待售
	public static final String STATUS_SOLD = "Y";	//售出
	public static final String STATUS_DISCARD = "D";//废弃
	public static final String STATUS_USED = "U";   //使用过
	public static final String STATUS_LOCK = "L";   //冻结使用, Y------>L

	
	
	public static final String EDITION_ALL = "ALL";		//所有版本可用
	public static final String EDITION_3D = "3D";		//3D可用
	public static final String EDITION_2D = "2D";		//3D可用
	private static final Map<String, String> imaxMap;
	private static final Map<String, String> normalMap;
	public static final Map<String, String> CARDTYPEMAP = new HashMap<String, String>();
	static{
		Map<String, String> imaxTmp = new HashMap<String, String>(3);
		imaxTmp.put("A", "IMAX券");
		imaxTmp.put("B", "绿券");
		imaxTmp.put("D", "抵扣券");
		imaxMap = UnmodifiableMap.decorate(imaxTmp);
		Map<String, String> normalTmp = new HashMap<String, String>(3);
		normalTmp.put("A", "橙券、蓝券");
		normalTmp.put("B", "绿券");
		normalTmp.put("D", "抵扣券");
		normalMap = UnmodifiableMap.decorate(normalTmp);
		
		CARDTYPEMAP.put("A", "兑换券");
		CARDTYPEMAP.put("B", "补差券");
		CARDTYPEMAP.put("C", "优惠券");
		CARDTYPEMAP.put("D", "优惠券");
		CARDTYPEMAP.put("E", "充值券");
	}
	
	public static Map<String, String> getNormalMap(){
		return normalMap;
	}
	
	public static Map<String, String> getImaxMap(){
		return imaxMap;
	}
	
	public static String getCardtype(String cardtype) {
		if(StringUtils.isBlank(cardtype)) return "";
		return CARDTYPEMAP.get(cardtype);
	}

}
