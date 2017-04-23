package com.gewara.service.content.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
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

import com.gewara.model.content.AdPosition;
import com.gewara.model.content.Advertising;
import com.gewara.service.content.AdService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

@Service("adService")
public class AdServiceImpl extends BaseServiceImpl implements AdService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<AdPosition> getAdPositionListByTag(String tag, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(AdPosition.class);
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.like("tag", tag, MatchMode.ANYWHERE));
		List<AdPosition> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override
	public Integer getAdPositionCountByTag(String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(AdPosition.class);
		query.add(Restrictions.like("tag", tag, MatchMode.ANYWHERE));
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<Advertising> getAdvertisingListByAdPositionid(String citycode,Long adpositionid, String order) {
		DetachedCriteria query = DetachedCriteria.forClass(Advertising.class);
		query.add(Restrictions.eq("adpositionid", adpositionid));
		query.add(Restrictions.eq("citycode", citycode));
		query.addOrder(Order.asc(order));
		List<Advertising> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override
	public Advertising getAdvertising(String tag, Long relatedid,String pTag) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(Advertising.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.or(Restrictions.eq("relatedid", relatedid),Restrictions.isNull("relatedid")));
		query.add(Restrictions.le("starttime", curtime));
		query.add(Restrictions.gt("endtime", curtime));
		query.addOrder(Order.desc("addtime"));
		DetachedCriteria subQuery = DetachedCriteria.forClass(AdPosition.class);
		subQuery.add(Restrictions.like("tag", pTag, MatchMode.ANYWHERE))
			.setProjection(Projections.property("id"));
		query.add(Subqueries.propertyIn("adpositionid",subQuery));
		List<Advertising> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(list.size()>0) return list.get(0);
		return null;
	}
	@Override
	public Integer getAdCountByAdPosition(String citycode,String pid){
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String qry = "select count(*) from Advertising a where a.starttime<=? and a.endtime>=? and a.citycode=? " +
				"and exists(select p.id from AdPosition p where p.id=a.adpositionid and p.pid=?)";
		List<Long> list = readOnlyTemplate.find(qry, now, now, citycode, pid);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public Integer getSumRemaintimesByAdPosition(String citycode,Long adpid){
		DetachedCriteria query = DetachedCriteria.forClass(Advertising.class);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		query.add(Restrictions.eq("adpositionid", adpid));
		query.add(Restrictions.eq("status",Advertising.STATUS_UP));
		query.add(Restrictions.gt("remaintimes", 0));
		query.add(Restrictions.ge("endtime", now));
		query.add(Restrictions.le("starttime", now));
		query.add(Restrictions.eq("citycode", citycode));
		query.setProjection(Projections.sum("remaintimes"));
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.get(0) == null) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public List<Advertising> getAdvertisingListByAdPositionid(String citycode,Long adpositionid) {
		DetachedCriteria query = DetachedCriteria.forClass(Advertising.class);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		query.add(Restrictions.eq("adpositionid", adpositionid));
		query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("status",Advertising.STATUS_UP));
		query.add(Restrictions.ge("endtime", now));
		query.add(Restrictions.le("starttime", now));
		query.add(Restrictions.gt("remaintimes", 0));
		query.addOrder(Order.asc("remaintimes"));
		List<Advertising> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}
	@Override
	public Advertising getRandomAd(String citycode,String pid){
		int count = getAdCountByAdPosition(citycode,pid);
		if(count>0) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			String qry = "from Advertising a where a.starttime<=? and a.endtime>=? and a.citycode=? " +
					"and exists(select p.id from AdPosition p where p.id=a.adpositionid and p.pid=?)";
			List<Advertising> list = readOnlyTemplate.find(qry, now, now, citycode, pid);
			return list.get(new Random().nextInt(count));
		}
		return null;
	}
	
	@Override
	public List<Advertising> getAdListByPid(String citycode, String pid){
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String qry = "from Advertising a where a.starttime<=? and a.endtime>=? and a.citycode=? " +
		"and exists(select p.id from AdPosition p where p.id=a.adpositionid and p.pid=?) order by ordernum asc";
		List<Advertising> list = readOnlyTemplate.find(qry, now, now, citycode, pid);
		return list;
	}
	
	@Override
	public List<Advertising> getAdListByPid(String citycode, String pid, String tag, Long relatedid){
		Timestamp now = new Timestamp(System.currentTimeMillis());
		String qry = "from Advertising a where a.starttime<=? and a.endtime>=? and a.citycode=? and a.tag=? and a.relatedid=? " +
		"and exists(select p.id from AdPosition p where p.id=a.adpositionid and p.pid=?)";
		List<Advertising> list = readOnlyTemplate.find(qry, now, now, citycode, tag, relatedid, pid);
		return list;
	}
		
	@Override
	public void changRaterang(String citycode,Long adpid){
		List<Advertising> adList = getAdvertisingListByAdPositionid(citycode,adpid);
		int sumtemp = 0;
		int num = 0;
		//int count = getAdCountByAdPosition(adpid);
		int count = getAdCountByAdPosition(citycode,adpid+"");
		int sumremaintimes = getSumRemaintimesByAdPosition(citycode,adpid);
		for (Advertising ad : adList) {
			num++;
			if (num == 1) {
				ad.setRang1(1);
				ad.setRang2(ad.getRemaintimes());
			} else {
				if (num == count) {
					ad.setRang1(sumtemp+1);
					ad.setRang2(sumremaintimes);
				}
				else{
					ad.setRang1(sumtemp+1);
					ad.setRang2(sumtemp + ad.getRemaintimes());
				}
			}
			sumtemp = sumtemp + ad.getRemaintimes();
		}
		baseDao.saveObjectList(adList);
	}
	/* (non-Javadoc)
	 * @see com.gewara.service.content.AdService#getFirstAdByPostionTag(java.lang.String)
	 */
	@Override
	public Advertising getFirstAdByPostionTag(String tag) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria query = DetachedCriteria.forClass(Advertising.class);
		query.add(Restrictions.le("starttime", curtime));
		query.add(Restrictions.gt("endtime", curtime));
		query.addOrder(Order.asc("ordernum"));
		
		DetachedCriteria subQuery = DetachedCriteria.forClass(AdPosition.class);
		subQuery.add(Restrictions.like("tag", tag, MatchMode.ANYWHERE))
			.setProjection(Projections.property("id"));
		query.add(Subqueries.propertyIn("adpositionid",subQuery));
		List<Advertising> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		
		return CollectionUtils.isNotEmpty(list) ? list.get(0) : null;
		
	}
}
