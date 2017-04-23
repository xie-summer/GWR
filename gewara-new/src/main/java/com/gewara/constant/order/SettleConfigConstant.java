package com.gewara.constant.order;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class SettleConfigConstant implements Serializable {
	private static final long serialVersionUID = 1607327403420971718L;

	public static final String DISCOUNT_TYPE_PERCENT = "percent";			//结算百分比
	public static final String DISCOUNT_TYPE_UPRICE = "uprice";			//物品折扣
	
	public static final Map<String, String> DISCOUNT_TYPEMAP = new HashMap<String,String>();
	static{
		DISCOUNT_TYPEMAP.put(DISCOUNT_TYPE_PERCENT, "按基价(乘)");
		DISCOUNT_TYPEMAP.put(DISCOUNT_TYPE_UPRICE, "按基价(减)");
	}
}
