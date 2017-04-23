package com.gewara.xmlbind.partner;

import java.io.Serializable;


public class IBoxPay implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IBoxPayResult result;

	public IBoxPayResult getResult() {
		return this.result;
	}

	public void setResult(IBoxPayResult result) {
		this.result = result;
	}
	
	public void addresult(IBoxPayResult payResult) {
		this.result = payResult;
	}
	
}
