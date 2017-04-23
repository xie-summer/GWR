package com.gewara.pay;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.pay.GewaOrder;

public class GewapayUtil {
	public static Map<String, String> getOrderPayParams(GewaOrder order, String absPath, String checkvalue){
		Map<String, String> params = new HashMap<String, String>();
		String url = absPath;
		if(order.sureOutPartner()) url += "/partner/";
		else  url += "/gewapay/";
		url += "payOrder.xhtml";
		params.put("orderId", ""+order.getId());
		params.put("payurl", url);
		if(StringUtils.isNotBlank(checkvalue)) params.put("checkvalue", checkvalue);
		if(order.sureOutPartner()) params.put("partnerid", ""+order.getPartnerid());
		params.put("submitMethod", "get");
		return params;
	}
	public static Map<String, String> getCcbposPayParams(GewaOrder order, String absPath){
		Map<String, String> params = new HashMap<String, String>();
		String url = absPath + "/gewapay/ccbquickpay.xhtml";
		params.put("orderId", ""+order.getId());
		params.put("payurl", url);
		params.put("submitMethod", "get");
		return params;
	}
}
