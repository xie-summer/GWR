package com.gewara.model.pay;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class PayBank extends BaseObject{
	private static final long serialVersionUID = -6637674809313295608L;
	public static final String TYPE_PC = "PC";
	public static final String TYPE_WAP = "WAP";
	public static final String TYPE_MOBILE = "MOBILE";
	
	private Long id;
	private String name;		//英文简称
	private String citycode;	//城市代码
	private String paymethod;	//完整支付方式：pnrPay:25
	private Integer sortnum;	//排序
	private String banktype;	//应用类型：手机、PC
	private String cnname;		//中文名称
	public PayBank(){
		
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public Integer getSortnum() {
		return sortnum;
	}
	public void setSortnum(Integer sortnum) {
		this.sortnum = sortnum;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getBanktype() {
		return banktype;
	}
	public void setBanktype(String banktype) {
		this.banktype = banktype;
	}
	public String getCnname() {
		return cnname;
	}
	public void setCnname(String cnname) {
		this.cnname = cnname;
	}
}