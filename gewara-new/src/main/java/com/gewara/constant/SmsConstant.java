package com.gewara.constant;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;

import com.gewara.model.pay.SMSRecordBase;

/**
 * 短信相关常量
 * @author acerge(acerge@163.com)
 * @since 3:03:01 PM Oct 10, 2011
 */
public class SmsConstant {
	//短信状态
	public static final String STATUS_Y = "Y"; 							//已发送
	public static final String STATUS_N = "N"; 							//未发送
	public static final String STATUS_Y_TRANS = "Y_TRANS";				//已传递给发送方
	public static final String STATUS_Y_IGNORE = "Y_IGNORE";			//合作方要求不发送
	public static final String STATUS_Y_LARGE = "Y_LARGE";				//内容超长发送
	public static final String STATUS_N_SENDERROR = "N_SEND_ERR";		//客户端未发送出去
	
	public static final String STATUS_N_ERROR = "N_ERR";				//网关发送返回失败，可能是关键字过滤或余额不足
	public static final String STATUS_FILTER = "FILTER";				//审核状态
	public static final String STATUS_D = "D"; 							//废弃
	public static final String STATUS_PROCESS = "P";					//已处理

	//短信类型:优先级按类型倒排
	public static final String SMSTYPE_NOW = "now";						//立即
	public static final String SMSTYPE_DYNCODE = "dyncode";				//手机动态码
	public static final String SMSTYPE_MANUAL = "manu";					//手工
	public static final String SMSTYPE_ACTIVITY = "activity";			//活动手机短信
	public static final String SMSTYPE_ECARD = "ec";					//电子卡
	public static final String SMSTYPE_3H = "3h";						//提前3小时
	public static final String SMSTYPE_10M = "10m";						//观影后10分钟
	public static final String SMSTYPE_CO = "co";						//优惠券过期提醒
	public static final String SMSTYPE_FB = "fb";						//退款提醒
	public static final String SMSTYPE_INVOICE = "invoice";				//发票邮寄发送短信
	public static final String SMSTYPE_NOW_API = "now_api";				//API立即发送
	
	public static final Map<String, String> typeMap;
	static {
		Map<String, String> tmp = new LinkedHashMap<String, String>();
		tmp.put(SMSTYPE_NOW, "立即");
		tmp.put(SMSTYPE_3H, "提前3小时");
		tmp.put(SMSTYPE_MANUAL, "手工");
		tmp.put(SMSTYPE_ACTIVITY, "活动");
		tmp.put(SMSTYPE_ECARD, "电子卡");
		tmp.put(SMSTYPE_10M, "观影后10分钟");
		tmp.put(SMSTYPE_CO, "优惠券过期提醒");
		tmp.put(SMSTYPE_FB, "退款提醒");
		tmp.put(SMSTYPE_INVOICE, "发票邮寄通知");
		tmp.put(SMSTYPE_DYNCODE, "各类动态码");
		typeMap = UnmodifiableMap.decorate(tmp);
	}
	public static String filterContent(SMSRecordBase sms){
		String content = sms.getContent();
		if(StringUtils.equals(sms.getSmstype(), SMSTYPE_ECARD)){
			return content.replaceAll("[a-zA-Z1-9]{12}", "********");
		}
		return content.replaceAll("\\d{5,9}", "*****");
	}

}
