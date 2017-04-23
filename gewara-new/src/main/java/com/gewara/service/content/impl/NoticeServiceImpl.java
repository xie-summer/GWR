package com.gewara.service.content.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.model.content.Notice;
import com.gewara.service.content.NoticeService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

@Service("noticeService")
public class NoticeServiceImpl extends BaseServiceImpl implements NoticeService{
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public Integer getNoticeCountByCount(Long relatedid,String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(Notice.class,"n");
		if(relatedid != null)query.add(Restrictions.eq("n.relatedid",relatedid));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("n.tag", tag));
		query.add(Restrictions.like("n.status",Status.Y,MatchMode.START));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<Notice> getNoticeListByCommuid(Long relatedid,String tag, int from,
			int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Notice.class,"n");
		if(relatedid != null)query.add(Restrictions.eq("n.relatedid",relatedid));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("n.tag", tag));
		query.add(Restrictions.like("n.status",Status.Y,MatchMode.START));
		query.addOrder(Order.desc("n.addtime"));
		List<Notice> noticeList = readOnlyTemplate.findByCriteria(query,from,maxnum);
		return noticeList;
	}

}
