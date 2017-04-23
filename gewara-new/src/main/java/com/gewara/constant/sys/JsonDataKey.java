package com.gewara.constant.sys;

public class JsonDataKey {
	public static final String KEY_EDITION_LANGUAGE = "mpielp";	//语言版本价格更改
	public static final String KEY_WEBSITEMSG = "websiteMsg";	//系统群发站内信
	public static final String KEY_MULTY_WEBSITEMSG = "multy_websiteMsg";	//针对部分用户群发站内信
	public static final String KEY_UDF_WEBSITEMSG = "udf_websiteMsg";		//自定义群发站内信
	public static final String KEY_COMMUCOLOR = "commucolor_";	//圈子主题配置_背景
	public static final String KEY_COMMULAYOUT = "commulayout_";//圈子主题配置_框架
	public static final String KEY_SMSREPLY = "smsreply_";		//短信回复
	public static final String KEY_HELPCENTER = "helpcenter";	//帮助中心
	public static final String KEY_SIFFPROXY = "siffproxy";		//电影节代理切换
	public static final String KEY_SMSCHANNEL = "smschannel";	//短信发送渠道
	public static final String KEY_SMSMOVIE = "smsmovie";			//电影短信模板
	public static final String KEY_SMSDRAMA = "smsdrama";			//话剧短信模板
	public static final String KEY_SMSSPORT = "smssport";			//运动短信模板
	public static final String KEY_BATCHBALANCE = "batchbalance";//批次结算
	public static final String KEY_SMSTEMPLATE = "opimsg";
	public static final String KEY_SENDMOVIEMAIL = "sendmoviemail";			//记录每次发送的最大电影订单号
	public static final String KEY_SENDDRAMAMAIL = "senddramamail";			//记录每次发送的最大话剧订单号
	public static final String KEY_UMPAYFEE = "umpayfee";			//话费支付手续费
	public static final String KEY_MARKCOUNT = "markcount"; 		//评分统计
	public static final String KEY_AVGMARKTIMES = "avgMarktimes";   //一月内所有影片平均购票用户评分次数
	public static final String KEY_MARKDATA = "markdata";   		//一个月内单部影片最高总评分次数
	public static final String KEY_REFUNDNOTIFY = "refundnotify";	// 帮助中心TAG
	public static final String KEY_MSGMOVIE = "msgmovie";			//电影站内信
	public static final String KEY_SYNCH_MOVIE = "synch_movie";
	public static final String KEY_SYNCH_MOVIEPLAYITEM = "synch_movieplayitem";
	public static final String KEY_MPI_TANKSGIVINGDAY = "mpi_TanksgivingDay"; 
	public static final String KEY_REFUNDACCOUNT = "refundaccount";	//退款短信站内提配模板
	public static final String KEY_REFUNDBANK = "refundbank";	//退款短信银行提配模板
	public static final String TAG_HELPCENTER = "mainMenu";	// 帮助中心TAG
	public static final String KEY_ELECCARD_DELAY = "elecCardDelayCardNo";//电子票券有偿延期操作记录
	
	public static final String KEY_SYNCH_SPIDER_MOVIEPLAYITEM = "synch_spider_movieplayitem";
	
	public static final String KEY_SYNCH_DRAMAPLAYITEM = "synch_dramaplayitem";
	
	public static final String KEY_MOBILE_APPSOURCE = "mobile_appsouce";
	
	//历史数据清理
	public static final String KEY_HISDATA_TIME_POINT = "pointLastTime";	//积分明细备份数据时间
	public static final String KEY_HISDATA_POINT_IDX = "pointIndex";		//积分明细索引byMemberId
	public static final String KEY_HISDATA_TIME_SMSRECORD = "smsLastTime";	//短信明细备份数据时间
	public static final String KEY_HISDATA_TIME_ORDER = "orderLastTime";	//订单备份数据时间
	
}
