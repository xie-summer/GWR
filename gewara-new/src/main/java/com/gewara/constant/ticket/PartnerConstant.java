package com.gewara.constant.ticket;

import java.util.List;

public class PartnerConstant {
	public static final long GEWA_SELF = 1L;					//Gewara本身
	public static final Long GEWAP = 50000010L;					//Gewara Wap版
	
	public static final Long IPHONE = 50000070L;				//iphone手机客户端
	public static final Long ANDROID = 50000020L;				//android手机客户端
	
	public static final Long GEWA_SPORT_IPHONE = 50000030L;		//iphone运动手机客户端
	public static final Long GEWA_SPORT_ANDROID = 50000035L;	//android运动手机客户端
	
	public static final Long GEWA_DRAMA_ANDROID = 50000080L;	//android演出手机客户端
	public static final Long GEWA_DRAMA_IPHONE = 50000081L;		//iphone演出手机客户端
	public static final Long GEWA_DRAMA_ADMIN_MOBILE = 50000082L;	//演出后台电话下单
	public static final Long GEWA_DRAMA_ADMIN_OFFLINE = 50000083L;	//演出后台线下支付下单
	
	public static final Long GEWA_HTC = 50000091L;				//HTC 和格瓦拉用户对应openmember
	public static final Long CMCC_ANDROID = 50000099L;			//移动定制版android
	public static final Long GEWA_CLIENT = 50000100L;			//小于等于此值的都是Gewara自己终端用户
	
	
	public static final Long MAX_MEMBERID = 50000000L;			//小于此值的是MemberId
	public static final Long PARTNER_CHANGTU = 50000040L;		//畅途
	public static final Long PARTNER_MACBUY = 50000161L;		//线下机器购票
	public static final Long PARTNER_CUS = 50000240L;			//CUS
	public static final Long PARTNER_SHOKW = 50000130L;			//联华
	public static final Long PARTNER_SPSDO = 50000145L;			//盛大商城
	public static final Long PARTNER_ONLINE = 50000150L;		//上海热线
	public static final Long PARTNER_UNION = 50000160L;			//银联便民
	public static final Long PARTNER_ANXIN_TERM = 50000170L;	//安欣终端
	public static final Long PARTNER_ANXIN_WEB = 50000171L;		//安欣WEB
	public static final Long PARTNER_SRCB = 50000547L;			//上海农商银行
	public static final Long PARTNER_WOGO = 50000575L;			//联通沃购商城
	public static final Long PARTNER_ZJCMWIFI = 50000302L;		//浙江无线城市
	public static final Long PARTNER_POINTPARK = 50000670L;		//积分乐园
	public static final Long PARTNER_JIFUTONG = 50000630L;		//集付通
	public static final Long PARTNER_QIEKE = 50000680L;			//切客
	public static final Long PARTNER_TAOBAO = 50000700L;		//淘宝
	public static final Long PARTNER_IPTV = 50000750L;			//iptv
	public static final Long PARTNER_SAND = 50000800L;			//杉德金卡通
	public static final Long PARTNER_JU_XIANG = 50000501L;		//巨象终端机
	public static final Long PARTNER_12580 = 50000890L;			//12580
	public static final Long PARTNER_SHOP10086 = 50000703L;		//沪动商城
	public static final Long PARTNER_FILMSH = 50000899L;		//联合院线
	public static final Long PARTNER_BESTV = 50000460L;
	public static final Long PARTNER_BOX_PAY = 50000420L;		//盒子支付      格瓦拉用户对应openmember
	public static final Long PARTNER_PUFABANK = 50000891L;		//浦发银行
	public static final Long PARTNER_962288 = 50000880L;		//上海对外服务热线
	public static final Long PARTNER_MOBILETICKET = 50000885L; 	//移动票务
	public static final Long PARTNER_VERYCD = 50000900L; 		//verycd
	public static final Long PARTNER_SXFILM = 50000901L; 		//绍兴电影网
	public static final Long PARTNER_ZHOUKANG = 50000994L; 		//周康网
	public static final Long PARTNER_TAIZHOU = 50000995L; 		//台州网
	public static final Long PARTNER_JIAXINGREN = 50000996L; 	//嘉兴人网
	public static final Long PARTNER_SUN0575 = 50000997L; 		//绍兴阳光网
	public static final Long PARTNER_TTHONGHUO_CZ = 50000991L; 	//天天红火网 常州社区
	public static final Long PARTNER_TTHONGHUO_NJ = 50000992L; 	//天天红火网 南京社区
	public static final Long PARTNER_TTHONGHUO_SZ = 50000993L; 	//天天红火网 苏州社区
	public static final Long PARTNER_TTHONGHUO_NT = 50000884L; 	//天天红火网 南通社区
	public static final Long PARTNER_SXOL = 50000886L; 			//绍兴在线
	public static final Long PARTNER_SRCBSHOP = 50000770L; 		//上海农商银行网上商城
	public static final Long PARTNER_CE9 = 50000771L; 			//神州运通
	public static final Long PARTNER_UNIONPAY = 50000772L; 		//中国银联--支付方式银联的
	
