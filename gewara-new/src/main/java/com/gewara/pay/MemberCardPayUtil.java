package com.gewara.pay;

import java.util.HashMap;
import java.util.Map;

import com.gewara.model.pay.GewaOrder;

public class MemberCardPayUtil {
	public static Map<String, String> getOrderPayParams(GewaOrder order, String absPath){
		Map<String, String> params = new HashMap<String, String>();
		String url = absPath + "/gewapay/memberCard/payOrder.xhtml";
		params.put("orderId", ""+order.getId());
		params.put("payurl", url);
		params.put("submitMethod", "get");
		return params;
	}
}
