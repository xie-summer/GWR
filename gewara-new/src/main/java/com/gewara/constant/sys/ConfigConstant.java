package com.gewara.constant.sys;


public class ConfigConstant {
	//2,15,17,31,150
	public static final Long CFG_CHINAPAY_FTPFILE = 5L;		//ChinaPay对账文件
	public static final Long CFG_SPECIAL_CHAR = 6L;			//新闻特殊字符
	
	public static final Long CFG_SMSCHANNEL = 19L;			//短信发送通道
	
	public static final Long CFG_COMMENTMSG = 25L;			//发影评短信
	public static final Long CFG_DRAMAMSG = 26L;			//发话剧影评短信
	public static final Long CFG_SPORTMSG = 27L;			//发场馆评论短信
	public static final Long CFG_PAUSE_TICKET = 60L;	 	//暂停订票
	public static final Long CFG_YUNYIN_MOBILE = 62L; 		//运营人员接收手机
	public static final Long CFG_SHARECITY = 70L;			//分享城市(论坛)
	public static final Long CFG_PASSCITY = 186L;			//分享城市(论坛)
	public static final Long CFG_CUS = 80L;					//运动场馆预定地址
	public static final Long CFG_PAUSE_SPORT = 81L;			//运动暂停订票
	public static final Long CFG_PAUSE_DRAMA = 83L;			//演出暂停订票
	public static final Long CFG_PAUSE_MEMBERCARD = 84L;	//会员卡暂停订票
	public static final Long CFG_TICKET_QUEUE = 90L;		//订票排队系统配置
	public static final Long CFG_TICKET_QUEUE_CINEMA = 91L; //订票排队系统影院配置
	public static final Long CFG_PAGECACHE_VERSION = 95L;	//页面缓存版本
	public static final Long CFG_HISTORY_UPDATE = 99L;		//新旧表数据的更新时间戳,Point_hist, diary_hist
	public static final Long CFG_CHECKTICKET = 129L;		//小取票机验证接口地址
	public static final Long CFG_MOVIEMSGACTION = 45L;		//发送电影站内信
	public static final Long CFG_POINTSTATS_UPDATE = 98L;	//积分统计列表最后更新时间
	public static final Long CFG_MOBILE_APIPATH = 78L;		//手机API加速url前缀：http://api.gewara.com
	//public static final Long CFG_TICKET_GOODS_MSG = 96L;	//影院卖品随影片订单一起发送短信的影院。
	
	public static final String KEY_FIXEDKEYWORDS = "fixedKeywords";		//过滤关键字ID
	public static final String KEY_MANUKEYWORDS = "manualKeywords";		//手工添加的过滤关键字
	public static final String KEY_MEMBERKEYWORDS = "memberKeywords";	//用户注册过滤关键字

	public static final Long CFG_PAYMETHOD = 200L;				//支付方式配置【配置后，支付数据来源支付项目】
	public static final Long CFG_PAYMETHODTEST = 201L;			//供74测试
	public static final Long CFG_CHARGE = 202L;					//充值
	public static final Long CFG_PAYLIMIT = 250L;				//支付方式限制【配置后，改支付方式不显示】
	public static final Long CFG_MARKCONSTANT = 300L;			//评分算法用常数
	
	public static final Long CFG_CCBPOSPAY = 320L;				//建设银行pos支付

	public static final Long CFG_FILMFEST_CACHE = 12345L;		//电影节缓存
	
	public static final Long CFG_DRAMA_QUESTION_ID = 401L;		//演出Gewara回答用户ID
	//public static final Long CFG_HFH_ERRORCOUNT = 40L;		//火凤凰订票错误次数暂停控制
	//public static final Long CFG_MTX_ERRORCOUNT = 41L;		//火凤凰订票错误次数暂停控制
	//public static final Long CFG_TICKET = 999L;				//订票系统
	
	public static final Long CFG_MARK_SCALE = 400L;				//评分放大
	
	public static final Long LOGIN_FAIL_LIMIT = 911L;			//登录失败限制
	public static final Long POINT_LIMIT = 912L;				//
	public static final Long CFG_BBS_TIME = 520L;				//BBS夜晚发贴时间
	public static final Long CFG_CUSTOM_PAPER = 876L;			//自定义票纸的默认内容 
	//其他配置
	public static final Long CFG_SUBJECT_DOUBLE_ELEVEN = 1111L;	//双十一特价活动ID
	public static final Long CFG_SUBJECT_BAIFUBAO = 1112L;
	public static final Long CFG_SUBJECT_BAIFUBAO_NUM = 1113L;
	
	public static final int POINT_RATIO = 100;					//积分<--->wabi兑换比例
}
