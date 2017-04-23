package com.gewara.service.member.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.gewara.model.user.Treasure;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.TreasureService;
import com.gewara.support.ReadOnlyTemplate;
@Service("treasureService")
public class TreasureServiceImpl extends BaseServiceImpl implements TreasureService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public boolean isExistsTreasure(Treasure treasure) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("memberid", treasure.getMemberid()));
			query.add(Restrictions.eq("tag", treasure.getTag()));
			query.add(Restrictions.eq("relatedid", treasure.getRelatedid()));
			query.add(Restrictions.eq("action", treasure.getAction()));
		List<Treasure> maList = readOnlyTemplate.findByCriteria(query);
		if(maList.isEmpty()) return false;
		return true;
	}
	@Override
	public List<Long> getTreasureIdList(Long memberId, String tag, String action) {
		List<Long> relatedids = readOnlyTemplate.find("select t.relatedid from Treasure t where t.memberid=? and t.tag=? and t.action=? order by t.addtime desc",memberId,tag,action);
		return relatedids;
	}
	@Override
	public List<Long> getTreasureIdList(Long memberId, String tag, String action, int from, int maxnum) {
		List<Long> relatedids = queryByRowsRange("select t.relatedid from Treasure t where t.memberid=? and t.tag=? and t.action=? order by t.addtime desc", from, maxnum, memberId, tag,action);
		return relatedids;
	}

	@Override
	public List<Treasure> getTreasureList(String action, Long memberid, String tag, Long relatedid,	int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		if(action!=null)
			query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(action))
			query.add(Restrictions.eq("action", action));
		if(StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if(relatedid!=null)
			query.add(Restrictions.eq("relatedid", relatedid));
		query.addOrder(Order.desc("addtime"));
		List<Treasure> tList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return tList;
	}
	@Override
	public List<Long> getTreasureMemberList(String action, String tag, Long relatedid, int from, int maxnum) {
		return getTreasureMemberList(action, tag, relatedid, null, false, from, maxnum);
	}
	
	@Override
	public List<Long> getTreasureMemberList(String action, String tag, Long relatedid, String order, boolean asc, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		if(StringUtils.isNotBlank(action))query.add(Restrictions.eq("action", action));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		if(StringUtils.isNotBlank(order) && ClassUtils.hasMethod(Treasure.class, "get" + StringUtils.capitalize(order))){
			if(asc) query.addOrder(Order.asc(order));
			else query.addOrder(Order.desc(order));
		}else query.addOrder(Order.desc("addtime"));
		query.setProjection(Projections.property("memberid"));
		List<Long> idList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return idList;
	}
	@Override
	public Treasure getTreasureByTagMemberidRelatedid(String tag, Long memberid, Long relatedid, String action) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("action", action));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.eq("memberid", memberid));
		List<Treasure> treasureList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(treasureList.isEmpty())
		return null;
		return treasureList.get(0);
	}
	
	@Override
	public void saveTreasure(Long memberid, String tag, Long relatedid, String action){
		Treasure treasure = new Treasure(memberid, tag, relatedid, action);
		if (!isExistsTreasure(treasure)) {
			baseDao.saveObject(treasure);
		}
	}
}