	public static final Long PARTNER_BAIDU=50000778L;			//百度
	public static final Long PARTNER_HANGZHOUAPP=50000986L;		//杭州银行APP
	public static final Long PARTNER_IMAX=50000987L;			//imax
	
	public static final Long PARTNER_LEWA=50000701L;			//乐蛙科技
	public static final Long PARTNER_ALIBABAYUN=50000702L;		//阿里巴巴云系统
	public static final Long PARTNER_91MOBILE=50000705L;		//91手机android
	public static final Long PARTNER_91MOBILE_IOS=50000706L;	//91手机IOS
	
	//停止合作
	//public static final Long PARTNER_MOBILETICKET_WAP = 50000521L; //移动票务,
	//public static final Long PARTNER_CMWIFI = 50000301L;		//无线城市
	//public static final Long PARTNER_10086 = 50000690L;			//10086
	//public static final Long PARTNER_CMPAY = 50000011L;		//手机支付(cmPay短信支付)
	//public static final Long GEWA_BAR_IPHONE = 50000045L;		//格瓦拉酒吧iphone
	//public static final Long GEWA_BAR_ANDROID = 50000046L;	//格瓦拉酒吧android
	//public static final Long PARTNER_ZS = 50000774L;			//掌苏wap
	//public static final Long PARTNER_WX_CMWIFI = 50000910L; 	//无锡移动城市
	//public static final Long PARTNER_NJ_CMWIFI = 50000911L; 	//南京移动城市
	//public static final Long PARTNER_JS_CMWIFI = 50000982L; 	//江苏移动城市
	//public static final Long PARTNER_YOUHUILA = 50000580L;	//优惠啦
	//public static final Long PARTNER_PPTV = 50000590L;		//PPTV
	//public static final Long PARTNER_MTIME = 50000600L;		//时光网
	//public static final Long PARTNER_KAIXIN = 50000190L;		//开心
	//public static final Long PARTNER_MTOUCH = 50000400L;		//魔屏
	//public static final Long PARTNER_XKQ = 50000510L;			//新空气
	//public static final Long PARTNER_DOUBAN = 50000120L;		//豆瓣
	//public static final Long PARTNER_QQ = 50000180L;			//腾讯
	//public static final Long PARTNER_ALIPAYJZH = 50000410L;	//支付宝金账户
	//public static final Long PARTNER_SDO = 50000140L;			//盛大
	//public static final Long PARTNER_AVAN = 50000500L;		//丰达
	//public static final Long PARTNER_MAPBAR = 50000710L;		//图吧
	//public static final Long PARTNER_CENGT = 50000740L;		//世纪高通
	//public static final Long PARTNER_BAISHITONG = 50000760L;	//号码百事通
	//public static final Long PARTNER_BKM = 50000300L;			//贝克曼
	//public static final Long PARTNER_ALIPAY2 = 50000520L;		//支付宝
	//public static final Long CMCC_XUEDIZI = 50000090L;		//血滴子定制版
	//public static final Long PARTNER_ALIPAYWX = 50000720L;	//支付宝无线

	//格瓦拉线下机器购票
	public static boolean isMacBuy(Long partnerid){
		return partnerid.equals(PARTNER_MACBUY);
	}
	public static boolean isMobilePartner(List<String> mList, Long partnerid){//wap 和 客户端
		if(partnerid==null) return false;
		return mList.contains(partnerid+"");
	}
}
