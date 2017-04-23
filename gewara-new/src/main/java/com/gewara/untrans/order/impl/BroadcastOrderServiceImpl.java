package com.gewara.untrans.order.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.jms.JmsConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.order.BroadcastOrderService;

@Service("broadcastOrderService")
public class BroadcastOrderServiceImpl implements BroadcastOrderService{
	@Autowired@Qualifier("jmsService")
	private JmsService jmsService;
	@Override
	public void broadcastOrder(GewaOrder order) {
		jmsService.sendMsgToDst(JmsConstant.QUEUE_TERMINAL_ORDER, JmsConstant.TAG_ORDER, "tradeNo,orderType", order.getTradeNo(), order.getOrdertype());
	}
	@Override
	public void broadcastOrder(GewaOrder order, String flag, String flagval, Long userid) {
		jmsService.sendMsgToDst(JmsConstant.QUEUE_TERMINAL_ORDER, JmsConstant.TAG_ORDER, "tradeNo,orderType,flag,flagval", 
				order.getTradeNo(), order.getOrdertype(), flag, flagval, userid);
	}
	@Override
	public void broadcastBarcode(String tradenos, Long cinemaid, String randcode, String machineno) {
		jmsService.sendMsgToDst(JmsConstant.QUEUE_TERMINAL_ORDER, JmsConstant.TAG_TERMINALBARCODE, 
				"tradenos,cinemaid,randcode,machineno", tradenos, cinemaid, randcode, machineno);
	}
}
