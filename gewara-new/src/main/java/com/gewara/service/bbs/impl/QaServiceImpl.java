package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaExpert;
import com.gewara.model.bbs.qa.GewaQaPoint;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.QaService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.MemberService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.RelateService;
import com.gewara.util.DateUtil;

@Service("qaService")
public class QaServiceImpl extends BaseServiceImpl implements QaService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("memberService")
	private MemberService memberService;
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	@Override
	public List<GewaQuestion> getQuestionListByHotvalue(String citycode, int hotvalue, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("hotvalue", hotvalue));
		query.addOrder(Order.desc("recommendtime"));
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public List<GewaQuestion> getQuestionListByQuestionstatus(String citycode, String questionstatus, String order, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(GewaQuestion.QS_STATUS_Y.equals(questionstatus))
			query.add(Restrictions.isNotNull("dealtime"));
		if (GewaQuestion.QS_STATUS_N.equals(questionstatus))
			query.add(Restrictions.or(Restrictions.eq("questionstatus", GewaQuestion.QS_STATUS_N), Restrictions.eq("questionstatus",
					GewaQuestion.QS_STATUS_Z)));
		else
			query.add(Restrictions.eq("questionstatus", questionstatus));
		if (StringUtils.isNotBlank(order))
			query.addOrder(Order.desc(order));
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public List<GewaQuestion> getQuestionListByStatus(String status, Date fromDate, Date endDate, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(status))query.add(Restrictions.eq("status", status));
		if(fromDate!=null) query.add(Restrictions.ge("addtime", fromDate));
		if(endDate!=null) query.add(Restrictions.le("addtime", endDate));
		query.addOrder(Order.desc("addtime"));
		List<GewaQuestion> questionList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return questionList;
	}
	
	@Override
	public Integer getQuestionCountByStatus(String status, Date fromDate, Date endDate){
		DetachedCriteria query=DetachedCriteria.forClass(GewaQuestion.class);
		query.add(Restrictions.eq("status", status));
		if(fromDate!=null) query.add(Restrictions.ge("addtime", fromDate));
		if(endDate!=null) query.add(Restrictions.le("addtime", endDate));
		query.setProjection(Projections.rowCount());
		List questionList = readOnlyTemplate.findByCriteria(query);
		if(questionList.isEmpty()) return 0;
		return new Integer(questionList.get(0)+"");
	}

	@Override
	public List<GewaAnswer> getAnswerListByQuestionid(Long questionid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("questionid", questionid));
		query.addOrder(Order.asc("addtime"));
		List<GewaAnswer> list = readOnlyTemplate.findByCriteria(query);
		return list;
	}

	@Override
	public Integer getAnswerCount(Long questionid) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaAnswer.class);
		criteria.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(questionid!=null)
			criteria.add(Restrictions.eq("questionid", questionid));
		criteria.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(criteria);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<GewaAnswer> getAnswerListByQuestionId(int start, int maxnum, Long questionid) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaAnswer.class);
		criteria.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(questionid!=null)
			criteria.add(Restrictions.eq("questionid", questionid));
		criteria.addOrder(Order.desc("addtime"));
		List<GewaAnswer> answerList = readOnlyTemplate.findByCriteria(criteria, start, maxnum);
		return answerList;
	}
	
	@Override
	public List<GewaAnswer> getAnswerListByQuestionAndMemId(int start, int maxnum, Long questionid, Long memberId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaAnswer.class);
		criteria.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(questionid!=null)
			criteria.add(Restrictions.eq("questionid", questionid));
		if(memberId!=null)
			criteria.add(Restrictions.eq("memberid", memberId));
		criteria.addOrder(Order.desc("addtime"));
		List<GewaAnswer> answerList = readOnlyTemplate.findByCriteria(criteria, start, maxnum);
		return answerList;
	}

	@Override
	public Integer getQuestionCountByQuestionstatus(String citycode, String questionstatus) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if (GewaQuestion.QS_STATUS_N.equals(questionstatus))
			query.add(Restrictions.or(Restrictions.eq("questionstatus", GewaQuestion.QS_STATUS_N), Restrictions.eq("questionstatus",
					GewaQuestion.QS_STATUS_Z)));
		else
			query.add(Restrictions.eq("questionstatus", questionstatus));
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public boolean isAnswerQuestion(Long qid, Long mid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("questionid", qid));
		query.add(Restrictions.eq("memberid", mid));
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return false;
		return Integer.parseInt(""+list.get(0)) > 0;
	}

	@Override
	public GewaAnswer getBestAnswerByQuestionid(Long qid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("questionid", qid));
		query.add(Restrictions.eq("answerstatus", GewaAnswer.AS_STATUS_Y));
		List<GewaAnswer> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public GewaQaExpert getQaExpertByMemberid(Long mid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQaExpert.class);
		// query.add(Restrictions.eq("status", GewaQaExpert.STATUS_Y));
		query.add(Restrictions.eq("memberid", mid));
		// query.setProjection(Projections.rowCount());
		List<GewaQaExpert> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public boolean updateQAHotValue(Long id, Integer hotvalue) {
		GewaQuestion gq = baseDao.getObject(GewaQuestion.class, id);
		if (gq != null) {
			gq.setHotvalue(hotvalue);
			gq.setRecommendtime(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(gq);
			return true;
		}
		return false;
	}

	@Override
	public boolean updateQAExpertHotValue(Long id, Integer hotvalue) {
		GewaQaExpert gqa = baseDao.getObject(GewaQaExpert.class, id);
		if (gqa != null) {
			gqa.setHotvalue(hotvalue);
			gqa.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			baseDao.saveObject(gqa);
			return true;
		}
		return false;
	}

	@Override
	public Integer getQAExpertCount() {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaQaExpert.class);
		criteria.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(criteria);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<GewaQaExpert> getQaExpertList() {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaQaExpert.class);
		criteria.addOrder(Order.desc("updatetime"));
		List<GewaQaExpert> QaExpertlist = readOnlyTemplate.findByCriteria(criteria);
		return QaExpertlist;
	}

	@Override
	public List<GewaQuestion> getQuestionByTagAndRelatedid(String citycode, String tag, Long relatedid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("tag", tag));
		if (relatedid != null) {
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		query.addOrder(Order.desc("updatetime"));
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public List<GewaQuestion> getQuestionByCategoryAndCategoryid(String citycode, String category, Long categoryid, int from, int maxnum) {
		return getQuestionByCategoryAndCategoryid(citycode, category, categoryid, false, null, from, maxnum);
	}
	
	@Override
	public List<GewaQuestion> getQuestionByCategoryAndCategoryid(String citycode, String category, Long categoryid, boolean status, String questionstatus, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("category", category));
		if (categoryid != null) {
			query.add(Restrictions.eq("categoryid", categoryid));
		}
		if(status && StringUtils.isNotBlank(questionstatus)){
			query.add(Restrictions.eq("questionstatus", questionstatus));
		}else if(status){
			query.add(Restrictions.ne("questionstatus", GewaQuestion.QS_STATUS_Z));
		}
		query.addOrder(Order.desc("updatetime"));
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	
	@Override
	public Integer getQuestionCountByCategoryAndCid(String citycode, String category, Long categoryid){
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if(StringUtils.isNotBlank(category)) query.add(Restrictions.eq("category", category));
		if(categoryid != null) query.add(Restrictions.eq("categoryid", categoryid));
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(list.get(0)+"");
	}

	@Override
	public Map<Member, Integer> getTopMemberListByBestAnswer(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQaPoint.class);
		query.add(Restrictions.eq("tag", GewaQaPoint.TAG_BESTANSWER));
		query.setProjection(Projections.projectionList().add(Projections.alias(Projections.rowCount(), "count")).add(
				Projections.alias(Projections.groupProperty("memberid"), "memberid")));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		query.addOrder(Order.desc("count"));
		List<Map> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		Map<Member, Integer> miMap = new LinkedHashMap<Member, Integer>();
		for (Map m : list) {
			Member member = baseDao.getObject(Member.class, new Long(m.get("memberid") + ""));
			Integer point = new Integer(m.get("count") + "");
			miMap.put(member, point);
		}
		return miMap;
	}

	@Override
	public GewaQaExpert getQaExpertStatusById(Long id) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaQaExpert.class);
		criteria.add(Restrictions.eq("id", id));
		List<GewaQaExpert> list = readOnlyTemplate.findByCriteria(criteria);
		if (list.size() > 0)
			return list.get(0);
		return null;
	}

	@Override
	public List<GewaAnswer> getAnswerByMemberId(Long Memberid) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GewaAnswer.class);
		criteria.add(Restrictions.like("status", Status.Y, MatchMode.START));
		criteria.add(Restrictions.eq("memberid", Memberid));
		List<GewaAnswer> list = readOnlyTemplate.findByCriteria(criteria);
		return list;
	}

	@Override
	public Map<Member, Integer> getTopMemberListByAnswer(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.setProjection(Projections.projectionList().add(Projections.alias(Projections.rowCount(), "count")).add(
				Projections.alias(Projections.groupProperty("memberid"), "memberid")));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		query.addOrder(Order.desc("count"));
		List<Map> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		Map<Member, Integer> miMap = new LinkedHashMap<Member, Integer>();
		for (Map m : list) {
			Member member = baseDao.getObject(Member.class, new Long(m.get("memberid") + ""));
			Integer point = new Integer(m.get("count") + "");
			miMap.put(member, point);
		}
		return miMap;
	}

	@Override
	public Integer getTopMemberCountByAnswer() {
		String hql = "select count(*) from GewaAnswer a group by a.memberid";
		List list = readOnlyTemplate.find(hql);
		if (!list.isEmpty())
			return list.size();
		return 0;
	}

	@Override
	public Integer getTopMemberCountByBestAnswer() {
		String hql = "select count(*) from GewaQaPoint a where a.tag=? group by a.memberid";
		List list = readOnlyTemplate.find(hql, GewaQaPoint.TAG_BESTANSWER);
		if (!list.isEmpty())
			return list.size();
		return 0;
	}

	@Override
	public List<GewaQuestion> getQuestionListByMemberid(Long memberid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class, "q");
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		
		DetachedCriteria answer = DetachedCriteria.forClass(GewaAnswer.class, "a");
		answer.add(Restrictions.like("a.status", Status.Y, MatchMode.START));
		answer.add(Restrictions.eq("a.memberid", memberid));
		answer.add(Restrictions.eq("a.answerstatus", GewaAnswer.AS_STATUS_Y));
		answer.add(Restrictions.eqProperty("q.id", "a.questionid"));
		answer.setProjection(Projections.property("a.questionid"));
		query.add(Subqueries.exists(answer));
		query.addOrder(Order.desc("q.replycount"));
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public Map<Map<Object, String>, Integer> getQuestionListByTagGroup(String tag, int from, int maxnum) {
		String hql = "select new map(q.relatedid as relatedid, count(*) as count) from GewaQuestion q where q.relatedid!=null and q.tag=? and q.questionstatus!=?  group by q.relatedid order by count(*) desc)";
		
		List<Map> map = queryByRowsRange(hql, from, maxnum, tag, GewaQuestion.QS_STATUS_NOPROPER);
		Object relate = null;
		Map<Map<Object, String>, Integer> biMap = new HashMap<Map<Object, String>, Integer>();
		for (Map m : map) {
			relate = relateService.getRelatedObject(tag, new Long(m.get("relatedid") + ""));
			Map<Object, String> tmap = new HashMap<Object, String>();
			tmap.put(relate, tag);
			biMap.put(tmap, new Integer(m.get("count") + ""));
		}
		return biMap;
	}

	@Override
	public Map<Map<Object, String>, Integer> getQuestionListByCategoryGroup(String category, int from, int maxnum) {
		String hql = "select new map(q.categoryid as categoryid, count(*) as count) from GewaQuestion q where q.category!=null and q.categoryid!=null and q.category=? and q.questionstatus!=? and q.addtime<=? and q.addtime>=? group by q.categoryid order by count(*) desc)";
		List<Map> map = queryByRowsRange(hql, from, maxnum, category, GewaQuestion.QS_STATUS_NOPROPER, new Timestamp(System.currentTimeMillis()), DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -7));
		Object relate = null;
		Map<Map<Object, String>, Integer> biMap = new HashMap<Map<Object, String>, Integer>();
		for (Map m : map) {
			relate = relateService.getRelatedObject(category, new Long(m.get("categoryid") + ""));
			Map<Object, String> tmap = new HashMap<Object, String>();
			tmap.put(relate, category);
			biMap.put(tmap, new Integer(m.get("count") + ""));
		}
		return biMap;
	}

	@Override
	public List<GewaQaExpert> getCommendExpertList(Integer hotvalue, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQaExpert.class);
		query.add(Restrictions.eq("hotvalue", hotvalue));
		query.add(Restrictions.eq("status", GewaQaExpert.STATUS_Y));
		query.add(Restrictions.isNotNull("updatetime"));
		query.addOrder(Order.desc("updatetime"));
		List<GewaQaExpert> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public List<Map> getTopMemberListByPoint(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQaPoint.class);
		query.setProjection(Projections.projectionList().add(Projections.alias(Projections.sum("point"), "point")).add(
				Projections.alias(Projections.groupProperty("memberid"), "memberid")));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		query.addOrder(Order.desc("point"));
		List<Map> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		List<Map> miMap = new LinkedList<Map>();
		for (Map m : list) {
			Long memberid = (Long) m.get("memberid");
			if(memberid != null) {
				Map map = new HashMap();
				map.putAll(memberService.getCacheMemberInfoMap(memberid));
				map.put("point", Integer.parseInt(""+m.get("point")));
				miMap.add(map);
			}
		}
		return miMap;
	}

	@Override
	public Integer getTopMemberCountByPoint() {
		String hql = "select count(*) from GewaQaPoint a group by a.memberid";
		List list = readOnlyTemplate.find(hql);
		if (!list.isEmpty())
			return list.size();
		return 0;
	}

	@Override
	public boolean isQuestion(Long memberid, Integer maxdays) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("questionstatus", GewaQuestion.QS_STATUS_N));
		query.add(Restrictions.lt("addtime", DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -maxdays)));
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.size() > 0)
			return false;
		return true;
	}

	@Override
	public GewaQaPoint getGewaQaPointByQuestionidAndTag(Long qid, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQaPoint.class);
		query.add(Restrictions.eq("questionid", qid));
		query.add(Restrictions.eq("tag", tag));
		List<GewaQaPoint> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public Integer getPointByMemberid(Long mid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQaPoint.class);
		query.add(Restrictions.eq("memberid", mid));
		query.setProjection(Projections.sum("point"));
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.get(0)==null) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public Integer getAnswerCountByMemberid(Long mid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("memberid", mid));
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		return new Integer(list.get(0)+"");
	}

	@Override
	public Integer getBestAnswerCountByMemberid(Long mid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("memberid", mid));
		query.add(Restrictions.eq("answerstatus", GewaAnswer.AS_STATUS_Y));
		query.setProjection(Projections.rowCount());
		List<Long> list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	public int getWeek() {
		Calendar cd = Calendar.getInstance();
		int w = cd.get(Calendar.DAY_OF_WEEK);
		return w;
	}

	@Override
	public List<GewaQuestion> getQuestionByQsAndTagList(String citycode, String qs, String tag, String order, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if (ServiceHelper.isTag(tag)) {
			query.add(Restrictions.eq("tag", tag));
		} else if (ServiceHelper.isCategory(tag)) {
			query.add(Restrictions.eq("category", tag));
		}
		if (GewaQuestion.QS_STATUS_N.equals(qs))
			query.add(Restrictions.or(Restrictions.eq("questionstatus", GewaQuestion.QS_STATUS_N), 
					Restrictions.eq("questionstatus",GewaQuestion.QS_STATUS_Z)));
		else if(StringUtils.isNotBlank(qs)){
			query.add(Restrictions.eq("questionstatus", qs));
		}else{
			query.add(Restrictions.ne("questionstatus", GewaQuestion.QS_STATUS_Z));
		}
		query.addOrder(Order.desc(order));
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, 0, maxnum);
		return list;
	}
	
	@Override
	public List<GewaQuestion> getQuestionListByQsAndTagAndRelatedid(String tag, Long relatedid, String qs,  String order, int maxnum){
		return this.getQuestionList(null,tag,relatedid,qs,order,0,maxnum);
	}
	
	
	/**
	 * ππΩ®≤È—Ø
	 * @param tag
	 * @param relatedid
	 * @param status
	 * @return
	 */
	private DetachedCriteria getQuestionQuery(String citycode,String tag, String order,Long relatedid, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		if (ServiceHelper.isTag(tag)) {
			query.add(Restrictions.eq("tag", tag));
			if (relatedid!=null)
			query.add(Restrictions.eq("relatedid", relatedid));
		} else if (ServiceHelper.isCategory(tag)) {
			query.add(Restrictions.eq("category", tag));
			if (relatedid!=null)
			query.add(Restrictions.eq("categoryid", relatedid));
		}
		if (GewaQuestion.QS_STATUS_N.equals(status))
			query.add(Restrictions.or(Restrictions.eq("questionstatus", GewaQuestion.QS_STATUS_N), 
					Restrictions.eq("questionstatus",GewaQuestion.QS_STATUS_Z)));
		else if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("questionstatus", status));
		}else{
			query.add(Restrictions.ne("questionstatus", GewaQuestion.QS_STATUS_Z));
		}
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
		}
		return query;
	}
	
	@Override
	public int getQuestionCount(String citycode,String tag, Long relatedid, String status){
		DetachedCriteria query =getQuestionQuery(citycode,tag,null, relatedid, status);
		query.setProjection(Projections.rowCount());
		List qList = readOnlyTemplate.findByCriteria(query);
		if (qList.isEmpty()) return 0;
		return new Integer(qList.get(0)+"");
	}
	
	@Override
	public List<GewaQuestion> getQuestionList(String citycode,String tag, Long relatedid, String status,  String order,int from,int maxnum){
		DetachedCriteria query =getQuestionQuery(citycode,tag,order, relatedid, status);
		List<GewaQuestion> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	
	@Override
	public Integer getQuestionCountByHotvalue(String citycode, Integer hotvalue){
		DetachedCriteria query = DetachedCriteria.forClass(GewaQuestion.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("hotvalue", hotvalue));
		query.setProjection(Projections.rowCount());
		List<GewaQuestion> qaList=readOnlyTemplate.findByCriteria(query);
		if(qaList.isEmpty()) return 0;
		return new Integer(qaList.get(0)+"");
	}
	@Override
	public Integer getAnswerCountByQuestionId(Long questionid) {
		DetachedCriteria query = DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.add(Restrictions.eq("questionid", questionid));
		query.setProjection(Projections.rowCount());
		List<GewaAnswer> qaList = readOnlyTemplate.findByCriteria(query);
		if(qaList.isEmpty()) return 0;
		return new Integer(qaList.get(0)+"");
	}
	
	@Override
	public Long getGewaraAnswerByMemberid(){
		GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_DRAMA_QUESTION_ID);
		return Long.parseLong(config.getContent());
	}
}
