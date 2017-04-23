package com.gewara.helper.discount;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.xmlbind.gym.CardItem;

public class GymSpecialDiscountHelper extends SpecialDiscountHelper{
	private GymOrder order;
	private CardItem item;
	private List<Discount> discountList;
	public GymSpecialDiscountHelper(GymOrder order, CardItem item, List<Discount> discountList){
		this.order = order;
		this.item = item;
		this.discountList = discountList;
	}

	@Override
	public String getFullDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		return getFullDisabledReason(spcounter, cpcounterList, sd, item, order);
	}
	private static String getFullDisabledReason(Spcounter spcounter, List<Cpcounter> cpcounterList, SpecialDiscount sd, CardItem item, GymOrder order) {
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
		reason += getOttFullDisabledReason(sd, item, order.getAddtime());
		return reason;
	}
	@Override
	public String getOrderFirstDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		if(order.getAddtime().before(sd.getTimefrom()) || order.getAddtime().after(sd.getTimeto())){
			return "本活动时间为" + DateUtil.formatTimestamp(sd.getTimefrom()) + "至" + DateUtil.formatTimestamp(sd.getTimeto()) + "！";
		}
		//注意，spcounter不能为空
		String rs = getSpcounterDisabledReason(spcounter, cpcounterList, order.getCitycode(), order.getPartnerid(), order.getQuantity());
		if(StringUtils.isNotBlank(rs)) return rs;
		if(order.getQuantity() > sd.getBuynum()|| order.getQuantity() < sd.getMinbuy()){
			if(sd.getBuynum() == sd.getMinbuy()) return "本活动单笔订必须购买" + sd.getBuynum() + "张！";
			else return "本活动单笔订只能购买" + sd.getMinbuy() + "～" + sd.getBuynum() + "张！";
		}
		return getOttFirstDisabledReason(sd, item, order.getAddtime());
	}

	@Override
	public ErrorCode<Integer> getSpdiscountAmount(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh) {
		if(sd==null) return ErrorCode.getFailure("本活动不存在");
		if(StringUtils.equals(order.getStatus(), OrderConstant.STATUS_NEW_CONFIRM)){
			return ErrorCode.getFailure("已经确认的订单不能修改！");
		}
		return validSpdiscountWithoutStatus(sd, spcounter, cpcounterList, pvh);
	}
	@Override
	public ErrorCode<Integer> validSpdiscountWithoutStatus(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh){
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		String umpayfee = otherFeeMap.get(OtherFeeDetail.FEETYPE_U);
		if (StringUtils.isNotBlank(umpayfee) && Integer.parseInt(umpayfee)>0) {
			if (!pvh.supportPaymethod(order.getPaymethod()))
				ErrorCode.getFailure("此活动不支持您选择的支付方式！");
			pvh = new PayValidHelper(order.getPaymethod());
		}
		if(discountList.size() > 0){
			return ErrorCode.getFailure("不能和其他优惠方式共用！");
		}
		ErrorCode enable = isEnabled(sd, pvh);
		if (!enable.isSuccess()) {
			if(isShowMsg()) return ErrorCode.getFailure(enable.getMsg());
			return ErrorCode.getFailure("本健身卡不支持此活动");
		}
		if(StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_SPECIAL)){
			if(!StringUtils.contains(item.getSpflag(), sd.getFlag())){
				return ErrorCode.getFailure("本健身卡不支持此活动！");
			}
		}else if (!StringUtils.contains(item.getElecard(), PayConstant.CARDTYPE_PARTNER)) {
			return ErrorCode.getFailure("本健身卡不支持此活动！");
		}
		
		if(StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_GEWA)){//意外情况
			if(order.surePartner()) return ErrorCode.getFailure("本健身卡不支持此活动！");//订单来自WAP或商家
		}else if(StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_WAP)){//WAP活动
			if(!order.sureGewaPartner()) return ErrorCode.getFailure("本活动不支持非手机用户订单！"); 
			List<Long> ptnidList = BeanUtil.getIdList(sd.getPtnids(), ",");
			if(!ptnidList.contains(order.getPartnerid())){
				return ErrorCode.getFailure("本活动不支持该客户端！");
			}
		}else if(StringUtils.equals(SpecialDiscount.OPENTYPE_PARTNER, sd.getOpentype())){//商家活动
			if(!order.getPartnerid().equals(Long.valueOf(sd.getPtnids()))){
				return ErrorCode.getFailure("本健身卡不支持此活动！");
			}
			//商家订单，直接判断支付方式是否正确
			if(!sd.isValidPaymethod(order.getPaymethod(), order.getPaybank())){
				return ErrorCode.getFailure("支付方式不正确，不能参与此活动！");
			}
		}
		
		String disableReason = getOrderFirstDisabledReason(sd, spcounter, cpcounterList);
		if(StringUtils.isNotBlank(disableReason)) return ErrorCode.getFailure(disableReason);
		
		int amount = 0;
		if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERORDER)){
			amount = sd.getDiscount();
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERTICKET)){
			amount = sd.getDiscount() * order.getQuantity();
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_FIXPRICE)){
			amount = order.getTotalfee() - sd.getDiscount() * order.getQuantity();
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERCENT)){
			amount = order.getTotalfee() * sd.getDiscount() /100;
		}else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_BUYONE_GIVEONE)){
			amount = order.getTotalfee()/2;

		}
		if(amount == 0 && sd.getRebates()==0) return ErrorCode.getFailure("此订单无优惠！");
		if(amount > order.getTotalfee()) amount = order.getTotalfee();
		return ErrorCode.getSuccessReturn(amount);
	}
	public static String getOttFirstDisabledReason(SpecialDiscount sd, CardItem item, Timestamp addtime) {
		if(!isEnabledByAddweek(sd, addtime)) return "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByRelatedid(sd, item.getGymid())) return "本活动不支持该场馆使用！";
		if(!isEnabledByItemid(sd, item.getId())) return "本活动不支持该卡使用！";
		if(!isEnabledByAddtime(sd, addtime)){
			return "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		return "";
	}
	private static String getOttFullDisabledReason(SpecialDiscount sd, CardItem item, Timestamp addtime) {
		String reason = "";
		if(!isEnabledByAddweek(sd, addtime)) reason += "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByRelatedid(sd, item.getGymid())) reason += "本活动不支持该场馆使用！";
		if(!isEnabledByItemid(sd, item.getId())) reason += "本活动不支持该卡使用！";
		if(!isEnabledByAddtime(sd, addtime)){
			reason += "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		return reason;
	}
	@Override
	public ErrorCode isEnabled(SpecialDiscount sd, PayValidHelper pvh){
		return isEnabled(sd, item, pvh);
	}

	public static ErrorCode isEnabled(SpecialDiscount sd, CardItem item, PayValidHelper pvh) {
		if (sd == null) return ErrorCode.getFailure("本活动不存在");
		if(!isEnabledByRelatedid(sd, item.getGymid()))	return ErrorCode.getFailure("场馆不支持！");
		if(StringUtils.isNotBlank(sd.getPaymethod()) && 
				StringUtils.isNotBlank(item.getOtherinfo())){
			String[] pay = StringUtils.split(sd.getPaymethod());
			if(!pvh.supportPaymethod(pay[0])) return ErrorCode.getFailure("支付限制！");
		}
		if(!isEnabledByPrice(sd, item.getPrice())) return ErrorCode.getFailure("卖价范围不支持！");
		if(!isEnabledByPricegap(sd, item.getPrice() - item.getCostprice())) return ErrorCode.getFailure("成本差额不支持！");
		if(!isEnabledByCostprice(sd, item.getCostprice())) return ErrorCode.getFailure("成本范围不支持！");
		return ErrorCode.SUCCESS;
	}
}
