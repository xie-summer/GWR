/**
 * 
 */
package com.gewara.pay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.Config;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.pay.QrySandOrder;

public class SandPayUtil {
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(SandPayUtil.class, Config.getServerIp(), Config.SYSTEMID);
	private static final String MD5KEY = "|||sand20111206";
	private static final String MERID = "999000000000006";
	public static Map<String, String> getPayParams(GewaOrder gorder,ApiUser partner,String notify) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		TicketOrder order = (TicketOrder)gorder;
		params.put("mer_id", MERID);
		params.put("bill_id", order.getTradeNo());
		params.put("bill_time", DateUtil.format(order.getAddtime(), "yyyMMddHHmmss"));
		params.put("charge_amt", order.getDue()+"");
		params.put("order_unitprice", order.getUnitprice()+"");
		params.put("order_num", order.getQuantity()+"");
		params.put("order_date", DateUtil.formatTimestamp(order.getValidtime()));
		params.put("user_phone", order.getMobile());
		params.put("time_stamp", DateUtil.format(DateUtil.getCurFullTimestamp(), "yyyMMddHHmmss"));
		params.put("sign", getSign(params));
		params.put("notify_url", notify);
		params.put("payurl", partner.getAddOrderUrl());
		params.put("desc", null);
		params.put("charge_ext1", null);
		params.put("charge_ext2", null);
		params.put("charge_ext3", null);
		return params;
	}
	private static String getSign(Map<String, String> params){
		StringBuilder sb = new StringBuilder();
		for (String key : params.keySet()) {
			sb.append("&").append(key).append("=").append(params.get(key));
		}
		String result = sb.append(MD5KEY).toString().replaceFirst("&", "");
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, result);
		return StringUtil.md5(result).toLowerCase();
	}
	
	public static String getQryOrderMsg(GewaOrder order) {
		String msg = "<tradeno>"+ order.getTradeNo() +"</tradeno>"+
					 "<paidAmount>"+ order.getDue() +"</paidAmount>"	;
		return msg;
	}
	public static boolean checkSign(QrySandOrder result) {
		String sign = "ret_order="+result.getRet_order()+"&tradeno="+result.getTradeno()+"&order_num="+
		result.getOrder_num()+"&order_stauts="+result.getOrder_stauts()+"&order_date="+result.getOrder_date();
		return StringUtils.equalsIgnoreCase(result.getSign(), StringUtil.md5(sign+MD5KEY));
	}
	
}
