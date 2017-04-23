package com.gewara.constant.sys;

import com.gewara.util.StringUtil;

public abstract class MongoData {
	
	public static final Integer Longtype = 18;
	public static final Integer Integertype = 16;
	public static final Integer Objectidtype  = 7;
	public static final Integer Stringtype = 2;
	public static final Integer Nulltype = 10;
	/**
	 *Mongo 表名(以NS_标识的常量)
	 **/
	//1、聚合数据
	public static final String NS_MEMBER_INFO = "memberInfo";	//用户储存表
	public static final String NS_MEMBERCOUNT = "member.count";					//用户聚合数据
	public static final String NS_LASTORDER = "member.last.order";					//用户最后一次订单数据
	public static final String NS_FIRSTORDER = "member.first.order";					//用户第一次订单数据
	//2、业务数据
	public static final String NS_ACTION_MULTYWSMSG = "websiteMsg.multy"; 		// 表名 针对部分用户群发站内信 Mongo标识
	public static final String NS_RECOMMEND_MEMBER = "recommend.member";	//推荐用户表
	public static final String NS_APPLYBETA_MEMBER = "beta.member";	//用户内测表
	public static final String NS_PROMPT_INFO = "promptinfo";	//提示信息表
	public static final String NS_INDEX_DATASHEET = "index.datasheet";		//电影首页数据统计
	public static final String NS_EXPLAIN="explain";	//后台使用帮助
	public static final String NS_PARTNER_CONTACT = "partner.contact";
	public static final String NS_DIARY = "DIARY_BODY";
	public static final String NS_KEFU_REPLYTEMPLATE = "kefu.replytemplate"; 	// 客服回复模板
	public static final String NS_ACTION_PARTNER  = "partner.action";
	public static final String NS_TICKET_MACHINE_IMAGES = "ticket_machine_images"; // 后台图片
	public static final String NS_TICKET_MACHINE_ERROR = "ticket_machine_error";	//一体机错误
	public static final String NS_EQUIPMENTSTATUS = "EQUIPMENT_STATUS";			//一体机状态监控
	public static final String NS_JOB_NAMESPACE = "gewa.job";
	public static final String NS_PRIMITIVE = "java.primitive";
	public static final String NS_SIGN = "gewa.sign";
	public static final String NS_TREASURE = "gewa.treasure";
	public static final String NS_MEMBER_MOBILE= "member_139email"; //139邮箱
	public static final String NS_INTEGRAL = "integral";
	public static final String NS_MEMBER_TRAINING_INFO = "member_training_info";		//用户培训信息
	public static final String NS_CITY_ROOM_CHARACTERISTIC= "city_room_characteristic"; //各个城市拥有的影厅特色
	
	public static final String NS_UNIONPAY_WALLET_MAPPING= "unionpayWalletMapping";  //银联钱包电子票票券映射
	public static final String NS_UNIONPAY_WALLET_URL= "unionpayWalletSPUrl";  //银联钱包电子票专题动态url变动
	
	public static final String NS_AUTO_SETTER_SEND_EMAIL= "autoSetterEmail";  //结算确认邮件
	public static final String NS_AUTO_SETTER_LIMIT = "autoSetterLimit";	//自动设置器时间限制信息

	//3、专题数据
	public static final String NS_MAINSUBJECT = "subject.main";					// 专题
	public static final String NS_CHINAPAY_ACTIVITY = "chinapay_activity";		//银联活动
	public static final String NS_DISNEY_JOIN= "disney_join";
	public static final String NS_DISNEY_MEMBER = "disney_member";
	public static final String NS_ACTIVITY_SINGLES = "actvity_singles";			//光棍表(mongo版)
	public static final String NS_ACTIVITY_PUBLIC_CINEMA ="public_cinema";		//情人节专题(这次作为公用表)
	
	public static final String NS_YQ = "invite_code";							//邀请

