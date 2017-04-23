package com.gewara.untrans.monitor.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.helper.sys.MonitorHelper;
import com.gewara.untrans.hbase.ChangeLogService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.untrans.hbase.HbaseData;
import com.gewara.untrans.monitor.MonitorEntry;
import com.gewara.untrans.monitor.MonitorService;

@Service("monitorService")
public class HbaseMonitorServiceImpl extends AbstractMonitorService implements MonitorService, InitializingBean{
	@Autowired@Qualifier("hbaseService")
	private HBaseService hbaseService;
	@Autowired@Qualifier("changeLogService")
	private ChangeLogService changeLogService;
	private boolean useHbase = false;
	@Override
	public void addMonitorEntry(String datatype, Map<String, String> entry) {
		if(useHbase){
			executor.execute(new HbaseWorker(new MapMonitorEntry(datatype, entry)));
		}else{
			dbLogger.warnMap(entry);
		}
	}
	@Override
	public void addMonitorEntry(MonitorEntry entry) {
		if(useHbase){
			executor.execute(new HbaseWorker(entry));
		}else{
			dbLogger.warnMap(entry.getDataMap());
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		setupConsumerThread(4);
		useHbase = true;
	}

	private class HbaseWorker implements Runnable {
		private MonitorEntry entry;
		public HbaseWorker(MonitorEntry entry){
			this.entry = entry;
		}
		@Override
		public void run() {
			try{
				String tag = entry.getDataMap().get("tag");
				String relatedid = entry.getDataMap().get("relatedid");
				String tablename = MonitorHelper.getTable(entry.getDatatype());
				if(StringUtils.isBlank(tablename)){
					tablename = entry.getDatatype();
				}
				if(StringUtils.equals(tablename, HbaseData.TABLE_CHANGEHIS) && 
						StringUtils.isNotBlank(tag) && StringUtils.isNotBlank(relatedid)){
					changeLogService.addChangeLog(Config.SYSTEMID, tag, relatedid, entry.getDataMap());
				}else{
					if(entry.getRowid()!=null){
						hbaseService.saveRow(tablename, entry.getRowid(), entry.getDataMap());	
					}else{
						hbaseService.saveRow(tablename, entry.getDataMap());
					}
				}
			}catch(Exception e){
				dbLogger.warn("", e);
			}
		}
	}

	public boolean isUseHbase() {
		return useHbase;
	}
	public void setUseHbase(boolean useHbase) {
		this.useHbase = useHbase;
	}

}
