package com.gewara.untrans.spider.impl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.model.common.JsonData;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.service.DaoService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.TicketSynchService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.spider.RemoteSpiderService;
import com.gewara.untrans.spider.SpiderOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.xmlbind.ticket.SynchPlayItem;

@Service("spiderOperationService")
public class SpiderOperationServiceImpl implements SpiderOperationService {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	
	@Autowired@Qualifier("remoteSpiderService")
	private RemoteSpiderService remoteSpiderService;
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("ticketSynchService")
	private TicketSynchService ticketSynchService;
	
	@Override
	public void updateMoviePlayItem(UpdateMpiContainer container, Long cinemaid, List<String> msgList, int notUpdateWithMin){
		if(cinemaid==null) throw new IllegalArgumentException("影院ID不能为空！");
		String jsonKey = JsonDataKey.KEY_SYNCH_SPIDER_MOVIEPLAYITEM + "_" + cinemaid;
		JsonData jsonData = daoService.getObject(JsonData.class, jsonKey);
		Timestamp updatetime = null;
		Timestamp cur = DateUtil.getCurFullTimestamp();
		if(jsonData == null){
			jsonData = new JsonData(jsonKey);
			updatetime = cur;
			jsonData.setTag("synch");
			jsonData.setValidtime(DateUtil.parseTimestamp("2022-01-01 00:00:00"));
		}else{
			Map<String, String> dataMap = JsonUtils.readJsonToMap(jsonData.getData());
			updatetime = DateUtil.parseTimestamp(dataMap.get("updatetime"));
		}
		
		if(updatetime.compareTo(DateUtil.addMinute(cur, notUpdateWithMin)) < 0){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "在" + notUpdateWithMin + "前已经更新，本次忽略！");
		}
		ErrorCode<List<SynchPlayItem>> code = remoteSpiderService.getRemotePlayItemListByUpdatetime(updatetime, cinemaid);
		if(!code.isSuccess()){
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "更新排片：" + code.getMsg());
			msgList.add("更新排片错误：" + code.getMsg() + ", code:" + code.getErrcode() + ", res:" + code.getRetval());
			return;
		}
		List<SynchPlayItem> synchPlayItemList = code.getRetval();
		List<Long> idList = BeanUtil.getBeanPropertyList(synchPlayItemList, Long.class, "cinemaid", true);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, idList);
		Map<Long, Cinema> cinemaMap = BeanUtil.beanListToMap(cinemaList, "id");
		Date curDate = DateUtil.getCurDate();
		for (SynchPlayItem synchPlayItem : synchPlayItemList) {
			Cinema cinema = cinemaMap.get(synchPlayItem.getCinemaid());
			if(synchPlayItem.getPlaytime().after(curDate)){//只有当前时间之后的更新才下载
				MoviePlayItem mpi = mcpService.getMpiBySeqNo(synchPlayItem.getMpiseq());
				ticketSynchService.updateSpiderPlayItem(synchPlayItem, mpi, cinema,msgList);
			}
		}
		Map<String, String> dataMap = JsonUtils.readJsonToMap(jsonData.getData());
		dataMap.put("updatetime", DateUtil.format(DateUtil.addMinute(cur,-20), "yyyy-MM-dd HH:mm:ss"));
		jsonData.setData(JsonUtils.writeObjectToJson(dataMap));
		daoService.saveObject(jsonData);
	}
}
