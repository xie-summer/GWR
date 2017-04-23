package com.gewara.helper.discount;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PayConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;

public class DramaSpecialDiscountHelper extends SpecialDiscountHelper{
	private List<OpenDramaItem> itemList;
	private DramaOrder order;
	private List<BuyItem> buyList;
	private List<Discount> discountList;
	private List<SellDramaSeat> seatList;
	public DramaSpecialDiscountHelper(DramaOrder order, List<OpenDramaItem> itemList, List<BuyItem> buyList, List<Discount> discountList, List<SellDramaSeat> seatList){
		this.itemList = itemList;
		this.buyList = buyList;
		this.order = order;
		this.discountList = discountList;
		this.seatList = seatList;
	}
	
	@Override
	public String getFullDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		return getFullDisabledReason(spcounter, cpcounterList, sd, itemList, order);
	}
	private static String getFullDisabledReason(Spcounter spcounter, List<Cpcounter> cpcounterList, SpecialDiscount sd, List<OpenDramaItem> itemList, DramaOrder order) {
		//跳过gewaprice, timefrom, timeto, time1, time2, pricegap, price1, price2检测
		String reason = "";
		if(StringUtils.isNotBlank(sd.getPaymethod())){
			String[] pay = sd.getPaymethod().split(":");
			if(!StringUtils.equals(pay[0], order.getPaymethod())) reason += "支付方式不支持！";
			if(pay.length > 1 && !StringUtils.equals(pay[1], order.getPaybank())){
				reason += "支付网关不支持！";
			}
		}
		if(order.getAddtime().before(sd.getTimefrom()) || order.getAddtime().after(sd.getTimeto()))
			reason += "本活动时间为" + DateUtil.formatTimestamp(sd.getTimefrom()) + "至" + DateUtil.formatTimestamp(sd.getTimeto()) + "！";
		//注意，spcounter不能为空
		String rs = getSpcounterDisabledReason(spcounter, cpcounterList, order.getCitycode(), order.getPartnerid(), order.getQuantity());
		if(StringUtils.isNotBlank(rs)) reason += rs;
		if(order.getQuantity() > sd.getBuynum()|| order.getQuantity() < sd.getMinbuy()){
			if(sd.getBuynum() == sd.getMinbuy()) reason += "本活动单笔订必须购买" + sd.getBuynum() + "张！";
			else reason += "本活动单笔订只能购买" + sd.getMinbuy() + "～" + sd.getBuynum() + "张！";
		}
		reason += getOdiFullDisabledReason(sd, itemList, order.getAddtime());
		Map<String, Object> ruleMap = new HashMap<String, Object>();
		ruleMap.put(SPECIAL_RULE_RELATE_KEY, order.getTheatreid());
		ruleMap.put(SPECIAL_RULE_CATEGORY_KEY, order.getDramaid());
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("ruleObj", ruleMap);
		ErrorCode ruleCode = validSpecialcountRule(sd, ruleMap, true);
		if(!ruleCode.isSuccess()){
			reason += ruleCode.getMsg();
		}
		return reason;
	}
	
	@Override
	public String getOrderFirstDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		Timestamp addtime = order.getAddtime();
		if(addtime.before(sd.getTimefrom()) || addtime.after(sd.getTimeto())){
			return "本活动时间为" + DateUtil.formatTimestamp(sd.getTimefrom()) + "至" + DateUtil.formatTimestamp(sd.getTimeto()) + "！";
		}
		//注意，spcounter不能为空
		String rs = getSpcounterDisabledReason(spcounter, cpcounterList, order.getCitycode(), order.getPartnerid(), order.getQuantity());
		if(StringUtils.isNotBlank(rs)) return rs;
		if(order.getQuantity() > sd.getBuynum()|| order.getQuantity() < sd.getMinbuy()){
			if(sd.getBuynum() == sd.getMinbuy()) return "本活动单笔订必须购买" + sd.getBuynum() + "张！";
			else return "本活动单笔订只能购买" + sd.getMinbuy() + "～" + sd.getBuynum() + "张！";
		}
		return getOdiFirstDisabledReason(sd, itemList, order.getAddtime());
	}
	@Override
	public ErrorCode<List<OpenDramaItem>> isEnabled(SpecialDiscount sd, PayValidHelper pvh){
		if(order.getUnitprice() >sd.getPrice2() || order.getUnitprice() < sd.getPrice1()){
			return ErrorCode.getFailure("卖价范围不支持！");
		}
		return isEnabled(sd, itemList, pvh);
	}

	public static ErrorCode<List<OpenDramaItem>> isEnabled(SpecialDiscount sd, List<OpenDramaItem> itemList, PayValidHelper pvh){
		ErrorCode code = ErrorCodeConstant.DATEERROR;
		List<OpenDramaItem> newItemList = new ArrayList<OpenDramaItem>();
		for (OpenDramaItem item : itemList) {
			code = isEnabled(sd, item, pvh);
			if(code.isSuccess()){
				newItemList.add(item);
			}
		}
		if(newItemList.isEmpty()) return code;
		return ErrorCode.getSuccessReturn(newItemList);
	}
	
	public static ErrorCode<OpenDramaItem> isEnabled(SpecialDiscount sd, OpenDramaItem item, PayValidHelper pvh){
		if (sd == null) return ErrorCode.getFailure("本活动不存在");
		if(!isEnabledByCitycode(sd, item.getCitycode())) return ErrorCode.getFailure("城市不支持！");
		if(!isEnabledByRelatedid(sd, item.getTheatreid())) return ErrorCode.getFailure("场馆不支持！");
		if(!isEnabledByCategoryid(sd, item.getDramaid())) return ErrorCode.getFailure("项目不支持！");
		Map<String, Object> ruleMap = new HashMap<String, Object>();
		ruleMap.put(SPECIAL_RULE_RELATE_KEY, item.getTheatreid());
		ruleMap.put(SPECIAL_RULE_CATEGORY_KEY, item.getDramaid());
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("ruleObj", ruleMap);
		ErrorCode ruleCode = validSpecialcountRule(sd, ruleMap, true);
		if(!ruleCode.isSuccess()){
			return ErrorCode.getFailure(ruleCode.getMsg());
		}
		if(!isEnabledByItemid(sd, item.getDpid())) return ErrorCode.getFailure("场次不支持！");
		if(StringUtils.isNotBlank(sd.getPaymethod()) && 
				StringUtils.isNotBlank(item.getOtherinfo())){
			if(pvh == null) pvh = new PayValidHelper(VmUtils.readJsonToMap(item.getOtherinfo()));
			String[] pay = StringUtils.split(sd.getPaymethod());
			if(!pvh.supportPaymethod(pay[0])) return ErrorCode.getFailure("支付限制！");
		}
		return ErrorCode.SUCCESS;
	}

	private ErrorCode<List<OpenDramaItem>> getSpdiscountOpentype(SpecialDiscount sd, List<OpenDramaItem> odiList){
		List<OpenDramaItem> newItemList = new ArrayList<OpenDramaItem>();
		ErrorCode code = ErrorCodeConstant.DATEERROR;
		for (OpenDramaItem odi : odiList) {
			code = getSpdiscountOpentype(sd, odi);
			if(code.isSuccess()) newItemList.add(odi);
		}
		if(newItemList.isEmpty()) return code;
		return ErrorCode.getSuccessReturn(newItemList);
	}
	
	private ErrorCode getSpdiscountOpentype(SpecialDiscount sd, OpenDramaItem item){
		if (StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_SPECIAL)) {
			if (!StringUtils.contains(item.getSpflag(), sd.getFlag())) {
				return ErrorCode.getFailure("本场次不支持此活动！");
			}
		} else if (!StringUtils.contains(item.getElecard(), PayConstant.CARDTYPE_PARTNER)) {
			return ErrorCode.getFailure("本场次不支持此活动！");
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<Integer> getSpdiscountAmount(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh) {
		if(sd==null) return ErrorCode.getFailure("本活动不存在");
		if(StringUtils.equals(order.getStatus(), OrderConstant.STATUS_NEW_CONFIRM)){
			return ErrorCode.getFailure("已经确认的订单不能修改！");
		}
		if(StringUtils.startsWith(order.getStatus(), OrderConstant.STATUS_PAID)){
			return ErrorCode.getFailure("已经支付的订单不能修改！");
		}
		if(StringUtils.startsWith(order.getStatus(), OrderConstant.STATUS_CANCEL)){
			return ErrorCode.getFailure("已经取消的订单不能修改！");
		}
		return validSpdiscountWithoutStatus(sd, spcounter, cpcounterList, pvh);
	}
	@Override
	public ErrorCode<Integer> validSpdiscountWithoutStatus(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh){
		if(discountList.size() > 0){
			return ErrorCode.getFailure("不能和其他优惠方式共用！");
		}
		ErrorCode<List<OpenDramaItem>> enable = isEnabled(sd, pvh);
		if (!enable.isSuccess()) {
			if(isShowMsg()) return ErrorCode.getFailure(enable.getMsg());
			return ErrorCode.getFailure("场次不支持此活动");
		}
		ErrorCode<List<OpenDramaItem>> openCode = getSpdiscountOpentype(sd, enable.getRetval());
		if(!openCode.isSuccess()) return ErrorCode.getFailure(openCode.getMsg());
		if(seatList!=null){
			List<Integer> priceList = BeanUtil.getBeanPropertyList(seatList, Integer.class, "price", true);
			if(priceList.size()>1) return ErrorCode.getFailure("本场次不支持不同座位价格的优惠！");
		}
		List<OpenDramaItem> baseItemList = openCode.getRetval();
		Map<Long, OpenDramaItem> odiMap = BeanUtil.beanListToMap(baseItemList, "dpid");
		String disableReason = getOrderFirstDisabledReason(sd, spcounter, cpcounterList);
		if(StringUtils.isNotBlank(disableReason)) return ErrorCode.getFailure(disableReason);
		int amount = 0;
		if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERORDER)){
			amount = sd.getDiscount();
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERTICKET)){
			int quantity = 0;
			for (BuyItem item : buyList){
				OpenDramaItem odi = odiMap.get(item.getRelatedid());
				if(odi == null) continue;
				if(item.getDisid() != null){
					Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
					String disquantity = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY);
					if(StringUtils.isBlank(disquantity)) continue;
					quantity += Integer.parseInt(disquantity);
				}else{
					quantity += item.getQuantity();
				}
			}
			amount = sd.getDiscount() * quantity;
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_FIXPRICE)){
			Collection<OpenDramaItem> otherList = CollectionUtils.disjunction(itemList, baseItemList);
			Map<Long,OpenDramaItem> otherMap = BeanUtil.beanListToMap(otherList, "dpid");
			int totalfee = 0;
			for (BuyItem item : buyList) {
				OpenDramaItem odi = otherMap.get(item.getRelatedid());
				if(odi == null) continue;
				totalfee += item.getDue();
			}
			int quantity = 0;
			for (BuyItem item : buyList){
				OpenDramaItem odi = odiMap.get(item.getRelatedid());
				if(odi == null) continue;
				if(item.getDisid() != null){
					Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
					String disquantity = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY);
					if(StringUtils.isBlank(disquantity)) continue;
					quantity += Integer.parseInt(disquantity);
				}else{
					quantity += item.getQuantity();
				}
			}
			amount = order.getTotalfee() - totalfee - sd.getDiscount() * quantity;
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERCENT)){
			int totalfee = 0;
			for (BuyItem item : buyList){
				OpenDramaItem odi = odiMap.get(item.getRelatedid());
				if(odi == null) continue;
				totalfee += item.getDue();
			}
			amount = totalfee*sd.getDiscount() /100;
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_BUYONE_GIVEONE)){
			for (BuyItem item : buyList){
				OpenDramaItem odi = odiMap.get(item.getRelatedid());
				//场次不存在优惠
				if(odi == null || item.getDisid() != null) continue;
				int quantity = item.getQuantity()/2;
				int unitprice = item.getUnitprice();
				if(quantity > 0){
					amount = unitprice * quantity;
				}
			}
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_EXPRESSION)){
			ScriptResult<Number> result = compute(buyList, sd.getExpression(), true);
			if(result.hasError()){
				return ErrorCode.getFailure("优惠计算错误！");
			}
			amount = result.getRetval().intValue();
		}
		if(amount == 0 && sd.getRebates()==0) return ErrorCode.getFailure("此订单无优惠！");
		if(amount > order.getTotalfee()) amount = order.getTotalfee();
		return ErrorCode.getSuccessReturn(amount);
	}
	
	private static ScriptResult<Number> compute(List<BuyItem> itemList, String expression, boolean cache){
		CachedScript script = ScriptEngineUtil.buildCachedScript(expression, cache);
		Map<String, Object> context = new HashMap<String, Object>();
		List<Map> buyList = new ArrayList<Map>();
		for (BuyItem item : itemList) {
			Map beanMap = BeanUtil.getBeanMapWithKey(item, "unitprice", "disid", "smallitemid", "quantity");
			Integer disquantity = 0, disprice = 0;
			if(item.getDisid() != null){
				Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
				String quantity = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY);
				String price = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISPRICE);
				if(StringUtils.isNotBlank(quantity)){
					disquantity += Integer.parseInt(quantity);
				}
				if(StringUtils.isNotBlank(price)){
					disprice = Integer.parseInt(price);
				}
			}
			beanMap.put(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY, disquantity);
			beanMap.put(BuyItemConstant.OTHERINFO_KEY_DISPRICE, disprice);
			buyList.add(beanMap);
		}
		context.put("itemList", buyList);
		ScriptResult<Number> result = script.run(context);
		return result;
	}
	
	public static String getOdiFirstDisabledReason(SpecialDiscount sd, List<OpenDramaItem> itemList, Timestamp addtime){
		String tmpStr = "";
		for (OpenDramaItem item : itemList) {
			tmpStr = getOdiFirstDisabledReason(sd, item, addtime);
		}
		return tmpStr;
	}
	
	public static String getOdiFirstDisabledReason(SpecialDiscount sd, OpenDramaItem item, Timestamp addtime) {
		if(!isEnabledByCategoryid(sd, item.getDramaid())) return "本活动不支持该话剧！";
		if(!isEnabledByAddweek(sd, addtime)) return "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByWeektype(sd, item.getPlaytime())) return "本活动仅限周" + sd.getWeektype() + "的场次！";
		
		if(!isEnabledByTime(sd, item.getPlaytime())){
			return "本活动仅限" + sd.getTime1().substring(0,2) + ":" + sd.getTime1().substring(2) +"～" + 
				sd.getTime2().substring(0,2) + ":" + sd.getTime2().substring(2) + "的场次！";
		}
		if(!isEnabledByAddtime(sd, addtime)){
			return "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		return "";
	}
	
	private static String getOdiFullDisabledReason(SpecialDiscount sd, List<OpenDramaItem> itemList, Timestamp addtime){
		String tmpStr = "";
		for (OpenDramaItem item : itemList) {
			tmpStr = getOdiFullDisabledReason(sd, item, addtime);
		}
		return tmpStr;
	}
	
	private static String getOdiFullDisabledReason(SpecialDiscount sd, OpenDramaItem item, Timestamp addtime) {
		String reason = "";
		if(!isEnabledByCategoryid(sd, item.getDramaid())) reason += "本活动不支持该话剧！";
		if(!isEnabledByAddweek(sd, addtime)) reason += "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByWeektype(sd, item.getPlaytime())) reason += "本活动仅限周" + sd.getWeektype() + "的场次！";
		if(!isEnabledByTime(sd, item.getPlaytime())){
			reason += "本活动仅限" + sd.getTime1().substring(0,2) + ":" + sd.getTime1().substring(2) +"～" + 
				sd.getTime2().substring(0,2) + ":" + sd.getTime2().substring(2) + "的场次！";
		}
		if(!isEnabledByAddtime(sd, addtime)){
			reason += "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		return reason;
	}
}
