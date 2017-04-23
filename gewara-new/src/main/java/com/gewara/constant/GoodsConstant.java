package com.gewara.constant;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.support.ErrorCode;

public abstract class GoodsConstant {
	public static final String MANAGER_USER = "user";					//管理员
	public static final String MANAGER_MEMBER = "member";				//普通用户
	public static final String MANAGER_ORGANIZATION = "organization";	//组织
	public static final List<String> MANAGER_LIST = Arrays.asList(MANAGER_MEMBER, MANAGER_USER, MANAGER_ORGANIZATION);
	
	public static final String GOODS_TYPE_GOODS = "goods";		
	public static final String GOODS_TYPE_ACTIVITY = "activity";					//活动收费
	public static final String GOODS_TYPE_SPORT = "sport";							//运动畅打培训
	public static final String GOODS_TYPE_TICKET = "ticket";							//通票
	public static final String GOODS_TYPE_TRAINING = "training";					//运动培训课程
	
	//活动收费的TAG
	public static final String GOODS_TAG_POINT = "point"; 							//积分兑换物品
	public static final String GOODS_TAG_GROUPON = "groupon";						//团购
	public static final String GOODS_TAG_BMH = "bmh";								//影院附属商品：爆米花	
	public static final String GOODS_TAG_BMH_SPORT= "bmh_sport";					//运动
	public static final String GOODS_TAG_BMH_THEATRE= "bmh_theatre";				//话剧
	
	
	public static final String DELIVER_ELEC = "elec"; 			//电子券
	public static final String DELIVER_ENTITY= "entity"; 		//实物
	public static final String DELIVER_ADDRESS= "address"; 		//地址
	public static final String GOODS_SHOPPING_COUNT = "shoppingcount";	//购买物品人数
	
	public static final String PERIOD_Y = "Y";		//有时间
	public static final String PERIOD_N = "N";		//无时间
	
	public static final String CHECK_GOODS_PRICE = "price";
	public static final String CHECK_GOODS_DISCOUNT = "discount";
	public static final List<String> CHECK_GOODSLIST = Arrays.asList(CHECK_GOODS_PRICE, CHECK_GOODS_DISCOUNT);
	
	
	public static final String FEETYPE_O = "O"; //第三方卖品（我们卖收服务费）
	public static final String FEETYPE_G = "G";	//Gewara卖品（我们自己货物）
	public static final String FEETYPE_P = "P";	//代售平台（别人卖收佣金）
	public static final String FEETYPE_C = "C";	//预售(物品卖券方式实现)
	public static final String FEETYPE_T = "T";	//指定服务平台
	
	public static final Map<String, String> feetypeMap;
	static{
		Map<String, String> tmp = new LinkedHashMap<String, String>();
		tmp.put(FEETYPE_O, "第三方卖品");
		tmp.put(FEETYPE_G, "Gewara卖品");
		tmp.put(FEETYPE_P, "代售平台");
		tmp.put(FEETYPE_C, "预售");
		tmp.put(FEETYPE_T, "指定服务平台");
		feetypeMap = UnmodifiableMap.decorate(tmp);
	}
	public static ErrorCode<String> getBookingStatusStr(BaseGoods goods){
		if(goods==null){
			return ErrorCode.getFailure("数据不存在！");
		}
		if(StringUtils.equals("status", Status.N)){
			return ErrorCode.getFailure("不接受预定");
		}
		if(goods.getUnitprice()==null){
			return ErrorCode.getFailure("价格错误！");
		}
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(cur.after(goods.getTotime())){
			return ErrorCode.getFailure("已过期");
		}else {
			if(cur.before(goods.getFromtime())){
				return ErrorCode.getFailure("未开始");
			}
		}
		if(goods instanceof ActivityGoods){
			if(goods.getRelatedid()==null){
				return ErrorCode.getFailure("关联错误");
			}
			if(goods.getLimitnum()>=goods.getQuantity()){
				return ErrorCode.getFailure("抢光了");
			}
		}
		return ErrorCode.SUCCESS;
	}
}
