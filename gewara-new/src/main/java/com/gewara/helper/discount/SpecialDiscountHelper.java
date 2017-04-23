package com.gewara.helper.discount;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

public abstract class SpecialDiscountHelper {
	protected boolean showMsg = false;
	
	public static final String SPECIAL_RULE_CATEGORY_KEY = "categoryid";	//项目ID
	public static final String SPECIAL_RULE_RELATE_KEY = "relatedid";		//场馆ID
	/**
	 * 订单被禁用的原因：订单+场次
	 * @param sd
	 * @return
	 */
	public abstract String getOrderFirstDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList);
	/**
	 * 被禁用的所有原因：订单+支付不兼容+场次
	 * @param sd
	 * @return
	 */
	public abstract String getFullDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList);
	public abstract ErrorCode isEnabled(SpecialDiscount sd, PayValidHelper pvh);
	/**
	 * 获取订单折扣金额
	 * @param sd
	 * @param spcounter
	 * @param cpcounterList
	 * @param pvh
	 * @return
	 */
	public abstract ErrorCode<Integer> getSpdiscountAmount(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh);
	/**
	 * 忽略订单状态，检测订单特价活动金额
	 * @param sd
	 * @param spcounter
	 * @param cpcounterList
	 * @param pvh
	 * @return
	 */
	public abstract ErrorCode<Integer> validSpdiscountWithoutStatus(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh);
	public static String[] getUniqueKey(SpecialDiscount sd, Spcounter spcounter, GewaOrder order) {
		String opkey = "sd" + sd.getId();
		if(spcounter!=null && StringUtils.equals(spcounter.getCtlmember(), "Y")) {
			opkey = "spd" + spcounter.getId();
		}
		if(!StringUtils.equals(sd.getPeriodtype(), SpecialDiscount.DISCOUNT_PERIOD_A)){
			Timestamp createtime = order.getCreatetime();
			if(StringUtils.equals(sd.getPeriodtype(), SpecialDiscount.DISCOUNT_PERIOD_D)){
				opkey = opkey + 'd' + DateUtil.format(createtime, "MMdd");
			}else if(StringUtils.equals(sd.getPeriodtype(), SpecialDiscount.DISCOUNT_PERIOD_W)){
				opkey = opkey + 'w' + getWeekOfYear(createtime);
			}else if(StringUtils.equals(sd.getPeriodtype(), SpecialDiscount.DISCOUNT_PERIOD_DW)){
				int week = getWeekOfYear(createtime);
				int tmp = 0;
				if(week%2!=0){
					tmp = week/2+1;
				}else {
					tmp = week/2;
				}
				opkey = opkey + 'w' + tmp;
			}else if(StringUtils.equals(sd.getPeriodtype(), SpecialDiscount.DISCOUNT_PERIOD_M)){
				opkey = opkey + 'm' + DateUtil.getMonth(DateUtil.getDateFromTimestamp(createtime));
			}
		}
		if(StringUtils.equals(OrderConstant.UNIQUE_BY_MEMBERID, sd.getUniqueby())){
			return new String[]{opkey + order.getMemberid()};
		}else if(StringUtils.equals(OrderConstant.UNIQUE_BY_MOBILE, sd.getUniqueby())){
			return new String[]{opkey + order.getMobile()};
		}else if(StringUtils.equals(OrderConstant.UNIQUE_BY_PARTNERNAME, sd.getUniqueby())){ //合作伙伴用户标识
			return new String[]{opkey + GewaOrderHelper.getPartnerUkey(order)};
		}
		if(order.sureOutPartner()){//商家不用
			return new String[]{opkey + order.getMobile()};	
		}
		return new String[]{opkey + order.getMemberid(), opkey + order.getMobile()};
	}
	private static int getWeekOfYear(Timestamp time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(DateUtil.getDateFromTimestamp(time));
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		return week;
	}
	public static String getSpcounterDisabledReason(Spcounter sc, List<Cpcounter> cpcounterList, String citycode, Long partnerid, int quantity){
		if(StringUtils.equals(sc.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
			if(sc.getBasenum() <= sc.getSellquantity() || sc.getLimitmaxnum() <= sc.getAllquantity()){
				return "本活动名额已满！";
			}
		}else if(sc.getBasenum() <= sc.getSellordernum() || sc.getLimitmaxnum() <= sc.getAllordernum()){
			return "本活动名额已满！";
		}
		if(sc.getAllowaddnum() <= 0){
			return "下单人数过多，您可等15分钟内未支付的订单释放名额！";
		}
		Map<String, List<Cpcounter>> cpcounterMap = BeanUtil.groupBeanList(cpcounterList, "flag");
		String msg = getScCity(sc, cpcounterMap.get(Cpcounter.FLAG_CITYCODE), citycode, quantity);
		if(StringUtils.isNotBlank(msg)) return msg;
		msg = getScPartner(sc, cpcounterMap.get(Cpcounter.FLAG_PARTNER), partnerid, quantity);
		return msg;
	}
	
	public static String getScCity(Spcounter sc, List<Cpcounter> cpcounterList, String citycode, int quantity){
		if(cpcounterList == null || cpcounterList.isEmpty()) return "";
		for(Cpcounter cpcounter: cpcounterList){
			String code = cpcounter.getCpcode();
			if(StringUtils.equals(cpcounter.getFlag(), Cpcounter.FLAG_CITYCODE) && Arrays.asList(StringUtils.split(code, ",")).contains(citycode)){
				//1.支付名额控制
				if(cpcounter.getLimitnum() != null){
					int limitnum = cpcounter.getLimitnum();
					if(StringUtils.equals(sc.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
						int sellquantity = cpcounter.getSellquantity();
						if(sellquantity + quantity > limitnum) return "此城市名额已满！";
					}else{
						int sellorder = cpcounter.getSellorder();
						if(sellorder >= limitnum) return "此城市名额已满！";
					}
				}
				//2.下单名额控制
				int count = cpcounter.getAllownum();
				if(count <= 0) return "此城市下单人数过多，您可等15分钟内未支付的订单释放名额！";
				if(StringUtils.equals(sc.getCtltype(), Spcounter.CTLTYPE_QUANTITY) && count - quantity<=0) return "此城市下单人数过多，您可等15分钟内未支付的订单释放名额！";
				return "";
			}
		}
		return "";
	}
	public static String getScPartner(Spcounter sc, List<Cpcounter> cpcounterList, Long partnerid, int quantity){
		if(cpcounterList == null || cpcounterList.isEmpty()) return "";
		for(Cpcounter cpcounter: cpcounterList){
			String code = cpcounter.getCpcode();
			if(StringUtils.equals(cpcounter.getFlag(),Cpcounter.FLAG_PARTNER) && BeanUtil.getIdList(code, ",").contains(partnerid)){//
				//1.支付名额控制
				if(cpcounter.getLimitnum() != null){
					int limitnum = cpcounter.getLimitnum();
					if(StringUtils.equals(sc.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
						int sellquantity = cpcounter.getSellquantity();
						if(sellquantity + quantity > limitnum) return "此渠道名额已满！";
					}else{
						int sellorder = cpcounter.getSellquantity();
						if(sellorder >= limitnum) return "此渠道名额已满！";
					}
				}
				//2.下单名额控制
				int count = cpcounter.getAllownum();
				if(count <= 0) return "此渠道名额已满！";
				if(StringUtils.equals(sc.getCtltype(), Spcounter.CTLTYPE_QUANTITY) && count - quantity<=0) return "此渠道名额已满！";
				return "";
			}
		}
		return "";
	}
	//城市下单控制
	public static Cpcounter updateCityAddCounter(Spcounter spcounter, List<Cpcounter> cpcounterList, GewaOrder order){
		if(cpcounterList != null){
			for(Cpcounter cpcounter : cpcounterList){
				String code = cpcounter.getCpcode();
				if(StringUtils.equals(cpcounter.getFlag(), Cpcounter.FLAG_CITYCODE) && Arrays.asList(StringUtils.split(code, ",")).contains(order.getCitycode())){//
					int count = cpcounter.getAllownum();
					if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
						count -= order.getQuantity();
					}else{
						count --;
					}
					cpcounter.setAllownum(count);
					return cpcounter;
				}
			}
		}
		return null;
	}
	//商家下单控制
	public static Cpcounter updatePartnerAddCounter(Spcounter spcounter, List<Cpcounter> cpcounterList, GewaOrder order){
		if(cpcounterList != null){
			//城市控制计数
			for(Cpcounter cpcounter: cpcounterList){
				String code = cpcounter.getCpcode();
				if(StringUtils.equals(cpcounter.getFlag(), Cpcounter.FLAG_PARTNER) && BeanUtil.getIdList(code, ",").contains(order.getPartnerid())){//
					int count = cpcounter.getAllownum();
					if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY)){
						count -= order.getQuantity();
					}else{
						count --;
					}
					cpcounter.setAllownum(count);
					return cpcounter;
				}
			}
		}
		return null;
	}
	//城市卖出更新
	public static Cpcounter updateCitySellCounter(List<Cpcounter> cpcounterList, GewaOrder order){
		if(cpcounterList != null){
			for (Cpcounter cpcounter : cpcounterList) {
				String code = cpcounter.getCpcode();
				if(StringUtils.equals(cpcounter.getFlag(), Cpcounter.FLAG_CITYCODE) && Arrays.asList(StringUtils.split(code, ",")).contains(order.getCitycode())){//
					//当期
					cpcounter.setSellorder(cpcounter.getSellorder() + 1);
					cpcounter.setSellquantity(cpcounter.getSellquantity() + order.getQuantity());
					//总数量
					cpcounter.setAllordernum(cpcounter.getAllordernum() + 1);
					cpcounter.setAllquantity(cpcounter.getAllquantity() + order.getQuantity());
					return cpcounter;
				}
			}
		}
		return null;
	}
	//商家卖出更新
	public static Cpcounter updatePartnerSellCounter(List<Cpcounter> cpcounterList,GewaOrder order){
		if(cpcounterList != null){
			for (Cpcounter cpcounter : cpcounterList) {
				String code = cpcounter.getCpcode();
				if(StringUtils.equals(cpcounter.getFlag(), Cpcounter.FLAG_PARTNER) && BeanUtil.getIdList(code, ",").contains(order.getPartnerid())){//
					//当期
					cpcounter.setSellorder(cpcounter.getSellorder() + 1);
					cpcounter.setSellquantity(cpcounter.getSellquantity() + order.getQuantity());
					//总数量
					cpcounter.setAllordernum(cpcounter.getAllordernum() + 1);
					cpcounter.setAllquantity(cpcounter.getAllquantity() + order.getQuantity());

					return cpcounter;
				}
			}
		}
		return null;
	}
	public static String getTimeStr(int minutes) {
		int hour = minutes/60;
		int min = minutes%60;
		int day = 0;
		if(hour > 24){
			day = hour/24;
			hour = hour % 24;
		}
		String result = (day > 0?day+"天":"") + (hour>0? hour+"小时":"") + (min>0?min+"分":"");
		return result;
	}
	
	public boolean isShowMsg() {
		return showMsg;
	}
	public void setShowMsg(boolean showMsg) {
		this.showMsg = showMsg;
	}

	/**
	 * 活动仅限城市可用
	 * @param sd
	 * @param citycode
	 * @return
	 */
	public static final boolean isEnabledByCitycode(SpecialDiscount sd, String citycode){
		if(StringUtils.isNotBlank(sd.getCitycode())){
			if(StringUtils.isNotBlank(sd.getCitycode())){
				if(!StringUtils.equals(AdminCityContant.CITYCODE_ALL, sd.getCitycode()) && 
					!ArrayUtils.contains(StringUtils.split(sd.getCitycode(), ","), citycode)){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 活动仅限场馆可用
	 * @param sd
	 * @param relatedid
	 * @return
	 */
	public static final boolean isEnabledByRelatedid(SpecialDiscount sd, Long relatedid){
		if(StringUtils.isNotBlank(sd.getRelatedid())){
			List<Long> idList = BeanUtil.getIdList(sd.getRelatedid(), ",");
			if(!idList.isEmpty() && !idList.contains(relatedid)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 活动仅限项目可用
	 * @param sd
	 * @param categoryid
	 * @return
	 */
	public static final boolean isEnabledByCategoryid(SpecialDiscount sd, Long categoryid){
		if(StringUtils.isNotBlank(sd.getCategoryid())){
			List<Long> idList = BeanUtil.getIdList(sd.getCategoryid(), ",");
			if(!idList.isEmpty() && !idList.contains(categoryid)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 活动仅限场次可用
	 * @param sd
	 * @param itemid
	 * @return
	 */
	public static final boolean isEnabledByItemid(SpecialDiscount sd, Long itemid){
		if(StringUtils.isNotBlank(sd.getItemid())){
			List<Long> idList = BeanUtil.getIdList(sd.getItemid(), ",");
			if(!idList.isEmpty() && !idList.contains(itemid)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 活动仅限周几下单可用
	 * @param sd
	 * @param addtime
	 * @return
	 */
	public static final <T extends Date> boolean isEnabledByAddweek(SpecialDiscount sd, T addtime){
		if(StringUtils.isNotBlank(sd.getAddweek())){
			String addweek = "" + DateUtil.getWeek(addtime);
			if(!StringUtils.contains(sd.getAddweek(), addweek)) return false;
		}
		return true;
	}
	
	/**
	 * 活动仅限场次周几可用
	 * @param sd
	 * @param playtime
	 * @return
	 */
	public static final <T extends Date> boolean isEnabledByWeektype(SpecialDiscount sd, T playtime){
		if(StringUtils.isNotBlank(sd.getWeektype())){
			String openweek = "" + DateUtil.getWeek(playtime);
			if(!StringUtils.contains(sd.getWeektype(), openweek)) return false;
		}
		return true;
	}
	
	/**
	 * 活动仅限场次时间段可用
	 * @param sd
	 * @param playtime
	 * @return
	 */
	public static final <T extends Date> boolean isEnabledByTime(SpecialDiscount sd, T playtime){
		String open_time = DateUtil.format(playtime, "HHmm");
		if(sd.getTime2().compareTo(open_time)<=0  || sd.getTime1().compareTo(open_time) > 0 ){
			return false;
		}
		return true;
	}
	
	/**
	 * 活动仅限下单时间段可用
	 * @param sd
	 * @param addtime
	 * @return
	 */
	public static final <T extends Date> boolean isEnabledByAddtime(SpecialDiscount sd, T addtime) {
		String add_time = DateUtil.format(addtime, "HHmm");
		if(add_time.compareTo(sd.getAddtime1())< 0 || add_time.compareTo(sd.getAddtime2())>0){
			return false;
		}
		return true;
	}
	
	/**
	 * 活动仅限卖价范围
	 * @param sd
	 * @param price
	 * @return
	 */
	public static final boolean isEnabledByPrice(SpecialDiscount sd, Integer price){
		if(price > sd.getPrice2()  || price < sd.getPrice1()){
			return false;
		}
		return true;
	}
	
	/**
	 * 活动仅限成本价范围
	 * @param sd
	 * @param costprice
	 * @return
	 */
	public static final boolean isEnabledByCostprice(SpecialDiscount sd, Integer costprice){
		if(costprice> sd.getCostprice2() || costprice <= sd.getCostprice1()){
			return false;
		}
		return true;
	}
	
	/**
	 * 活动仅限成本差额可用
	 * @param sd
	 * @param pricegap
	 * @return
	 */
	public static final boolean isEnabledByPricegap(SpecialDiscount sd, Integer pricegap){
		return !(sd.getPricegap() > pricegap);		
	}
	
	/**
	 * 活动是否开始
	 * @param sd
	 * @param curtime
	 * @return
	 */
	public static final boolean isEnabledByFromToTime(SpecialDiscount sd, Timestamp curtime){
		return sd.getTimefrom().getTime()<= curtime.getTime() && curtime.getTime() < sd.getTimeto().getTime();
		//return sd.getTimefrom().before(curtime) && sd.getTimeto().after(curtime);
	}
	
	/**
	 * 特殊规则正则表达示
	 * @param sd
	 * @param simpleRuleMap
	 * @param cache
	 * @return
	 */
	public static final ErrorCode validSpecialcountRule(SpecialDiscount sd, Map<String, Object> context, boolean cache){
		if(StringUtils.isBlank(sd.getSpecialrule()) || CollectionUtils.isEmpty(context)) return ErrorCode.SUCCESS;
		CachedScript script = ScriptEngineUtil.buildCachedScript(sd.getSpecialrule(), cache);
		ScriptResult<String> result = script.run(context);
		if(result.hasError()){
			return ErrorCode.getFailure("优惠计算错误！");
		}
		String retval = result.getRetval();
		if(StringUtils.equals("success", retval)){
			return ErrorCode.SUCCESS;
		}
		return ErrorCode.getFailure(retval);
	}
}
