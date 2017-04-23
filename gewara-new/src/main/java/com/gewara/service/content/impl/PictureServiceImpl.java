package com.gewara.service.content.impl;

import java.sql.Timestamp;
import java.util.Date;
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

import com.gewara.constant.Status;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.model.content.Picture;
import com.gewara.model.user.MemberPicture;
import com.gewara.service.content.PictureService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-10-9ÉÏÎç08:58:41
 */
@Service("pictureService")
public class PictureServiceImpl extends BaseServiceImpl implements PictureService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Override
	public Integer getPictureCountByRelatedid(String tag, Long relatedid) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<Picture> getPictureListByRelatedid(String tag, Long relatedid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.addOrder(Order.desc("id"));
		return hibernateTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public List<Picture> getPictueList(String tag, Long relatedid, String orderField, boolean asc, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid != null) query.add(Restrictions.eq("relatedid", relatedid));
		if(StringUtils.isNotBlank(orderField) && ClassUtils.hasMethod(Picture.class, "get" + StringUtils.capitalize(orderField))){
			if(asc){
				query.addOrder(Order.asc(orderField));
			}else{
				query.addOrder(Order.desc(orderField));
			}
		}else{
			query.addOrder(Order.desc("posttime"));
		}
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}
	@Override
	public List<String> getSinglePictureListByRelatedid(String tag, Long relatedid, int from, int maxnum) {
		String key = CacheConstant.buildKey("getSinglePic", tag, relatedid, from, maxnum);
		List<String> result = (List<String>) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(result == null){
			String hql = "select picturename from Picture where tag=? and relatedid=? order by posttime desc";
			result = queryByRowsRange(hql, from, maxnum, tag, relatedid);
			cacheService.set(CacheConstant.REGION_HALFHOUR, key, result);
		}
		return result;
	}
	@Override
	public List<Picture> getPictureList(String tag, Long relatedid, Long memberid, Timestamp starttime, Timestamp endtime, int from,int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag",tag));
		if(relatedid!=null) query.add(Restrictions.eq("relatedid",relatedid));
		if(memberid !=null) query.add(Restrictions.eq("memberid", memberid));
		if(starttime !=null) query.add(Restrictions.ge("posttime", starttime));
		if(endtime != null) query.add(Restrictions.le("posttime", endtime));
		query.addOrder(Order.desc("posttime"));
		List<Picture> listPicture = hibernateTemplate.findByCriteria(query,from,maxnum);
		return listPicture;
	}
	
	@Override
	public Integer getPictureCount(String tag, Long relatedid ,Long memberid, Timestamp starttime, Timestamp endtime) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag",tag));
		if(relatedid!=null) query.add(Restrictions.eq("relatedid",relatedid));
		if(memberid !=null) query.add(Restrictions.eq("memberid", memberid));
		if(starttime !=null) query.add(Restrictions.ge("posttime", starttime));
		if(endtime != null) query.add(Restrictions.le("posttime", endtime));
		query.setProjection(Projections.rowCount());
		List listPicture = hibernateTemplate.findByCriteria(query);
		return new Integer(listPicture.get(0)+"");
	}
	
	@Override
	public Integer getMemberPictureCount(Long relatedid, String tag, Long memberid, String flag, String status){
		DetachedCriteria query = DetachedCriteria.forClass(MemberPicture.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.ne("status", Status.N_DELETE));
		if(relatedid!=null)query.add(Restrictions.eq("relatedid", relatedid));
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.add(Restrictions.eq("flag", flag));
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty())return 0;
		return Integer.valueOf(result.get(0)+"");
	}
	@Override
	public List<MemberPicture> getMemberPictureList(Long relatedid, String tag, Long memberid, String flag, String status, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(MemberPicture.class);
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(relatedid!=null)query.add(Restrictions.eq("relatedid", relatedid));
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		//É¾³ý×´Ì¬
		query.add(Restrictions.ne("status", Status.N_DELETE));
		query.add(Restrictions.eq("flag", flag));
		query.addOrder(Order.desc("addtime"));
		List<MemberPicture> memberPictureList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return memberPictureList;
	}
	@Override
	public List<Picture> getPictureListCheck(String tag, Long relatedid,Long memberid, Timestamp starttime, Timestamp endtime,Date modifytime,Date updatetime,boolean check, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag",tag));
		if(relatedid!=null || memberid !=null || starttime !=null || endtime != null){
			if(relatedid!=null) query.add(Restrictions.eq("relatedid",relatedid));
			if(memberid !=null) query.add(Restrictions.eq("memberid", memberid));
			if(starttime !=null) query.add(Restrictions.ge("posttime", starttime));
			if(endtime != null) query.add(Restrictions.le("posttime", endtime));
		}else{
			if(modifytime != null){
				if(check){
					if(modifytime.equals(updatetime)){
						query.add(Restrictions.le("posttime", modifytime));
					}else{
						query.add(Restrictions.le("posttime", modifytime));
						query.add(Restrictions.gt("posttime", updatetime));
					}
				}else{
					query.add(Restrictions.gt("posttime", modifytime));
				}
			}
		}
		query.addOrder(Order.asc("posttime"));
		List<Picture> listPicture = hibernateTemplate.findByCriteria(query,from,maxnum);
		return listPicture;
	}
	
	@Override
	public Integer getPictureCountCheck(String tag,Date datetime,boolean check){
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		if(datetime != null){
			if(check){
				query.add(Restrictions.le("posttime", datetime));
			}else{
				query.add(Restrictions.gt("posttime", datetime));
			}
		}
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty())return 0;
		return Integer.valueOf(result.get(0)+"");
	}
}
