package com.gewara.constant.app;

import java.util.Arrays;
import java.util.List;


public class AppConstant {
	public static final String TABLE_APPSOURCE = "appsource";
	
	public static final String OSTYPE_ANDROID = "ANDROID";
	public static final String OSTYPE_IPHONE = "IPHONE";
	public static final String APPTYPE_CINEMA = "cinema";
	public static final String APPTYPE_SPORT = "sport";
	public static final String CHINASMART_STARTWIDTH = "chinaSmart";
	//电影APP版本常量
	//早期版块
	public static final String MOVIE_APPVERSION_1_5_6 = "1.5.6";
	//切换到api2
	public static final String MOVIE_APPVERSION_2_1_0 = "2.0.0";
	//增加活动、新闻资讯等
	public static final String MOVIE_APPVERSION_3_0_0 = "3.0.0";
	//增加积分支付，增加多个余额不足支付
	public static final String MOVIE_APPVERSION_3_1_0 = "3.1.0";
	public static final String MOVIE_APPVERSION_3_2_0 = "3.2.0";
	//使用openapi、增加活动收费、套餐订购
	public static final String MOVIE_APPVERSION_4_0_0 = "4.0.0";
	
	public static final String SPORT_APPVERSION_3_0_0 = "3.0.0";
	
	//加入微信、财付通支付
	public static final String MOVIE_APPVERSION_4_5 = "4.5";
	//加入二维码取票及观影日程等等
	public static final String MOVIE_APPVERSION_4_6 = "4.6";
	
	//观影日程，出行工具
	public static List<String> transToolList = Arrays.asList("foot","subway", "drive", "transit");
}
