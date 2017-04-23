package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.order.SettleConfigConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.pay.SettleConfig;
import com.gewara.util.VmBaseUtil;

public abstract class BaseSettle extends BaseObject{
	private static final long serialVersionUID = 6737480903216804323L;

	protected Long id;
	protected SettleConfig settle;
	protected String remark;
	protected Timestamp addtime;
	
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SettleConfig getSettle() {
		return settle;
	}

	public void setSettle(SettleConfig settle) {
		this.settle = settle;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	
	public Long getSettleid(){
		return this.getSettle().getId();
	}
	
	public Double getDiscount(){
		return this.getSettle().getDiscount();
	}
	
	public String gainSettleRemark(){
		String tmp = "";
		if(StringUtils.equals(this.getSettle().getDistype(), SettleConfigConstant.DISCOUNT_TYPE_UPRICE)){
			tmp = "按基价(减)" + this.getDiscount() + "结算";
		}else if(StringUtils.equals(this.getSettle().getDistype(), SettleConfigConstant.DISCOUNT_TYPE_PERCENT)){
			tmp = "按基价(乘)" + VmBaseUtil.formatPercent(this.getDiscount(), 100.0)+ "结算";
		}
		return tmp;
	}
	public abstract String getSettletype();
}
