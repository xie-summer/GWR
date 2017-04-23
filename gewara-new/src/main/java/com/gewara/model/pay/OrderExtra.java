package com.gewara.model.pay;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.OrderExtraConstant;

public class OrderExtra extends BaseOrderExtra {
	private static final long serialVersionUID = 2550978580963024444L;
	public OrderExtra(){}
	public OrderExtra(GewaOrder order){
		this.id = order.getId();
		this.tradeno = order.getTradeNo();
		this.memberid = order.getMemberid();
		this.partnerid = order.getPartnerid();
		this.ordertype = order.getOrdertype();
		this.addtime = order.getAddtime();
		this.invoice = OrderExtraConstant.INVOICE_N;
		this.pretype = OrderExtraConstant.PRETYPE_MANAGE;
		this.updatetime = this.addtime;
		this.status = OrderConstant.STATUS_PAID_SUCCESS;
		this.processLevel = LEVEL_INIT;//Œ¥¥¶¿Ì
		this.expressStatus = OrderExtraConstant.EXPRESS_STATUS_NEW;
		this.dealStatus = OrderExtraConstant.EXPRESS_STATUS_NEW;
	}

}
