package com.gewara.model.pay;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public class SportOrder extends GewaOrder {
	private static final long serialVersionUID = -4810261064815316992L;
	public static final String SPORT_CONFIRM = "sportconfirm";
	private Long ottid;
	private Long sportid;
	private Long itemid;
	private Long cardid;
	private Integer costprice;
	public SportOrder(){
	}
	public SportOrder(Long memberId, String membername, OpenTimeTable ott, String ukey){
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.addtime = createtime;
		this.updatetime = createtime;
		this.modifytime = this.createtime;
		this.paymethod = PaymethodConstant.PAYMETHOD_PNRPAY;			//默认网银
		this.validtime = DateUtil.addDay(this.addtime, 1);	//默认一天
		this.status = OrderConstant.STATUS_NEW_UNLOCK;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_SPORT;
		this.alipaid = 0;
		this.gewapaid = 0;
		this.discount = 0;
		this.wabi = 0;
		this.memberid = memberId;
		this.membername = membername;
		this.ottid = ott.getId();
		this.sportid = ott.getSportid();
		this.citycode = ott.getCitycode();
		this.itemid = ott.getItemid();
		this.ukey = ukey;
		this.itemfee = 0;
		this.otherfee = 0;
		this.settle = OrderConstant.SETTLE_NONE;
		this.express = Status.N;
	}
	
	public Long getSportid() {
		return sportid;
	}
	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}
	public Long getItemid() {
		return itemid;
	}
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	
	@Override
	public String getOrdertype() {
		return "sport";
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	
	
	public Long getOttid() {
		return ottid;
	}
	public void setOttid(Long ottid) {
		this.ottid = ottid;
	}
	

	public Long getMpid() {
		return ottid;
	}
	public boolean assertConfirmed() {
		String result = JsonUtils.getJsonValueByKey(otherinfo, SPORT_CONFIRM);
		return StringUtils.equals(result, "Y");
	}	
	public Integer getSumcost(){
		Map<String, String> infoMap = JsonUtils.readJsonToMap(otherinfo);
		String strcost = infoMap.get("sumcost");
		if(StringUtils.isNotBlank(strcost)) return Integer.valueOf(strcost);
		return quantity*costprice;
	}
	public Long getCardid() {
		return cardid;
	}
	public void setCardid(Long cardid) {
		this.cardid = cardid;
	}
	public boolean hasMemberCardPay(){
		return StringUtils.equals(paymethod, PaymethodConstant.PAYMETHOD_MEMBERCARDPAY);
	}
}
