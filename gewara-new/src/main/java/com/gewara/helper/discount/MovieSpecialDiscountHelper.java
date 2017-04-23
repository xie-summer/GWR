package com.gewara.helper.discount;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.pay.PayValidHelper;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public class MovieSpecialDiscountHelper extends SpecialDiscountHelper{
	private OpenPlayItem opi;
	private TicketOrder order;
	private List<SellSeat> seatList;
	private List<Discount> discountList;

	public MovieSpecialDiscountHelper(OpenPlayItem opi, TicketOrder order, List<SellSeat> seatList, List<Discount> discountList) {
		this.opi = opi;
		this.order = order;
		this.seatList = seatList;
		this.discountList = discountList;
	}

	@Override
	public String getFullDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		return getFullDisabledReason(spcounter, cpcounterList, sd, opi, order);
	}
	public static String getFullDisabledReason(Spcounter spcounter, List<Cpcounter> cpcounterList, SpecialDiscount sd, OpenPlayItem opi, TicketOrder order) {
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
		reason += getOpiFullDisabledReason(sd, opi, order.getAddtime());
		return reason;
	}

	@Override
	public String getOrderFirstDisabledReason(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList) {
		//跳过gewaprice, timefrom, timeto, time1, time2, pricegap, price1, price2检测
		if(!isEnabledByFromToTime(sd, order.getAddtime())){
			return "本活动时间为" + DateUtil.formatTimestamp(sd.getTimefrom()) + "至" + DateUtil.formatTimestamp(sd.getTimeto()) + "！";
		}
		//注意，spcounter不能为空
		String rs = getSpcounterDisabledReason(spcounter, cpcounterList, order.getCitycode(), order.getPartnerid(), order.getQuantity());
		if(StringUtils.isNotBlank(rs)) return rs;
		if(order.getQuantity() > sd.getBuynum()|| order.getQuantity() < sd.getMinbuy()){
			if(sd.getBuynum() == sd.getMinbuy()) return "本活动单笔订必须购买" + sd.getBuynum() + "张！";
			else return "本活动单笔订只能购买" + sd.getMinbuy() + "～" + sd.getBuynum() + "张！";
		}
		return getOpiFirstDisabledReason(sd, opi, order.getAddtime());
	}

	@Override
	public ErrorCode isEnabled(SpecialDiscount sd, PayValidHelper pvh) {
		return isEnabled(sd, opi, pvh);
	}

	@Override
	public ErrorCode<Integer> getSpdiscountAmount(SpecialDiscount sd, Spcounter spcounter, List<Cpcounter> cpcounterList, PayValidHelper pvh) {
		if (sd == null) return ErrorCode.getFailure("本活动不存在");
		if (StringUtils.equals(order.getStatus(), OrderConstant.STATUS_NEW_CONFIRM)) {
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
		Map<String, String> otherFeeMap = JsonUtils.readJsonToMap(order.getOtherFeeRemark());
		String umpayfee = otherFeeMap.get(OtherFeeDetail.FEETYPE_U);
		if (StringUtils.isNotBlank(umpayfee) && Integer.parseInt(umpayfee)>0) {
			if (!pvh.supportPaymethod(order.getPaymethod()))
				return ErrorCode.getFailure("此活动不支持您选择的支付方式！");
			pvh = new PayValidHelper(order.getPaymethod());
		}
		if (discountList.size() > 0) {
			return ErrorCode.getFailure("不能和其他优惠方式共用！");
		}
		ErrorCode enable = isEnabled(sd, pvh);
		if (!enable.isSuccess()) {
			if(isShowMsg()) return ErrorCode.getFailure(enable.getMsg());
			return ErrorCode.getFailure("本场次不支持此活动");
		}
		if (StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_SPECIAL)) {
			if (!StringUtils.contains(opi.getSpflag(), sd.getFlag()) && StringUtils.isBlank(sd.getVerifyType())) {
				return ErrorCode.getFailure("本场次不支持此活动！");
			}
		} else if (!StringUtils.contains(opi.getElecard(), PayConstant.CARDTYPE_PARTNER)) {
			return ErrorCode.getFailure("本场次不支持此活动！");
		}

		if (StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_GEWA)) {// 意外情况
			if (order.surePartner()) return ErrorCode.getFailure("本场次不支持此活动！");// 订单来自WAP或商家
		} else if (StringUtils.equals(sd.getOpentype(), SpecialDiscount.OPENTYPE_WAP)) {// WAP活动
			if (!order.sureGewaPartner()) return ErrorCode.getFailure("本活动不支持非手机用户订单！");
			List<Long> ptnidList = BeanUtil.getIdList(sd.getPtnids(), ",");
			if(!ptnidList.contains(order.getPartnerid())){
				return ErrorCode.getFailure("本活动不支持该客户端！");
			}
		} else if (StringUtils.equals(SpecialDiscount.OPENTYPE_PARTNER, sd.getOpentype())) {// 商家活动
			if(!order.getPartnerid().equals(Long.valueOf(sd.getPtnids()))){
				return ErrorCode.getFailure("本场次不支持此活动！");
			}
		}
		String disableReason = getOrderFirstDisabledReason(sd, spcounter, cpcounterList);
		if (StringUtils.isNotBlank(disableReason))
			return ErrorCode.getFailure(disableReason);

		int amount = 0;
		if (StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERORDER)) {
			amount = sd.getDiscount();
		} else if (StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERTICKET)) {
			amount = sd.getDiscount() * order.getQuantity();
		} else if (StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_FIXPRICE)) {
			amount = order.getTotalfee() - sd.getDiscount() * order.getQuantity();
		} else if (StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_PERCENT)) {
			amount = order.getTotalfee() * sd.getDiscount() / 100;
		} else if (StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_BUYONE_GIVEONE)) {
			int disquantity = order.getQuantity() / 2;
			if (disquantity > 0) {
				List<SellSeat> mseatList = GewaOrderHelper.getMaxSellSeat(seatList, discountList, disquantity);
				for (SellSeat seat : mseatList) {
					if (order.surePartner()) {
						amount += order.getUnitprice();
					} else {
						amount += seat.getPrice();
					}
				}
			}
		} else if(StringUtils.equals(sd.getDistype(), SpecialDiscount.DISCOUNT_TYPE_EXPRESSION)){//表达式
			ScriptResult<Integer> result = compute(order, opi, sd.getExpression(), true);
			if(result.hasError()){
				return ErrorCode.getFailure("优惠计算错误！");
			}
			amount = result.getRetval();
		}
		if (amount == 0 && sd.getRebates() == 0)
			return ErrorCode.getFailure("此订单无优惠！");
		if (amount > order.getTotalfee())
			amount = order.getTotalfee();
		return ErrorCode.getSuccessReturn(amount);
	}
	public static ScriptResult<Integer> compute(TicketOrder order, OpenPlayItem opi, String expression, boolean cache){
		CachedScript script = ScriptEngineUtil.buildCachedScript(expression, cache);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("order", BeanUtil.getBeanMapWithKey(order, "mobile","addtime","memberid","partnerid","membername","paymethod","paybank","quantity","unitprice"));
		context.put("opi", BeanUtil.getBeanMapWithKey(opi, "mpid","movieid","cinemaid","playtime","price","costprice","gewaprice","language","edition","citycode","opentype"));
		ScriptResult<Integer> result = script.run(context);
		return result;
	}
	public static String getOpiFirstDisabledReason(SpecialDiscount sd, OpenPlayItem opi, Timestamp addtime) {
		if(!isEnabledByCategoryid(sd, opi.getMovieid())) return "本活动不支持该影片！";
		if(!isEnabledByAddweek(sd, addtime)) return "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByWeektype(sd, opi.getPlaytime())) return "本活动仅限周" + sd.getWeektype() + "的场次！";
		if(!isEnabledByTime(sd, opi.getPlaytime())){
			return "本活动仅限" + sd.getTime1().substring(0,2) + ":" + sd.getTime1().substring(2) +"～" + 
				sd.getTime2().substring(0,2) + ":" + sd.getTime2().substring(2) + "的场次！";
		}
		if(!isEnabledByAddtime(sd, addtime)){
			return "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		if(StringUtils.isNotBlank(sd.getFieldid())){
			List<Long> roomidList = BeanUtil.getIdList(sd.getFieldid(), ",");
			if(!roomidList.contains(opi.getRoomid())){
				return "本活动不支持该厅！";
			}
		}
		return "";
	}
	private static String getOpiFullDisabledReason(SpecialDiscount sd, OpenPlayItem opi, Timestamp addtime) {
		String reason = "";
		if(!isEnabledByCategoryid(sd, opi.getMovieid())) reason += "本活动不支持该影片！";
		if(!isEnabledByAddweek(sd, addtime))  reason += "本活动仅限周" + sd.getAddweek() + "购买！";
		if(!isEnabledByWeektype(sd, opi.getPlaytime())) reason += "本活动仅限周" + sd.getWeektype() + "的场次！";
		if(!isEnabledByTime(sd, opi.getPlaytime())){
			reason += "本活动仅限" + sd.getTime1().substring(0,2) + ":" + sd.getTime1().substring(2) +"～" + 
				sd.getTime2().substring(0,2) + ":" + sd.getTime2().substring(2) + "的场次！";
		}
		if(!isEnabledByAddtime(sd, addtime)){
			reason += "本活动仅限" + sd.getAddtime1().substring(0,2) + ":" + sd.getAddtime1().substring(2) +"～" + 
			sd.getAddtime2().substring(0,2) + ":" + sd.getAddtime2().substring(2) + "购买！";
		}
		if(StringUtils.isNotBlank(sd.getFieldid())){
			List<Long> roomidList = BeanUtil.getIdList(sd.getFieldid(), ",");
			if(!roomidList.contains(opi.getRoomid())){
				reason += "本活动不支持该厅！";
			}
		}
		return reason;
	}
	public static ErrorCode isEnabled(SpecialDiscount sd, OpenPlayItem opi, PayValidHelper pvh){
		if (sd == null) return ErrorCode.getFailure("本活动不存在");
		if(!isEnabledByCitycode(sd, opi.getCitycode())) return ErrorCode.getFailure("城市不支持！");
		if(!isEnabledByRelatedid(sd, opi.getCinemaid())) return ErrorCode.getFailure("场馆不支持！");
		if(!isEnabledByCategoryid(sd, opi.getMovieid())) return ErrorCode.getFailure("电影不支持！");
		if(!isEnabledByItemid(sd, opi.getMpid())) return ErrorCode.getFailure("场次不支持！");
		if(!isEnableByEdtion(sd, opi.getEdition())) return ErrorCode.getFailure("版本不支持！");
		if(StringUtils.isNotBlank(sd.getPaymethod()) && 
				StringUtils.isNotBlank(opi.getOtherinfo())){
			String[] pay = StringUtils.split(sd.getPaymethod());
			if(!pvh.supportPaymethod(pay[0])) 
				return ErrorCode.getFailure("支付限制！");
		}
		if(opi.getGewaprice() > sd.getPrice2()  || opi.getGewaprice() < sd.getPrice1()){
			return ErrorCode.getFailure("卖价范围不支持！");
		}
		int costprice = opi.getCostprice() == null ? 0 : opi.getCostprice();
		if(!isEnabledByPricegap(sd, opi.getGewaprice() - costprice)) return ErrorCode.getFailure("成本差额不支持！");
		if(!isEnabledByCostprice(sd, costprice)) return ErrorCode.getFailure("成本范围不支持！");
		return ErrorCode.SUCCESS;
	}
	
	public static boolean isEnableByEdtion(SpecialDiscount sd, String edition){
		if(StringUtils.isNotBlank(sd.getEdition())){
			List<String> editionList = Arrays.asList(StringUtils.split(sd.getEdition(), ","));
			if(!editionList.isEmpty() && !editionList.contains(edition)){
				return false;
			}
		}
		return true;
	}
}