	public static final String NS_GBD_WINNER = "gbd_winner";					//广发信用卡申请用户
	public static final String NS_ABC_CHARGE = "abc.charge";					//农业银行合作后台充值记录，（重要）
	public static final String NS_WCAN_CHARGE = "wcan.charge";					//威能积分兑换瓦币充值记录，（重要）
	public static final String NS_GBD_WINNER_DATE = "gbd_winner_date";			//广发信用卡日期
	public static final String NS_GEWA_CUP_NAMESPACE="gewa.cup";
	public static final String NS_ACTION_NAMESPACE = "gf.partner.action";
	public static final String NS_API_WARNCALLBACK = "api.warncallback";		// 商家无法正常出票时回调
	public static final String NS_PAY_CARDNUMBER = "pay_cardnumber";
	public static final String NS_FLASH_PICTRUE = "flash_pictrue";				//flash图片(mongo表)
	public static final String NS_ACTIVITY_COMMON_PICTRUE = "common_pictrue";	//用户上传图片表
	public static final String NS_ACTIVITY_COMMON_MEMBER = "common_member";		//用户信息表
	public static final String NS_SYSMESSAGEACTION = "sysMessageAction";			//站内信定时发送
	public static final String NS_WINNING_RECEIPT_INFO = "winningReceiptInfo";	//用户中奖
	public static final String NS_POSTCARD_INFO = "postcardInfo";		//明信片信息
	public static final String NS_FILMFEST_FIFTEEN = "film_fifteen";	//第15届电影节计算场次预订数表
	public static final String NS_SINGLEDAY = "single_day";				// 单身节场次
	public static final String NS_BUYTICKET_RANKING = "buy_ticket_ranking";				// 单身节场次
	
	public static final String NS_GFBANK_ORDER = "gfbank.order";			//使用广发银行支付并使用优惠活动的订单
	public static final String NS_GFBANK_USER = "gfbank.user";				//签约广发银行网银的用户[每周广发银行会给出名单]

	public static final String NS_SUBJECT_COUNT = "activity.count"; 		//白色情人节活动人数
	public static final String NS_PRICETIER = "price.tier"; 				//价格类别
	public static final String NS_CITYPRICETIER = "cityprice.tier"; 		//城市所在类别
	public static final String NS_VOUCHERCARD_TYPE = "vouchercard_type"; 	//兑换券种类
	public static final String NS_VOUCHERCARD_ISSUERID = "vouchercard_issuerid";//电子票券发行人配置
	public static final String NS_VOUCHERCARD_CINEMA = "vouchercard_cinema";
	
	public static final String NS_MACHINECONFIG = "machine.machineconfig";	//一体机关机时间配置  add by taiqichao
	public static final String NS_OPI_LOCKNUM = "opilockstat";				//场次锁定座位数量统计
	public static final String NS_CCBPOS_ACTIVITY = "ccbPosActivity";
	public static final String NS_CCBPOS_GZ_ACTIVITY = "ccbPosGZActivity"; //广州建设银行活动
	public static final String NS_CCBPOS_EVERYDAYTWICE= "ccbEveryDayTwice"; //广州建设银行活动
	public static final String NS_CCBPOS_WEEKONE= "ccbWeekOne"; 			//广州建设银行活动--一周一次
	public static final String NS_CCBPOS_CARDBIN = "ccbPosCardbin";
	public static final String NS_CCBPOS_CARDBIN_2013 = "ccbPosCardbin2013"; //建行   新的活动前8后4卡bin
	public static final String NS_CCBPOS_ORDER = "ccbPosOrder";
	public static final String NS_UNIONPAYFAST_CARDBIN = "unionPayFastCardbin";
	
	public static final String NS_COMMON_WHITEDAY = "subject_whiteday";	//浙江省白色情人节专题
	public static final String NS_COMMON_WHITEDAY_DRAWTIMES = "subject_whiteday_drawTimes";	//浙江省白色情人节专题 用户获取的免费抽奖机会
	public static final String NS_COMMON_MOTORSHOW_DRAWTIMES = "subject_motorShow_drawTimes";	//浙江省白色情人节专题 用户获取的免费抽奖机会
	
	public static final String NS_COMMON_VOTE = "common_vote";	//通用投票程序
	public static final String NS_WEIXIN = "weixin";			//微信
	public static final String NS_TIMERIFT = "time_rift";	//盛大时空裂痕特权码
	public static final String NS_BADEGG = "bad_egg";
	public static final String NS_REGEXP = "reg_exp";
	public static final String NS_DRAWDAYCOUNT = "drawcount"; 

	//后台用户密码保护
	public static final String FIELD_REAL_NAME = "realName";				//中奖姓名
	public static final String FIELD_RECEIPT_ADDRESS = "receiptAddress";	//收货地址
	public static final String FIELD_TELEPHONE = "telephone";				//联系电话
	public static final String FIELD_EMAIL = "email";						//电子邮箱
	public static final String FIELD_SEX = "sex";							//性别
	

