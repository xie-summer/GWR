package com.gewara.model.sport;

import com.gewara.model.common.BaseSettle;
import com.gewara.model.pay.SettleConfig;
import com.gewara.util.DateUtil;

public class SportSettle extends BaseSettle {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6807328863149033834L;

	private Long sportid;
	private Long itemid;
	
	public SportSettle(){}
	
	public SportSettle(Long sportid, Long itemid, SettleConfig settle){
		this.sportid = sportid;
		this.itemid = itemid;
		this.settle = settle;
		this.addtime = DateUtil.getCurFullTimestamp();
	}
	
	public Long getSportid() {
		return sportid;
	}
	
	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}
	
	public Long getItemid() {
		return itemid;
	}
	
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	@Override
	public String getSettletype() {
		return "sport";
	}

}
