package com.gewara.helper.api;

import java.util.HashMap;
import java.util.Map;

import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsPrice;

public class GewaApiGoodsHelper {
	//物品
	public static Map<String, Object> getGoodsMap(BaseGoods goods, String logo){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("goodsid", goods.getId());
		params.put("goodsname", goods.getGoodsname());
		params.put("relatedid", goods.getRelatedid());
		params.put("logo", logo);
		params.put("summary", goods.getSummary());
		params.put("oriprice", goods.getOriprice());
		params.put("price", goods.getUnitprice());
		params.put("maxbuy", goods.getMaxbuy());
		params.put("description", goods.getDescription());
		return params;
	}
	//物品价格
	public static Map<String, Object> getGoodsPriceData(GoodsPrice goodsPrice){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", goodsPrice.getId());
		params.put("goodsid", goodsPrice.getGoodsid());
		params.put("price", goodsPrice.getPrice());
		params.put("oriprice", goodsPrice.getOriprice());
		params.put("remark", goodsPrice.getRemark());
		return params;
	}
}