	//public static final String NS_APPCLIENT_DOWNRECORD = "appclientDownRecord";//客户
	//public static final String NS_PSBCBANK_USER = "psbcbank.user";		//邮政储蓄银行网银的用户2012-07-05到期

	//用户操作聚合数据
	public static final String SYSTEM_ID = "_id";
	public static final String DEFAULT_ID_NAME  = "id";						// 公用字段 ID_NAME
	public static final String ACTION_MEMBERID = "memberid";
	public static final String ACTION_USERID = "userid";					// 公用字段
	public static final String ACTION_MEMBERNAME = "membername";
	public static final String ACTION_BODY = "body";							// 公用字段
	public static final String ACTION_ADDTIME = "addtime";						// 公用字段
	public static final String ACTION_MODIFYTIME = "modifytime";				// 公用字段
	public static final String ACTION_TAG = "tag";								// 公用字段
	public static final String ACTION_RELATEDID = "relatedid";					// 关联ID
	public static final String ACTION_PARENTID = "parentid";					// 关联ID
	public static final String ACTION_ORDERNUM = "ordernum";					// 排序
	public static final String ACTION_TITLE = "title";							// 标题
	public static final String ACTION_SUBJECT = "subject";						// 主题
	public static final String ACTION_TYPE = "type";
	public static final String ACTION_ADDRESS = "address";
	public static final String ACTION_SIGNNAME = "signname";					//光棍节
	public static final String ACTION_SUPPORT="support";						//支持人数
	public static final String ACTION_SUBJECT_TYPE = "subjecttype";				//专题类型
	public static final String ACTION_CONTENT = "content";						//内容
	public static final String ACTION_CONTENT2 = "content2";					//内容2
	public static final String ACTION_PROVINCE="province";
	public static final String ACTION_PROVINCE_NAME="provincename";
	public static final String ACTION_CITYCODE="citycode";
	public static final String ACTION_COUNTYCODE="countycode";
	public static final String ACTION_MULTYWSMSG_MSGID = "msgid";
	public static final String ACTION_MULTYWSMSG_ISREAD = "isread";
	public static final String ACTION_MULTYWSMSG_ISDEL = "isdel";
	public static final String ACTION_PICTRUE_URL = "picurl";					//图片路径
	public static final String ACTION_COUNT="count";
	public static final String ACTION_TO_NAME = "toname";						//收件人
	public static final String ACTION_FROM_NAME = "fromname";					//寄件人
	public static final String ACTION_FROM_ADDRESS = "fromaddress";				//寄件人地址
	public static final String ACTION_TO_POSTCODE = "topostcode";				//收件人邮编
	public static final String ACTION_FROM_POSTCODE = "frompostcode";			//寄件人邮编
	public static final String ACTION_NAME = "name";
	public static final String ACTION_ENDTIME="endtime";
	public static final String ACTION_STARTTIME="starttime";
	public static final String ACTION_CHECK_TIME="checktime"; //审核日期
	
	public static final String ACTION_ATTACH_MOVIE_ID = "attachmovieid";//影评的id
	public static final String ACTION_ATTACH_MOVIE_MOVIEID = "movieid";//电影的ID
	public static final String ACTION_LUCK = "luck";//幸运用户
	public static final String ACTION_STATUS = "status";//状态
	public static final String ACTION_REMARK = "remark";
	public static final String ACTION_TRADENO = "tradeNo";
	public static final String ACTION_ATTACH_MOVIE_DESCRIPTION = "description";//内容
	public static final String ACTION_ATTACH_MOVIE_POINTVALUE = "pointvalue";//加积分值
	public static final String ACTION_LOTTERY_CODE="lotterycode";//抽奖码
	public static final String ACTION_NOTREADCOUNT = "notreadcount";
	public static final String ACTION_OTHERINFO = "otherinfo";
	
	public static final String ACTION_HOME_TAG = "home";
	public static final String ACTION_IDNAME = "acrId";
	public static final String ACTION_VALUENAME = "resultStr";
	
	public static final String ACTION_TAG_SHOWPAY ="showPay";
	public static final String ACTION_ACTIONID = "actionid";
	public static final String ACTION_UKEY = "ukey";
	public static final String ACTION_ACTION= "action";
	
	public static final String ACTION_API_PARTNERNAME = "partnername";
	public static final String ACTION_API_PARTNERKEY = "partnerkey";
	
