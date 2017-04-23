package com.gewara.service.bbs.impl;

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

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.Status;
import com.gewara.model.bbs.CustomerAnswer;
import com.gewara.model.bbs.CustomerQuestion;
import com.gewara.service.bbs.CustomerQuestionService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.DateUtil;

@Service("customerQuestionService")
public class CustomerQuestionServiceImpl extends BaseServiceImpl implements CustomerQuestionService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<CustomerQuestion> getQuestionsBykey(String citycode, String tag, String searchkey, String status, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(CustomerQuestion.class);
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (StringUtils.isNotBlank(status))
			query.add(Restrictions.like("status", status, MatchMode.START));
		if (StringUtils.isNotBlank(searchkey)) {
			// query.add(Restrictions.or(Restrictions.ilike("subject",
			// searchkey, MatchMode.ANYWHERE), Restrictions.ilike("membername",
			// searchkey, MatchMode.ANYWHERE)));
			query.add(Restrictions.ilike("body", searchkey, MatchMode.ANYWHERE)); // 只匹配帖子标题
		}
		query.addOrder(Order.desc("addtime"));
		List<CustomerQuestion> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}

	@Override
	public Integer getQuestionCountBykey(String citycode, String tag, String searchkey, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(CustomerQuestion.class);
		query.setProjection(Projections.rowCount());
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		if (StringUtils.isNotBlank(status))
			query.add(Restrictions.like("status", status, MatchMode.START));
		if (StringUtils.isNotBlank(searchkey)) {
			query.add(Restrictions.ilike("body", searchkey, MatchMode.ANYWHERE));
		}
		List<CustomerQuestion> result = readOnlyTemplate.findByCriteria(query);
		if (result.isEmpty())
			return 0;
		return Integer.parseInt("" + result.get(0));
	}

	@Override
	public List<CustomerAnswer> getAnswersByQid(Long qid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(CustomerAnswer.class);
		query.add(Restrictions.eq("questionid", qid));
		query.addOrder(Order.asc("addtime"));
		List<CustomerAnswer> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}

	@Override
	public Integer getAnswerCountByQid(Long qid) {
		DetachedCriteria query = DetachedCriteria.forClass(CustomerAnswer.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("questionid", qid));
		List result = readOnlyTemplate.findByCriteria(query);
		if (result.isEmpty())
			return 0;
		return Integer.parseInt("" + result.get(0));
	}

	@Override
	public List<CustomerQuestion> getCustomerQList(Long memberid, String citycode, String tag, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(CustomerQuestion.class);
		query.add(Restrictions.eq("memberid", memberid));
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.addOrder(Order.desc("addtime"));
		List<CustomerQuestion> cqList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return cqList;
	}

	public Integer getCustometQCount(Long memberid, String citycode, String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(CustomerQuestion.class);
		query.add(Restrictions.eq("memberid", memberid));
		if (StringUtils.isNotBlank(citycode))
			query.add(Restrictions.eq("citycode", citycode));
		if (StringUtils.isNotBlank(tag))
			query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.addOrder(Order.desc("addtime"));
		query.setProjection(Projections.rowCount());
		List cqList = readOnlyTemplate.findByCriteria(query);
		return Integer.valueOf(cqList.get(0) + "");
	}

	@Override
	public CustomerQuestion addCustomerQuestion(String citycode, Long memberid, String email, String tag, String body, String type) {
		CustomerQuestion question = new CustomerQuestion(email);
		question.setMemberid(memberid);
		question.setType(type);
		if (StringUtils.isBlank(tag)){
			tag = CustomerQuestion.TAG_ADVISE;
		}
		question.setTag(tag);
		question.setBody(body);
		question.setUpdatetime(DateUtil.getCurFullTimestamp());
		if (StringUtils.isBlank(citycode))
			citycode = AdminCityContant.CITYCODE_SH;
		question.setCitycode(citycode);
		question = baseDao.saveObject(question);
		return question;
	}

}
