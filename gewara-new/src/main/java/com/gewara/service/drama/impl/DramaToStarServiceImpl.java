package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.stereotype.Service;

import com.gewara.constant.TagConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

@Service("dramaToStarService")
public class DramaToStarServiceImpl extends BaseServiceImpl implements DramaToStarService {

	@Override
	public void saveDramaToStar(Long dramaid, String starids) {
		//if(StringUtils.isBlank(starids) || dramaid == null){return;}
		if(dramaid == null) return;
		// 保存数据前先删除对应关系
		List<DramaToStar> dramatostarlist = this.getDramaToStarListByDramaid(TagConstant.TAG_DRAMA, dramaid, false);
		baseDao.removeObjectList(dramatostarlist);
		List<Long> idList = BeanUtil.getIdList(starids, ",");
		for(Long id : idList){
			DramaToStar dts = new DramaToStar(dramaid, id);
			dts.setTag(TagConstant.TAG_DRAMA);
			baseDao.saveObject(dts);
		}
	}
	@Override
	public List<DramaToStar> getDramaToStarListByDramaid(String type, Long dramaid, boolean isGtZero){
		DetachedCriteria query = DetachedCriteria.forClass(DramaToStar.class);
		query.add(Restrictions.eq("tag", type));
		query.add(Restrictions.eq("dramaid", dramaid));
		if(isGtZero){
			query.add(Restrictions.gt("numsort", 0));
		}
		query.addOrder(Order.asc("numsort"));
		return hibernateTemplate.findByCriteria(query);
	}
	@Override
	public Integer getStarCount(Long relatedid, Long starid){
		DetachedCriteria query = DetachedCriteria.forClass(DramaToStar.class);
		query.add(Restrictions.eq("dramaid", relatedid));
		query.add(Restrictions.eq("starid", starid));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<Drama> getDramaListByStarid(Long starid){
		return getDramaListByStarid(starid, false, -1, -1);
	}
	@Override
	public List<Drama> getDramaListByStarid(Long starid, boolean isCurrent, int from, int maxnum) {
		Timestamp current = DateUtil.getCurTruncTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
		DetachedCriteria subquery = DetachedCriteria.forClass(DramaToStar.class, "dts");
		subquery.add(Restrictions.eqProperty("dts.dramaid", "d.id"));
		subquery.add(Restrictions.eq("dts.starid", starid));
		subquery.add(Restrictions.eq("dts.tag", TagConstant.TAG_DRAMA));
		subquery.setProjection(Projections.property("dts.id"));
		query.add(Subqueries.exists(subquery));
		if(isCurrent){
			query.add(Restrictions.and(Restrictions.le("d.releasedate", current), Restrictions.ge("d.enddate", current)));
		}
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public Integer getDramaCountByStarid(Long starid) {
		return getDramaCountByStarid(starid, false);
	}
	@Override
	public Integer getDramaCountByStarid(Long starid, boolean isCurrent) {
		Timestamp current = DateUtil.getCurTruncTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
		query.setProjection(Projections.rowCount());
		DetachedCriteria subquery = DetachedCriteria.forClass(DramaToStar.class, "dts");
		subquery.add(Restrictions.eqProperty("dts.dramaid", "d.id"));
		subquery.add(Restrictions.eq("dts.starid", starid));
		subquery.add(Restrictions.eq("dts.tag", TagConstant.TAG_DRAMA));
		subquery.setProjection(Projections.property("dts.id"));
		query.add(Subqueries.exists(subquery));
		if(isCurrent){
			query.add(Restrictions.ge("d.enddate", current));
		}
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<DramaStar> getDramaStarListByDramaid(Long dramaid, String starType, int from, int maxnum, String...notNullPropertys) {
		DetachedCriteria query = DetachedCriteria.forClass(DramaStar.class, "st");
		if(notNullPropertys != null){
			for(String property : notNullPropertys){
				query.add(Restrictions.isNotNull(property));
			}
		}
		DetachedCriteria subquery = DetachedCriteria.forClass(DramaToStar.class, "dts");
		subquery.add(Restrictions.eqProperty("dts.starid", "st.id"));
		subquery.add(Restrictions.eq("dts.dramaid", dramaid));
		subquery.add(Restrictions.eq("dts.tag", TagConstant.TAG_DRAMA));
		subquery.setProjection(Projections.property("dts.id"));
		if(StringUtils.isNotBlank(starType))query.add(Restrictions.like("startype", starType, MatchMode.ANYWHERE));
		query.add(Subqueries.exists(subquery));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
}
