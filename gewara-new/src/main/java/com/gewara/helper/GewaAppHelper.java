package com.gewara.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.model.api.ApiUserExtra;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.mobile.AsConfig;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.OpenMember;
import com.gewara.support.ErrorCode;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;

public class GewaAppHelper {
	//手机app展示的支付方式及名称
	public static Map<String, String> textMap = null;
	static{
		Map<String, String> text = new HashMap<String, String>();
		text.put(PaymethodConstant.PAYMETHOD_ELECARDPAY, "电子券");
		text.put(PaymethodConstant.PAYMETHOD_GEWAPAY, "格瓦拉余额:支持账户瓦币、余额支付");
		
		text.put(PaymethodConstant.PAYMETHOD_CMWAPPAY, "移动手机支付");
		text.put(PaymethodConstant.PAYMETHOD_CMPAY, "移动手机短信支付");

		text.put(PaymethodConstant.PAYMETHOD_CMBWAPPAY, "招商银行:借记卡、信用卡都可以");
		text.put(PaymethodConstant.PAYMETHOD_BOCWAPPAY, "中国银行手机支付");
		text.put(PaymethodConstant.PAYMETHOD_SPDWAPPAY, "浦发银行手机支付:借记卡、信用卡都可以");
		text.put(PaymethodConstant.PAYMETHOD_SPDWAPPAY_ACTIVITY, "浦发银行借记卡支付");
		
		text.put(PaymethodConstant.PAYMETHOD_ALIWAPPAY, "支付宝WAP支付:支持支付宝余额、卡通");
		text.put("aliwapPay:CREDITCARD_CCB", "支付宝建行信用卡支付");
		
		text.put(PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY, "支付宝移动快捷支付:推荐已安装支付宝APP的瓦友使用");
		text.put(PaymethodConstant.PAYMETHOD_CHINASMARTMOBILEPAY, "银联手机在线支付:银行最多、无需开通网银也能支付");
		text.put(PaymethodConstant.PAYMETHOD_CHINASMARTJSPAY, "江苏银联手机端支付");
		text.put(PaymethodConstant.PAYMETHOD_CMSMARTPAY, "移动手机安全支付:移动用户短信发送ktsjzf至10086可开通");
		text.put(PaymethodConstant.PAYMETHOD_HZWAPPAY, "杭州银行");
		text.put(PaymethodConstant.PAYMETHOD_PARTNERPAY, "合作支付");
		text.put(PaymethodConstant.PAYMETHOD_CMBWAPSTOREPAY, "招商银行");
		
		text.put(PaymethodConstant.PAYMETHOD_CCBWAPPAY, "建行手机支付:借记卡、信用卡都可以");
		text.put(PaymethodConstant.PAYMETHOD_WXWCPAY, "微信支付");
		
		text.put(PaymethodConstant.PAYMETHOD_WXAPPTENPAY, "微信客户端支付");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY, "财付通一键支付: 无需安装插件，200元以下免密码支付");
		
		text.put(PaymethodConstant.PAYMETHOD_BFBWAPPAY, "百度钱包wap支付");
		
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":4186", "农业银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2011", "招商银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2013", "建设银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2016", "广发银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2014", "光大银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2024", "平安银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2008", "民生银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2125", "中信银行储蓄卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":2147", "上海银行储蓄卡");
		
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3007", "农业银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3006", "招商银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3106", "建设银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3003", "工商银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3107", "中国银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3109", "广发银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3108", "光大银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3114", "深圳发展信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3113", "兴业银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3110", "平安银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3119", "民生银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3115", "中信银行信用卡");
		text.put(PaymethodConstant.PAYMETHOD_ONECLICKTENPAY+":3205", "上海银行信用卡");
		
