package com.gewara.model.pay;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class SpecialDiscountExtra extends BaseObject{
	private static final long serialVersionUID = -6156933926976349030L;
	private Long id;
	private String applycity;			//申请区域
	private String applydept;			//申请部门
	private String applytype;			//申请类型
	private String allowancetype;
	
	private Double orderAllowance;		//订单补贴
	private Double unitAllowance;		//单价补贴
	private Double maxAllowance;		//合作方所有订单总共最大补贴金额
	private String partnername;
	private Long partnerid;
	
	private Double orderAllowance1;		//订单补贴
	private Double unitAllowance1;		//单价补贴
	private Double maxAllowance1;		//合作方所有订单总共最大补贴金额
	private String partnername1;
	private Long partnerid1;
	
	private Double orderAllowance2;		//订单补贴
	private Double unitAllowance2;		//单价补贴
	private Double maxAllowance2;		//合作方所有订单总共最大补贴金额
	private String partnername2;
	private Long partnerid2;
	
	public SpecialDiscountExtra(){
		
	}
	public SpecialDiscountExtra(Long id){
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getApplycity() {
		return applycity;
	}
	public void setApplycity(String applycity) {
		this.applycity = applycity;
	}
	public String getApplydept() {
		return applydept;
	}
	public void setApplydept(String applydept) {
		this.applydept = applydept;
	}
	public String getApplytype() {
		return applytype;
	}
	public void setApplytype(String applytype) {
		this.applytype = applytype;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getAllowancetype() {
		return allowancetype;
	}
	public void setAllowancetype(String allowancetype) {
		this.allowancetype = allowancetype;
	}
	public Double getOrderAllowance() {
		return orderAllowance;
	}
	public void setOrderAllowance(Double orderAllowance) {
		this.orderAllowance = orderAllowance;
	}
	public Double getUnitAllowance() {
		return unitAllowance;
	}
	public void setUnitAllowance(Double unitAllowance) {
		this.unitAllowance = unitAllowance;
	}
	public Double getMaxAllowance() {
		return maxAllowance;
	}
	public void setMaxAllowance(Double maxAllowance) {
		this.maxAllowance = maxAllowance;
	}
	public String getPartnername() {
		return partnername;
	}
	public void setPartnername(String partnername) {
		this.partnername = partnername;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public Double getOrderAllowance1() {
		return orderAllowance1;
	}
	public void setOrderAllowance1(Double orderAllowance1) {
		this.orderAllowance1 = orderAllowance1;
	}
	public Double getUnitAllowance1() {
		return unitAllowance1;
	}
	public void setUnitAllowance1(Double unitAllowance1) {
		this.unitAllowance1 = unitAllowance1;
	}
	public Double getMaxAllowance1() {
		return maxAllowance1;
	}
	public void setMaxAllowance1(Double maxAllowance1) {
		this.maxAllowance1 = maxAllowance1;
	}
	public String getPartnername1() {
		return partnername1;
	}
	public void setPartnername1(String partnername1) {
		this.partnername1 = partnername1;
	}
	public Long getPartnerid1() {
		return partnerid1;
	}
	public void setPartnerid1(Long partnerid1) {
		this.partnerid1 = partnerid1;
	}
	public Double getOrderAllowance2() {
		return orderAllowance2;
	}
	public void setOrderAllowance2(Double orderAllowance2) {
		this.orderAllowance2 = orderAllowance2;
	}
	public Double getUnitAllowance2() {
		return unitAllowance2;
	}
	public void setUnitAllowance2(Double unitAllowance2) {
		this.unitAllowance2 = unitAllowance2;
	}
	public Double getMaxAllowance2() {
		return maxAllowance2;
	}
	public void setMaxAllowance2(Double maxAllowance2) {
		this.maxAllowance2 = maxAllowance2;
	}
	public String getPartnername2() {
		return partnername2;
	}
	public void setPartnername2(String partnername2) {
		this.partnername2 = partnername2;
	}
	public Long getPartnerid2() {
		return partnerid2;
	}
	public void setPartnerid2(Long partnerid2) {
		this.partnerid2 = partnerid2;
	}
}
