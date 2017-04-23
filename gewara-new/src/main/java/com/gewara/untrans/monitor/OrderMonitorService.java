package com.gewara.untrans.monitor;

import java.util.Map;

import com.gewara.model.pay.GewaOrder;

public interface OrderMonitorService {
	/**
	 * 下单日志记录接口
	 * @param memberid
	 * @param order
	 * @param action
	 */
	void addOrderChangeLog(String tradeNo, String action, GewaOrder order, String otherinfo);
	void addOrderChangeLog(String tradeNo, String action, Map<String, String> info, Long userid);
	void addOrderChangeLog(String tradeNo, String action, String otherinfo, Long userid);
	void addOrderPayCallback(String tradeNo, String returnType, String paymethod, String otherinfo);
}
