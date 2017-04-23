package com.gewara.constant;


public abstract class PayConstant {
	
	//PUSH_CONSTANT
	public static final String PUSH_FLAG_NEW = "new";
	public static final String PUSH_FLAG_PAID = "paid";
	public static final String PUSH_FLAG_SUCCESS = "success";
	public static final String PUSH_FLAG_REFUND = "refund";
	
	public static final String KEY_STEP4 = "step4";					//商家合作订票成功页面
	public static final String KEY_IFRAME_URL = "iframeUrl";		//商家合作自适应iframe
	public static final String KEY_KEY = "key";						//合作商家系统接口提供的key
	public static final String KEY_PARTNERID = "partnerid";			//合作商家系统接口提供的账号
	public static final String KEY_URL = "url";						//合作商家系统接口提供的URL
	public static final String KEY_PAYMETHOD = "paymethod";			//商家合作可用的支付方式
	public static final String KEY_ORDERFIXPAY2 = "fixedpay";		//订单是否固定支付方式:Partner中也有设置，很多商家订单一开始支付方式就是固定的
	public static final String KEY_CARDBINDPAY = "cardbindpay";		//卡绑定支付方式
	public static final String KEY_BINDGOODS = "bindgoods";			//订单赠送套餐，高于goodsgift
	public static final String KEY_GOODSGIFT = "goodsgift";			//场次绑定套餐
	public static final String KEY_GOODSNUM = "goodsnum";			//
	public static final String KEY_CHANGECOST = "changecost";		//成本价改变
	public static final String KEY_BIND_TRADENO = "bindtradeno";	//绑定的套餐订单号
	public static final String KEY_OPENDISCOUNT = "openDiscount";	//商家合作页面开放优惠支付活动
	public static final String KEY_USE_SPCODE = "spcode";			//使用电子码特价活动


	public static final String KEY_CASH_ACCOUNT = "cash_account";	//现金+账户余额支付
	//卡类型
	public static final String CARDTYPE_A = "A";//次卡：每次抵用一张票的钱
	public static final String CARDTYPE_B = "B";//补差券：每次一张票最多抵用xx元
	public static final String CARDTYPE_C = "C";//抵值：每次抵用xx元
	public static final String CARDTYPE_D = "D";//抵值：每次抵用xx元，每次只限1张
	public static final String CARDTYPE_E = "E";//充值卡：每张可充值xx元
	//Discount中的
	public static final String CARDTYPE_POINT = "P";	//积分
	public static final String CARDTYPE_PARTNER = "M";	//商家合作merchant
	public static final String CARDTYPE_DEPOSIT = "T";	//保证金
	public static final String CARDTYPE_INNER_MOVIE = "IM";	//电影换票
	public static final String CARDTYPE_INNER_SPORT = "IS";	//运动换票
	
	public static final String DISCOUNT_TAG_ECARD = "ecard";		//兑换券
	public static final String DISCOUNT_TAG_POINT = "point";		//积分支付
	public static final String DISCOUNT_TAG_PARTNER = "partner"; //合作商家
	public static final String DISCOUNT_TAG_DEPOSIT = "deposit";	//保证金
	public static final String DISCOUNT_TAG_INNER = "inner";		//内部优惠
	
	//折扣可使用的版块
	public static final String APPLY_TAG_MOVIE = "movie";
	public static final String APPLY_TAG_DRAMA = "drama";
	public static final String APPLY_TAG_SPORT = "sport";
	public static final String APPLY_TAG_GYM = "gym";
	public static final String APPLY_TAG_GOODS = "goods";
	//各类条件匹配模式：排除、包含
	public static final String MATCH_PATTERN_EXCLUDE = "exclude";
	public static final String MATCH_PATTERN_INCLUDE = "include";
	
	public static final String WAPORG_QIEKE = "qieke";
	public static final String WAPORG_BST = "baishitong";
	public static final String WAPORG_HTC = "htc";
	public static final String WAPORG_BAIDU = "baidu-20120813";
}
