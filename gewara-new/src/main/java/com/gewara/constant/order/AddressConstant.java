package com.gewara.constant.order;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.PartnerConstant;

public class AddressConstant {
	public static final String ADDRESS_WEB="web";//web
	public static final String ADDRESS_WAP="wap";//wap
	public static final String ADDRESS_MOBILE="mobile";//ÊÖ»ú¶ÌÐÅ
	public static final String ADDRESS_IPHONE="iphone";//iphone
	public static final String ADDRESS_ANDROID="android";//android
	public static final String ADDRESS_SYMBIAN="symbian";//symbian
	public static final String ADDRESS_BARANDROID = "barandroid";
	public static final String ADDRESS_BARIPHONE = "bariphone";
	public static final String ADDRESS_SPORTANDROID = "sportandroid";
	public static final String ADDRESS_SPORTIPHONE = "sportiphone";
	public static Map<String,String> addressMap;
	public static Map<Long,String> partnerAddressMap;
	static{
		Map<String,String> map = new HashMap<String, String>();
		map.put(ADDRESS_WEB,"ÍøÒ³°æ");
		map.put(ADDRESS_WAP,"WAP°æ");
		map.put(ADDRESS_IPHONE,"iPhone°æ");
		map.put(ADDRESS_ANDROID,"Android°æ");
		map.put(ADDRESS_SYMBIAN, "Symbian°æ");
		map.put(ADDRESS_MOBILE, "ÊÖ»ú¶ÌÐÅ");
		map.put(ADDRESS_BARIPHONE,"iPhone°æ");
		map.put(ADDRESS_BARANDROID,"Android°æ");
		map.put(ADDRESS_SPORTIPHONE,"iPhone°æ");
		map.put(ADDRESS_SPORTANDROID,"Android°æ");
		addressMap = UnmodifiableMap.decorate(map);
		
		Map<Long,String> amap = new HashMap<Long, String>();
		amap.put(PartnerConstant.GEWA_SELF,"ÍøÒ³°æ");
		amap.put(PartnerConstant.GEWAP,"WAP°æ");
		amap.put(PartnerConstant.ANDROID,"Android°æ");
		amap.put(PartnerConstant.IPHONE,"iPhone°æ");
		amap.put(PartnerConstant.GEWA_DRAMA_ANDROID,"Android°æ");
		amap.put(PartnerConstant.GEWA_DRAMA_IPHONE,"iPhone°æ");
		amap.put(PartnerConstant.GEWA_SPORT_ANDROID,"Android°æ");
		partnerAddressMap = UnmodifiableMap.decorate(amap);
	}
	public static String getApiAddress(Long partnerid){
		String address = partnerAddressMap.get(partnerid);
		if(StringUtils.isBlank(address)) address = partnerAddressMap.get(PartnerConstant.GEWA_SELF);
		return address;
	}
	public static final String ADDRESS_ALL = "all";
}
