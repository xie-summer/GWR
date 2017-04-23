package com.gewara.job.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.job.JobService;
import com.gewara.service.ticket.TicketSynchService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.untrans.ticket.TicketOperationService;

public class TicketRemoteJobImpl extends JobService{
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("ticketSynchService")
	private TicketSynchService ticketSynchService;
	
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	@Autowired@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;

	
	public void updateMoviePlayItem(){
		String hql = "select id from CinemaProfile where opentype is not null";
		List<Long> cinemaidList = hibernateTemplate.find(hql);
		List<String> msgList = new ArrayList<String>();
		int insert = 0, update = 0, del = 0;
		for(Long cinemaid: cinemaidList){
			try{
				List<String> tmpList = new LinkedList<String>();
				UpdateMpiContainer container = new UpdateMpiContainer();
				ticketOperationService.updateMoviePlayItem(container, cinemaid, tmpList, 10);
				msgList.addAll(tmpList);
				insert += container.getInsertList().size();
				update += container.getUpdateList().size();
				del += container.getDelList().size();
				mpiOpenService.asynchAutoOpenMpiList(container.getInsertList());
			}catch(Exception e){
				monitorService.logException(EXCEPTION_TAG.JOB, "updateMoviePlayItem", "TicketRemoteJobImpl", e, null);
				msgList.add("更新排片有异常，请联系系统管理员，影院ID：" + cinemaid + "," + e.getMessage());
				dbLogger.error("更新排片有错误，影院ID：" + cinemaid, e);
			}
		}
		if(msgList.size() > 0){
			Map model = new HashMap();
			model.put("msgList", new LinkedHashSet<String>(msgList));
			monitorService.saveSysTemplateWarn("自动更新排片有错", "warn/msgmail.vm", model, RoleTag.dingpiao);
		}
		ticketSynchService.updateOpenPlayItem(new ArrayList<String>());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_JOB, "本次自动更新排片结束，新增：" + insert + ", 修改：" + update + ", 删除：" + del);
	}
}
