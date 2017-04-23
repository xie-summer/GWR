package com.gewara.model.pay;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
public class TicketOrder extends GewaOrder{
	private static final long serialVersionUID = -84016418448009250L;
	private Long mpid;				//关联场次
	private Long cinemaid;			//关联影院
	private Long movieid;			//关联影片
	private String hfhpass;
	private Integer costprice;		//成本价
	public String getHfhpass() {
		return hfhpass;
	}
	public void setHfhpass(String hfhpass) {
		this.hfhpass = hfhpass;
	}
	
	public TicketOrder() {}
	
	public TicketOrder(Long memberId){
		this.version = 0;
		this.itemfee = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.memberid = memberId;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_MOVIE;
		this.otherinfo = "{}";
		this.settle = OrderConstant.SETTLE_NONE;
	}
	public TicketOrder(Long memberId, String membername, String ukey, OpenPlayItem opi){
		this.version = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = this.createtime;
		this.addtime = this.createtime;
		this.modifytime = this.createtime;
		this.paymethod = PaymethodConstant.PAYMETHOD_PNRPAY;			//默认PNR
		this.validtime = DateUtil.addDay(this.addtime, 1);	//默认一天
		this.status = OrderConstant.STATUS_NEW_UNLOCK;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_MOVIE;
		this.alipaid = 0;
		this.gewapaid = 0;
		this.discount = 0;
		this.memberid = memberId;
		this.membername = membername;
		this.mpid = opi.getMpid();
		this.cinemaid = opi.getCinemaid();
		this.citycode = opi.getCitycode();
		this.movieid = opi.getMovieid();
		this.playtime = opi.getPlaytime();
		this.ukey = ukey;
		this.itemfee = 0;
		this.otherinfo = "{}";
		this.settle = OrderConstant.SETTLE_NONE;
		this.express = Status.N;
	}
	public Long getMpid() {
		return mpid;
	}
	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}
	public Long getCinemaid() {
		return cinemaid;
	}
	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
	public String getOrdertype(){
		return "ticket";
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Long getMovieid() {
		return movieid;
	}
	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}
	public boolean needChangeSeat() {
		if(StringUtils.contains(changehis, OrderConstant.CHANGEHIS_KEY_CHANGESEAT)) return false;
		String processtimes = JsonUtils.getJsonValueByKey(changehis, OrderConstant.CHANGEHIS_KEY_PROCESSTIMES);
		if(StringUtils.isBlank(processtimes) || Integer.parseInt(processtimes)<5) return false;
		return true;
	}
	public String getFullPaymethod() {
		return paymethod + (StringUtils.isBlank(paybank)?"" : ":" + paybank);
	}
	public String gainSeatTextFromDesc() {
		return JsonUtils.getJsonValueByKey(description2, "影票");
	}
	public boolean hasUnlock() {
		return status.equals(OrderConstant.STATUS_NEW_UNLOCK);
	}
}
