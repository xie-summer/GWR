package com.gewara.jms;

public interface JmsConstant {
	String TAG_ORDER = "order";									//订单信息
	String TAG_UPADATE_PAGE_CACHE = "update_page_cache";		//更新页面缓存
	String TAG_SHARE2Out = "share2Out";							//分享微博
	String TAG_SHARECUSTOM = "shareCustom";						//分享微博自定义
	String TAG_SAVELOG = "saveLog";								//发送日志
	String TAG_CHARGE = "charge";								//充值
	
	String TAG_TERMINALBARCODE = "terminalbarcode";				//终端二维码取票
	
	String QUEUE_PAY = "paidOrderQueue";						//支付完成的队列
	String QUEUE_CHARGE = "paidChargeQueue";					//支付完成的队列
	String QUEUE_SUCCORDER = "addOrderQueue";					//成功订单队列
	String QUEUE_UPDATECACHE = "updateCacheQueue";				//更新缓存
	String QUEUE_SHARE = "shareQueue";							//分享队列
	String QUEUE_ORDER_ACTIVITYGOODS = "activityGoodsQueue";	//活动订单队列
	String QUEUE_ORDER_GYM = "gymOrderQueue";					//健身订单队列
	String QUEUE_TICKETPLAYITEM = "ticketPlayItemQueue";		//订票系统排片更新
	
	String QUEUE_TICKETREMOTEORDER = "ticketRemoteOrderQueue";	//满天星处理订单队列	

	String QUEUE_SPIDERPLAYITEM = "spiderPlayItemQueue";		//Spider系统排片更新
	
	String QUEUE_TERMINAL_ORDER = "terminalOrderQueue";			//订单交易成功通知到终端机
	
	String QUEUE_GPTICKETREMOTEORDER= "gpticketRemoteOrderQueue";	//演出订单退票通知
}
