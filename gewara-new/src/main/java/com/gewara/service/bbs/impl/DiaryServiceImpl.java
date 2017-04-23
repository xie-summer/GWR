package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.springframework.util.ClassUtils;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Flag;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.bbs.VoteChoose;
import com.gewara.model.bbs.VoteOption;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.user.Friend;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
@Service("diaryService")
public class DiaryServiceImpl extends BaseServiceImpl implements DiaryService{
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Override
	public DiaryBase getDiaryBase(Long id){
		DiaryBase diaryBase = baseDao.getObject(Diary.class, id);
		if(diaryBase == null)
			diaryBase = baseDao.getObject(DiaryHist.class, id);
		return diaryBase;
	}
	
	@Override
	public <T extends DiaryBase> List<T> getDiaryList(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum) {
		return getDiaryList(clazz, citycode, type, tag, relatedid, start, maxnum, null, null, null, null, Status.Y,null,null);
	}
	@Override
	public <T extends DiaryBase> List<T> getDiaryList(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum, String order) {
		return getDiaryList(clazz, citycode, type, tag, relatedid, start, maxnum, null, null, order, null, Status.Y,null,null);
	}
	@Override
	public <T extends DiaryBase> List<T> getDiaryListByKey(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum, String key , Timestamp startTime,Timestamp endTime){
		return getDiaryList(clazz, citycode, type, tag, relatedid, start, maxnum, null, key, null, null, Status.Y, startTime, endTime);
	}
	@Override
	public <T extends DiaryBase> List<T> getDiaryListByFlag(Class<T> clazz, String citycode, String type, String tag, String flag, int start, int maxnum){
		return getDiaryList(clazz, citycode, type, tag, null, start, maxnum, flag, null, null, null, Status.Y,null,null);
	}
	@Override
	public List<Diary> getTopDiaryList(String citycode, String type, String tag, boolean isCache) {
		List<Long> idList = null;
		String key = null;
		if(isCache){
			key = CacheConstant.buildKey("aget1Top2Diary3List4a", citycode, type, tag);
			idList = (List<Long>) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		}
		if(idList == null){
			String flag = null;
			if(StringUtils.isBlank(tag)){
				flag = Flag.TOP1;
			}else if(ServiceHelper.isTag(tag)){
				flag = Flag.TOP2;
			}else{
				return new ArrayList<Diary>();//只有一级、二级置顶
			}
			DetachedCriteria query = DetachedCriteria.forClass(Diary.class, "d");
			if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("d.type", type, MatchMode.START));
			query.add(Restrictions.like("d.status", Status.Y, MatchMode.START));
			if(StringUtils.isNotBlank(citycode)){
				query.add(Restrictions.eq("d.citycode", citycode));
			}
			if(ServiceHelper.isTag(tag)){
				query.add(Restrictions.eq("d.tag", tag));
			}else if(ServiceHelper.isCategory(tag)){
				query.add(Restrictions.eq("d.category", tag));
			}
			query.add(Restrictions.like("d.flag", flag, MatchMode.ANYWHERE));
			query.addOrder(Order.desc("d.utime"));
			query.setProjection(Projections.id());
			idList = readOnlyTemplate.findByCriteria(query, 0, 20);
			if(isCache) cacheService.set(CacheConstant.REGION_ONEHOUR, key, idList);
		}
		if(idList.isEmpty()) return new ArrayList<Diary>();
		DetachedCriteria q =  DetachedCriteria.forClass(Diary.class);
		q.add(Restrictions.in("id", idList));
		List<Diary> result = readOnlyTemplate.findByCriteria(q);
		return result;
	}
	@Override
	public <T extends DiaryBase> List<T> getDiaryListByOrder(Class<T> clazz, String citycode, String type, String tag, Long relatedid, Timestamp startTime,Timestamp endTime, String order, boolean asc, int start, int maxnum){
		return getDiaryList(clazz, citycode, type, tag, relatedid, start, maxnum, null, null, order, null, Status.Y,startTime,endTime);
	}
	private <T extends DiaryBase> List<T> getDiaryList(Class<T> clazz, String citycode, String type, String tag, Long relatedid, int start, int maxnum, String flag, String searchkey, String order, Long communityid, String status,Timestamp startTime,Timestamp endTime) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz, "d");
		if(communityid!=null) query.add(Restrictions.eq("d.communityid", communityid));
		if(status!=null) query.add(Restrictions.like("d.status", status, MatchMode.START));
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("d.type", type, MatchMode.START));
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("d.tag", tag));
			if(relatedid!=null) query.add(Restrictions.eq("d.relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("d.category", tag));
			if(relatedid!=null) query.add(Restrictions.eq("d.categoryid", relatedid));
		}
		if(StringUtils.isNotBlank(searchkey)) {
			query.add(Restrictions.or(Restrictions.ilike("d.subject", searchkey, MatchMode.ANYWHERE), Restrictions.ilike("d.membername", searchkey, MatchMode.ANYWHERE)));
		}
		if(startTime != null) query.add(Restrictions.ge("d.addtime", startTime));
		if(endTime !=null) query.add(Restrictions.le("d.addtime",endTime));
		if(StringUtils.isNotBlank(citycode)){
			boolean restrictCity = getRealteCityList().contains(citycode);
			if(!restrictCity){ 
				query.add(Restrictions.or(Restrictions.eq("d.citycode", citycode),Restrictions.eq("d.division", DiaryConstant.DIVISION_A)));
			}
		}
		if(StringUtils.isNotBlank(flag)) query.add(Restrictions.like("d.flag", flag, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(order) && ClassUtils.hasMethod(clazz, "get" + StringUtils.capitalize(order))){
			if("poohnum".equals(order)){
				query.addOrder(Order.asc("flag"));
				query.addOrder(Order.desc("sumnum"));
			}else{
			  query.addOrder(Order.desc(order));
			}
			query.addOrder(Order.asc("d.id"));
		}else if("sumnumed".equals(order)){
			query.addOrder(Order.desc("d.sumnumed"));
		}else query.addOrder(Order.desc("d.utime"));
		List<T> result = readOnlyTemplate.findByCriteria(query, start, maxnum);
		return result;
	}

	@Override
	public <T extends DiaryBase> Integer getDiaryCount(Class<T> clazz, String citycode, String type, String tag, Long relatedid){
		return getDiaryCount(clazz, citycode, type, tag, relatedid, null, null, null, null, null, Status.Y);
	}
	@Override
	public <T extends DiaryBase> Integer getDiaryCountByKey(Class<T> clazz, String citycode, String type, String tag, Long relatedid, String key, Timestamp startTime, Timestamp endTime){
		return getDiaryCount(clazz, citycode, type, tag, relatedid, null, key, startTime, endTime, null, Status.Y);
	}
	
	private <T extends DiaryBase> Integer getDiaryCount(Class<T> clazz, String citycode, String type, String tag, Long relatedid, String flag, String searchkey, Timestamp startTime, Timestamp endTime, Long communityid, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz, "d");
		if(communityid!=null) query.add(Restrictions.eq("d.communityid", communityid));
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("d.type", type, MatchMode.START));
		if(status!=null) query.add(Restrictions.like("d.status", status, MatchMode.START));
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("d.tag", tag));
			if(relatedid!=null) query.add(Restrictions.eq("d.relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("d.category", tag));
			if(relatedid!=null) query.add(Restrictions.eq("d.categoryid", relatedid));
		}
		if(startTime !=null && endTime != null) query.add(Restrictions.between("d.addtime", startTime, endTime));
		else if(startTime != null) query.add(Restrictions.ge("d.addtime", startTime));
		else if(endTime !=null) query.add(Restrictions.le("d.addtime",endTime));
		if(StringUtils.isNotBlank(searchkey)) query.add(Restrictions.ilike("d.subject", searchkey, MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(citycode)){
			boolean restrictCity = getRealteCityList().contains(citycode);
			if(!restrictCity){ 
				query.add(Restrictions.or(Restrictions.eq("d.citycode", citycode),Restrictions.eq("d.division", DiaryConstant.DIVISION_A)));
			}
		}
		if(StringUtils.isNotBlank(flag)) query.add(Restrictions.like("d.flag", flag, MatchMode.ANYWHERE));
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	
	@Override
	public List<Map> getOneDayHotDiaryList(String citycode, String tag){
		String key = CacheConstant.buildKey("OneDayHotDiary", citycode, tag);
		List<Map> result = (List<Map>) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(result == null){
			DetachedCriteria query = DetachedCriteria.forClass(DiaryComment.class, "c");
			if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
			query.add(Restrictions.ge("c.addtime", new Timestamp(System.currentTimeMillis() - DateUtil.m_day)));
			
			DetachedCriteria sub = DetachedCriteria.forClass(Diary.class, "d");
			if(StringUtils.isNotBlank(tag)){
				if(ServiceHelper.isTag(tag)){
					sub.add(Restrictions.eq("d.tag", tag));
				}else if(ServiceHelper.isCategory(tag)){
					sub.add(Restrictions.eq("d.category", tag));
				}
			}
			sub.add(Restrictions.like("d.status", Status.Y, MatchMode.START));
			sub.setProjection(Projections.property("d.id"));
			sub.add(Restrictions.eqProperty("d.id", "c.diaryid"));
			
			query.add(Subqueries.exists(sub));
			query.setProjection(Projections.projectionList()
					.add(Projections.groupProperty("c.diaryid"))
					.add(Projections.count("id"), "commentcount"));
			query.addOrder(Order.desc("commentcount"));
			List<Object[]> rowList = readOnlyTemplate.findByCriteria(query, 0, 10);
			List<Long> idList = new ArrayList<Long>();
			for(Object[] row:rowList){
				idList.add((Long) row[0]);
			}
			List<Diary> diaryList = baseDao.getObjectList(Diary.class, idList);
			result = BeanUtil.getBeanMapList(diaryList, "id", "subject");
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, result);
		}
		
		return result;
	}
	@Override
	public <T extends DiaryBase> List<T> getDiaryListByMemberid(Class<T> clazz, String type, String tag, Long memberId, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("communityid", 0L));
		query.add(Restrictions.eq("memberid", memberId));
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("type", type, MatchMode.START));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(StringUtils.isNotBlank(tag)){
			if(ServiceHelper.isTag(tag)) query.add(Restrictions.eq("tag", tag));
			else if(ServiceHelper.isCategory(tag)) query.add(Restrictions.eq("category", tag));
		}
		query.addOrder(Order.desc("utime"));
		List<T> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public <T extends DiaryBase> Integer getDiaryCountByMemberid(Class<T> clazz, String type, String tag, Long memberId) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("communityid", 0L));
		query.add(Restrictions.eq("memberid", memberId));
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.like("type", type, MatchMode.START));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(StringUtils.isNotBlank(tag)){
			if(ServiceHelper.isTag(tag)) query.add(Restrictions.eq("tag", tag));
			else if(ServiceHelper.isCategory(tag)) query.add(Restrictions.eq("category", tag));
		}
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public List<DiaryComment> getDiaryCommentList(Long diaryId, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(DiaryComment.class);
		query.add(Restrictions.eq("diaryid", diaryId));
		query.addOrder(Order.asc("addtime"));
		List<DiaryComment> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<DiaryComment> getDiaryCommentList(Long diaryId) {
		DetachedCriteria query = DetachedCriteria.forClass(DiaryComment.class);
		query.add(Restrictions.eq("diaryid", diaryId));
		query.addOrder(Order.desc("addtime"));
		List<DiaryComment> result = readOnlyTemplate.findByCriteria(query);
		return result;
	}
	@Override
	public List<Diary> getFriendDiaryList(String type, String category, Long categoryid, Long memberid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Diary.class, "d");
		query.add(Restrictions.like("type", type, MatchMode.START));
		query.add(Restrictions.eq("category", category));
		query.add(Restrictions.eq("categoryid", categoryid));
		query.addOrder(Order.desc("d.flowernum"));
		
		DetachedCriteria subqry = DetachedCriteria.forClass(Friend.class, "f");
		subqry.add(Restrictions.eq("f.memberfrom", memberid));
		subqry.add(Restrictions.eqProperty("d.memberid", "f.memberto"));
		subqry.setProjection(Projections.property("f.id"));
		query.add(Subqueries.exists(subqry));
		query.addOrder(Order.asc("d.id"));
		List<Diary> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<VoteOption> getVoteOptionByVoteid(Long vid) {
		List<VoteOption> list = readOnlyTemplate.find("from VoteOption v where v.diaryid=? order by v.id asc",vid);
		return list;
	}
	@Override
	public Integer getVotecount(Long did){
		DetachedCriteria query = DetachedCriteria.forClass(VoteOption.class);
		query.add(Restrictions.eq("diaryid", did));
		query.setProjection(Projections.sum("selectednum"));
		List list = this.readOnlyTemplate.findByCriteria(query);
		if(list.get(0) == null) return 0;
		if(StringUtils.equals("null", list.get(0)+"")) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
	@Override
	public List<VoteChoose> getVoteChooseByDiaryidAndMemberid(Long did, Long mid) {
		List list = readOnlyTemplate.find("from VoteChoose vc where vc.diaryid=? and vc.memberid=? order by vc.id asc", did,mid );
		return list;
	}
	@Override
	public Integer getDiaryCommentCount(String tag, Long diaryId){
		DetachedCriteria query = DetachedCriteria.forClass(DiaryComment.class, "c");
		if(StringUtils.isNotBlank(tag)) {
			DetachedCriteria sub = DetachedCriteria.forClass(Diary.class, "d");
			sub.add(Restrictions.eq("d.tag", tag));
			sub.add(Restrictions.eqProperty("d.id", "c.diaryid"));
			sub.setProjection(Projections.property("d.id"));
			
			query.add(Subqueries.exists(sub));
		}
		if(diaryId!=null)query.add(Restrictions.eq("diaryid", diaryId));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}
	@Override
	public boolean isMemberVoted(Long memberid, Long diaryid){
		DetachedCriteria query = DetachedCriteria.forClass(VoteChoose.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("diaryid", diaryid));
		List<VoteChoose> list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return true;
		return false;
	}
	@Override
	public List<Diary> getHotCommentDiary(String citycode, String type, String tag, Long relatedid, int from, int maxnum) {
		Timestamp obj=new Timestamp(System.currentTimeMillis());
		Object obj1=new Timestamp(System.currentTimeMillis()-604800000);
		DetachedCriteria query=DetachedCriteria.forClass(Diary.class);
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("category", tag));
			if(relatedid!=null) query.add(Restrictions.eq("categoryid", relatedid));
		}
		query.add(Restrictions.eq("type", type));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.between("addtime",obj1,obj));
		query.addOrder(Order.desc("replycount"));
		List<Diary> listdiary=readOnlyTemplate.findByCriteria(query,from,maxnum);
		return listdiary;
	}
	@Override
	public <T extends DiaryBase> List<T> getHotCommuDiary(Class<T> clazz, String citycode, boolean isCommu,String type,
			int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(clazz);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(type)) query.add(Restrictions.eq("type", type));
		query.add(Restrictions.gt("communityid",0l));
		query.add(Restrictions.like("status",Status.Y,MatchMode.START));
		query.addOrder(Order.desc("addtime"));
		List<T> diaryList = readOnlyTemplate.findByCriteria(query,from,maxnum);
		return diaryList;
	}
	@Override
	public <T extends DiaryBase> List<T> getDiaryListByStatus(Class<T> clazz, String keyname, String status, Date fromDate, Date endDate, int from, int maxnum){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("status", status));
		if(fromDate!=null) query.add(Restrictions.ge("addtime", fromDate));
		if(endDate!=null) query.add(Restrictions.le("addtime", endDate));
		if(StringUtils.isNotBlank(keyname)) query.add(Restrictions.like("subject", keyname, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		List<T> diaryList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return diaryList;
	}
	@Override
	public <T extends DiaryBase> Integer getDiaryCountByStatus(Class<T> clazz, String keyname, String status, Date fromDate, Date endDate){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("status", status));
		if(fromDate!=null) query.add(Restrictions.ge("addtime", fromDate));
		if(endDate!=null) query.add(Restrictions.le("addtime", endDate));
		if(StringUtils.isNotBlank(keyname)) query.add(Restrictions.like("subject", keyname, MatchMode.ANYWHERE));
		query.setProjection(Projections.rowCount());
		List<T> diaryList = readOnlyTemplate.findByCriteria(query);
		if(diaryList.isEmpty()) return 0;
		return new Integer(diaryList.get(0)+"");
	}
	@Override
	public List<DiaryComment> getDiaryCommentListByStatus(String keyname, String status, Date fromDate, Date endDate, int from, int maxnum){
		DetachedCriteria query=DetachedCriteria.forClass(DiaryComment.class);
		query.add(Restrictions.eq("status", status));
		if(fromDate!=null) query.add(Restrictions.ge("addtime", fromDate));
		if(endDate!=null) query.add(Restrictions.le("addtime", endDate));
		if(StringUtils.isNotBlank(keyname)) query.add(Restrictions.like("body", keyname, MatchMode.ANYWHERE));
		query.addOrder(Order.desc("addtime"));
		List<DiaryComment> diaryList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return diaryList;
	}
	@Override
	public Integer getDiaryCommentCountByStatus(String keyname, String status, Date fromDate, Date endDate){
		DetachedCriteria query=DetachedCriteria.forClass(DiaryComment.class);
		query.add(Restrictions.eq("status", status));
		if(fromDate!=null) query.add(Restrictions.ge("addtime", fromDate));
		if(endDate!=null) query.add(Restrictions.le("addtime", endDate));
		if(StringUtils.isNotBlank(keyname)) query.add(Restrictions.like("body", keyname, MatchMode.ANYWHERE));
		query.setProjection(Projections.rowCount());
		List<DiaryComment> diaryList = readOnlyTemplate.findByCriteria(query);
		if(diaryList.isEmpty()) return 0;
		return new Integer(diaryList.get(0)+"");
	}
	@Override
	public <T extends DiaryBase> List<T> getRepliedDiaryList(Class<T> clazz, Long memberid, int from, int maxnum) {
		String query = "select new map(d.diaryid as id, max(addtime) as addtime) from DiaryComment d " +
				"where memberid=? group by d.diaryid order by addtime desc";
		List<Map> rowList = queryByRowsRange(query, from, maxnum, memberid);
		List<Long> idList = new ArrayList<Long>();
		for(Map row: rowList){
			idList.add((Long) row.get("id"));
		}
		List<T> result = baseDao.getObjectList(clazz, idList);
		return result;
	}
	@Override
	public Integer getRepliedDiaryCount(Class clazz, Long memberid) {
		//只需要要回复
		DetachedCriteria query = DetachedCriteria.forClass(DiaryComment.class, "c");
		query.add(Restrictions.eq("memberid", memberid));
		
		DetachedCriteria subquery = DetachedCriteria.forClass(clazz, "d");
		subquery.add(Restrictions.eqProperty("c.diaryid", "d.id"));
		subquery.setProjection(Projections.property("d.id"));
		query.add(Subqueries.exists(subquery));
		
		query.setProjection(Projections.countDistinct("diaryid"));
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.size()==0) return 0;
		return new Integer(result.get(0)+"");
	}
	@Override
	public List<Map> getMDSDiaryListByKeyname(String citycode, String keyname, String tag, String name, int from, int maxnum){
		String hql=" select new map(d.subject as subject,d.id as id,d.flowernum as flowernum,d.memberid as memberid,d.addtime as addtime, d.categoryid as categoryid) from Diary d where d.type=? and d.citycode=? and exists (select m.id from "+ tag +" m "+
		" where m."+name+" like ? and d.categoryid=m.id) order by d.flowernum,d.id desc";
		List<Map> diaryList=queryByRowsRange(hql, from, maxnum, DiaryConstant.DIARY_TYPE_COMMENT, citycode, keyname);
		return diaryList;
	}
	
	@Override
	public Integer getMDSDiaryCountByKeyname(String citycode, String keyname, String tag, String name){
		String hql=" select count(d.id) from Diary d where d.type=?  and d.citycode=? and exists (select m.id from "+ tag +" m "+
		" where m."+name+ " like ? and d.categoryid=m.id)";
		List<Diary> diaryList=hibernateTemplate.find(hql, DiaryConstant.DIARY_TYPE_COMMENT, citycode, keyname);
		if(diaryList.size()>0) return new Integer(diaryList.get(0)+"");
		return 0;
	}
	
	private List<String> getRealteCityList(){
		List<String> relateCitys = (List<String>) cacheService.get(CacheConstant.REGION_TWOHOUR, CacheConstant.KEY_CITY_KEY);
		if(relateCitys == null){
			GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_SHARECITY);
			String sharecitys = gewaConfig.getContent();
			relateCitys = Arrays.asList(StringUtils.split(sharecitys, ","));
			cacheService.set(CacheConstant.REGION_TWOHOUR, CacheConstant.KEY_CITY_KEY, relateCitys);
		}
		return relateCitys;
	}

	@Override
	public List<Diary> getDiaryBySearchkeyAndOrder(String citycode, String key, Timestamp starttime, Timestamp endtime, String order, int from, int maxnum) {
		return getDiaryList(Diary.class, citycode, null, TagConstant.TAG_DRAMA, null, from, maxnum, null, key, order, null, Status.Y, starttime, endtime);
	}

}
