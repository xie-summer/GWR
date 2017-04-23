package com.gewara.constant;

import org.apache.commons.lang.StringUtils;

public abstract class ChargeConstant {
	public static final String TYPE_CHARGE = "charge";			//充值
	public static final String TYPE_ORDER = "order";			//订单支付中转
	
	public static final String BANKPAY = "bank";			//账户
	public static final String WABIPAY = "wabi";			//瓦币
	public static final String DEPOSITPAY = "deposit";			//保证金
	
	public static final String KEY_CHARGE_VALIDTIME_ = "CHARGE_VALIDTIME_";
	
	public static boolean isBankPay(String chargetype){
		return StringUtils.equals(chargetype, TYPE_ORDER);
	}
}
