package com.gewara.model.api;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;


public class OrderResult extends BaseObject {
	private static final long serialVersionUID = -6800394265547863600L;
	public static final String ORDERTYPE_TICKET = "ticket";//电影票
	public static final String ORDERTYPE_MEAL = "meal";//套餐
	public static final String ORDERTYPE_SPORT = "sport";//运动
	public static final String ORDERTYPE_DRAMA = "drama";//话剧
	public static final String ORDERTYPE_GYM = "gym";//健身
	public static final String ORDERTYPE_GOODS = "goods";//物品
	public static final String ORDERTYPE_BARCODE = "barcode";//订单产生的条形码
	
	public static final String RESULTU = "U";//强制同步所有 flag标记
	public static final String RESULTD = "D";//删除所有 flag标记
	
	private String tradeno;
	private Timestamp taketime;
	private String caption;//说明
	private String istake;
	private String conflict;//冲突
	private String result;
	private Integer ticketnum;
	private Timestamp updatetime;
	private String ordertype;
	private Long placeid;
	public String getOrdertype() {
		return ordertype;
	}
	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}
	public Integer getTicketnum() {
		return ticketnum;
	}
	public void setTicketnum(Integer ticketnum) {
		this.ticketnum = ticketnum;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getIstake() {
		return istake;
	}
	public void setIstake(String istake) {
		this.istake = istake;
	}
	public String getConflict() {
		return conflict;
	}
	public void setConflict(String conflict) {
		this.conflict = conflict;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public OrderResult(){}
	public OrderResult(String tradeno, Timestamp taketime){
		this.tradeno = tradeno;
		this.taketime = taketime;
		this.istake = "Y";
	}
	@Override
	public Serializable realId() {
		return tradeno;
	}
	public Timestamp getTaketime() {
		return taketime;
	}
	public void setTaketime(Timestamp taketime) {
		this.taketime = taketime;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}

}
