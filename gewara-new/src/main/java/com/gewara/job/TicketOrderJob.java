package com.gewara.job;


public interface TicketOrderJob {
	/**
	 *定时更正“待处理（paidFailure）订单” 
	 */
	void correctOrder();
	void checkHfhOrder();
	/**
	 * 定时传送订单
	 */
	void sendCallbackOrder();
	/**
	 * 没有通知的订单
	 */
	void unNotifyOrder();
	//void addOpiUpdate(Long mpid, boolean isFinished);
}
