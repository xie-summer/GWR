package com.gewara.helper.order;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class OrderOther {
	
	public static final String PAY_CARD = "ABD";
	public static final String PAY_DISCOUNT = "M";
	
	private String takemethod;		//(A 电子票  、E 快递  、 A,E 电子票+快递)
	private String expressid;		//快递方式
	private String elecard;
	private boolean openPointPay;
	private int minpoint;
	private int maxpoint;
	private boolean ewarning;		//是否包含电子票
	private boolean greetings;
	
	public String getTakemethod() {
		return takemethod;
	}
	public void setTakemethod(String takemethod) {
		this.takemethod = takemethod;
	}
	public boolean isExpress() {
		return StringUtils.isNotBlank(expressid);
	}
	public boolean isOpenCardPay() {
		return StringUtils.containsAny(this.elecard, PAY_CARD);
	}
	
	public boolean isOpenPointPay() {
		return openPointPay;
	}
	
	public boolean isDisCountPay(){
		return StringUtils.contains(this.elecard, PAY_DISCOUNT);
	}
	public void setOpenPointPay(boolean openPointPay) {
		this.openPointPay = openPointPay;
	}
	public int getMinpoint() {
		return minpoint;
	}
	public void setMinpoint(int minpoint) {
		this.minpoint = minpoint;
	}
	public int getMaxpoint() {
		return maxpoint;
	}
	public void setMaxpoint(int maxpoint) {
		this.maxpoint = maxpoint;
	}
	
	public String getExpressid() {
		return expressid;
	}
	public String getElecard() {
		return elecard;
	}
	public void setElecard(String elecard) {
		this.elecard = elecard;
	}
	public void setExpressid(String expressid) {
		this.expressid = expressid;
	}
	public boolean isEwarning() {
		return ewarning;
	}
	public void setEwarning(boolean ewarning) {
		this.ewarning = ewarning;
	}
	public boolean isGreetings() {
		return greetings;
	}
	public void setGreetings(boolean greetings) {
		this.greetings = greetings;
	}
	public boolean hasTakemethod(String... methods){
		if(ArrayUtils.isEmpty(methods)) return false;
		for (String method : methods) {
			if(!StringUtils.contains(this.takemethod, method)){
				return false;
			}
		}
		return true;
	}
	
	public void insertElecard(String ecard){
		if(isOpenCardPay() && isDisCountPay() || StringUtils.isBlank(ecard)){
			return;
		}
		if(StringUtils.isBlank(this.elecard)){
			this.elecard = ecard;
		}else if(StringUtils.equals(ecard, PAY_CARD)){
			if(!StringUtils.contains(this.elecard, ecard)){
				this.elecard = ecard + this.elecard;
			}
		}else if(StringUtils.equals(ecard,PAY_DISCOUNT)){
			if(!StringUtils.contains(this.elecard, ecard)){
				this.elecard += ecard;
			}
		}
	}
}
