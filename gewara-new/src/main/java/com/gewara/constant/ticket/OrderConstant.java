package com.gewara.constant.ticket;

import java.util.HashMap;
import java.util.Map;

public class OrderConstant {
	public static final String CHANGEHIS_KEY_CHANGESEAT = "changeSeat";	//更改座位
	public static final String CHANGEHIS_KEY_SUCCESSCHANGE = "successChange";//成功订单更换座位
	public static final String CHANGEHIS_KEY_MPITO = "mpito";				//更换场次到
	public static final String CHANGEHIS_KEY_MPIFROM = "mpifrom";			//从场次来
	public static final String CHANGEHIS_KEY_RECONFIRMS = "reconfirms";	//重新确认订单次数
	public static final String CHANGEHIS_KEY_BUYTIMES = "buytimes";		//该手机历史订单数量
	public static final String CHANGEHIS_KEY_PROCESSTIMES = "processtimes";	//该订单处理次数
	
	public static final String OTHERKEY_PROCESSERROR = "processError";//
	public static final String OTHERKEY_CREDENTIALSID = "credentialsId";		//商家认证ID
	public static final String OTHERKEY_BINDMEMBER = "bindMember";				//电话下单绑定用户
	public static final String OTHERKEY_TELEPHONE = "telephone";				//电话下单接听电话
	public static final String OTHERKEY_BINDMOBILE = "bindMobile";				//电话下单绑定手机
	public static final String OTHERKEY_CREATEMEMBER = "createMember";			//电话下单是否创建
	public static final String OTHERKEY_DELAY_CARDNO = "delayCardNo";      //电子票券有偿延期票券号
	
	public static final String OTHERKEY_GREETINGS = "greetings";				//个性化票面
	
	public static final String STATUS_NEW = "new";						//新订单，座位已锁定
	public static final String STATUS_NEW_UNLOCK = "new_unlock";		//新订单，未锁定（临时状态）
	public static final String STATUS_NEW_CONFIRM = "new_confirm";		//新订单，确认去付款
	public static final String STATUS_PAID = "paid";					//付完款，订单未必是成交的
	public static final String STATUS_PAID_FAILURE = "paid_failure";	//付完款，订单有错误
	public static final String STATUS_PAID_UNFIX = "paid_failure_unfix";//付完款，订单座位未确认
	public static final String STATUS_PAID_SUCCESS = "paid_success";	//付款后，订单成交
	public static final String STATUS_PAID_RETURN = "paid_return";		//付完款，订单取消退款到余额
	public static final String STATUS_EMAIL_ID = "email_id";				//邮件ID

	public static final String STATUS_CANCEL = "cancel";					//订单取消了
	public static final String STATUS_SYS_CANCEL = "cancel_sys";		//系统取消
	public static final String STATUS_SYS_CHANGE_CANCEL = "cancel_sys_change";	//成功订单系统换座取消
	public static final String STATUS_REPEAT = "cancel_repeat";			//重复订单
	public static final String STATUS_USER_CANCEL = "cancel_user";		//用户取消了
	public static final String STATUS_TIMEOUT = "cancel_timeout";		//超时取消
	public static final Map<String, String> statusMap = new HashMap<String, String>();
	
	public static final String UNIQUE_BY_MEMBERID = "memberid";			//订单参与活动唯一性标识：使用用户唯一性
	public static final String UNIQUE_BY_MOBILE = "mobile";				//订单参与活动唯一性标识：使用手机
	public static final String UNIQUE_BY_MEMBER_AND_MOBILE = "all";	//订单参与活动唯一性标识：使用手机+账号
	public static final String UNIQUE_BY_PARTNERNAME = "partnername";	//订单参与活动唯一性标识：使用手机+账号
	
	public static final String ORDER_EXPRESSNO = "expressNo";			//快递订单号
	public static final String ORDER_EXPRESSMode = "expressMode";			//快递订单号
	public static final String SYSBANK_BUY = "buy";
	public static final String SYSBANK_GIFT = "gift";
	
	//培训商户确认订单
	public static final String TRAINING_ORDER_IS_SURE = "isSure";
	
