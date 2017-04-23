package com.gewara.constant;



public abstract class Flag {
	public static final String POINT_ADDED = "point";	//已审核且加分
	public static final String POINT_IGNORE = "point2"; //已审核不加分TODO: 有问题
	
	public static final String TOP1 = "top1";// 总论坛置顶
	public static final String TOP2 = "top2";// 分论坛置顶
	public static final String RECOMMEND = "recommend";// 推荐
	public static final String HOT = "hot";// 精华
	public static final String TICKET = "ticket";//购票用户
	
	
	public static final String FLAG_MEMBER = "member";
	public static final String FLAG_ADMIN = "admin";
	public static final String FLAG_API = "api";
	public static final String FLAG_USER = "api";
	public static final String FLAG_USERFILES = "userfiles";

	/***
	 *  图片上传模块 - TAG
	 * */
	public static final String FLAG_MICRO = "micro";
	public static final String FLAG_COMMUBG = "commubg";
	public static final String FLAG_HEAD = "head"; //套头信息

	
	/**
	 *  电影版块 - 非重要信息(停车场/游乐场/刷卡..etc.)
	 * 
	 */
	public static final String SERVICE_PARK = "park";					// 停车场
	public static final String SERVICE_OTHER_PARK = "otherPark";		// 周边停车场
	public static final String SERVICE_EARLY_END_MPI = "earlyOrEndMpi";	//早晚场散场通道
	public static final String SERVICE_VISACARD = "visacard";			// 刷卡
	public static final String SERVICE_PLAYGROUND = "playground";		// 游乐场
	public static final String SERVICE_3D = "3D";						// 3D
	public static final String SERVICE_SALE = "sale";					// 卖品
	public static final String SERVICE_FOOD = "food";					// 餐饮
	public static final String SERVICE_RESTREGION = "restregion";	// 休息区
	public static final String SERVICE_PAIRSEAT = "pairseat";		// 情侣座
	public static final String SERVICE_RECREATION = "recreation";		// 娱乐
	public static final String SERVICE_SHOPPING = "shopping";		// 购物
	public static final String SERVICE_SHOPPING_TIME = "shoppingTime";		// 商场营业时间
	public static final String SERVICE_CHARACTERISTIC = "characteristic";	//特色影厅
	public static final String SERVICE_IMAX = "imax";					//	IMAX
	public static final String SERVICE_CHILD = "child";				//儿童票优惠
	public static final String SERVICE_POPCORN = "popcorn";			//套餐
	public static final String SERVICE_WEBCOMMENT = "webcomment";	//网友点评
	public static final String SERVICE_CLOSESALEMSG = "closesalemsg";	//停止售票信息
	public static final String SERVICE_MEMBERCARD = "membercard";	//会员卡
	
	public static final String SERVICE_LINESEAT = "lineseat";		//在线选座
	public static final String SERVICE_EXPRESS = "express";			//支持快递
	public static final String SERVICE_ETICKET = "eticket";			//支持电子票
	public static final String SERVICE_POINTPAY = "pointpay";		//支持积分
	public static final String SERVICE_CARDPAY = "cardpay";			//支持票券抵值
	public static final String SERVICE_TICKETDESC = "ticketdesc";	//购票说明
	
