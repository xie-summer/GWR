package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.BaseObject;

public class BaseOrderExtra extends BaseObject {
	private static final long serialVersionUID = 4821814105905969549L;
	public static final String LEVEL_INIT = "init";
	public static final String LEVEL_MAIN = "main";
	public static final String LEVEL_FINISH = "finish";
	
	protected Long id;					//订单ID
	protected String tradeno;			//订单号
	protected String status;			//订单状态gewOrder ---> status
	protected Timestamp addtime;		//下单时间
	protected Timestamp updatetime;		//更新时间
	protected String invoice;			//是否可开发票,Y已开,N可开,F不可开
	protected String pretype;			//代售E、主营M
	protected Long memberid;			//用户ID
	protected Long partnerid;			//合作商ID
	protected String ordertype;			//订单类型
	protected String expressnote;		//快递单号
	protected String expresstype;		//快递类型
	protected String processLevel;		//后期处理进程
	protected String expressStatus;		//快递状态
	protected String dealStatus;		//处理状态
	protected Long dealUser;			//处理用户
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getAddtime() {
		return addtime;
	}
	
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getInvoice() {
		return invoice;
	}

	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}
	
	public String getPretype() {
		return pretype;
	}

	public void setPretype(String pretype) {
		this.pretype = pretype;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Long getPartnerid() {
		return partnerid;
	}

	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}

	public String getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}

	public String getExpressnote() {
		return expressnote;
	}

	public void setExpressnote(String expressnote) {
		this.expressnote = expressnote;
	}

	public String getExpresstype() {
		return expresstype;
	}

	public void setExpresstype(String expresstype) {
		this.expresstype = expresstype;
	}

	public boolean hasExpressType(String type){
		return StringUtils.equals(this.expresstype, type);
	}
	
	public boolean hasPaidSuccess(){
		return StringUtils.equals(this.status, OrderConstant.STATUS_PAID_SUCCESS);
	}
	
	@Override
	public Serializable realId(){
		return id;
	}

	public String getProcessLevel() {
		return processLevel;
	}

	public void setProcessLevel(String processLevel) {
		this.processLevel = processLevel;
	}

	public String getExpressStatus() {
		return expressStatus;
	}

	public void setExpressStatus(String expressStatus) {
		this.expressStatus = expressStatus;
	}

	public String getDealStatus() {
		return dealStatus;
	}

	public void setDealStatus(String dealStatus) {
		this.dealStatus = dealStatus;
	}

	public Long getDealUser() {
		return dealUser;
	}

	public void setDealUser(Long dealUser) {
		this.dealUser = dealUser;
	}
}
