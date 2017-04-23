package com.gewara.helper;

import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.pay.GoodsOrder;

public abstract class ActivityGoodsHelper {

	public static String[] getUniqueKey(ActivityGoods goods, GoodsOrder order) {
		String opkey = "activityGoods" + goods.getId();
		return new String[]{opkey + order.getMemberid(), opkey + order.getMobile()};
	}
}