	//与商家结算状态
	public static final String SETTLE_NONE = "O";	//未知
	public static final String SETTLE_N = "N";		//不结算
	public static final String SETTLE_Y = "Y";		//结算
	
	public static final String DISCOUNT_STATUS_Y = "Y";		//已使用成功
	public static final String DISCOUNT_STATUS_N = "N";		//未使用成功

	//订单类型
	public static final String ORDER_TYPE_TICKET = "ticket";		//电影票
	public static final String ORDER_TYPE_GOODS = "goods";			//物品
	public static final String ORDER_TYPE_SPORT = "sport";			//运动
	public static final String ORDER_TYPE_DRAMA = "drama";			//话剧
	public static final String ORDER_TYPE_GYM = "gym";				//健身
	public static final String ORDER_TYPE_PUBSALE = "pubsale";		//竞拍
	public static final String ORDER_TYPE_GUARANTEE = "guarantee";	//保证金
	public static final String ORDER_TYPE_MEMBERCARD = "membercard";//会员卡
	
	//订单模块
	public static final String ORDER_PRICATEGORY_MOVIE = "movie";		//电影模块	
	public static final String ORDER_PRICATEGORY_DRAMA = "drama";		//演出模块	
	public static final String ORDER_PRICATEGORY_SPORT = "sport";		//运动模块	
	public static final String ORDER_PRICATEGORY_GYM = "gym";			//健身模块	
	public static final String ORDER_PRICATEGORY_BAR = "bar";			//酒吧模块	
	public static final String ORDER_PRICATEGORY_ACTIVITY = "activity";	//活动模块	
	public static final String ORDER_PRICATEGORY_PUBSALE = "pubsale";	//竞拍模块	
	public static final String ORDER_PRICATEGORY_POINT = "point";		//积分对兑模块	
	public static final String ORDER_PRICATEGORY_GROUPON = "groupon";	//团购模块	
	
	//远程订单状态
	public static final String REMOTE_STATUS_NEW = "N";		//新订单
	public static final String REMOTE_STATUS_LOCK = "0";		//座位锁定
	public static final String REMOTE_STATUS_FIXED = "1";		//订单成功
	public static final String REMOTE_STATUS_UNLOCK = "2";		//座位解锁
	public static final String REMOTE_STATUS_ERROR = "X";		//错误
	public static final String REMOTE_STATUS_CANCEL = "C";		//退票 

	public static final String CHECKMARK_N = "N";		//未检查
	public static final String CHECKMARK_Y = "Y";		//检查
	
	//订单产生其他费用的原因
	public static final String OTHERFEE_REASON_UMPAY = "umPay";						//话费支付手续费
	public static final String OTHERFEE_REASON_UMPAY_SH = "umPay_sh";				//话费支付手续费
	public static final String OTHERFEE_REASON_EXPRESS = "express";					//快递费用
	
	public static final String ORDER_TAKETIME = "taketime";							//取票时间
	
	public static final Long CARD_DELAY_GOODSID = 152385164L;//线上卖品id    152385164
	static{
		statusMap.put(STATUS_CANCEL, "订单被取消");
		statusMap.put(STATUS_REPEAT, "重复订单");
		statusMap.put(STATUS_USER_CANCEL, "用户取消");
		statusMap.put(STATUS_SYS_CANCEL, "系统取消");
		statusMap.put(STATUS_SYS_CHANGE_CANCEL, "系统换座取消");
		statusMap.put(STATUS_NEW, "新订单");
		statusMap.put(STATUS_NEW_CONFIRM, "等待付款");
		statusMap.put(STATUS_NEW_UNLOCK, "临时订单");
		statusMap.put(STATUS_PAID, "付款成功");
		statusMap.put(STATUS_PAID_SUCCESS, "交易成功");
		statusMap.put(STATUS_PAID_FAILURE, "订单待处理");
		statusMap.put(STATUS_PAID_UNFIX, "座位待处理");
		statusMap.put(STATUS_PAID_RETURN, "退款取消");
		statusMap.put(STATUS_TIMEOUT, "超时取消");
	}
}
