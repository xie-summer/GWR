package com.gewara.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.model.machine.Machine;
import com.gewara.service.MachineService;

@Service("machineService")
public class MachineServiceImpl extends BaseServiceImpl implements MachineService {
	
	@Override
	public List<Machine> getGewaMachineList(String citycode, String machinenumber, String machinename, Long cinemaid, String linkmethod, 
			String machineowner, Integer ticketcount, String machinetype, String machinestatus, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Machine.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(machinenumber)) query.add(Restrictions.eq("machinenumber", machinenumber));
		if(StringUtils.isNotBlank(machinename)) query.add(Restrictions.eq("machinename", machinename));
		if(cinemaid!=null) query.add(Restrictions.eq("cinemaid", cinemaid));
		if(StringUtils.isNotBlank(linkmethod)) query.add(Restrictions.eq("linkmethod", linkmethod));
		if(StringUtils.isNotBlank(machineowner)) query.add(Restrictions.like("machineowner", machineowner, MatchMode.ANYWHERE));
		if(ticketcount!=null) query.add(Restrictions.eq("ticketcount", ticketcount));
		if(StringUtils.isNotBlank(machinetype)) query.add(Restrictions.eq("machinetype", machinetype));
		if(StringUtils.isNotBlank(machinestatus)) query.add(Restrictions.eq("machinestatus", machinestatus));
		query.addOrder(Order.desc("addtime"));
		List<Machine> gewaMaList=hibernateTemplate.findByCriteria(query, from, maxnum);
		return gewaMaList;
	}
	@Override
	public Integer gewaMachineCount(String citycode, String machinenumber, String machinename, Long cinemaid, String linkmethod, 
			String machineowner, Integer ticketcount, String machinetype, String machinestatus){
		DetachedCriteria query = DetachedCriteria.forClass(Machine.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(machinenumber)) query.add(Restrictions.eq("machinenumber", machinenumber));
		if(StringUtils.isNotBlank(machinename)) query.add(Restrictions.eq("machinename", machinename));
		if(cinemaid!=null) query.add(Restrictions.eq("cinemaid", cinemaid));
		if(StringUtils.isNotBlank(linkmethod)) query.add(Restrictions.eq("linkmethod", linkmethod));
		if(StringUtils.isNotBlank(machineowner)) query.add(Restrictions.like("machineowner", machineowner, MatchMode.ANYWHERE));
		if(ticketcount!=null) query.add(Restrictions.eq("ticketcount", ticketcount));
		if(StringUtils.isNotBlank(machinetype)) query.add(Restrictions.eq("machinetype", machinetype));
		if(StringUtils.isNotBlank(machinestatus)) query.add(Restrictions.eq("machinestatus", machinestatus));
		query.setProjection(Projections.rowCount());
		List<Machine> gewaMaList=hibernateTemplate.findByCriteria(query);
		if(gewaMaList.isEmpty()) return 0;
		return new Integer (gewaMaList.get(0)+"");
	}
	@Override
	public Integer getMaxMachineNumber(String machinename,String machineprefix) {
		String hql = "select max(replace(m.machinenumber,?,'')) from Machine m where m.machinename = ?";
		List list = hibernateTemplate.find(hql,machineprefix,machinename);
		if (list.isEmpty()) return 0;
		Object obj = list.get(0);
		if(obj== null) return 0;
		return Integer.parseInt(""+obj);
	}
}
