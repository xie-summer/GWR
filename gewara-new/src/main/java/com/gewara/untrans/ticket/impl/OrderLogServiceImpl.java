package com.gewara.untrans.ticket.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gewara.untrans.monitor.MonitorData;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.untrans.monitor.impl.MapMonitorEntry;
import com.gewara.untrans.ticket.OrderLogService;
import com.gewara.util.DateUtil;
@Service("orderLogService")
public class OrderLogServiceImpl implements OrderLogService{
	@Autowired
	private MonitorService monitorService;
	@Override
	public void addSysLog(String tradeNo, String paymethod, String action, Long userid){
		Map<String, String> logEntry = new HashMap<String, String>();
		logEntry.put("tradeNo", tradeNo);
		logEntry.put("paymethod", paymethod);
		logEntry.put("action", action);
		if(userid!=null) logEntry.put("userid", ""+userid);
		monitorService.addSysLog(SysLogType.order, logEntry);
	}
	@Override
	public void addOrderChangeLog(String tradeNo, String action, Map<String, String> info) {
		if(info==null){
			info = new HashMap<String, String>();
		}
		info.put("tradeNo", tradeNo);
		info.put("action", action);
		info.put("addtime", DateUtil.formatTimestamp(System.currentTimeMillis()));
		MapMonitorEntry entry = new MapMonitorEntry(MonitorData.DATATYPE_GEWAORDER, tradeNo.getBytes(), info);
		monitorService.addMonitorEntry(entry);
	}
}
