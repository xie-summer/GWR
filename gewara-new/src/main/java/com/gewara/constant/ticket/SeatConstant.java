package com.gewara.constant.ticket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SeatConstant {
	public static final String STATUS_NEW = "A";//新座位
	public static final String STATUS_SELLING = "W";//售出未付款
	public static final String STATUS_SOLD = "S";//售出
	
	public static final String STATUS_LOCKB = "B";//影院售出锁定
	public static final String STATUS_LOCKC = "C";//保留座位锁定
	public static final String STATUS_LOCKD = "D";//赠票锁定
	
	public static final String SEAT_TYPE_A = "A";//通用座位
	public static final String SEAT_TYPE_B = "B";
	public static final String SEAT_TYPE_C = "C";
	public static final String SEAT_TYPE_D = "D";
	private static final Map<String, String> statusTextMap = new HashMap<String, String>();
	static{
		statusTextMap.put("B", "影院售出锁定");
		statusTextMap.put("C", "保留座位锁定");
		statusTextMap.put("D", "赠票锁定");
	}
	public static final List<String> STATUS_LOCK_LIST = Arrays.asList("B","C","D");

}
