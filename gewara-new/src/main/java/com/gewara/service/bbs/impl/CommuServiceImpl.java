package com.gewara.service.bbs.impl;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.gewara.constant.Status;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuCard;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.bbs.commu.VisitCommuRecord;
import com.gewara.model.user.Album;
import com.gewara.model.user.Friend;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.RelateService;
import com.gewara.util.DateUtil;

/**
 * @author chenhao(sky_stefanie@hotmail.com)
 */
@Service("commuService")
public class CommuServiceImpl extends BaseServiceImpl implements CommuService {
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public <T extends DiaryBase> List<T> getCommuDiaryListById(Class<T> clazz, Long id,String[] type,Long commuTopicId,int from, int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("communityid",id));
		if(!ArrayUtils.isEmpty(type)){
			if(ArrayUtils.getLength(type) == 1){
				query.add(Restrictions.eq("type",type[0]));
			}else query.add(Restrictions.in("type",type));
		}
		if(commuTopicId!=null) query.add(Restrictions.eq("moderatorid", commuTopicId));
		query.add(Restrictions.ne("type","commu_topic"));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.addOrder(Order.desc("replytime"));
		List<T> listCommuDiary=readOnlyTemplate.findByCriteria(query, from, maxnum);
		return listCommuDiary;
	}
	
	@Override
	public <T extends DiaryBase> List<T> getCommuDiaryListBySearch(Class<T> clazz, Long id, String type, Long commuTopicId, Date fromDate, Integer flag, String text, int from, int maxnum){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("communityid",id));
		if(StringUtils.isNotBlank(type))query.add(Restrictions.like("type",type,MatchMode.START));
		if(commuTopicId!=null) query.add(Restrictions.eq("moderatorid", commuTopicId));
		if(fromDate != null){
			query.add(Restrictions.between("addtime", fromDate, DateUtil.addDay(fromDate, 1)));
		}
		if(flag != null){
			if(flag == 1){
				query.add(Restrictions.like("summary",text,MatchMode.ANYWHERE));
			}else{
				query.add(Restrictions.like("membername",text,MatchMode.ANYWHERE));
			}
		}
		query.add(Restrictions.like("status", "Y", MatchMode.START));
		query.addOrder(Order.desc("replytime"));
		List<T> listCommuDiary=readOnlyTemplate.findByCriteria(query, from, maxnum);
		return listCommuDiary;
	}
	
	@Override
	public <T extends DiaryBase> Integer getCommuDiaryCountBySearch(Class<T> clazz, Long id,String type,Long commuTopicId, Date fromDate, Integer flag, String text){
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("communityid",id));
		if(StringUtils.isNotBlank(type))query.add(Restrictions.like("type",type,MatchMode.START));
		if(commuTopicId!=null) query.add(Restrictions.eq("moderatorid", commuTopicId));
		if(fromDate != null){
			query.add(Restrictions.between("addtime", fromDate, DateUtil.addDay(fromDate, 1)));
		}
		if(flag != null){
			if(flag == 1){
				query.add(Restrictions.like("summary",text,MatchMode.ANYWHERE));
			}else{
				query.add(Restrictions.like("membername",text,MatchMode.ANYWHERE));
			}
		}
		query.add(Restrictions.like("status", "Y", MatchMode.START));
		query.setProjection(Projections.rowCount());
		List listCommuDiary=readOnlyTemplate.findByCriteria(query);
		if(listCommuDiary.get(0)==null) return 0;
		return new Integer(listCommuDiary.get(0)+"");
	}

	@Override
	public <T extends DiaryBase> Integer getCommuDiaryCount(Class<T> clazz, Long id,String[] type,Long commuTopicId) {
		DetachedCriteria query=DetachedCriteria.forClass(clazz);
		query.add(Restrictions.eq("communityid", id));
		query.add(Restrictions.like("status", "Y", MatchMode.START));
		if(!ArrayUtils.isEmpty(type)){
			if(ArrayUtils.getLength(type) == 1){
				query.add(Restrictions.eq("type", type[0]));
			}else query.add(Restrictions.in("type",type));
		}
		if(commuTopicId!=null) query.add(Restrictions.eq("moderatorid", commuTopicId));
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.ne("type","commu_topic"));
		List list=readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public Integer getCommumemberCount(Long id, Long adminid) {
		return getCommuCount(id, adminid, true);
	}
	private Integer getCommuCount(Long id, Long memberid, boolean isAdmin){
		DetachedCriteria query=DetachedCriteria.forClass(CommuMember.class);
		if(id != null) query.add(Restrictions.eq("commuid", id));
		if(memberid != null){
			if(isAdmin){
				query.add(Restrictions.ne("memberid", memberid));
			}else{
				query.add(Restrictions.eq("memberid", memberid));
			}
		}
		query.setProjection(Projections.rowCount());
		List list=readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0) + "");
	}
	
	@Override
	public List<Diary> getAllCommuDiaryById(Long id, int from, int maxnum) { 
		String hql="select d from Diary d where d.status like ? and d.communityid in " +
				"(select commuid from CommuMember where memberid=?) " +
				" order by d.replytime desc";
		List<Diary> listCommuDiary=queryByRowsRange(hql, from, maxnum, Status.Y+"%", id);
		return listCommuDiary;
	}

	@Override
	public List<Member> getAllCommuMemberById(Long id, int from, int maxnum) {
		String hql="select m from Member m where m.id in " +
				"(select memberid from CommuMember where commuid in " +
				"(select commuid from CommuMember where memberid=?))";
		List<Member> listCommuMember=queryByRowsRange(hql, from, maxnum, id);
		return listCommuMember;
	}

	@Override
	public Integer getAllCommuDiaryCountById(Long id) {
		String hql="select count(*) from Diary d where d.status like ? and d.communityid in " +
		"(select commuid from CommuMember where memberid=?)";
		List listCommuDiaryCount = readOnlyTemplate.find(hql, Status.Y+"%",id);
		if(listCommuDiaryCount.get(0)==null) return 0;
		return new Integer(listCommuDiaryCount.get(0)+"");
	}

	@Override
	public Integer getAllCommuMemberCountById(Long id) {
		String hql="select count(*) from Member m where m.id in " +
		"(select memberid from CommuMember where commuid in " +
		"(select commuid from CommuMember where memberid=?))";
		List listCommuMemberCount = readOnlyTemplate.find(hql, id);
		if(listCommuMemberCount.get(0)==null) return 0;
		return new Integer(listCommuMemberCount.get(0)+"");
	}

	@Override
	public List<Map> getCommuSmallByTag(String tag) {
		String hql="select new map(smallcategoryid as smallid,count(smallcategoryid) as countSmaill," +
				"smallcategory as smalltag) from Commu  where tag=? " +
				"and smallcategoryid is not null and status=?  group by smallcategory,smallcategoryid";
		List<Map> listCommuSmall = readOnlyTemplate.find(hql, tag,Status.Y);
		return listCommuSmall;
	}

	@Override
	public List<Map> getCommuType() {
		String hql="select new map(nvl(tag,'qita') as tag,count(*) as tagcount) from Commu where status=? group by tag " +
				 "order by decode(tag, 'cinema', '1 ', 'ktv', '2', 'bar', '3', 'gym', '4', 'sport','5','qita','6')";
		List<Map> listCommuType = readOnlyTemplate.find(hql,Status.Y);
		return listCommuType;
	}
	
	@Override
	public boolean isCommuMember(Long commuid, Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(CommuMember.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("commuid", commuid));
		List<CommuMember> commuMemberList = readOnlyTemplate.findByCriteria(query);
		if(commuMemberList.size()>0){
			return true;
		}
		return false;
	}

	
	@Override
	public List<Commu> getCommunityListByMemberid(Long memberid, int from, int maxrows){
		String hql = "from Commu c where exists(select cm.id from CommuMember cm where cm.memberid=? and cm.commuid=c.id) and c.status=?";
		List<Commu> list = queryByRowsRange(hql, from, maxrows, memberid, Status.Y);
		return list;
	}
	@Override
	public List<Commu> getCommunityListByHotvalue(String tag, Long relatedid, boolean commuNum, Long hotvalue,int from,int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if(relatedid != null)query.add(Restrictions.eq("relatedid", relatedid));
		if(hotvalue != null)query.add(Restrictions.eq("hotvalue", hotvalue));
		query.add(Restrictions.eq("status", Status.Y));
		if(commuNum)query.addOrder(Order.desc("commumembercount"));
		query.addOrder(Order.desc("updatetime"));
		List<Commu> list = readOnlyTemplate.findByCriteria(query,from,maxnum);
		return list;
	}
	@Override
	public List<Commu> getCommunityListByHotvalue(Long hotvalue,int from,int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("hotvalue", hotvalue));
		query.add(Restrictions.eq("status", Status.Y));
		query.addOrder(Order.desc("updatetime"));
		List<Commu> list = readOnlyTemplate.findByCriteria(query,from,maxnum);
		return list;
	}
	@Override
	public Integer getCommunityCountByHotvalue(Long hotvalue){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("hotvalue", hotvalue));
		query.add(Restrictions.eq("status", Status.Y));
		query.setProjection(Projections.rowCount());
		List<Commu> list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return new Integer (list.get(0)+"");
	}
	@Override
	public List<CommuMember> getCommuMemberById(Long id, Long adminid, Long subadminid, String blackmember, int from, int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(CommuMember.class);
		if(StringUtils.isNotBlank(blackmember)) query.add(Restrictions.eq("flag", CommuMember.FLAG_BLACK));
		if(adminid!=null) query.add(Restrictions.ne("memberid", adminid)); 
		if(subadminid!=null) query.add(Restrictions.ne("memberid", subadminid)); 
		query.add(Restrictions.eq("commuid", id));
		query.addOrder(Order.desc("addtime"));
		List<CommuMember>  result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<CommuMember> getCommuMemberByCommu(String tag, Long relatedid, Long adminid, Long subadminid, String blackmember, int from, int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(CommuMember.class, "cm");
		if(StringUtils.isNotBlank(blackmember)) query.add(Restrictions.eq("cm.flag", CommuMember.FLAG_BLACK));
		if(adminid!=null) query.add(Restrictions.ne("cm.memberid", adminid)); 
		if(subadminid!=null) query.add(Restrictions.ne("cm.memberid", subadminid)); 
		DetachedCriteria subQuery = DetachedCriteria.forClass(Commu.class, "c");
		subQuery.setProjection(Projections.property("c.id"));
		subQuery.add(Restrictions.eqProperty("c.id", "cm.commuid"));
		subQuery.add(Restrictions.eq("c.status", Status.Y));
		if(StringUtils.isNotBlank(tag)) subQuery.add(Restrictions.eq("c.tag", tag));
		if(relatedid != null) subQuery.add(Restrictions.eq("c.relatedid", relatedid));
		query.add(Subqueries.exists(subQuery));
		query.addOrder(Order.desc("cm.addtime"));
		List<CommuMember>  result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<Long> getCommuMemberIdListByCommuId(Long commuid){
		DetachedCriteria query=DetachedCriteria.forClass(CommuMember.class);
		query.add(Restrictions.eq("commuid", commuid));
		query.setProjection(Projections.property("memberid"));
		List<Long> memberList = readOnlyTemplate.findByCriteria(query);
		return memberList;
	}
		@Override
	public List<Commu> getHotCommuList(int from, int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("status", Status.Y));
		query.addOrder(Order.desc("hotvalue"));
		List<Commu> listHotCommu=readOnlyTemplate.findByCriteria(query,from,maxnum);
		return listHotCommu;
	}

	@Override
	public Integer getCommuCountByMemberId(Long memberid) {
		String hql="select count(*) from Commu where id in (select commuid from CommuMember where memberid=?) and status=? order by addtime desc";
		List listCommu=readOnlyTemplate.find(hql, memberid,Status.Y);
		if(listCommu.get(0)==null) return 0;
		return new Integer(listCommu.get(0)+"");
	}

	@Override
	public List<Commu> getCommuMemberLoveToCommuList(Long commuid, int from, int maxnum) {
		String hql="from Commu where status=? and id in (select commuid from CommuMember where memberid in (select memberid from CommuMember where commuid=?)) order by addtime desc";
		List<Commu> listCommu=queryByRowsRange(hql, from, maxnum, Status.Y,commuid);
		return listCommu;
	}

	@Override
	public List<Album> getJoinedCommuAlbumList(Long memberid, int from, int maxnum) {
		final String hql = "from Album c where c.commuid in(select ch.commuid from CommuMember ch where ch.memberid = ?)";
		List<Album> list = queryByRowsRange(hql, from, maxnum, memberid);
		return list;
	}
	@Override
	public Integer getJoinedCommuAlbumCount(Long memberid) {
		final String hql = "select count(c.id) from Album c where c.commuid in(select ch.commuid from CommuMember ch where ch.memberid=?)";
		List list = readOnlyTemplate.find(hql, memberid);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public List<Album> getCommuAlbumById(final Long id, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Album.class);
		query.add(Restrictions.eq("commuid", id));
		List<Album> result = readOnlyTemplate.findByCriteria(query,from,maxnum);
		return result;
	}
	@Override
	public Integer getCommuAlbumCountById(final Long id) {
		final String hql = "select count(*) from Album c where c.commuid =?";
		List list = readOnlyTemplate.find(hql, id);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public Integer getPictureCountByCommuid(Long commuid){
		final String sql = "select count(*) from Picture t where t.tag=? and t.relatedid in (select a.id from Album a where a.commuid = ? )";
		List list = readOnlyTemplate.find(sql, "album", commuid);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<Commu> getAlikeCommuList(Long commuid, int from, int maxnum){
		String hql="select new map(commuid as commuid, count(memberid) as vcount) from CommuMember " +
				"where memberid in (select memberid from CommuMember where commuid=?) " +
				"group by commuid order by count(memberid)";
		List<Map> rowList = queryByRowsRange(hql, from, maxnum, commuid);
		List<Commu> commuList = new ArrayList<Commu>();
		for(Map row:rowList){
			Commu commu = baseDao.getObject(Commu.class, (Long) row.get("commuid"));
			if(commu == null || !commu.hasStatus(Status.Y)) continue;
			commuList.add(commu);
		}
		return commuList;
	}

	@Override
	public boolean isJoinCommuMember(Long memberid, Long commuid) {
		String hql="select count(*) from CommuMember where commuid=? and memberid=?";
		List list=readOnlyTemplate.find(hql,commuid,memberid);
		if (list.get(0).equals(0L)) return false;
		return true;
	}

	@Override
	public void joinCommuMember(Long memberid, Long commuid) {
		CommuMember cm = new CommuMember(memberid);
		cm.setCommuid(commuid);
		cm.setFlag(CommuMember.FLAG_NORMAL);
		baseDao.saveObject(cm);
	}

	
	@Override
	public List<Commu> getCommuBySearch(String tag, String citycode, Long smallcategoryid, String value, String sort, String countycode, int from, int maxnum) {
		List<Commu> listCommu = null;
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("status",Status.Y));
		if("qita".equals(tag)) query.add(Restrictions.isNull("tag"));
		if(StringUtils.isNotBlank(countycode)) query.add(Restrictions.eq("countycode", countycode));
		if(StringUtils.isNotBlank(tag) && !"qita".equals(tag)) query.add(Restrictions.eq("tag", tag));
		if(smallcategoryid!=null && "cinema".equals(tag)){
			query.add(Restrictions.eq("smallcategory", "movie"));
			query.add(Restrictions.eq("smallcategoryid", smallcategoryid));
		}else if(smallcategoryid!=null && "gym".equals(tag)){
			query.add(Restrictions.eq("smallcategory", "gymcourse"));
			query.add(Restrictions.eq("smallcategoryid", smallcategoryid));
		}else if(smallcategoryid!=null && "sport".equals(tag)){
			query.add(Restrictions.eq("smallcategory", "sportservice"));
			query.add(Restrictions.eq("smallcategoryid", smallcategoryid));
		}
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(value)) query.add(Restrictions.or(Restrictions.ilike("name", value, MatchMode.ANYWHERE),Restrictions.ilike("info", value, MatchMode.ANYWHERE)));
		query.addOrder(Order.desc("checkstatus"));
		if(StringUtils.isNotBlank(sort)) query.addOrder(Order.desc(sort));
		else query.addOrder(Order.desc("commumembercount"));
		query.addOrder(Order.asc("id"));
		listCommu=readOnlyTemplate.findByCriteria(query, from, maxnum);
		return listCommu;
	}

	@Override
	public Integer getCommuCountBySearch(String tag, String citycode, Long smallcategoryid,
			String value,  String sort, String countycode) {
		
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("status",Status.Y));
		if("qita".equals(tag))
			query.add(Restrictions.isNull("tag"));
		if(StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(countycode))
			query.add(Restrictions.eq("countycode", countycode));
		if(StringUtils.isNotBlank(tag) && !"qita".equals(tag)) query.add(Restrictions.eq("tag", tag));
		if(smallcategoryid!=null && tag.equals("cinema")){
			query.add(Restrictions.eq("smallcategory", "movie"));
			query.add(Restrictions.eq("smallcategoryid", smallcategoryid));
		}else if(smallcategoryid!=null && tag.equals("gym")){
			query.add(Restrictions.eq("smallcategory", "gymcourse"));
			query.add(Restrictions.eq("smallcategoryid", smallcategoryid));
		}else if(smallcategoryid!=null && tag.equals("sport")){
			query.add(Restrictions.eq("smallcategory", "sportservice"));
			query.add(Restrictions.eq("smallcategoryid", smallcategoryid));
		}
		if(StringUtils.isNotBlank(value)) 
			query.add(Restrictions.or(Restrictions.ilike("name", value.trim(), MatchMode.ANYWHERE),Restrictions.ilike("info", value, MatchMode.ANYWHERE)));
		if(StringUtils.isNotBlank(sort)) 
			query.addOrder(Order.desc(sort));
		query.setProjection(Projections.rowCount());
		List listCommu=readOnlyTemplate.findByCriteria(query);
		if(listCommu.get(0)==null)
			return 0;
		return new Integer(listCommu.get(0)+"");
	}
	@Override
	public List<Commu> getCommuList(int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("status",Status.Y));
		query.addOrder(Order.desc("addtime"));
		List<Commu> commuList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return commuList;
	}
	@Override
	public CommuCard getCommuCardByCommuidAndMemberid(Long memberid, Long commuid){
		DetachedCriteria query=DetachedCriteria.forClass(CommuCard.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("commuid", commuid));
		List<CommuCard> commuCardList=readOnlyTemplate.findByCriteria(query);
		if(commuCardList.size()>0) return commuCardList.get(0);
		return  null;
	}
	@Override
	public VisitCommuRecord getVisitCommuRecordByCommuidAndMemberid(Long commuid, Long memberid){
		DetachedCriteria query=DetachedCriteria.forClass(VisitCommuRecord.class);
		query.add(Restrictions.eq("commuid", commuid));
		query.add(Restrictions.eq("memberid", memberid));
		List<VisitCommuRecord> commuCardList=readOnlyTemplate.findByCriteria(query);
		if(commuCardList.size()>0) return commuCardList.get(0);
		return  null;
	}
	@Override
	public CommuMember getCommuMemberByMemberidAndCommuid(Long memberid, Long commuid){
		DetachedCriteria query=DetachedCriteria.forClass(CommuMember.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("commuid", commuid));
		List<CommuMember> commuMemberList = readOnlyTemplate.findByCriteria(query);
		if(commuMemberList.size()>0) return commuMemberList.get(0);
		return  null;
	}

	@Override
	public boolean isExistCommuName(Long commuid,String communame) {
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("name",communame));
		if(commuid != null) query.add(Restrictions.ne("id", commuid));
		query.add(Restrictions.ne("status",Status.N_DELETE));
		List list = readOnlyTemplate.findByCriteria(query);
		if(list.size()==0) return false; 
		return true;
	}
	@Override
	public boolean isHadVisitCommuByMemberidAndDate(Long memberid, String date){
		String query = "from VisitCommuRecord where memberid = ?  and to_char(addtime,'yyyy-mm-dd')=?";
		List<VisitCommuRecord> vcrList = readOnlyTemplate.find(query, memberid, date);
		if(vcrList.isEmpty()) return false;
		return true;
	}
	@Override
	public List<Commu> getCommuListByTagAndRelatedid(String citycode, String tag, Long relatedid, int from ,int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("smallcategory", tag));
			if(relatedid!=null) query.add(Restrictions.eq("smallcategoryid", relatedid));
		}
		query.addOrder(Order.desc("commumembercount"));
		List<Commu> commuList=readOnlyTemplate.findByCriteria(query, from, maxnum);
		return commuList;
	}
	
	
	/**
	 *  根据 commuid, memberid 匹配 CommuManage, 查询状态
	 */
	public String getCheckStatusByIDAndMemID(Long commuid){
		DetachedCriteria query = DetachedCriteria.forClass(CommuManage.class);
		query.add(Restrictions.eq("commuid", commuid));
		List<CommuManage> commuManageList = readOnlyTemplate.findByCriteria(query);
		if(commuManageList.size() > 0){
			return commuManageList.get(0).getCheckstatus();
		}
		return null;
	}
	public void initCommuRelate(List<Commu> commuList) {
		if(commuList==null) return;
		Object relate = null;
		for (Commu commu : commuList) {
			if (StringUtils.isNotBlank(commu.getTag()) && commu.getRelatedid() != null) {
				relate = relateService.getRelatedObject((commu.getTag()), commu.getRelatedid());
				commu.setRelate(relate);
			}
			if (StringUtils.isNotBlank(commu.getSmallcategory()) && commu.getSmallcategoryid() != null) {
				relate = relateService.getRelatedObject((commu.getSmallcategory()), commu.getSmallcategoryid());
				commu.setRelate2(relate);
			}
		}
	}
	
	
	@Override
	public boolean isCommuAdminByMemberid(Long commuid, Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.or(Restrictions.eq("adminid", memberid), Restrictions.eq("subadminid", memberid)));
		query.add(Restrictions.eq("id", commuid));
		List<Commu> commuList = readOnlyTemplate.findByCriteria(query);
		if(commuList.size()>0){
			return true;
		}
		return false;
	}
	@Override
	public Integer getCommuCountByCountycode(String countycode){
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("countycode", countycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.setProjection(Projections.rowCount());
		List<Commu> commuList=readOnlyTemplate.findByCriteria(query);
		if(commuList.isEmpty()) return 0;
		return new Integer(commuList.get(0)+"");
	}
	@Override
	public Integer getCommuDiaryCountByCommuid(Long commuid){
		DetachedCriteria query = DetachedCriteria.forClass(Diary.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("communityid", commuid));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		List diaryList=readOnlyTemplate.findByCriteria(query);
		if(diaryList.isEmpty()) return 0;
		return new Integer(diaryList.get(0)+"");
	}
	
	/**
	 *  根据圈子ID 查找对应的commuManage
	 */
	@Override
	public CommuManage getCommuManageByCommuid(Long commuid){
		DetachedCriteria query = DetachedCriteria.forClass(CommuManage.class);
		query.add(Restrictions.eq("commuid", commuid));
		List<CommuManage> commuManageList = readOnlyTemplate.findByCriteria(query);
		if(commuManageList != null && commuManageList.size() > 0){
			return commuManageList.get(0);
		}
		return null;
	}
	@Override
	public Map<Long/*friendid*/, Commu> getFriendCommuMap(Long memberid, int from, int maxnum){
		String query = "select new map(max(cm.memberid) as memberid, cm.commuid as commuid) " +
				"from CommuMember cm where exists(select f.id from Friend f where f.memberfrom = ? and f.memberto=cm.memberid)" +
				" group by cm.commuid order by cm.commuid";
		List<Map> rowList = queryByRowsRange(query, from, maxnum, memberid);
		Map<Long, Commu> result = new HashMap<Long, Commu>();
		for(Map row:rowList){
			Commu commu = baseDao.getObject(Commu.class, (Long)row.get("commuid"));
			if(StringUtils.startsWith(commu.getStatus(), Status.Y)) result.put((Long)row.get("memberid"), commu);
		}
		return result;
	}
	@Override
	public Integer getFriendCommuCount(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(CommuMember.class, "cm");
		
		DetachedCriteria subqry = DetachedCriteria.forClass(Friend.class, "f");
		subqry.add(Restrictions.eq("f.memberfrom", memberid));
		subqry.add(Restrictions.eqProperty("cm.memberid", "f.memberto"));
		subqry.setProjection(Projections.property("f.id"));
		query.add(Subqueries.exists(subqry));

		query.setProjection(Projections.countDistinct("commuid"));

		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return new Integer(result.get(0)+"");
	}
	@Override
	public List<CommuMember> getCommuMemberListByMemberid(Long memberid, int from, int maxnum){
		DetachedCriteria query=DetachedCriteria.forClass(CommuMember.class);
		query.add(Restrictions.eq("memberid", memberid));
		List<CommuMember> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<CommuCard> getCommuCardListByMemberid(Long memberid, int from, int maxnum){
		DetachedCriteria query=DetachedCriteria.forClass(CommuCard.class);
		query.add(Restrictions.eq("memberid", memberid));
		List<CommuCard> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public List<Commu> getOwnerCommuList(Long memberid){
		DetachedCriteria query=DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.eq("adminid", memberid));
		query.add(Restrictions.eq("status", Status.Y));
		List<Commu> commuList=readOnlyTemplate.findByCriteria(query);
		return commuList;
	}
	@Override
	public List<Commu> getCommuListByMemberId(Long memberid, int from, int maxnum) {
		String hql="from Commu where id in (select commuid from CommuMember where memberid=?) and status=? order by addtime desc";
		List<Commu> listCommu=queryByRowsRange(hql, from, maxnum, memberid,Status.Y);
		return listCommu;
	}

	@Override
	public List<Commu> getManagedCommuList(Long memberid){
		DetachedCriteria query=DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.or(Restrictions.eq("adminid", memberid), 
				Restrictions.eq("subadminid", memberid)));
		List<Commu> commuList=readOnlyTemplate.findByCriteria(query);
		return commuList;
	}
	
	@Override
	public void initCommunityRelate(List<Commu> communityList) {
		for (Commu community : communityList) {
			Object relate = relateService.getRelatedObject(community.getTag(), community.getRelatedid());
			community.setRelate(relate);
			Object relate2 = relateService.getRelatedObject(community.getSmallcategory(), community.getSmallcategoryid());
			community.setRelate2(relate2);
		}
	}
	
	final String comm_relatedid = "from Commu d where d.tag = ? and d.relatedid = ? or d.smallcategory = ? and d.smallcategoryid = ? order by d.addtime desc";

	@Override
	public List<Commu> getCommunityListByRelatedId(String tag, Long relatedid, int from, int maxnum) {
		return queryByRowsRange(comm_relatedid, from, maxnum, tag, relatedid, tag, relatedid);
	}

	@Override
	public List<Commu> getCommunityListByTag(String tag, String order, int from, int maxrows) {
		String commQry = "from Commu d where (d.tag = ? or d.smallcategory = ?) and status = ? ";
		if(ClassUtils.hasMethod(Commu.class, "get" + StringUtils.capitalize(order))){
			commQry += " order by " + order + " desc ";
		}else commQry += " order by hotvalue desc ";
		List result = queryByRowsRange(commQry, from, maxrows, tag, tag, Status.Y);
		return result;
	}

	@Override
	public List<Commu> getCommuListOrderByProperty(String tag, int from, int maxnum, String order) {
		String qry = "from Commu d where (d.tag = ? or d.smallcategory = ?) and status = ?  order by " + order + " desc";
		List result = queryByRowsRange(qry, from, maxnum, tag, tag, Status.Y);
		return result;
	}

	@Override
	public List<Long> getCommuIdByTag(String tag) {
		List<Long> list = readOnlyTemplate.find("select c.id from Commu c where c.tag=?", tag);
		return list;
	}
	@Override
	public Integer getCommunityCountByRelatedId(String tag, Long relatedId) {
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		LogicalExpression tagR = Restrictions.and(Restrictions.eq("tag", tag), Restrictions.eq("relatedid", relatedId));
		LogicalExpression categoryR = Restrictions.and(Restrictions.eq("smallcategory", tag), Restrictions.eq("smallcategoryid", relatedId));
		query.add(Restrictions.or(tagR, categoryR));
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		if (result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public Integer getCommunityCountByTag(String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.add(Restrictions.or(Restrictions.eq("tag", tag), Restrictions.eq("smallcategory", tag)));
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		if (result.isEmpty())
			return 0;
		return Integer.parseInt("" + result.get(0));
	}

}