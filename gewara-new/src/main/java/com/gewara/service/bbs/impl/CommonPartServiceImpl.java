package com.gewara.service.bbs.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.user.Festival;
import com.gewara.service.bbs.CommonPartService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

@Service("commonPartService")
public class CommonPartServiceImpl extends BaseServiceImpl implements CommonPartService {

	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public Festival getCurFestival(Date date) {
		DetachedCriteria query = DetachedCriteria.forClass(Festival.class);
		query.add(Restrictions.eq("festdate", date));
		List<Festival> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}
	
	@Override
	public Festival getNextFestival(Date date){
		DetachedCriteria query = DetachedCriteria.forClass(Festival.class);
		query.add(Restrictions.gt("festdate", date));
		query.addOrder(Order.asc("festdate"));
		List<Festival> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}


}
