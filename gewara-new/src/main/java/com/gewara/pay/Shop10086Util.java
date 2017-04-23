package com.gewara.pay;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.partner.Shop10086Order;

/**
 * 沪动商城
 * @author user
 *
 */
public class Shop10086Util {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(Shop10086Util.class, Config.getServerIp(), Config.SYSTEMID);
	
	public static String getSign(ApiUser partner,Map map){
		Object[] keys = {"funCode","instId","rpId","orderId","stlDate","retCode","retMsg"};
		Map<String, String> dataMap = JsonUtils.readJsonToMap(partner.getOtherinfo());
		StringBuilder s = new StringBuilder(dataMap.get("checkSign"));
		for(Object key : keys){
			s.append(map.get(key) == null ? "" : map.get(key));
		}
		String sign = StringUtils.substring(s.toString(),1);
		dbLogger.warn(partner.getId() + "签名原始串:" + sign);
		return StringUtil.md5(sign);
	}
	
	public static boolean query(ApiUser partner,String orderId,String reqDate,String reqTime,String instId){
		Map<String, String> dataMap = JsonUtils.readJsonToMap(partner.getOtherinfo());
		StringBuilder sign = new StringBuilder(dataMap.get("checkSign"));
		sign.append("V2012015").append(instId).append(reqDate).append(reqTime).append(orderId);
		StringBuilder s = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><requestMsg><funCode>V2012015</funCode><instId>");
		s.append(instId).append("</instId><reqDate>").append(reqDate).append("</reqDate><reqTime>").append(reqTime).append("</reqTime><orderId>")
			.append(orderId).append("</orderId><sign>").append(StringUtil.md5(sign.toString())).append("</sign></requestMsg>");
		if(StringUtils.isNotBlank(partner.getQryurl())){
			HttpResult code = HttpUtils.postBodyAsString(partner.getQryurl(), s.toString());
			if(!code.isSuccess()){
				dbLogger.warn(partner.getId() + "订单反查出错：" + code.getMsg());
				return false;
			}
			BeanReader beanReader = ApiUtils.getBeanReader("requestMsg", Shop10086Order.class);
			Shop10086Order so = (Shop10086Order)ApiUtils.xml2Object(beanReader,code.getResponse());
			if(so == null){
				dbLogger.warn(partner.getId() + "订单反查数据格式有问题：" + code.getResponse());
				return false;
			}
			if(StringUtils.equals("0000", so.getRetCode()) && StringUtils.equals("006",so.getOrderStatus())){
				return true;
			}
			return false;
		}else{
			return true;
		}
	}
	
	public static String getCheckSign(ApiUser partner,String[] keys,Object obj){
		//Arrays.sort(keys);
		Map<String, String> dataMap = JsonUtils.readJsonToMap(partner.getOtherinfo());
		StringBuilder sb = new StringBuilder(dataMap.get("checkSign"));
		Class c = obj.getClass();
		try {
			for(int index = 0 ;index < keys.length;index++){
				Method m = c.getDeclaredMethod("get" + StringUtils.substring(keys[index],0, 1).toUpperCase() + StringUtils.substring(keys[index], 1));
				Object value = m.invoke(obj);
				sb.append(value == null ? "":value.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} 
		dbLogger.warn(partner.getId() + "移动商城签名：" + sb.toString());
		return sb.toString();
	}
	
	public static boolean pushOrder(ApiUser partner,GewaOrder order){
		Date now = new Date();
		String reqDate = DateUtil.format(now,"yyyyMMdd");
		String reqTime = DateUtil.format(now,"HHmmss");
		Map<String, String> dataMap = JsonUtils.readJsonToMap(partner.getOtherinfo());
		StringBuilder sign = new StringBuilder(dataMap.get("checkSign"));
		sign.append("V2012013gewa").append(order.getTradeNo()).append(reqDate).append(reqTime).append(order.getPayseqno()).append("002");
		StringBuilder s = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><requestMsg><funCode>V2012013</funCode><instId>gewa</instId>");
		s.append("<rpId>").append(order.getTradeNo()).append("</rpId>")
			.append("<reqDate>").append(DateUtil.format(now,"yyyyMMdd")).append("</reqDate>")
			.append("<reqTime>").append(DateUtil.format(now,"HH:mm:ss")).append("</reqTime>")
			.append("<orderId>").append(order.getPayseqno()).append("</orderId>")
			.append("<orderStatus>002</orderStatus><expand></expand>")
			.append("<sign>").append(StringUtil.md5(sign.toString())).append("</sign></requestMsg>");
		HttpResult result = HttpUtils.postBodyAsString(dataMap.get("pushOrderUrl"), s.toString());
		if(result.isSuccess()){
			dbLogger.warn(partner.getId() + "订单推送成功：" + result.getResponse());
			return true;
		}
		dbLogger.warn(partner.getId() + "订单推送出错：" + result.getMsg());
		return false;
	}
}
