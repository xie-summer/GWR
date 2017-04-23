package com.gewara.xmlbind.pay;

public class QrySandOrder {

	private String ret_order;
	private String tradeno;
	private String order_num;
	private String order_stauts;
	private String order_date;
	private String sign;

	public String getRet_order() {
		return ret_order;
	}

	public void setRet_order(String ret_order) {
		this.ret_order = ret_order;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	public String getOrder_num() {
		return order_num;
	}

	public void setOrder_num(String order_num) {
		this.order_num = order_num;
	}

	public String getOrder_stauts() {
		return order_stauts;
	}

	public void setOrder_stauts(String order_stauts) {
		this.order_stauts = order_stauts;
	}

	public String getOrder_date() {
		return order_date;
	}

	public void setOrder_date(String order_date) {
		this.order_date = order_date;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
}
