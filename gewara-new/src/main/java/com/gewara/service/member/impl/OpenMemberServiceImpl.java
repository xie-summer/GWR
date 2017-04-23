package com.gewara.service.member.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.stereotype.Service;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.OpenMemberService;


@Service("openMemberService")
public class OpenMemberServiceImpl extends BaseServiceImpl implements OpenMemberService {
	
	@Override
	public Integer getOpenMemberCountBySource(String source){
		DetachedCriteria query=DetachedCriteria.forClass(OpenMember.class);
		query.add(Restrictions.eq("source", source));
		query.setProjection(Projections.rowCount());
		List<OpenMember> result=hibernateTemplate.findByCriteria(query);
		if(result.size()>0) return new Integer(result.get(0)+"");
		return 0;
	}
	@Override
	public List<String> getOpenMemberSourceList(){
		String hql="select o.source as source from OpenMember o group by o.source";
		List<String> result=hibernateTemplate.find(hql);
		return result;
	}
	@Override
	public Integer getTicketOrderMemberCount(Date fromDate, Date toDate, String source){
		//String hql = "select distinct(t.memberid) as memberid from OpenMember o, GewaOrder t where o.memberid=t.memberid and t.status=? " +
		//" and t.paidtime>=? and t.paidtime<=? and o.source =? ";
		//List result=hibernateTemplate.find(hql, GewaOrder.STATUS_PAID_SUCCESS, fromDate, toDate, source);
		DetachedCriteria query=DetachedCriteria.forClass(GewaOrder.class,"t");
		query.add(Restrictions.eq("t.status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.ge("t.paidtime", fromDate));
		query.add(Restrictions.le("t.paidtime", toDate));
		query.setProjection(Projections.countDistinct("t.memberid"));
		
		DetachedCriteria subquery=DetachedCriteria.forClass(OpenMember.class, "o");
		subquery.add(Restrictions.eqProperty("o.memberid", "t.memberid"));
		subquery.add(Restrictions.eq("o.source", source));
		subquery.setProjection(Projections.property("o.memberid"));
		query.add(Subqueries.exists(subquery));
		List result=hibernateTemplate.findByCriteria(query);
		if(result.size()>0) return new Integer(result.get(0)+"");
		return 0;
	}
	@Override
	public Integer getOpenMemberBindEmailCount(Date fromDate, Date toDate, String source, String newTask){
		DetachedCriteria query=DetachedCriteria.forClass(MemberInfo.class,"mi");
		query.add(Restrictions.like("mi.newtask", newTask, MatchMode.ANYWHERE));
		
		DetachedCriteria subquery=DetachedCriteria.forClass(Member.class,"t");
		subquery.add(Restrictions.ge("t.addtime", fromDate));
		subquery.add(Restrictions.le("t.addtime", toDate));
		subquery.setProjection(Projections.property("t.id"));
		subquery.add(Restrictions.eqProperty("mi.id", "t.id"));
		query.add(Subqueries.exists(subquery));
		
		DetachedCriteria subquery2=DetachedCriteria.forClass(OpenMember.class,"o");
		subquery2.add(Restrictions.eq("o.source", source));
		subquery2.add(Restrictions.eqProperty("o.memberid", "mi.id"));
		subquery2.setProjection(Projections.property("o.id"));
		query.add(Subqueries.exists(subquery2));
		
		query.setProjection(Projections.rowCount());
		List<MemberInfo> list=hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
}
