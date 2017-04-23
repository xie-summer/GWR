package com.gewara.untrans.monitor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.support.ErrorCode;
import com.gewara.support.magent.MessageCommand;
import com.gewara.support.magent.MessageCommandCenter;
import com.gewara.support.magent.MessageCommandGroup;
import com.gewara.untrans.monitor.MemberMonitorService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.support.DynamicStats;
import com.gewara.web.support.DynamicStats.LogCounter;

@Service("memberMonitorService")
public class MemberMonitorServiceImpl implements MemberMonitorService, InitializingBean, MessageCommandGroup{
	private GewaLogger dbLogger = LoggerUtils.getLogger(this.getClass(), Config.getServerIp(), Config.SYSTEMID);
	private Map<String, DynamicStats> monitorTypeMap = new ConcurrentHashMap<String, DynamicStats>();
	private Map<String/*memberid*/, Long/*time*/> disableMap = new ConcurrentHashMap<String, Long>();
	private long disableTime = 5*DateUtil.m_minute;
	private int log_thredhold  = 40;	//单机40
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	@Override
	public ErrorCode increament(Long memberid, CountType type, HttpServletRequest request) {
		if(memberid==null) return ErrorCode.SUCCESS;
		DynamicStats stats = monitorTypeMap.get(type.name());
		LogCounter result = stats.incrementCount(memberid.toString());
		int count = result.getCount();
		Long lasttime = null;
		if(count >= log_thredhold){
			Map<String, String> entry = WebUtils.getRequestMap(request);
			WebUtils.removeSensitiveInfo(entry);
			entry.put("tag", "mpistats");
			entry.put("category", "request");
			entry.put("memberid", memberid.toString());
			entry.put("ip", WebUtils.getRemoteIp(request));
			monitorService.addSysLog(SysLogType.monitor, entry);
			if(count % log_thredhold==0){
				lasttime = System.currentTimeMillis() + disableTime;
				disableMap.put(memberid.toString(), lasttime);
			}else{
				lasttime = disableMap.get(memberid);
			}	
		}
		
		if(lasttime!=null && System.currentTimeMillis() < lasttime){
			result.increamentCount2();//禁用次数
			return ErrorCode.getFailure("您操作过于频繁，请先信息一会吧");
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		for(CountType type: CountType.values()){
			monitorTypeMap.put(type.name(), new DynamicStats("member"));
		}
		MessageCommandCenter.getDefaultInstance().registerGroup(this);
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				try{
					long cur = System.currentTimeMillis();
					long timeBefore = cur - DateUtil.m_hour * 4;
					for(DynamicStats stats: monitorTypeMap.values()){
						List<Map> rowList = stats.removeCountBefore(timeBefore);
						for(Map row: rowList){
							int count = (Integer) row.get("count");
							if((count > log_thredhold)){
								row.put("tag", "mstats");
								row.put("category", stats.getType());
								monitorService.addSysLog(SysLogType.monitor, BeanUtil.toSimpleStringMap(row));
							}
						}
					}
				}catch(Exception e){
					dbLogger.error("", e);
				}
			}
		}, DateUtil.m_hour*4, DateUtil.m_minute * 37);
	}
	@Override
	public String getGroupName() {
		return "mstats";
	}
	@Override
	public List<MessageCommand> getCommandList() {
		List<MessageCommand> result = new ArrayList<MessageCommand>();
		result.add(new MessageCommand(){
			@Override
			public String getName() {
				return "mpiStats";
			}

			@Override
			public String getGroup() {
				return "mstats";
			}

			@Override
			public String getHelp() {
				return "[calltimes]统计用户打开座位图次数";
			}

			@Override
			public String getReply(String[] cmd) {
				DynamicStats stats = monitorTypeMap.get(CountType.getSeat.name());
				int mincount = 30;
				if(cmd.length>1){
					mincount = Integer.valueOf(cmd[1]);
				}
				List<Map> rowList = stats.getCountList(mincount, false);
				if(rowList.isEmpty()) return MessageCommand.nodata;
				StringBuilder reply = new StringBuilder("\n").append(StringUtils.join(rowList.get(0).keySet(), "\t")).append("\n");
				for(Map row: rowList){
					reply.append(StringUtils.join(row.values(), "\t")).append("\n");
				}
				return reply.toString();
			}
			
		});
		return result;
	}
}
