package com.gewara.helper.discount;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.constant.sys.ErrorCodeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public class GoodsSpecialDiscountHelper extends SpecialDiscountHelper{
	private GoodsOrder order;
	private List<BaseGoods> goodsList;
	private List<BuyItem> itemList;
	public GoodsSpecialDiscountHelper(GoodsOrder order, List<BaseGoods> goodsList, List<BuyItem> itemList){
		this.order = order;
		this.goodsList = goodsList;
		this.itemList = itemList;
	}
	@Override
	public String getOrderFirstDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		//注意，spcounter不能为空
		String rs = getSpcounterDisabledReason(spcounter, cpcounterList, order.getCitycode(), order.getPartnerid(), order.getQuantity());
		if(StringUtils.isNotBlank(rs)) return rs;
		if(order.getQuantity() > sd.getBuynum())
			return "本次活动每次限量" + sd.getBuynum() + "个！";
		if(order.getQuantity() < sd.getMinbuy())
			return "本活动单笔订单最少购买" + sd.getBuynum() + "张！";
		if(StringUtils.isNotBlank(sd.getAddweek())){
			String addweek = "" + DateUtil.getWeek(order.getAddtime());
			if(!StringUtils.contains(sd.getAddweek(), addweek)) return "本活动仅限周" + sd.getAddweek() + "购买！";
		}

		if(order.getAddtime().before(sd.getTimefrom()) || order.getAddtime().after(sd.getTimeto()))
			return "本次活动时间为" + DateUtil.formatTimestamp(sd.getTimefrom()) + "至" + DateUtil.formatTimestamp(sd.getTimeto());
		
		String add_time = DateUtil.format(order.getAddtime(), "HHmm");
		if(add_time.compareTo(sd.getAddtime1())< 0 || add_time.compareTo(sd.getAddtime2())>0){
			return "本活动仅限" + sd.getTime1().substring(0,2) + ":" + sd.getTime1().substring(2) +"～" + 
			sd.getTime2().substring(0,2) + ":" + sd.getTime2().substring(2) + "购买！";
		}
		return "";
	}
	@Override
	public String getFullDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		return getFullDisabledReason(spcounter, cpcounterList, sd, order, goodsList);
	}

	private static String getFullDisabledReason(Spcounter spcounter, List<Cpcounter> cpcounterList, SpecialDiscount sd, GoodsOrder order, List<BaseGoods> goodsList) {
		//跳过gewaprice, timefrom, timeto, time1, time2, pricegap, price1, price2检测
		String reason = "";
		if(StringUtils.isNotBlank(sd.getPaymethod())){
			String[] pay = sd.getPaymethod().split(":");
			if(!StringUtils.equals(pay[0], order.getPaymethod())) reason += "支付方式不支持！";
			if(pay.length > 1 && !StringUtils.equals(pay[1], order.getPaybank())){
				reason += "支付网关不支持！";
			}
		}
		if(!isEnabledByFromToTime(sd, order.getAddtime()))
			reason += "本活动时间为" + DateUtil.formatTimestamp(sd.getTimefrom()) + "至" + DateUtil.formatTimestamp(sd.getTimeto()) + "！";
		//注意，spcounter不能为空
		String rs = getSpcounterDisabledReason(spcounter, cpcounterList, order.getCitycode(), order.getPartnerid(), order.getQuantity());
		if(StringUtils.isNotBlank(rs)) reason += rs;
		if(order.getQuantity() > sd.getBuynum()|| order.getQuantity() < sd.getMinbuy()){
			if(sd.getBuynum() == sd.getMinbuy()) reason += "本活动单笔订必须购买" + sd.getBuynum() + "张！";
			else reason += "本活动单笔订只能购买" + sd.getMinbuy() + "～" + sd.getBuynum() + "张！";
		}
		reason += getGoodsFullDisabledReason(sd, goodsList, order.getAddtime());
		return reason;
	}
	private static String getGoodsFullDisabledReason(SpecialDiscount sd, List<BaseGoods> goodsList, Timestamp addtime){
		String tmpStr = "";
		for (BaseGoods goods : goodsList) {
			tmpStr = getGoodsFullDisabledReason(sd, goods, addtime);
		}
		return tmpStr;
	}
	
	private static String getGoodsFullDisabledReason(SpecialDiscount sd, BaseGoods goods, Timestamp addtime) {
		String reason = "";
		Long itemid = (Long) BeanUtil.get(goods, "itemid");
		Long roomid = (Long) BeanUtil.get(goods, "roomid");
		if(itemid != null  && !isEnabledByCategoryid(sd, itemid)) reason += "本活动不支持该影片！";
		if(!isEnabledByAddweek(sd, addtime))  reason += "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByAddtime(sd, addtime)){
			reason += "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		if(roomid != null && StringUtils.isNotBlank(sd.getFieldid())){
			List<Long> roomidList = BeanUtil.getIdList(sd.getFieldid(), ",");
			if(!roomidList.contains(roomid)){
				reason += "本活动不支持该厅！";
			}
		}
		return reason;
	}
	private ErrorCode<List<BaseGoods>> getSpdiscountOpentype(SpecialDiscount sd, List<BaseGoods> baseGoodsList){
		List<BaseGoods> newGoodsList = new ArrayList<BaseGoods>();
		ErrorCode code = ErrorCodeConstant.DATEERROR;
		for (BaseGoods goods : baseGoodsList) {
			code = getSpdiscountOpentype(sd, goods);
			if(code.isSuccess()) newGoodsList.add(goods);
		}
		if(newGoodsList.isEmpty()) return code;
		return ErrorCode.getSuccessReturn(newGoodsList);
	}
	
	private ErrorCode getSpdiscountOpentype(SpecialDiscount sd, BaseGoods goods){
		if (StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_SPECIAL)) {
			if (!StringUtils.contains(goods.getSpflag(), sd.getFlag())) {
				return ErrorCode.getFailure("本场次不支持此活动！");
			}
		} else if (!StringUtils.contains(goods.getElecard(), PayConstant.CARDTYPE_PARTNER)) {
			return ErrorCode.getFailure("本场次不支持此活动！");
		}
		return ErrorCode.SUCCESS;
	}
	
	
	@Override
	public ErrorCode<Integer> getSpdiscountAmount(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh) {
		if(order.getDiscount() > 0) return ErrorCode.getFailure("不能和其他优惠方式共用！");
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
	public ErrorCode<Integer> validSpdiscountWithoutStatus(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh) {
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		String umpayfee = otherFeeMap.get(OtherFeeDetail.FEETYPE_U);
		if (StringUtils.isNotBlank(umpayfee) && Integer.parseInt(umpayfee)>0) {
			if (!pvh.supportPaymethod(order.getPaymethod()))
				return ErrorCode.getFailure("此活动不支持您选择的支付方式！");
			pvh = new PayValidHelper(order.getPaymethod());
		}
		ErrorCode<List<BaseGoods>> enable = isEnabled(sd, pvh);
		if (!enable.isSuccess()) {
			if(isShowMsg()) return ErrorCode.getFailure(enable.getMsg());
			return ErrorCode.getFailure("不支持此活动");
		}
		ErrorCode<List<BaseGoods>> openCode = getSpdiscountOpentype(sd, enable.getRetval());
		if(!openCode.isSuccess()) return ErrorCode.getFailure(openCode.getMsg());
		List<BaseGoods> baseGoodsList = openCode.getRetval();
		String disableReason = getOrderFirstDisabledReason(sd, spcounter, cpcounterList);
		if(StringUtils.isNotBlank(disableReason)) return ErrorCode.getFailure(disableReason);
		Integer amount = 0;
		Map<Long, BaseGoods> goodsMap = BeanUtil.beanListToMap(baseGoodsList, "id");
		if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERORDER)){
			amount = sd.getDiscount();
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERTICKET)){
			int quantity = 0;
			if(StringUtils.equals(order.getCategory(), GoodsConstant.GOODS_TYPE_TICKET)){
				for (BuyItem item : itemList){
					BaseGoods goods = goodsMap.get(item.getRelatedid());
					if(goods == null) continue;
					if(item.getDisid() != null){
						Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
						String disquantity = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY);
						if(StringUtils.isBlank(disquantity)) continue;
						quantity += Integer.parseInt(disquantity);
					}else{
						quantity += item.getQuantity();
					}
				}
			}else{
				amount = sd.getDiscount() * order.getQuantity();
			}
			amount = sd.getDiscount() * quantity;
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_FIXPRICE)){
			if(StringUtils.equals(order.getCategory(), GoodsConstant.GOODS_TYPE_TICKET)){
				Collection<BaseGoods> otherList = CollectionUtils.disjunction(goodsList, baseGoodsList);
				Map<Long,BaseGoods> otherMap = BeanUtil.beanListToMap(otherList, "id");
				int totalfee = 0;
				for (BuyItem item : itemList) {
					BaseGoods goods = otherMap.get(item.getRelatedid());
					if(goods == null) continue;
					totalfee += item.getDue();
				}
				int quantity = 0;
				for (BuyItem item : itemList){
					BaseGoods goods = goodsMap.get(item.getRelatedid());
					if(goods == null) continue;
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
			}else{
				amount = order.getTotalfee() - sd.getDiscount() * order.getQuantity();
			}
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERCENT)){
			if(StringUtils.equals(order.getCategory(), GoodsConstant.GOODS_TYPE_TICKET)){
				int totalfee = 0;
				for (BuyItem item : itemList){
					BaseGoods goods = goodsMap.get(item.getRelatedid());
					if(goods == null) continue;
					totalfee += item.getDue();
				}
				amount = totalfee*sd.getDiscount() /100;
			}else{
				amount = order.getTotalfee() * sd.getDiscount() /100;
			}
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_BUYONE_GIVEONE)){
			if(StringUtils.equals(order.getCategory(), GoodsConstant.GOODS_TYPE_TICKET)){
				for (BuyItem item : itemList){
					BaseGoods goods = goodsMap.get(item.getRelatedid());
					//该物品或场次不存在优惠
					if(goods == null || item.getDisid() != null) continue;
					int quantity = item.getQuantity()/2;
					int unitprice = item.getUnitprice();
					/* 套票不支持买一送一
					 * if(item.getDisid() != null){
						Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
						String disquantity = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISQUANTITY);
						String disprice = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_DISPRICE);
						if(StringUtils.isBlank(disquantity) || StringUtils.isBlank(disprice)) continue;
						quantity = Integer.parseInt(disquantity)/2;
						unitprice = Integer.parseInt(disprice);
					}*/
					if(quantity > 0){
						amount = unitprice * quantity;
					}
				}
			}else{
				amount = order.getUnitprice();
			}
		}
		if(amount <= 0) return ErrorCode.getFailure("此订单无优惠！");
		if(amount > order.getTotalfee()) amount = order.getTotalfee();
		return ErrorCode.getSuccessReturn(amount);
	}
	
	@Override
	public ErrorCode<List<BaseGoods>> isEnabled(SpecialDiscount sd, PayValidHelper pvh) {
		//成本价限制：costprice1, costprice2??
		if(order.getUnitprice() > sd.getPrice2()  || order.getUnitprice() < sd.getPrice1()){
			return ErrorCode.getFailure("卖价范围不支持！");
		}
		return isEnabled(sd, goodsList, pvh);
	}
	
	public static ErrorCode<List<BaseGoods>> isEnabled(SpecialDiscount sd, List<BaseGoods> goodsList, PayValidHelper pvh){
		ErrorCode code = ErrorCodeConstant.DATEERROR;
		List<BaseGoods> newGoodsList = new ArrayList<BaseGoods>();
		for (BaseGoods goods : goodsList) {
			code = isEnabled(sd, goods, pvh);
			if(code.isSuccess()){
				newGoodsList.add(goods);
			}
		}
		if(newGoodsList.isEmpty()) return code;
		return ErrorCode.getSuccessReturn(newGoodsList);
	}
	
	public static ErrorCode isEnabled(SpecialDiscount sd, BaseGoods goods, PayValidHelper pvh){
		if (sd == null) return ErrorCode.getFailure("本活动不存在");
		if(StringUtils.isNotBlank(sd.getPaymethod()) && 
				StringUtils.isNotBlank(goods.getOtherinfo())){
			if(pvh == null) pvh = new PayValidHelper(JsonUtils.readJsonToMap(goods.getOtherinfo()));
			String[] pay = StringUtils.split(sd.getPaymethod());
			if(!pvh.supportPaymethod(pay[0])) return ErrorCode.getFailure("支付限制！");
		}
		if(StringUtils.isNotBlank(sd.getCitycode())){
			if(!StringUtils.equals(AdminCityContant.CITYCODE_ALL, sd.getCitycode()) && 
					!StringUtils.equals(goods.getCitycode(), sd.getCitycode())){
				return ErrorCode.getFailure("城市不支持！");
			}
		}
		if(goods.getRelatedid() != null){
			if(!isEnabledByRelatedid(sd, goods.getRelatedid())){
				return ErrorCode.getFailure("场馆不支持！");
			}
		}
		if(goods instanceof TicketGoods){
			Long categoryid = (Long) BeanUtil.get(goods, "categoryid");
			if(!isEnabledByCategoryid(sd, categoryid)){
				return ErrorCode.getFailure("项目不支持！");
			}
		}else if(goods instanceof SportGoods){
			Long categoryid = (Long) BeanUtil.get(goods, "itemid");
			if(!isEnabledByCategoryid(sd, categoryid)){
				return ErrorCode.getFailure("项目不支持！");
			}
		}
		if(!isEnabledByItemid(sd, goods.getId())){
			return ErrorCode.getFailure("物品不支持！");
		}
		
		return ErrorCode.SUCCESS;
	}

}
