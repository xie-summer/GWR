package com.gewara.constant;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheatreSeatConstant implements Serializable {
	private static final long serialVersionUID = -3625538381513198225L;
	public static final String STATUS_NEW = "A";//新座位
	public static final String STATUS_SELLING = "W";//售出未付款
	public static final String STATUS_SOLD = "S";//售出
	
	public static final String STATUS_LOCKB 	= "B";//影院售出锁定(自己操作)
	public static final String STATUS_LOCKC 	= "C";//保留座位锁定
	public static final String STATUS_LOCKD 	= "D";//赠票锁定
	
	public static final String SEATMAP_KEY = "ODI_AREA_SEATMAP_";
	public static final String SEATMAP_UPDATE = "ODI_AREA_SEATMAP_UPDATE_";

	private static final Map<String, String> statusTextMap = new HashMap<String, String>();
	public static final List<String> STATUS_LOCK_LIST = Arrays.asList(STATUS_LOCKB, STATUS_LOCKC, STATUS_LOCKD);
	static{
		statusTextMap.put("B", "场馆售出锁定");
		statusTextMap.put("C", "保留座位锁定");
		statusTextMap.put("D", "赠票锁定");
	}

}
