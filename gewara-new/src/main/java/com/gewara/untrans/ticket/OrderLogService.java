package com.gewara.untrans.ticket;

import java.util.Map;

public interface OrderLogService {
	String ACTION_GETPARAMS = "getPayParams";		//获取支付参数
	String ACTION_APIERROR = "apiError";
	String ACTION_CALLBACK = "callback";
	String ACTION_MANUAL2PAY = "manual2Pay";
	String ACTION_FINDMERCHANT = "findMerchant";
	void addSysLog(String tradeNo, String paymethod, String action, Long userid);
	void addOrderChangeLog(String tradeNo, String action, Map<String, String> info);
}