	public static final String SERVICE_PARK_RECOMMEND = "parkRecommend";					// 停车场推荐
	public static final String SERVICE_PARK_RECOMMEND_REMARK = "parkRecommendRemark";		// 停车场推荐其它停车位
	public static final String SERVICE_VISACARD_RECOMMEND = "visacardRecommend";		// 刷卡推荐
	public static final String SERVICE_3D_RECOMMEND = "3DRecommend";						// 3D推荐
	public static final String SERVICE_SALE_RECOMMEND = "saleRecommend";					// 卖品推荐
	public static final String SERVICE_FOOD_RECOMMEND = "foodRecommend";					// 餐饮推荐
	public static final String SERVICE_RESTREGION_RECOMMEND = "restregionRecommend";	// 休息区推荐
	public static final String SERVICE_PAIRSEAT_RECOMMEND = "pairseatRecommend";		// 情侣座推荐
	public static final String SERVICE_RECREATION_RECOMMEND = "recreationRecommend";		// 娱乐
	public static final String SERVICE_SHOPPING_RECOMMEND = "shoppingRecommend";		// 购物
	public static final String SERVICE_SHOPPING_TIME_RECOMMEND = "shoppingTimeRecommend";		// 商场营业时间
	public static final String SERVICE_CHARACTERISTIC_RECOMMEND = "characteristicRecommend";	//特色影厅推荐
	public static final String SERVICE_IMAX_RECOMMEND = "imaxRecommend";					//	IMAX推荐
	public static final String SERVICE_CHILD_RECOMMEND = "childRecommend";				//儿童票优惠推荐
	public static final String SERVICE_COMMENTID = "commentID";								//影院测评
	//public static final String SERVICE_JOINT_CINEMA = "jointCinema";						//所属院线
	
	public static final String SERVICE_CUPBOARD_RECOMMEND = "cupboardRecommend";		//柜子租凭推荐
	public static final String SERVICE_BATHE_RECOMMEND = "batheRecommend";				//洗澡区推荐
	public static final String SERVICE_MEAL_RECOMMENDL = "mealRecommend";						//套餐推荐
	public static final String SERVICE_TRAIN_RECOMMENDL = "trainRecommend";					//专业培训推荐
	public static final String SERVICE_LEASE_RECOMMENDL ="leaseRecommend";					//器材租借推荐
	public static final String SERVICE_MAINTAIN_RECOMMENDL ="maintainRecommend";			//器材维护推荐
	public static final String SERVICE_MEMBERCARD_RECOMMENDL = "membercardRecommend";	//会员卡
	
	public static final String SERVICE_CUPBOARD = "cupboard";		//柜子租凭
	public static final String SERVICE_BATHE = "bathe";				//洗澡区
	public static final String SERVICE_INDOOR = "indoor";			//室内
	public static final String SERVICE_OUTDOOR = "outdoor";			//室外
	public static final String SERVICE_SITECOUNT = "sitecount";		//场地数量
	public static final String SERVICE_TRAIN="train";				//专业培训
	public static final String SERVICE_MEAL="meal";					//套餐
	public static final String SERVICE_HEIGHTVENUE ="heightvenue";	//场馆高度
	public static final String SERVICE_FLOORING ="flooring";		//地板材料
	public static final String SERVICE_LEASE ="lease";				//器材租借	
	public static final String SERVICE_MAINTAIN ="maintain";		//器材维护
	public static final String SERVICE_OPENINFO ="openinfo";		//GYM开放情况
	public static final String SERVICE_SEOTITLE ="seotitle";		//GYM seotitle
	public static final String SERVICE_SEODESCRIPTION ="seodescription";			//GYM seodesc
	public static final String SERVICE_EXPLOSIVE = "explosive";		//爆发力指数
	public static final String SERVICE_CALORIE = "calorie";			//卡路里指数
	public static final String SERVICE_ENDURANCE = "endurance";		//耐力指数
	public static final String SERVICE_RATIO = "ratio";				//男女比例
	
	public static final String SERVICE_QUALIFICATIONS = "qualifications";//教练资质
	public static final String SERVICE_UID = "uid";		//教练身份
	
	//健身项目
	public static final String APPLE_PEOPLE= "applypeople";				//适用人群
	public static final String CONSUMPTION_LEVEL= "consumptionlevel";				//消费水平
	public static final String DIFFICULT_EASY= "difficulteasy";				//难易程度
	public static final String ESSENTIALEQUIPMENT= "essentialequipment";				//装备
	
	
	public static final String FLAG_HISTORY = "history";
}
