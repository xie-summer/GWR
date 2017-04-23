package com.gewara.model.pay;

import java.sql.Timestamp;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.sport.MemberCardType;
import com.gewara.util.DateUtil;

public class MemberCardOrder extends GewaOrder{
	private static final long serialVersionUID = -5785636832203327207L;
	private Long mctid;
	private Long placeid;
	private Integer costprice;
	private Long cardid;
	public MemberCardOrder(){
		
	}
	public MemberCardOrder(MemberCardType mct, Long memberId, String membername, String ukey){
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.addtime = createtime;
		this.updatetime = createtime;
		this.modifytime = this.createtime;
		this.paymethod = PaymethodConstant.PAYMETHOD_PNRPAY;			
		this.validtime = DateUtil.addMinute(this.addtime, 15);	
		this.status = OrderConstant.STATUS_NEW;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_SPORT;
		this.alipaid = 0;
		this.gewapaid = 0;
		this.discount = 0;
		this.wabi = 0;
		this.memberid = memberId;
		this.membername = membername;
		this.mctid = mct.getId();
		this.ukey = ukey;
		this.itemfee = 0;
		this.otherfee = 0;
		this.quantity = 1;
		this.settle = OrderConstant.SETTLE_NONE;
		this.express = Status.N;
	}
	@Override
	public String getOrdertype() {
		return "membercard";
	}
	public Long getMctid() {
		return mctid;
	}
	public void setMctid(Long mctid) {
		this.mctid = mctid;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}
	public Long getCardid() {
		return cardid;
	}
	public void setCardid(Long cardid) {
		this.cardid = cardid;
	}

}
