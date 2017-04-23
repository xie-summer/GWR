package com.gewara.job.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.gewara.job.GewaJob;
import com.gewara.job.JobService;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.TspSaleCount;
import com.gewara.service.DaoService;
import com.gewara.untrans.drama.OdiOpenService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.MonitorService.EXCEPTION_TAG;
import com.gewara.util.DateUtil;

public class TheatreSeatPriceJobImpl extends JobService {
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired@Qualifier("odiOpenService")
	private OdiOpenService odiOpenService;
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public void updatePriceAllownum(){
		dbLogger.warn("update TheatreSeatPrice allownum start....");
		try{
			List<Serializable> idList = daoService.getObjectPropertyList(TspSaleCount.class, "dpid", true);
			int updateError = 0;
			Timestamp cur = DateUtil.getCurFullTimestamp();
			List<Serializable> removeIdList = new ArrayList<Serializable>();
			for (Serializable id : idList) {
				try{
					OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", id, true);
					if(odi != null){
						if(odi.getEndtime().after(cur)){
							odiOpenService.asynchUpdateAreaStats(odi);
						}else{
							removeIdList.add(id);
						}
					}else{
						removeIdList.add(id);
					}
				}catch(Exception e){
					monitorService.logException(EXCEPTION_TAG.JOB, "updatePriceAllownum", "TheatreSeatPriceJobImpl", e, null);
					dbLogger.warn("update TheatreSeatPrice", e);
					updateError ++;
				}
			}
			if(!removeIdList.isEmpty()){
				String hql = "delete from TspSaleCount where dpid in (?"+ StringUtils.repeat(",?", removeIdList.size() - 1) + ")";
				int updated = hibernateTemplate.bulkUpdate(hql, removeIdList.toArray());
				dbLogger.warn("updatePriceAllownum removeTspSaleCount: " + StringUtils.join(removeIdList, ",") + ", updated:" + updated);
			}
			dbLogger.warnWithType(GewaJob.LOG_TYPE_JOB, "Update TheatreSeatPrice:" + idList.size() + ",error:" + updateError);
		}catch(Exception e){
			dbLogger.warn("update TheatreSeatPrice", e);
		}
	}
}
