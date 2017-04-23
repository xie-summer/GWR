package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.model.bbs.Survey;
import com.gewara.model.bbs.SurveyItem;
import com.gewara.model.bbs.SurveyOption;
import com.gewara.model.bbs.SurveyResult;
import com.gewara.service.bbs.SurveyService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.DateUtil;

@Service("surveyService")
public class SurveyServiceImpl extends BaseServiceImpl implements SurveyService {
	
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<SurveyResult> getSurveyResultListBySurveyid(Long surveyid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SurveyResult.class);
		query.add(Restrictions.eq("surveyid", surveyid));
		query.addOrder(Order.desc("addtime"));
		List<SurveyResult> surveyResultList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return surveyResultList;
	}

	@Override
	public Long getSurveyResultCountBySurveyid(Long surveyid, Integer optionid) {
		DetachedCriteria query = DetachedCriteria.forClass(SurveyResult.class);
		query.add(Restrictions.eq("surveyid", surveyid));
		if(optionid != null)query.add(Restrictions.eq("optionid", optionid));
		query.setProjection(Projections.count("id"));
		List<Long> surveyResultList = readOnlyTemplate.findByCriteria(query);
		return surveyResultList.get(0);
	}
	
	@Override
	public List<SurveyOption> getSurveyOptionByOptionid(Long surveyid, Integer itemid, Integer optionid) {
		DetachedCriteria query = DetachedCriteria.forClass(SurveyOption.class);
		query.add(Restrictions.eq("surveyid", surveyid));
		query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("optionid", optionid));
		List<SurveyOption> surveyOptionList = readOnlyTemplate.findByCriteria(query);
		return surveyOptionList;
	}
	@Override
	public void updateSurveyResult(Long memberid, Long surveyId, String optionType, String mark, String otherinfo) {
		SurveyItem surveyItem = baseDao.getObjectByUkey(SurveyItem.class, "surveyid", surveyId, true);
		SurveyOption surveyOption = getSurveyOptionByOptionType(surveyItem.getSurveyid(),surveyItem.getItemid(),optionType);
		SurveyResult surveyResult = new SurveyResult();
		surveyResult.setSurveyid(surveyItem.getSurveyid());
		surveyResult.setItemid(surveyItem.getItemid());
		surveyResult.setOptionid(surveyOption.getOptionid());
		surveyResult.setMemberid(memberid);
		surveyResult.setAddtime(DateUtil.getCurFullTimestamp());
		surveyResult.setFlag(optionType);
		if(StringUtils.isBlank(mark)) mark = Status.Y;
		surveyResult.setMark(mark);
		surveyResult.setOtherinfo("url:"+ otherinfo);
		baseDao.saveObject(surveyResult);
	}
	
	@Override
	public SurveyOption getSurveyOptionByOptionType(Long surveyid, Integer itemid, String optionType) {
		DetachedCriteria query = DetachedCriteria.forClass(SurveyOption.class);
		query.add(Restrictions.eq("surveyid", surveyid));
		query.add(Restrictions.eq("itemid", itemid));
		query.add(Restrictions.eq("optiontype", optionType));
		List<SurveyOption> surveyOptionList = readOnlyTemplate.findByCriteria(query);
		if(surveyOptionList.isEmpty()) return null;
		return surveyOptionList.get(0);
	}
	
	public SurveyItem getSurveyItemByBody(String surveyBody) {
		DetachedCriteria query = DetachedCriteria.forClass(SurveyItem.class);
		query.add(Restrictions.eq("body", surveyBody));
		List<SurveyItem> surveyItemList = readOnlyTemplate.findByCriteria(query);
		if(surveyItemList.isEmpty()) return null;
		return surveyItemList.get(0);
	}
	
	@Override
	public Long getSurveyResultCountByMember(Long memberid) {
		Timestamp time = DateUtil.getCurTruncTimestamp();
		DetachedCriteria query = DetachedCriteria.forClass(SurveyResult.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.gt("addtime", time));
		query.setProjection(Projections.count("id"));
		List<Long> resultList = readOnlyTemplate.findByCriteria(query);
		return resultList.get(0);
	}
	@Override
	public void saveSurveyItem(Survey survey, String itembody, String itemType) {
		if(StringUtils.isBlank(itemType)) itemType = SurveyItem.TEXTTYPE;
		baseDao.saveObject(survey);
		SurveyItem surveyItem = new SurveyItem(survey.getId(), itembody, itemType);
		baseDao.saveObject(surveyItem);
		
		List<SurveyOption> surveyOpList = new ArrayList<SurveyOption>();
		SurveyOption surveyOptionY = new SurveyOption(survey.getId(),surveyItem.getItemid(),itembody,Status.Y);
		surveyOpList.add(surveyOptionY);
		if(itemType.equals(SurveyItem.SINGLETYPE)) {
			SurveyOption surveyOptionN = new SurveyOption(survey.getId(),surveyItem.getItemid(),itembody,Status.N);
			surveyOpList.add(surveyOptionN);
		}
		baseDao.saveObjectList(surveyOpList);
		
	}
}