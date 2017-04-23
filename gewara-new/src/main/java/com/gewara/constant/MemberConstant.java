package com.gewara.constant;

import java.util.Arrays;
import java.util.List;

public class MemberConstant {
	// 注册来源
	public static final String REGISTER_EMAIL = "email"; //邮箱注册
	public static final String REGISTER_MOBLIE = "mobile"; // 手机注册
	public static final String REGISTER_CODE = "code"; //动态码
	public static final String REGISTER_APP = "app"; //联名登录
	
	// 首页权限
	public static final String RIGHTS_INDEX_PUBLIC = "index_public";
	public static final String RIGHTS_INDEX_FRIEND = "index_friend";
	public static final String RIGHTS_INDEX_PRIVATE = "index_private";
	// 相册权限
	public static final String RIGHTS_ALBUM_PUBLIC = "album_public";
	public static final String RIGHTS_ALBUM_FRIEND = "album_friend";
	public static final String RIGHTS_ALBUM_PRIVATE = "album_private";
	// 好友权限
	public static final String RIGHTS_FRIEND_PUBLIC = "friend_public";
	public static final String RIGHTS_FRIEND_FRIEND = "friend_friend";
	public static final String RIGHTS_FRIEND_PRIVATE = "friend_private";
	// 圈子权限
	public static final String RIGHTS_COMMU_PUBLIC = "commu_public";
	public static final String RIGHTS_COMMU_FRIEND = "commu_friend";
	public static final String RIGHTS_COMMU_PRIVATE = "commu_private";
	// 帖子权限
	public static final String RIGHTS_TOPIC_PUBLIC = "topic_public";
	public static final String RIGHTS_TOPIC_FRIEND = "topic_friend";
	public static final String RIGHTS_TOPIC_PRIVATE = "topic_private";
	
	// 活动权限
	public static final String RIGHTS_ACTIVITY_PUBLIC = "activity_public";
	public static final String RIGHTS_ACTIVITY_FRIEND = "activity_friend";
	public static final String RIGHTS_ACTIVITY_PRIVATE = "activity_private";
	
	// 知道权限
	public static final String RIGHTS_QA_PUBLIC = "qa_public";
	public static final String RIGHTS_QA_FRIEND = "qa_friend";
	public static final String RIGHTS_QA_PRIVATE = "qa_private";
	
	//生活权限
	public static final String RIGHTS_AGENDA_PUBLIC = "agenda_public";
	public static final String RIGHTS_AGENDA_FRIEND = "agenda_friend";
	public static final String RIGHTS_AGENDA_PRIVATE = "agenda_private";
	
	//新手任务

	public static final String NEWTASK = "newtask";
	//现用
	public static final String TASK_CONFIRMREG = "confirmreg";		//注册后邮箱确认
	public static final String TASK_SENDWALA = "sendwala";			//发表一条哇啦
	public static final String TASK_JOINCOMMU = "joincommu";		//加入一个圈子
	public static final String TASK_FIVEFRIEND= "fivefriend";		//已经有5位朋友
	public static final String TASK_UPDATE_HEAD_PIC= "headpic"; 	//更新头像
	public static final String TASK_BINDMOBILE = "bindmobile";		//绑定手机

	public static final String TASK_BUYED_TICKET = "buyticket"; 	//成功购买电影票
	public static final String TASK_MOVIE_COMMENT = "moviecomment";	//完成一个影评
	
	//public static final String TASK_FINISHED = "finished";//完成并领取
	public static final List<String> TASK_LIST = Arrays.asList(
			TASK_UPDATE_HEAD_PIC, TASK_BUYED_TICKET, TASK_MOVIE_COMMENT,
			TASK_BINDMOBILE, TASK_CONFIRMREG, TASK_FIVEFRIEND, TASK_SENDWALA, TASK_JOINCOMMU);
	