	public static final String JOB_IDNAME = "jobid";
	public static final String JOB_TITLE = "title";
	public static final String JOB_DESC = "desc";
	
	public static final String SIGNNAME_DAOHANG = "daohang";
	public static final String SIGNNAME_XINZHUAN = "xinzhuan";
	public static final String SIGNNAME_XINWEN = "xinwen";
	public static final String SIGNNAME_XINTU = "xintu";
	public static final String SIGNNAME_TUPIAN = "tupian";
	public static final String SIGNNAME_SHIPIN = "shipin";
	public static final String SIGNNAME_HUDONG = "hudong";
	public static final String SIGNNAME_LUNTAN = "luntan";
	public static final String SIGNNAME_ZHIDAO = "zhidao";
	public static final String SIGNNAME_YINGPIAN = "yingpian";
	public static final String SIGNNAME_HUAJU = "huaju";
	public static final String SIGNNAME_HUODONG = "huodong";
	
	// 统一专题 signname
	public static final String SIGNNAME_XINWEN_01 = "xinwen_01";
	public static final String SIGNNAME_XINWEN_02 = "xinwen_02";
	public static final String SIGNNAME_XINWEN_03 = "xinwen_03";
	public static final String SIGNNAME_XINWEN_04 = "xinwen_04";
	
	public static final String ACTION_TYPE_SUBJECT = "subject";
	public static final String ACTION_TYPE_RECOMMEND = "recommend";
	public static final String ACTION_TYPE_SIMPLETEMPLATE = "simpletemplate"; // 单一影片固定模板
	public static final String ACTION_TYPE_UNIONTEMPLATE = "uniontemplate";	// 统一专题模板
	public static final String ACTION_TYPE_SUBUNIONTEMPLATE = "subuniontemplate";	// 统一专题模板
	public static final String ACTION_TYPE_VIEWINGGROUPTEMPLATE = "viewinggrouptemplate";//统一观影团专题模板
	
	
	public static final String ACTION_BOARD = "board";
	public static final String ACTION_BOARDRELATEDID = "boardrelatedid";
	public static final String ACTION_NEWSTITLE = "newstitle";
	public static final String ACTION_NEWSSUBJECT = "newssubject";
	public static final String ACTION_NEWSLOGO = "newslogo";
	public static final String ACTION_NEWSSMALLLOGO = "newssmalllogo";
	public static final String ACTION_NEWSLINK = "newslink";
	public static final String ACTION_NEWSBOARD = "newsboard";
	public static final String ACTION_NAVIGATION = "navigation";
	public static final String ACTION_WALATITLE = "walatitle";
	public static final String ACTION_JSONINFO = "jsoninfo";		// 存json数据
	public static final String ACTION_SUBJECTTYPE = "subjecttype";	// 专题类型: movie / drama etc..
	public static final String ACTION_LINKCOLOR = "linkcolor";
	public static final String ACTION_SEOKEYWORDS ="seokeywords";			//关键字
	public static final String ACTION_SEODESCRIPTION ="seodescription";		//关键描述
	/******观影团字段*********/
	public static final String ACTION_SP_ACTIVIES = "sp_activies";//报名活动
	public static final String ACTION_VIEWBGCOLOR = "viewbgcolor";//背景颜色
	public static final String ACTION_VIEWREPORT = "viewReport";//观影报告ACTION_ATTACH_MOVIE_MOVIEID
	public static final String ACTION_VOTEID = "voteid";//投票ID
	//观影团明星字段
	public static final String ACTION_STARSEX = "starsex";//明星性别
	public static final String ACTION_CONSTELLATION = "constellation";//明星星座
	public static final String ACTION_BIRTHDAY = "birthday";//生日
	public static final String ACTION_BIRTHPLACE = "birthplace";//出生地
	
	/******盛大时空裂缝特权码模块*********/
	public static final String ACTION_SEQ ="seq";
	public static final String ACTION_PRIVILEGED_CODE = "privilegedcode";
	public static final String ACTION_IS_USED = "isused";
	public static final String ACTION_MEMBER_ID = "memberid";
	public static final String ACTION_USED_TIME = "usedtime";
	
