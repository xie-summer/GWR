package com.gewara.constant;

import java.util.ArrayList;
import java.util.List;
public abstract class OpenTimeTableConstant {
	public static final int MAX_MINUTS_TICKETS = 15;	//交易最大保留时间（分钟）
	public static final int MAX_HOUR_TICKETS = 12;		//交易最大保留时间（小时）
	
	public static final String STATUS_BOOK = "Y"; 								// 接受预订
	public static final String STATUS_NOBOOK = "N"; 							// 不接受预订
	public static final String STATUS_DISCARD = "D"; 							// 废弃
	public static final String VERSION_V2 = "2.0";
	
	public static final String OPEN_TYPE_PERIOD = "period";						//时段
	public static final String OPEN_TYPE_FIELD = "field";						//场地
	public static final String OPEN_TYPE_INNING = "inning"; 					//局数
	
	public static final String UNIT_TYPE_WHOLE = "whole";						//整体时段
	public static final String UNIT_TYPE_TIME = "time";							//单位时段
	
	public static final String SALE_STATUS_LOCK = "lock";						//锁定
	public static final String SALE_STATUS_UNLOCK = "unlock";					//未锁定
	public static final String SALE_STATUS_SUCCESS = "success";					//竞价成功
	public static final String SALE_STATUS_SUCCESS_PAID = "success_paid";		//竞价支付成功
	
	public static final String ITEM_TYPE_COM = "0";			//普通场地
	public static final String ITEM_TYPE_VIP = "1";			//会员场地
	public static final String ITEM_TYPE_VIE = "2";			//竞拍场地
	
	public static final String KEY_OPENTIMESALE_OPENTIME_ = "OPENTIMESALE_OPENTIME_";
	public static final String KEY_OPENTIMESALE_CLOSETIME_ = "OPENTIMESALE_CLOSETIME_";
	
	public static final String GUARANTEE_UNPAY = "unpay";		//未付保证金
	
	public static List<String> opentypeList;
	static{
		opentypeList = new ArrayList<String>();
		opentypeList.add(OPEN_TYPE_FIELD);
		opentypeList.add(OPEN_TYPE_PERIOD);
		opentypeList.add(OPEN_TYPE_INNING);
	}
	public static List<String> getOpentypeList() {
		return opentypeList;
	}
	public boolean isValidOpentype(String opentype){
		return opentypeList.contains(opentype);
	}
}
