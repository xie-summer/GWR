package com.gewara.helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.drama.DisQuantity;
import com.gewara.util.DateUtil;

public class DisQuanHelper {
	private List<DisQuantity> discountList;
	public DisQuanHelper(List<DisQuantity> discountList){
		this.discountList = discountList;
	}
	
	public DisQuantity getDisByQuantity(Integer squantity){
		for(DisQuantity quan : discountList){
			if(StringUtils.equals(quan.getQuantity()+"", squantity+"")) return quan;
		}
		return null;
	}
	public String getDisInfo(){
		String result = "";
		for(DisQuantity dq : getValidDiscountList()){
			result = result + "," +dq.getQuantity() + "уе" + dq.getPrice() + "т╙";
		}
		if(StringUtils.isNotBlank(result)) result = result.substring(1);
		return result;
	}

	public List<DisQuantity> getValidDiscountList(){
		List<DisQuantity> tmpList = new ArrayList<DisQuantity>();
		Timestamp cur = DateUtil.getCurFullTimestamp();
		for (DisQuantity disQuantity : discountList) {
			if(disQuantity.hasStatus(Status.Y) && disQuantity.getEndtime().after(cur)){
				tmpList.add(disQuantity);
			}
		}
		return tmpList;
	}
	
	public List<DisQuantity> getDiscountList() {
		return discountList;
	}

	public void setDiscountList(List<DisQuantity> discountList) {
		this.discountList = discountList;
	}
}
