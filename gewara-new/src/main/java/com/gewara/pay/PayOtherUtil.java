package com.gewara.pay;

import java.util.Properties;

public class PayOtherUtil {
	//Ö§¸¶±¦
	private static String alipayShareUrl;
	private static String alipaySignUrl;
	private static String alipayCheckUrl;
	private static String alipayRSASignUrl;
	private static String alipayRSACheckSignUrl;
	private static String aliUserDetailUrl;
	private static String aliUserTokenUrl;
	private static String aliUserInfoUrl;
	//Ê¢´ó
	private static String spsdoSendOrderUrl;
	//RSAÇ©Ãû
	private static String rsaSignUrl;
	private static String tenPayBankUrl;
	private static boolean initialized = false;
	public static synchronized void init(String propertyFile){
		if(initialized) return;
		Properties props = new Properties();
		try {
			props.load(ChinapayUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		alipayShareUrl = props.getProperty("alipayShareUrl");
		alipaySignUrl = props.getProperty("alipaySignUrl");
		alipayCheckUrl = props.getProperty("alipayCheckUrl");
		alipayRSASignUrl = props.getProperty("alipayRSASignUrl");
		alipayRSACheckSignUrl = props.getProperty("alipayRSACheckSignUrl");
		
		aliUserDetailUrl = props.getProperty("aliUserDetailUrl");
		aliUserTokenUrl = props.getProperty("aliUserTokenUrl");
		aliUserInfoUrl = props.getProperty("aliUserInfoUrl");
		
		spsdoSendOrderUrl = props.getProperty("spsdoSendOrderUrl");
		
		rsaSignUrl = props.getProperty("rsaSignUrl");
		tenPayBankUrl = props.getProperty("tenPayBankUrl");
		
		initialized = true;
	}
	public static String getAlipayShareUrl() {
		return alipayShareUrl;
	}
	public static String getAlipaySignUrl() {
		return alipaySignUrl;
	}
	public static String getAlipayCheckUrl() {
		return alipayCheckUrl;
	}
	public static String getSpsdoSendOrderUrl() {
		return spsdoSendOrderUrl;
	}
	public static String getAlipayRSASignUrl(){
		return alipayRSASignUrl;
	}
	
	public static String getAlipayRSACheckSignUrl(){
		return alipayRSACheckSignUrl;
	}
	public static String getAliUserDetailUrl() {
		return aliUserDetailUrl;
	}
	public static String getAliUserTokenUrl() {
		return aliUserTokenUrl;
	}
	public static String getAliUserInfoUrl() {
		return aliUserInfoUrl;
	}
	public static String getRsaSignUrl() {
		return rsaSignUrl;
	}
	public static String getTenPayBankUrl() {
		return tenPayBankUrl;
	}
	
}
