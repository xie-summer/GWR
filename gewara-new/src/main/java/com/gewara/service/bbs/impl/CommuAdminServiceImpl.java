package com.gewara.service.bbs.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.CommuAdminService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.SearchService;

@Service("commuAdminService")
public class CommuAdminServiceImpl extends BaseServiceImpl implements CommuAdminService{
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	@Override
	public void updateCommuStatus(Long commuid,String status) {
		Commu commu = baseDao.getObject(Commu.class, commuid);
		if(commu!=null){
			commu.setStatus(status);
			baseDao.saveObject(commu);
			searchService.pushSearchKey(commu);
		}
	}

	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public Map getCommuInfoList(Long commuid,String commname, String nickname,String status,int from,int maxnum) {
		Map map=new HashMap();
		Map<Long,Member> mapMember=new HashMap<Long,Member>();
		DetachedCriteria queryCommu=DetachedCriteria.forClass(Commu.class, "c");
		if(StringUtils.isNotBlank(commname)) queryCommu.add(Restrictions.ilike("c.name", commname,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(nickname)){
			DetachedCriteria memberQuery=DetachedCriteria.forClass(Member.class, "m");
			memberQuery.add(Restrictions.eq("m.nickname", nickname));
			memberQuery.add(Restrictions.eqProperty("m.id", "c.adminid"));
			memberQuery.setProjection(Projections.property("m.id"));
			
			queryCommu.add(Subqueries.exists(memberQuery));
		}
		if(commuid!=null)queryCommu.add(Restrictions.eq("c.id", commuid));
		if(StringUtils.isNotBlank(status))
			queryCommu.add(Restrictions.eq("c.status", status));
		queryCommu.addOrder(Order.desc("c.addtime"));
		List<Commu> listCommu=readOnlyTemplate.findByCriteria(queryCommu,from, maxnum);
		for (Commu commu : listCommu) {
			mapMember.put(commu.getAdminid(), readOnlyTemplate.get(Member.class,commu.getAdminid()));
		}
		map.put("mapMember", mapMember);
		map.put("listCommu",listCommu);
		return map;
	}

	@Override
	public Integer getCommuInfoCount(Long commuid, String commname,
			String nickname, String status) {
		DetachedCriteria queryCommu=DetachedCriteria.forClass(Commu.class, "c");
		if(StringUtils.isNotBlank(commname)) queryCommu.add(Restrictions.ilike("c.name", commname,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(nickname)){
			DetachedCriteria memberQuery=DetachedCriteria.forClass(Member.class, "m");
			memberQuery.add(Restrictions.eq("m.nickname", nickname));
			memberQuery.add(Restrictions.eqProperty("m.id", "c.adminid"));
			memberQuery.setProjection(Projections.property("m.id"));
			
			queryCommu.add(Subqueries.exists(memberQuery));
		}
		if(commuid!=null)queryCommu.add(Restrictions.eq("c.id", commuid));
		if(StringUtils.isNotBlank(status))
			queryCommu.add(Restrictions.eq("c.status", status));
		queryCommu.setProjection(Projections.rowCount());
		List<Commu> listCommu=readOnlyTemplate.findByCriteria(queryCommu);
		if(listCommu.get(0)==null) return 0;
		return new Integer(listCommu.get(0)+"");
	}

	/**
	 *  圈子认证申请 列表.
	 */
	@Override
	public List<CommuManage> getCommuManageListByStatus(String status, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(CommuManage.class);
		query.add(Restrictions.eq("checkstatus", status));
		query.addOrder(Order.desc("addtime"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public Integer getCommuManageCount(String status){
		DetachedCriteria query = DetachedCriteria.forClass(CommuManage.class);
		query.add(Restrictions.eq("checkstatus", status));
		query.setProjection(Projections.rowCount());
		List<CommuManage> list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
}
