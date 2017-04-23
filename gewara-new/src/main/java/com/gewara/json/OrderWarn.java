package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;

public class OrderWarn implements Serializable{
	private static final long serialVersionUID = -7456534355848744653L;
	public static final String TYPE_REPEAT = "repeat";
	private String id;
	private String tradeno;
	private String paymethod1;
	private String paymethod2;
	private String adddate;
	private String addtime;
	private Integer due;
	private Integer alipaid;
	private String type;
	private String status;
	private String auser;		//accept接受此任务的人
	private String fuser;		//fixed完成此任务的人
	
	public OrderWarn(){
		this.id = StringUtil.getRandomString(5) + System.currentTimeMillis();
		this.status = Status.N;
		this.adddate = DateUtil.formatDate(new Date());
		this.addtime = DateUtil.formatTimestamp(new Date());
	}
	
	public OrderWarn(GewaOrder order, String paymethod, Integer alipaid){
		this();
		this.type = TYPE_REPEAT;
		this.tradeno = order.getTradeNo();
		this.paymethod1 = order.getPaymethod();
		this.paymethod2 = paymethod;
		this.due = order.getDue();
		this.alipaid = alipaid;
		this.addtime = DateUtil.formatTimestamp(order.getAddtime());
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	public String getPaymethod1() {
		return paymethod1;
	}

	public void setPaymethod1(String paymethod1) {
		this.paymethod1 = paymethod1;
	}

	public String getPaymethod2() {
		return paymethod2;
	}

	public void setPaymethod2(String paymethod2) {
		this.paymethod2 = paymethod2;
	}

	public String getAdddate() {
		return adddate;
	}

	public void setAdddate(String adddate) {
		this.adddate = adddate;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

	public Integer getDue() {
		return due;
	}

	public void setDue(Integer due) {
		this.due = due;
	}

	public Integer getAlipaid() {
		return alipaid;
	}

	public void setAlipaid(Integer alipaid) {
		this.alipaid = alipaid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAuser() {
		return auser;
	}
	public void setAuser(String auser) {
		this.auser = auser;
	}
	public String getFuser() {
		return fuser;
	}
	public void setFuser(String fuser) {
		this.fuser = fuser;
	}
	public void addAccept(String user) {
		if(StringUtils.isBlank(auser)) auser = user;
		else auser += user;
	}
	public void addFixed(String user) {
		if(StringUtils.isBlank(fuser)) fuser = user;
		else fuser += user;
	}
}
