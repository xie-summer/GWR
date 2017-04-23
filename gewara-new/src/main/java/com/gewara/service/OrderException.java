package com.gewara.service;

/**
 * @author acerge(acerge@163.com)
 * @since 3:48:57 PM Nov 23, 2009
 */
public class OrderException extends Exception {
	private static final long serialVersionUID = -7147976717398138894L;
	private String msg;
	private String code;
	private String detailMsg;
	public OrderException(String code, String msg){
		super(msg);
		this.code = code;
		this.msg = msg;
	}
	public OrderException(String code, String msg, String detailMsg){
		this(code, msg);
		this.detailMsg = detailMsg;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDetailMsg() {
		return detailMsg;
	}
	public void setDetailMsg(String detailMsg) {
		this.detailMsg = detailMsg;
	}
}
