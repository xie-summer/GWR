package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.bbs.Moderator;
import com.gewara.service.bbs.ModeratorService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

@Service("moderatorService")
public class ModeratorServiceImpl extends BaseServiceImpl implements ModeratorService {
	
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Override
	public Integer getModeratorCount(String type, Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(Moderator.class);
		if(StringUtils.isBlank(type))type = Moderator.TYPE_TODAY;
		if(StringUtils.isNotBlank(type))query.add(Restrictions.eq("type", type));
		if(memberid != null) query.add(Restrictions.eq("memberid",String.valueOf(memberid)));
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("mstatus", Status.Y));
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<Moderator> getModeratorList(String type, List showaddress, String mstatus, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Moderator.class);
		if(StringUtils.isBlank(type))
			query.add(Restrictions.or(Restrictions.eq("type", "today"), Restrictions.eq("type", "hot")));
		else query.add(Restrictions.eq("type", type));
		query.add(Restrictions.eq("mstatus", mstatus));
		if(showaddress!=null){
			query.add(Restrictions.in("showaddress", showaddress));
		}
		if(StringUtils.equals(type, Moderator.TYPE_HOT)){
			query.addOrder(Order.desc("commentcount"));
		}
		List<Moderator> moderatorList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return moderatorList;
	}

	
	@Override
	public List<Moderator> getModeratorByType(Integer showAddress,String type) {
		return getModeratorByType(showAddress, type, 0, 20, false);
	}
	public List<Moderator> getModeratorByType(Integer showAddress,String type, int from, int maxnum, boolean isRule) {
		DetachedCriteria query = DetachedCriteria.forClass(Moderator.class);
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.eq("type", type));
		if(showAddress != null) {
			query.add(Restrictions.or(Restrictions.eq("showaddress",showAddress), Restrictions.eq("showaddress", 2)));
		}
		if(isRule){
			Timestamp currentdate = DateUtil.getCurTruncTimestamp();
			query.add(Restrictions.or(Restrictions.and(Restrictions.gt("addtime", DateUtil.addDay(currentdate, -30)), Restrictions.and(Restrictions.lt("addtime", DateUtil.addDay(currentdate, -7)),Restrictions.gt("commentcount", 100))),Restrictions.gt("addtime", DateUtil.addDay(currentdate, -7))));
		}
		query.add(Restrictions.eq("mstatus", Status.Y));
		query.addOrder(Order.desc("commentcount"));
		query.addOrder(Order.asc("ordernum"));
		List<Moderator> moderatorList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return moderatorList;
	}
	
	@Override
	public List<Moderator> getModeratorList(Long memberid, String type, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Moderator.class);
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.eq("type", type));
		if(memberid != null) query.add(Restrictions.eq("memberid",String.valueOf(memberid)));
		query.add(Restrictions.eq("mstatus", Status.Y));
		query.addOrder(Order.desc("addtime"));
		List<Moderator> moderatorList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return moderatorList;
	}

	/**
	 *  @function 从缓存中取并更新
	 * 	@author bob.hu
	 *	@date	2011-12-02 17:36:18
	 */
	@Override
	public List<Map> updateHotModeratorFromCache(int from,int max) {
		String key = CacheConstant.buildKey("TwoHoursHotTopic");
		List<Map> result = (List<Map>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(result == null){
			List<Moderator> moderators = getModeratorByType(Moderator.SHOW_TYPE_WEB, Moderator.TYPE_HOT, from, max, false); 
			for(Moderator moderator : moderators){
				int commentcount = commentService.getModeratorDetailCount(moderator.getTitle());
				moderator.setCommentcount(commentcount);
			}
			baseDao.saveObjectList(moderators);
			result = BeanUtil.getBeanMapList(moderators, "title", "commentcount", "summary");
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, result);
		}
		return result; 
	}
}