	// 专题子模块编号
	public static final String L_UNIONSUB_XINWEN1 	= "L_xinwen_01";	// 
	public static final String L_UNIONSUB_XINWEN2 	= "L_xinwen_02";
	public static final String L_UNIONSUB_XINWEN3 	= "L_xinwen_03";
	public static final String L_UNIONSUB_SHIPIN 	= "L_shipin";
	public static final String L_UNIONSUB_SHIPIN2	= "L_shipin_02";
	public static final String L_UNIONSUB_JUZHAO 	= "L_juzhao";
	public static final String L_UNIONSUB_JUZHAO2	= "L_juzhao_02";
	public static final String L_UNIONSUB_HUODONG 	= "L_huodong";
	public static final String L_UNIONSUB_WALA 		= "L_wala";
	public static final String L_UNIONSUB_BIANJI	= "L_bianji";
	public static final String L_UNIONSUB_DAOHANG	= "L_daohang";
	public static final String L_UNIONSUB_CHOUJIANG	= "L_choujiang";
	public static final String L_UNIONSUB_MINGXINPIAN  = "L_mingxinpian";
	public static final String L_UNIONSUB_TOUPIAO = "L_toupiao";
	
	public static final String R_UNIONSUB_XINWEN4 	= "R_xinwen_04";
	public static final String R_UNIONSUB_YINGPIAN = "R_yingpian";
	public static final String R_UNIONSUB_HUAJU 	= "R_huaju";
	public static final String R_UNIONSUB_SHIPIN 	= "R_shipin";
	public static final String R_UNIONSUB_JUZHAO2	= "R_juzhao_02";
	public static final String R_UNIONSUB_HUODONG	= "R_huodong";
	public static final String R_UNIONSUB_ZHIDAO 	= "R_zhidao";
	public static final String R_UNIONSUB_LUNTAN 	= "R_luntan";
	public static final String R_UNIONSUB_WALA 		= "R_wala";
	public static final String R_UNIONSUB_BIANJI	= "R_bianji";
	
	public static final String T_UNIONSUB_JUZHAO = "T_juzhao";
	public static final String T_UNIONSUB_JUZHAO2 = "T_juzhao2";
	public static final String T_UNIONSUB_BIANJI	= "T_bianji";
	
	public static final String B_UNIONSUB_JUZHAO = "B_juzhao";
	public static final String B_UNIONSUB_JUZHAO2 = "B_juzhao2";
	public static final String B_UNIONSUB_BIANJI	= "B_bianji";
	
	//对比专题模块
	public static final String L_COMPARE_MOVIE = "L_movie";
	public static final String R_COMPARE_MOVIE = "R_movie";
	public static final String L_COMPARE_NEWS = "L_news";
	public static final String R_COMPARE_NEWS = "R_news";
	
	public static final String SINGLES_FOREIGNID = "singles_foreignid";//外键ID
	public static final String SINGLES_CINEMAURL = "singles_cinemaurl";//链接
	public static final String SINGLE_TIMES = "singles_time"; //时间
	
	public static final String DOUBLE_FESTIVAL_THIRTEEN="DF_thirteen";	//双旦节个活动参数
	public static final String DOUBLE_FESTIVAL_POM="DF_pom";
	public static final String DOUBLE_FESTIVAL_TREE="DF_tree";
	public static final String DOUBLE_FESTIVAL_PARTY="DF_party";
	
	public static final String VALENTINE_SWEET_IMAGE = "sweet_image";	//情人节（甜蜜影像）
	public static final String VALENTINE_SESSION_CINEMA = "session_cinema";	//情人节专场
	public static final String VALENTINE_MOVIE_DAREN = "movie_daren";	//电影达人
	public static final String VALENTINE_ACTIVITY_DAREN = "activity_daren";//活动达人
	public static final String VALENTINE_PHONE_DAREN = "phone_daren";	//手机达人
	public static final String VALENTINE_SCENE_LOVE = "scene_love";		//现场恩爱
	public static final String VALENTINE_HAPPY = "happy";			//晒幸福
	
	public static final String WHITE_DAY = "whiteday";//白色情人节

	public static final String ANNUALSELECTION = "annualSelection";	//专题回顾
	public static final String ANNUALSELECTION_AD = "ad";			//专题回顾广告
	public static final String ANNUALSELECTION_CRITICS = "critics";	//影评
	public static final String ANNUALSELECTION_CITY_OF_LOVE = "cityOfLove";	//都市爱情
	public static final String ANNUALSELECTION_SAME_ART = "sameArt";		//所谓文艺
	public static final String ANNUALSELECTION_IDOL_INVINCIBLE = "idolInvincible";	//偶像无敌
	public static final String ANNUALSELECTION_REJUVENATE = "rejuvenate";			//返老还童
	public static final String ANNUALSELECTION_ANNUAL_JUXIAN= "annualJuxian";		//年度巨献
	
