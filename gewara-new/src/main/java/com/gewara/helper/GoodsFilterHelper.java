package com.gewara.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.goods.Goods;

public class GoodsFilterHelper {
	public static void goodsFilter(List<Goods> goodsList, Long partnerid){
		if(goodsList==null || partnerid==null) return;
		List<Goods> removeList = new ArrayList<Goods>();
		for(Goods goods : goodsList){
			if(StringUtils.isNotBlank(goods.getPartners())){
				List<String> partneridList = Arrays.asList(goods.getPartners().split(","));
				if(!partneridList.contains(partnerid+"")) removeList.add(goods);
			}
		}
		goodsList.removeAll(removeList);
	}
}
