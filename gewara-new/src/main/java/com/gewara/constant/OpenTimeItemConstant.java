package com.gewara.constant;

import java.util.Arrays;
import java.util.List;
public abstract class OpenTimeItemConstant {
	public static final String STATUS_NEW 		= "A";			//新座位
	public static final String STATUS_LOCKR 	= "R";			//场馆售出锁定
	public static final String STATUS_LOCKL 	= "L";			//锁定[本地人为锁定]
	public static final String STATUS_LOCKLF 	= "LF";			//锁定[格瓦拉强制锁定]
	public static final String STATUS_LOCKD 	= "D_GW";		//锁定[新订单锁定]
	public static final String STATUS_SOLD 	= "S_GW";			//售出
	public static final String STATUS_DELETE = "delete";		//删除
	public static final String TIGHT_SPORT_TIGHT = "SportTight";
	public static final String TIGHT_SPORT_ID = "sportid";
	public static final String TIGHT_ITEM_ID = "itemid";
	public static final String TIGHT_PLAY_DATE = "playdate";
	public static final String TIGHT_BEFORE_TIME_NUM = "beforeTimeNum";		//几点之前总数
	public static final String TIGHT_BEFORE_TIME_COUNT = "beforeTimeCount";	//几点之前售出数量
	public static final String TIGHT_AFTER_TIME_NUM = "afterTimeNum";		//几点之后总数
	public static final String TIGHT_AFTER_TIME_COUNT = "afterTimeCount";	//几点之后售出数量
	public static final String TIGHT_TIME_TEMP = "timetemp"; //几点
	
	public static final List<String> LOCKEDLIST = Arrays.asList(new String[]{OpenTimeItemConstant.STATUS_LOCKR, OpenTimeItemConstant.STATUS_LOCKL, OpenTimeItemConstant.STATUS_LOCKD, OpenTimeItemConstant.STATUS_LOCKLF});
	
	public static final List<String> remoteStatus = Arrays.asList(new String[]{"W", "S", "D"});
}