	public static final String ALIENBATTLEFIELD = "alienBattlefield";	//异星战场（迪斯尼）
	public static final String PEPSICOLA = "pepsiCola"; //百事可乐合作专题
	
	public static final String BATTLESHIP = "battleship";		//超级战舰
	
	//public static final String WYWK_ACTIVITY = "wywkActivity";	//网吧活动
	//public static final String WYWK_IP = "wywkIP";	//网吧IP
	
	public static final String AVENGERS_ACTIVITY = "avengersActivity";	//复仇者联盟
	public static final String TAG_AVENGER = "TAG_AVENGER";
	public static final String JAZZ_TYPE = "jazz";
	
	public static final String TITANIC = "titanic";				//泰坦尼克号
	public static final String TITANIC_POSTCARD = "postcard";	//明信片
	public static final String TITANIC_VOTE = "vote";			//投票
	public static final String TITANIC_CHILDACTIVITY = "childActivity"; //分站活动
	public static final String TITANIC_CHILDATYPE ="childType";
	public static final String SITES_HANGZHOU = "hangzhou"; //杭州分站
	
	public static final String GYM_BELLYDANCE = "bellydanceGYM";	//健身肚皮舞
	public static final String SPORT_POOL_PARTY = "poolparty";		//泳池派对
	public static final String DRAMA_REDCAT = "drama_redcat"; //话剧红毯猫
	public static final String DRAMA_MUSICCAT = "drama_musiccat"; //音乐剧-猫
	
	public static final String MESSAGE_FANS_ADD = "add";		//添加fans数
	public static final String MESSAGE_FANS = "fans";
	public static final String MESSAGE_FANS_REMOVE = "remove";	//移除fans数
	
	public static final String IDNAME_YQ = "HT";
	public static final String FIELD_GW = "GW";
	public static final String FIELD_HT = "HT";
	public static final String FIELD_YG = "YG";
	public static final String FIELD_XS = "XS";
	public static final String FIELD_GJ = "GJ";
	public static final String FIELD_5173 = "5173";
	
	public static final String GEWA_CUP_IDNAME="cupid";
	public static final String GEWA_CUP_STATUS="cupstatus";
	public static final String GEWA_CUP_MEMBERID="memberid";
	public static final String GEWA_CUP_ORDERID="orderid";
	
	public static final String GEWA_CUP_YEARS_2013 = "gewaCup2013";		//举办年份
	public static final String GEWA_CUP_YEARS_2012 = "gewaCup2012";		//举办年份
	public static final String GEWA_CUP_ANSWER = "answer";				//举报
	public static final String GEWA_CUP_BOY_SINGLE = "boysingle";		//男单
	public static final String GEWA_CUP_BOY_DOUBLE = "boydouble";		//男双
	public static final String GEWA_CUP_GIRL_SINGLE = "girlsingle";		//女单
	public static final String GEWA_CUP_GIRL_DOUBLE = "girldouble";		//女双
	public static final String GEWA_CUP_MIXED_DOUBLE = "mixeddouble";	//混双
	
	
	
	//首页数据统计
	public static final String INDEX_KEY = "index"; //全站首页key
	public static final String INDEX_TICKET_COUNT = "ticketcount"; //电影票数
	public static final String INDEX_DRAMA_COUNT = "dramacount";	//话剧票数
	public static final String INDEX_SPORT_COUNT = "sportcount";	//运动票数
	public static final String INDEX_POINT_COUNT = "pointcount";	//领积分数
	public static final String INDEX_COMMENT_COUNT = "commentcount";	//哇啦数
	public static final String INDEX_DIARY_COUNT = "diarycount";	//帖子数（影评、剧评、运动心得）
	public static final String INDEX_ALL_DIARY_COUNT = "alldiarycount"; //帖子总数
	public static final String INDEX_JOIN_ACTIVITY_COUNT = "joinactivitycount"; //参加活动人数
	
	public static final String NS_TELE_MOBILE = "teleSms"; //电信短信
	
	public static String buildId(){
		return buildId(2);
	}
	public static String buildId(int num){
		return StringUtil.getRandomString(num) + System.currentTimeMillis();
	}
}
