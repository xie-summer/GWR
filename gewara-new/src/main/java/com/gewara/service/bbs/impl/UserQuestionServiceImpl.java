package com.gewara.service.bbs.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.service.bbs.UserQuestionService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

@Service("userQuestionService")
public class UserQuestionServiceImpl extends BaseServiceImpl implements UserQuestionService {
	@Override
	public List<GewaQuestion> getAnswerByMemberid(Long memberid, int from, int maxnum) {
		String hql="from GewaQuestion where id in(select questionid from GewaAnswer where memberid=?) and status=? order by addtime desc";
		List<GewaQuestion> listanswer=queryByRowsRange(hql, from, maxnum, memberid, Status.Y_NEW);
		return listanswer;
	}

	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<GewaQuestion> getQuestionByMemberid(Long memberid, int from, int maxnum) {
		DetachedCriteria query=DetachedCriteria.forClass(GewaQuestion.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.like("status", Status.Y, MatchMode.START));
		query.addOrder(Order.desc("addtime"));
		List<GewaQuestion> listquestion=readOnlyTemplate.findByCriteria(query,from,maxnum);
		return listquestion;
	}

	@Override
	public Integer getAnswerCountByMemberid(Long memberid) {
		String hql="select count(*) from GewaQuestion where id in(select questionid from GewaAnswer where memberid=?) and status=?";
		List<GewaQuestion> listanswer = readOnlyTemplate.find(hql, memberid,Status.Y_NEW);
		if(listanswer.isEmpty()) return 0;
		return new Integer(listanswer.get(0)+"");
	}

	@Override
	public Integer getQuestionCountByMemberid(Long memberid) {
		DetachedCriteria query=DetachedCriteria.forClass(GewaQuestion.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("status", Status.Y_NEW));
		query.setProjection(Projections.rowCount());
		query.addOrder(Order.desc("addtime"));
		List<GewaQuestion> listquestion=readOnlyTemplate.findByCriteria(query);
		if(listquestion.isEmpty()) return 0;
		return new Integer(listquestion.get(0)+"");
	}
	
	@Override
	public GewaAnswer getGewaAnswerByAnswerid(Long questionid, Long memberid){
		DetachedCriteria query=DetachedCriteria.forClass(GewaAnswer.class);
		query.add(Restrictions.eq("questionid", questionid));
		query.add(Restrictions.eq("memberid", memberid));
		List<GewaAnswer> answerList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(answerList.isEmpty())return null;
		return answerList.get(0);
	}
}
