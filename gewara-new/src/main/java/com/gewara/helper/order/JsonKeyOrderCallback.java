package com.gewara.helper.order;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.util.JsonUtils;

public class JsonKeyOrderCallback implements OrderCallback{
	private String jsonKey;
	private String jsonValue;
	public JsonKeyOrderCallback(String jsonKey, String jsonValue){
		this.jsonKey = jsonKey;
		this.jsonValue = jsonValue;
	}
	@Override
	public void processOrder(SpecialDiscount sd, GewaOrder order) {
		if(StringUtils.isNotBlank(jsonKey) && StringUtils.isNotBlank(jsonValue)){
			order.setOtherinfo(JsonUtils.addJsonKeyValue(order.getOtherinfo(), jsonKey, jsonValue));
		}		
	}

}
