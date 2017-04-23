package com.gewara.helper.order;

import java.util.List;

import com.gewara.model.goods.BaseGoods;
import com.gewara.model.pay.GoodsOrder;

public class GoodsOrderContainer extends OrderContainer{
	
	private List<BaseGoods> baseGoodsList;
	public GoodsOrderContainer(GoodsOrder goodsOrder){
		this.order = goodsOrder;
	}
	public GoodsOrderContainer(GoodsOrder goodsOrder, List<BaseGoods> baseGoodsList){
		this.order = goodsOrder;
		this.baseGoodsList = baseGoodsList;
	}
	
	public GoodsOrder getGoodsOrder(){
		return (GoodsOrder) order;
	}

	public List<BaseGoods> getBaseGoodsList() {
		return baseGoodsList;
	}

	public void setBaseGoodsList(List<BaseGoods> baseGoodsList) {
		this.baseGoodsList = baseGoodsList;
	}
	
}
