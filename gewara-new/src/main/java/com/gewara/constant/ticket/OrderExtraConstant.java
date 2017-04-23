package com.gewara.constant.ticket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

public abstract class OrderExtraConstant {
	public static final String EXPRESS_YUNDA = "YUNDA";		//韵达快递
	public static final String EXPRESS_SF = "SF";			//顺风快递
	
	public static final List<String> EXPRESS_TYPE_LIST = Arrays.asList(EXPRESS_YUNDA, EXPRESS_SF);
	public static final Map<String,String> EXPRESS_TYPE_TEXT_MAP = new HashMap<String, String>();
	static{
		EXPRESS_TYPE_TEXT_MAP.put(EXPRESS_YUNDA, "韵达快递");
		EXPRESS_TYPE_TEXT_MAP.put(EXPRESS_SF, "顺风快递");
	}
	
	public static String getExpressTypeText(String expresstype){
		String tmp = EXPRESS_TYPE_TEXT_MAP.get(expresstype);
		if(StringUtils.isNotBlank(tmp)) return tmp;
		return "未知";
	}
	
	public static final String EXPRESS_STATUS_NEW = "new";				//待处理
	public static final String EXPRESS_STATUS_PRINT = "print";          //完成打印配货单
	public static final String EXPRESS_STATUS_ALLOCATION = "allocation";//完成配货
	public static final String EXPRESS_STATUS_TRANSIT = "transit";		//运输中
	public static final String EXPRESS_STATUS_SIGNFAIL = "signfail";	//签收失败
	public static final String EXPRESS_STATUS_SIGNED = "signed";		//签收成功
	
	public static final Map<String,String> EXPRESS_DEALSTATUS_STEP;		//快递流程步骤
	public static final Map<String,String> EXPRESS_STATUS_TEXT;			//状态说明
	static{
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put(EXPRESS_STATUS_NEW, "待处理");
		tmp.put(EXPRESS_STATUS_PRINT, "完成打印配货单");
		tmp.put(EXPRESS_STATUS_ALLOCATION, "完成配货");
		tmp.put(EXPRESS_STATUS_TRANSIT, "运输中");
		tmp.put(EXPRESS_STATUS_SIGNFAIL, "签收失败");
		tmp.put(EXPRESS_STATUS_SIGNED, "签收成功");
		EXPRESS_STATUS_TEXT = UnmodifiableMap.decorate(tmp);
		Map<String, String> tmpStep = new HashMap<String, String>();
		tmpStep.put(EXPRESS_STATUS_PRINT, EXPRESS_STATUS_NEW);
		tmpStep.put(EXPRESS_STATUS_ALLOCATION, EXPRESS_STATUS_PRINT);
		tmpStep.put(EXPRESS_STATUS_TRANSIT, EXPRESS_STATUS_ALLOCATION);
		tmpStep.put(EXPRESS_STATUS_SIGNFAIL, EXPRESS_STATUS_TRANSIT);
		tmpStep.put(EXPRESS_STATUS_SIGNED, EXPRESS_STATUS_TRANSIT);
		EXPRESS_DEALSTATUS_STEP = UnmodifiableMap.decorate(tmpStep);
	}
	
	public static final String DEAL_TYPE_FRONT = "front";				//处理类型:前端
	public static final String DEAL_TYPE_BACKEND = "backend";			//处理类型:后端
	
	public static final String INVOICE_N = "N";							//可开发票
	public static final String INVOICE_F = "F";							//不能开发票
	public static final String INVOICE_Y = "Y";							//已开发票
	
	public static final String PRETYPE_ENTRUST = "E"; 					//委托代售
	public static final String PRETYPE_MANAGE = "M";					//自主经营
	
}