		textMap = UnmodifiableMap.decorate(text);
	}
	
	//过滤折扣后的支付方式
	public static Map<String, String> getFilterMap(ApiUserExtra extra, List<Discount> discountList, String category, String appVersion){
		boolean dicount = false;
		if(discountList!=null && discountList.size()>0){
			if(StringUtils.equals(PayConstant.DISCOUNT_TAG_PARTNER, discountList.get(0).getTag())){
				dicount = true;
			}
		}
		Map<String, String> result = new LinkedHashMap<String, String>();
		if(StringUtils.isBlank(extra.getPaymethod())) return result;
		String strpaymethod = dicount?extra.getAllPaymethodByCategory(category):extra.getPaymethodByCategory(category);
		String[] pms = StringUtils.split(strpaymethod, ",");
		for(String pm : pms){
			if(textMap.containsKey(pm)) result.put(pm, textMap.get(pm));
		}
		List<String> removeKeyList = new ArrayList<String>();
		if(StringUtils.isNotBlank(appVersion)){
			String pmv = VmUtils.getJsonValueByKey(extra.getOtherinfo(), ApiUserExtra.OTHER_KEY_PAYMETHOD_VERSION);
			if(StringUtils.isNotBlank(pmv)){
				List<String> vpmList = Arrays.asList(pmv.split(","));
				for(String vpm : vpmList){
					String[] tmp = StringUtils.split(vpm, ":");
					if(appVersion.compareTo(tmp[0])<0){
						removeKeyList.addAll(Arrays.asList(tmp[1].split("@")));
					}
				}
			}
		}
		for(String removekey : removeKeyList){
			result.remove(removekey);
		}
		return result;
	}
	public static Map<String, String> getFilterMap(ApiUserExtra extra, List<Discount> discountList, String appVersion){
		return getFilterMap(extra, discountList, null, appVersion);
	}
	//获取余额不足的订单支付方式
	public static Map<String, String> getChargeMap(ApiUserExtra extra, GewaOrder order, String apptype, String appVersion){
		Map<String, String> result = new HashMap<String, String>();
		if(StringUtils.isBlank(extra.getChargemethod())) return result;
		String[] pms = StringUtils.split(extra.getChargemethod(), ",");
		for(String pm : pms){
			if(textMap.containsKey(pm)) {
				result.put(pm, textMap.get(pm));
				if(StringUtils.isBlank(appVersion) || !isSupportPoint(order, apptype, appVersion)){
					break;
				}
			}
		}
		return result;
	}
	//银联手机在线支付 调用不通的版本
	public static String getChinaSmartPayVersion(GewaOrder order, String apptype, String paymethod, String appVersion){
		if(!StringUtils.startsWith(paymethod, AppConstant.CHINASMART_STARTWIDTH)) return null;
		if(isSupportPoint(order, apptype, appVersion)) return "1.1.0";
		return null;
	}
	//是否支持积分
	public static boolean isSupportPoint(GewaOrder order, String apptype, String appVersion){
		if(StringUtils.isBlank(appVersion)){ 
			return false;
		}
		if(order instanceof DramaOrder){
			return true;
		}
		if(order instanceof GoodsOrder){
			return true;
		}
		if(StringUtils.isBlank(apptype)){
			if(order instanceof TicketOrder){
				apptype = AppConstant.APPTYPE_CINEMA;
			}else if(order instanceof SportOrder){
				apptype = AppConstant.APPTYPE_SPORT;
			}
		}
		if(StringUtils.equals(apptype, "fest")){
			apptype = AppConstant.APPTYPE_CINEMA;
		}
		if(StringUtils.equalsIgnoreCase(apptype, AppConstant.APPTYPE_CINEMA)){
			if(appVersion.compareTo(AppConstant.MOVIE_APPVERSION_3_1_0)>=0){
				return true;
			}
		}else if(StringUtils.equalsIgnoreCase(apptype, AppConstant.APPTYPE_SPORT)){
			if(appVersion.compareTo(AppConstant.SPORT_APPVERSION_3_0_0)>=0) {
				return true;
			}
		}
		return false;
	}
	//重新获取特价活动
	public static List<SpecialDiscount> getDiscountList(List<SpecialDiscount> oldspList, ApiUserExtra extra, ErrorCode<OpenMember> code){
		if(oldspList!=null){
			List<SpecialDiscount> spList = new ArrayList<SpecialDiscount>();
			List<SpecialDiscount> spList2 = new ArrayList<SpecialDiscount>();
			List<SpecialDiscount> spList3 = new ArrayList<SpecialDiscount>();
			if(StringUtils.isNotBlank(extra.getAllPaymethod())){ 
				String[] pmList = StringUtils.split(extra.getAllPaymethod(), ",");
				for(String pm : pmList){ 
					for(SpecialDiscount oldsp : oldspList){
						if(StringUtils.isNotBlank(oldsp.getPaymethod())){
							//没有该支付方式的特价活动不展示
							List<String> tmppmList = Arrays.asList(StringUtils.split(oldsp.getPaymethod(), ","));
							if(!spList.contains(oldsp) && tmppmList.contains(pm)){
								spList.add(oldsp);
							}
						}else {
							if(!spList.contains(oldsp)) spList.add(oldsp);
						}
					}
				}
				spList2 = new ArrayList<SpecialDiscount>(spList);
				if(code.isSuccess()){
					OpenMember om = code.getRetval();
					Map<String, String> otherMap = VmUtils.readJsonToMap(extra.getSourcemethod());
					String sourcepaymethod = otherMap.get(om.getCategory());
					if(StringUtils.isNotBlank(sourcepaymethod)){
						String[] ssList = StringUtils.split(sourcepaymethod, ",");
						for(String pm : ssList){
							for(SpecialDiscount oldsp : spList2){
								//登录来源不满足的不加入
								String loginfrom = oldsp.getLoginfrom();
								if(StringUtils.isNotBlank(loginfrom)){
									List<String> lfList = Arrays.asList(StringUtils.split(loginfrom, ","));
									if(!lfList.contains(om.getCategory())){
										continue;
									}
								}
								if(StringUtils.isNotBlank(oldsp.getPaymethod())){
									List<String> tmppmList = Arrays.asList(StringUtils.split(oldsp.getPaymethod(), ","));
									if(!spList3.contains(oldsp) && tmppmList.contains(pm)){
										spList3.add(oldsp);
									}
								}else {
									if(!spList3.contains(oldsp)) spList3.add(oldsp);
								}
							}
						}
					}else {
						for(SpecialDiscount oldsp : spList2){
							//登录来源不满足的不加入
							String loginfrom = oldsp.getLoginfrom();
							if(StringUtils.isNotBlank(loginfrom)){
								List<String> lfList = Arrays.asList(StringUtils.split(loginfrom, ","));
								if(StringUtils.isBlank(om.getCategory()) || !lfList.contains(om.getCategory())){
									continue;
								}
							}
							if(!spList3.contains(oldsp)) spList3.add(oldsp);
						}
					}
				}else {
					for(SpecialDiscount oldsp : spList2){
						if(StringUtils.isBlank(oldsp.getLoginfrom())){
							spList3.add(oldsp);
						}
					}
				}
			}
			return spList3;
		}else {
			return oldspList;
		}
	}
	public static String getOrderOther(String mainPaymethod, GewaOrder order, ErrorCode<OpenMember> omcode){
		Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		if(StringUtils.equals(PaymethodConstant.PAYMETHOD_ALISMARTMOBILEPAY, mainPaymethod)){
			if(omcode.isSuccess()){
				OpenMember om = omcode.getRetval();
				if(StringUtils.equals(om.getCategory(), MemberConstant.CATEGORY_ALIWALLET)){
					String token = JsonUtils.getJsonValueByKey(om.getOtherinfo(), MemberConstant.ALIWALLET_SHORTTOKEN);
					if(StringUtils.isNotBlank(token)){
						otherinfoMap.put(MemberConstant.ALIWALLET_EXTERN_TOKEN, token);
					}
				}
			}
		}else {
			otherinfoMap.remove(MemberConstant.CATEGORY_ALIWALLET);
		}
		return JsonUtils.writeMapToJson(otherinfoMap);
	}
	
	
	//过滤折扣后的支付方式
	public static Map<String, String> getFilterMap(AsConfig as, List<Discount> discountList){
		boolean dicount = false;
		if(discountList!=null && discountList.size()>0){
			if(StringUtils.equals(PayConstant.DISCOUNT_TAG_PARTNER, discountList.get(0).getTag())){
				dicount = true;
			}
		}
		Map<String, String> result = new LinkedHashMap<String, String>();
		if(StringUtils.isBlank(as.getPaymethod())) return result;
		String strpaymethod = dicount?as.getAllPaymethod():as.getPaymethod();
		String[] pms = StringUtils.split(strpaymethod, ",");
		for(String pm : pms){
			if(textMap.containsKey(pm)) result.put(pm, textMap.get(pm));
		}
		return result;
	}
		
}
