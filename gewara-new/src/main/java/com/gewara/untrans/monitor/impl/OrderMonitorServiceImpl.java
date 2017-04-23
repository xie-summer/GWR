package com.gewara.untrans.monitor.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.constant.ticket.OrderProcessConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.untrans.hbase.HbaseData;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.util.DateUtil;

@Service("orderMonitorService")
public class OrderMonitorServiceImpl implements OrderMonitorService{
	@Autowired
	private MonitorService monitorService;
	@Override
	public void addOrderChangeLog(String tradeNo, String action, GewaOrder order, String otherinfo) {
		Map<String, String> info = new HashMap<String, String>();
		if (order != null) {
			info.put("orderid", "" + order.getId());
			info.put("membername", order.getMembername());
			info.put("memberid", ""+order.getMemberid());
			info.put("paymethod", order.getPaymethod());
			info.put("paybank", order.getPaybank());
			if(StringUtils.isNotBlank(order.getPayseqno())) {
				info.put("payseqno", order.getPayseqno());
			}
		}
		if (StringUtils.isNotBlank(otherinfo)){
			info.put("otherinfo", otherinfo);
		}
		addOrderChangeLog(tradeNo, action, info, null);
	}
	
	@Override
	public void addOrderChangeLog(String tradeNo, String action, String otherinfo, Long userid) {
		Map<String, String> info = new HashMap<String, String>();
		if (StringUtils.isNotBlank(otherinfo)){
			info.put("otherinfo", otherinfo);
		}
		
		addOrderChangeLog(tradeNo, action, info, userid);
	}
	@Override
	public void addOrderChangeLog(String tradeNo, String action, Map<String, String> info, Long userid) {
		if(info==null){
			info = new HashMap<String, String>();
		}
		info.put("tradeNo", tradeNo);
		info.put("action", action);
		info.put("addtime", DateUtil.formatTimestamp(System.currentTimeMillis()));
		if(userid!=null) info.put("userid", ""+userid);
		MapMonitorEntry entry = new MapMonitorEntry(HbaseData.TABLE_GEWAORDER, tradeNo.getBytes(), info);
		monitorService.addMonitorEntry(entry);
	}
	@Override
	public void addOrderPayCallback(String tradeNo, String returnType, String paymethod, String otherinfo){
		Map<String, String> info = new HashMap<String, String>();
		info.put("paymethod", paymethod);	//
		info.put("returnType", returnType);
		if(StringUtils.isNotBlank(otherinfo)){
			info.put("otherinfo", otherinfo);
		}
		addOrderChangeLog(tradeNo, OrderProcessConstant.ACTION_PAYCALLBACK, info, null);
	}

}