	//~~~~~~~~~~~~联名登录~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public static final String TAG_SOURCE = "bindstatus"; //非自动注册用户的otherinfo标识
	public static final String TAG_MOBILE_BINDTIME = "mblbindtime";
	public static final String TAG_EMAIL_BINDTIME = "emlbindtime";
	public static final String TAG_DANGER = "danger";
	public static final String SOURCE_ALIPAY = "alipay";	//支付宝
	public static final String SOURCE_SDO = "sdo";
	public static final String SOURCE_SINA = "sina";
	public static final String SOURCE_QQ ="qq";
	public static final String SOURCE_TENCENT = "tencent"; //腾讯QQ
	public static final String SOURCE_RENREN="renren";
	public static final String SOURCE_NETEASE="netease";//网易
	public static final String SOURCE_KAIXIN="kaixin";//开心
	public static final String SOURCE_MSN="msn";//msn
	public static final String SOURCE_DOUBAN = "douban"; //豆瓣
	public static final String SOURCE_CHINAPAY = "chinapay"; //银联
	public static final String SOURCE_139EMAIL = "139email"; //139邮箱
	public static final String SOURCE_DYNCODE = "dyncode"; //手机动态码登录
	public static final String SOURCE_TAOBAO = "taobao";
	
	public static final String SOURCE_WEIXIN = "weixin";
	
	
	public static final String SHORT_ALIPAY = "a";
	public static final String SHORT_SDO = "s";
	public static final String SHORT_SINA = "S";
	public static final String SHORT_QQ ="q";
	public static final String SHORT_TENCENT = "t";
	public static final String SHORT_RENREN="r";
	public static final String SHORT_NETEASE="n";//网易
	public static final String SHORT_KAIXIN="k";//开心
	public static final String SHORT_MSN= "m";//msn
	public static final String SHORT_DOUBAN = "d";
	public static final String SHORT_CHINAPAY = "c";
	public static final String SHORT_139EMAIL = "e";
	
	public static final String CATEGORY_ALIKUAIJIE = "alikuaijie";
	public static final String CATEGORY_ALIWALLET = "aliwallet";
	
	//~~~~~~~~~~~~~~~~~~~~用户行为记录~~~~~~~~~~~~~~~
	//public static final String ACTION_RELEASECARD = "releaseCard";//解绑卡
	public static final String ACTION_REGCARD = "regcard";		//绑定卡
	public static final String ACTION_MODPWD = "modpwd";		//修改密码
	public static final String ACTION_NEWTASK = "newtask";		//完成新手任务
	public static final String ACTION_SETPAYPWD = "setpaypwd";	//设置支付密码
	public static final String ACTION_GETPAYPWD = "getpaypwd";	//找回支付密码
	public static final String ACTION_MDYPAYPWD = "mdypaypwd";	//修改支付密码
	public static final String ACTION_MODEMAIL = "modemail";	//修改邮箱
	public static final String ACTION_BINDMOBILE = "bindmobile";//绑定手机
	public static final String ACTION_VDDRAWMOBILE = "validdrawmobile";//验证抽奖手机
	public static final String ACTION_CHGBINDMOBILE = "chgbindmobile";//修改绑定手机
	public static final String ACTION_RELIEVEMOBILE = "relievemobile";//解绑手机
	public static final String ACTION_TOWABI = "towabi";		//账户金额转wabi
	public static final String ACTION_DROPMESS = "drpmessage";		//删除私信
	/*
	public static final String ACTION_LOGIN = "login";
	
	public static final String ACTION_ORDER = "changeorder";*/

	//public static final String 
	
	public static final String OM_MOBILE = "mobile";			//第三方手机号
	public static final String OPENMEMBER = "openMember";		//记录是第三方用户登陆
	public static final String OMSOURCE = "omsource";			//第三方登录用户来源
	
	public static final String ALIWALLET_SHORTTOKEN = "aliwallet_shorttoken";	//支付宝钱包短token
	public static final String ALIWALLET_LONGTOKEN = "aliwallet_longtoken";		//支付宝钱包短token
	public static final String ALIWALLET_EXPIRESIN = "aliwallet_expiresIn";		//支付宝钱包短token
	public static final String ALIWALLET_REEXPIRESIN = "aliwallet_reExpiresIn";		//支付宝钱包短token
	public static final String ALIWALLET_SHORTVALIDTIME = "aliwallet_shortvalidtime";	//支付宝钱包短token
	public static final String ALIWALLET_LONGVALIDTIME = "aliwallet_longvalidtime";
	public static final String ALIWALLET_EXTERN_TOKEN = "extern_token";
	
	public static final String BINDMOBILE_STATUS_Y = "Y";	//绑定
	public static final String BINDMOBILE_STATUS_N = "N";	//未绑定
	public static final String BINDMOBILE_STATUS_YS = "Y_S";	//手机能通话验证过
	public static final String BINDMOBILE_STATUS_X = "X";	//未知
}
