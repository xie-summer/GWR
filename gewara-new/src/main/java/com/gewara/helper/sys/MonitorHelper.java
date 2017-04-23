package com.gewara.helper.sys;

import java.util.HashMap;
import java.util.Map;

import com.gewara.untrans.hbase.HbaseData;
import com.gewara.untrans.monitor.MonitorData;

public class MonitorHelper {
	private static final Map<String, String> TABLE_MAP = new HashMap<String, String>();
	static{
		TABLE_MAP.put(MonitorData.DATATYPE_APILOG, HbaseData.TABLE_APILOG);
		TABLE_MAP.put(MonitorData.DATATYPE_LOGENTRY, HbaseData.TABLE_LOGENTRY);
		TABLE_MAP.put(MonitorData.DATATYPE_CHANGEHIS, HbaseData.TABLE_CHANGEHIS);
		TABLE_MAP.put(MonitorData.DATATYPE_APISTATS, HbaseData.TABLE_HFHLOG);
		TABLE_MAP.put(MonitorData.DATATYPE_SYSWARN, HbaseData.TABLE_SYSWARN);
		TABLE_MAP.put(MonitorData.DATATYPE_SYSLOG, HbaseData.TABLE_SYSLOG);
		TABLE_MAP.put(MonitorData.DATATYPE_MEMBERLOG, HbaseData.TABLE_MEMBERLOG);
		TABLE_MAP.put(MonitorData.DATATYPE_APICALL, HbaseData.TABLE_APICALL);
		//-------------------------------------------------------------------------
		TABLE_MAP.put("APPSOURCE", "appsource");
	}
	public static String getTable(String datatype){
		return TABLE_MAP.get(datatype);
	}
}
