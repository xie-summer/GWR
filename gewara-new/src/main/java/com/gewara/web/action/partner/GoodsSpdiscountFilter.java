package com.gewara.web.action.partner;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.GoodsSpecialDiscountHelper;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.util.JsonUtils;

public class GoodsSpdiscountFilter extends SpdiscountFilter {
	private BaseGoods goods;
	
	public GoodsSpdiscountFilter(BaseGoods goods){
		this.goods = goods;
	}
	
	@Override
	public boolean excludeOpi(SpecialDiscount sd) {
		PayValidHelper pvh = new PayValidHelper(JsonUtils.readJsonToMap(goods.getOtherinfo()));
		return ((StringUtils.isNotBlank(goods.getSpflag()) && !StringUtils.contains(goods.getSpflag(), sd.getTag()))
				|| !GoodsSpecialDiscountHelper.isEnabled(sd, goods, pvh).isSuccess());
	}

}
