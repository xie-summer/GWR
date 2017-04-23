package com.gewara.service.sport.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.model.sport.Guarantee;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.sport.GuaranteeService;
import com.gewara.util.ClassUtils;

@Service("guaranteeService")
public class GuaranteeServiceImpl extends BaseServiceImpl implements GuaranteeService {

	@Override
	public List<Guarantee> getGuaranteeList(String citycode, Timestamp starttime, Timestamp endtime, String status, String orderField, boolean asc, int from, int maxnum){
		DetachedCriteria query = queryCriteria(citycode, starttime, endtime, status);
		if(StringUtils.isNotBlank(orderField) && ClassUtils.hasMethod(Guarantee.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}else{
			if(asc){
				query.addOrder(Order.asc("addtime"));
			}else{
				query.addOrder(Order.desc("addtime"));
			}
		}
		List<Guarantee> guaranteeList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return guaranteeList;
	}
	
	@Override
	public Integer getGuaranteeCount(String citycode, Timestamp starttime, Timestamp endtime, String status){
		DetachedCriteria query = queryCriteria(citycode, starttime, endtime, status);
		query.setProjection(Projections.rowCount());
		List<Long> result = hibernateTemplate.findByCriteria(query, 0, 1);
		if(result.isEmpty()) return 0;
		return result.get(0).intValue();
	}
	
	private DetachedCriteria queryCriteria(String citycode, Timestamp starttime, Timestamp endtime, String status){
		DetachedCriteria query = DetachedCriteria.forClass(Guarantee.class, "g");
		if(StringUtils.isNotBlank(citycode)){
			query.add(Restrictions.eq("g.citycode", citycode));
		}
		if(starttime != null && endtime != null){
			query.add(Restrictions.between("g.addtime", starttime, endtime));
		}else if(starttime != null){
			query.add(Restrictions.ge("g.addtime", starttime));
		}else if(endtime != null){
			query.add(Restrictions.le("g.addtime", endtime));
		}
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("g.status", status));
		}
		return query;
	}
}
