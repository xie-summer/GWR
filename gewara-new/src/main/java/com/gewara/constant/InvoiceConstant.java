package com.gewara.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceConstant {
	public static final String STATUS_OPEN = "Y"; //已开
	public static final String STATUS_UNOPEN = "N"; //未开

	public static final String STATUS_OPENED = "Y_OPEN"; //已开
	public static final String STATUS_UNPOST = "Y_NOTPOST";//未邮寄
	public static final String STATUS_POST_EXPRESS = "Y_EXP"; //快递
	public static final String STATUS_POST_COMMON = "Y_POST";//平邮 
	public static final String STATUS_UNOPENED = "N_NOTOPEN";//未开
	public static final String STATUS_APPLY = "N_APPLY";//申请
	public static final String STATUS_TRASH="N_TRASH";//发票废弃，重新申请
	public static final String STATUS_APPLY_AGAIN="N_APPLYAGAIN";//申请补开
	public static final String STATUS_OPEN_AGAIN="Y_AGAIN";//已补开
	//发票操作列表(已申请、已邮寄、未邮寄、已快递、未开、已重新补开申请、已开、已补开)
	public static final List<String> statusList= Arrays.asList(
					STATUS_OPENED, STATUS_UNPOST, STATUS_POST_EXPRESS,
					STATUS_POST_COMMON,STATUS_UNOPENED, STATUS_APPLY, 
					STATUS_APPLY_AGAIN, STATUS_OPEN_AGAIN);
	public static final Map<String, String> STATUSDESC_MAP = new HashMap<String, String>();
	static{
		STATUSDESC_MAP.put(STATUS_APPLY, "申请中");
		STATUSDESC_MAP.put(STATUS_OPENED, "发票已开");
		STATUSDESC_MAP.put(STATUS_UNPOST, "未邮寄");
		STATUSDESC_MAP.put(STATUS_POST_EXPRESS, "快递");
		STATUSDESC_MAP.put(STATUS_POST_COMMON, "平邮");
		STATUSDESC_MAP.put(STATUS_UNOPENED, "未开");
		STATUSDESC_MAP.put(STATUS_OPEN, "已开");
		STATUSDESC_MAP.put(STATUS_UNOPEN, "未开");
		STATUSDESC_MAP.put(STATUS_APPLY, "申请");
		STATUSDESC_MAP.put(STATUS_TRASH, "废弃发票");
		STATUSDESC_MAP.put(STATUS_APPLY_AGAIN, "申请补开");
		STATUSDESC_MAP.put(STATUS_OPEN_AGAIN, "已补开");
	}
	
}
