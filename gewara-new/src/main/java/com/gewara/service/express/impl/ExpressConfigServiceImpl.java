package com.gewara.service.express.impl;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.model.express.ExpressConfig;
import com.gewara.model.express.ExpressProvince;
import com.gewara.service.express.ExpressConfigService;
import com.gewara.service.impl.BaseServiceImpl;

@Service("expressConfigService")
public class ExpressConfigServiceImpl extends BaseServiceImpl implements ExpressConfigService {
	
	
	@Override
	public List<ExpressConfig> getExpressConfigList(int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(ExpressConfig.class);
		query.addOrder(Order.desc("addtime"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}

	@Override
	public ExpressProvince getExpress(String expressid, String provincecode) {
		DetachedCriteria query = DetachedCriteria.forClass(ExpressProvince.class);
		query.add(Restrictions.eq("expressid", expressid));
		query.add(Restrictions.eq("provicecode", provincecode));
		List<ExpressProvince> expressFeeList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(expressFeeList.isEmpty()) return null;
		return expressFeeList.get(0);
	}

	@Override
	public List<ExpressProvince> getExpressList(String expressid, String... provincecode) {
		DetachedCriteria query = DetachedCriteria.forClass(ExpressProvince.class);
		query.add(Restrictions.eq("expressid", expressid));
		if(!ArrayUtils.isEmpty(provincecode)){
			query.add(Restrictions.in("provincecode", provincecode));
		}
		List<ExpressProvince> expressFeeList = hibernateTemplate.findByCriteria(query);
		return expressFeeList;
	}

	@Override
	public List<ExpressProvince> getExpressList(String expressid, List<String> provincecodeList) {
		return getExpressList(expressid, provincecodeList.toArray(new String[]{}));
	}

}
