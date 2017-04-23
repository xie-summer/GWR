package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.bbs.BlackMember;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.SnsService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

@Service("snsService")
public class SnsServiceImpl extends BaseServiceImpl implements SnsService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<Map> getMemberListByUpdatetime(Timestamp starttime, Timestamp endtime, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(MemberInfo.class);
		query.add(Restrictions.le("updatetime", endtime));
		if(starttime!=null) query.add(Restrictions.ge("updatetime", starttime));
		ProjectionList list = Projections.projectionList().add(Projections.property("id"),"id")
								.add(Projections.property("headpic"),"headpic")
								.add(Projections.property("nickname"),"nickname")
								.add(Projections.property("addtime"),"addtime")
								.add(Projections.property("sign"),"sign")
								.add(Projections.property("introduce"),"introduce")
								.add(Projections.property("updatetime"),"updatetime");
		query.setProjection(list);
		query.add(Restrictions.or(Restrictions.isNotNull("headpic"), Restrictions.or(Restrictions.isNotNull("introduce"),Restrictions.isNotNull("sign"))));
		query.addOrder(Order.asc("updatetime"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> result = hibernateTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	
	@Override
	public Integer getMemberCountByUpdatetime(Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(MemberInfo.class);
		if(starttime!=null) query.add(Restrictions.ge("updatetime", starttime));
		if(endtime!=null) query.add(Restrictions.le("updatetime", endtime));
		query.add(Restrictions.or(Restrictions.isNotNull("headpic"), Restrictions.or(Restrictions.isNotNull("introduce"),Restrictions.isNotNull("sign"))));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	
	@Override
	public List<Member> searchMember(Long memberid, String nickname, String mobile,String email, int from,int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(Member.class);
		if(memberid!=null) query.add(Restrictions.eq("id", memberid));
		if(StringUtils.isNotBlank(nickname)) query.add(Restrictions.like("nickname",nickname,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("mobile", mobile));
		if(StringUtils.isNotBlank(email)) query.add(Restrictions.eq("email", email));
		query.addOrder(Order.desc("id"));
		List<Member> listMember=readOnlyTemplate.findByCriteria(query, from, maxnum);
		return listMember;
	}

	@Override
	public void updateMemberPasswordAndDelete(Member m) {
		hibernateTemplate.update(m);
	}

	@Override
	public BlackMember isJoinBlackMember(Long memberid) {
		DetachedCriteria query=DetachedCriteria.forClass(BlackMember.class);
		query.add(Restrictions.eq("memberId", memberid));
		List<BlackMember> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public Integer searchMemberCount(Long memberid, String nickname, String mobile, String email) {
		DetachedCriteria query=DetachedCriteria.forClass(Member.class);
		if(memberid!=null) query.add(Restrictions.eq("id", memberid));
		if(StringUtils.isNotBlank(nickname)) query.add(Restrictions.like("nickname",nickname,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("mobile", mobile));
		if(StringUtils.isNotBlank(email)) query.add(Restrictions.eq("email", email));
		query.setProjection(Projections.rowCount());
		List<Member> listMember=readOnlyTemplate.findByCriteria(query);
		if(listMember.get(0)==null) return 0;
		return new Integer(listMember.get(0)+"");
	}
	@Override
	public Integer getMemberExpValueCount(Integer startExp, Integer endExp) {
		DetachedCriteria query = getMemberExpQuery(startExp, endExp);
		query.setProjection(Projections.rowCount());
		List memberInfoList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(memberInfoList.isEmpty()) return 0;
		return Integer.parseInt(memberInfoList.get(0)+"");
	}
	
	private DetachedCriteria getMemberExpQuery(Integer startExp, Integer endExp){
		DetachedCriteria query = DetachedCriteria.forClass(MemberInfo.class,"mi");
		if(startExp!=null) query.add(Restrictions.ge("mi.expvalue", startExp));
		if(endExp!=null) query.add(Restrictions.le("mi.expvalue", endExp));
		
		DetachedCriteria subQuery = DetachedCriteria.forClass(Member.class,"m");
		subQuery.setProjection(Projections.property("m.id"));
		subQuery.add(Restrictions.eqProperty("m.id", "mi.id"));
		query.add(Subqueries.exists(subQuery));
		return query;
	}

	@Override
	public List<MemberInfo> getMemberExpValueList(Integer startExp, Integer endExp, int from,
			int maxnum) {
		DetachedCriteria query = getMemberExpQuery(startExp, endExp);
		query.addOrder(Order.desc("mi.expvalue"));
		query.addOrder(Order.desc("mi.id"));
		List<MemberInfo> memberInfoList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return memberInfoList;
	}

	@Override
	public Integer getSumExpValue(Integer startExp, Integer endExp) {
		DetachedCriteria query = getMemberExpQuery(startExp, endExp);
		query.setProjection(Projections.sum("mi.expvalue"));
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.get(0) == null) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
}
