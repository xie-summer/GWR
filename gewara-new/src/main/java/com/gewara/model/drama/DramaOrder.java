package com.gewara.model.drama;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;


public class DramaOrder extends GewaOrder {
	private static final long serialVersionUID = -5464802368981872046L;
	private Long dpid; 			// 关联场次
	private Long areaid;
	private Long theatreid; 	// 关联话剧院
	private Long dramaid; 		// 关联话剧
	private Integer costprice; // 成本价
	
	public DramaOrder(){}
	
	public DramaOrder(Long memberid, String membername) {
		this.version = 0;
		this.itemfee = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.memberid = memberid;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_DRAMA;
		this.membername = membername;
		this.settle = OrderConstant.SETTLE_NONE;
		this.otherinfo = "{}";
	}
	public DramaOrder(Long memberId) {
		this.version = 0;
		this.itemfee = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.memberid = memberId;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_DRAMA;
		this.status = OrderConstant.STATUS_NEW_UNLOCK;
		this.otherinfo = "{}";
		this.settle = OrderConstant.SETTLE_NONE;
	}
	public DramaOrder(Long memberid, String membername, OpenDramaItem odi, Long partnerid,  String ukey) {
		this(memberid, membername);
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.addtime = this.createtime;
		this.updatetime = this.addtime;
		this.modifytime = this.createtime;
		this.validtime = DateUtil.addMinute(this.addtime, OdiConstant.MAX_MINUTS_TICKETS);
		this.paymethod = PaymethodConstant.PAYMETHOD_PNRPAY; // 默认网银
		this.status = OrderConstant.STATUS_NEW_UNLOCK;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_DRAMA;
		this.alipaid = 0;
		this.otherfee = 0;
		this.gewapaid = 0;
		this.discount = 0;
		this.wabi = 0;
		this.dpid = odi.getDpid();
		this.theatreid = odi.getTheatreid();
		this.dramaid = odi.getDramaid();
		this.citycode = odi.getCitycode();
		this.partnerid = partnerid;
		this.ukey = ukey;
		this.settle = OrderConstant.SETTLE_NONE;
		this.playtime = odi.getPlaytime();
		this.express = Status.N;
		this.otherinfo = "{}";
	}
	public DramaOrder(Long memberid, String membername, OpenDramaItem odi,  String ukey) {
		this(memberid, membername, odi, PartnerConstant.GEWA_SELF, ukey);
	}
	public Long getTheatreid() {
		return theatreid;
	}

	public Long getAreaid() {
		return areaid;
	}

	public void setAreaid(Long areaid) {
		this.areaid = areaid;
	}

	public void setTheatreid(Long theatreid) {
		this.theatreid = theatreid;
	}

	public Long getDramaid() {
		return dramaid;
	}

	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	@Override
	public String getOrdertype() {
		return "drama";
	}
	
	public Long getDpid() {
		return dpid;
	}

	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}
	public Integer getSumcost(){
		return this.quantity*this.costprice;
	}
	
	public boolean isSeatChanged() {
		return StringUtils.contains(changehis, OrderConstant.CHANGEHIS_KEY_CHANGESEAT);
	}
	
	public boolean needChangeSeat() {
		if(StringUtils.contains(changehis, OrderConstant.CHANGEHIS_KEY_CHANGESEAT)) return false;
		String processtimes = JsonUtils.getJsonValueByKey(changehis, OrderConstant.CHANGEHIS_KEY_PROCESSTIMES);
		if(StringUtils.isBlank(processtimes) || Integer.parseInt(processtimes)<5) return false;
		return true;
	}
}
