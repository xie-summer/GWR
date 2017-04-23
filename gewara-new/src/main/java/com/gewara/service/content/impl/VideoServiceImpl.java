package com.gewara.service.content.impl;

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

import com.gewara.model.content.Video;
import com.gewara.model.movie.MovieVideo;
import com.gewara.service.content.VideoService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.DateUtil;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
@Service("videoService")
public class VideoServiceImpl extends BaseServiceImpl implements VideoService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public Integer getVideoCountByTag(String tag, Long relatedid) {
		return getVideoCountByTag(tag, relatedid, null);
	}
	@Override
	public List<Video> getVideoListByTag(String tag, Long relatedid, int from, int maxnum) {
		return getVideoListByTag(tag, relatedid, null, "updatetime", false, from, maxnum);
	}
	@Override
	public List<Video> getVideoListByTag(String tag, Long relatedid, Integer hotvalue, String orderField, boolean asc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Video.class);
		query.add(Restrictions.eq("tag", tag));
		if(relatedid != null){
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(hotvalue != null){
			query.add(Restrictions.eq("hotvalue", hotvalue));
		}
		if(StringUtils.isNotBlank(orderField) && ClassUtils.hasMethod(Video.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}
		query.addOrder(Order.asc("id"));
		List result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public Integer getVideoCountByTag(String tag, Long relatedid, Integer hotvalue) {
		DetachedCriteria query = DetachedCriteria.forClass(Video.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("tag", tag));
		if(relatedid != null){
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(hotvalue != null){
			query.add(Restrictions.eq("hotvalue", hotvalue));
		}
		List result = readOnlyTemplate.findByCriteria(query);
		if (result.isEmpty()) return 0;
		return new Integer(result.get(0)+"");
	}
	
	@Override
	public MovieVideo getMovieVideo(Long movieid) {
		MovieVideo mv = baseDao.getObjectByUkey(MovieVideo.class, "movieid", movieid);
		return mv;
	}
	@Override
	public MovieVideo getMovieVideoByVideoid(String videoid) {
		MovieVideo mv = baseDao.getObjectByUkey(MovieVideo.class, "videoid", videoid);
		return mv;
	}
	/*@Override
	public List<MovieVideo> getMovieVideoList(Long movieid, int from, int maxnum) {
		String hql = "from MovieVideo where movieid=?";
		List<MovieVideo> result = queryByRowsRange(hql, from, maxnum, movieid);
		return result;
	}*/
	public List<Video> getHotVideo(String tag,String order, int from, int maxnum) {
		DetachedCriteria qry = DetachedCriteria.forClass(Video.class,"v");
		qry.add(Restrictions.eq("v.tag", tag));
		qry.add(Restrictions.gt("v.addtime", DateUtil.addDay(DateUtil.getMillTimestamp(), -7)));
		if(order != null && order != ""){
			qry.addOrder(Order.desc(order));
		}
		List<Video> videoList = readOnlyTemplate.findByCriteria(qry, from, maxnum);
		return videoList;
	}
}
