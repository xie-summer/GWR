package com.gewara.helper;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.goods.GoodsDisQuantity;

public class GoodsDisQuanHelper {
	private List<GoodsDisQuantity> discountList;
	public GoodsDisQuanHelper(List<GoodsDisQuantity> discountList){
		this.discountList = discountList;
	}
	
	public GoodsDisQuantity getDisByQuantity(Integer squantity){
		for(GoodsDisQuantity quan : discountList){
			if(StringUtils.equals(quan.getQuantity()+"", squantity+"")) return quan;
		}
		return null;
	}
	public String getDisInfo(){
		String result = "";
		for(GoodsDisQuantity dq : discountList){
			result = result + "," +dq.getQuantity() + "уе" + dq.getPrice() + "т╙";
		}
		if(StringUtils.isNotBlank(result)) result = result.substring(1);
		return result;
	}

	public List<GoodsDisQuantity> getDiscountList() {
		return discountList;
	}

	public void setDiscountList(List<GoodsDisQuantity> discountList) {
		this.discountList = discountList;
	}
}
