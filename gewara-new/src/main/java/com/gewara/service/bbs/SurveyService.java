package com.gewara.service.bbs;

import java.util.List;

import com.gewara.model.bbs.Survey;
import com.gewara.model.bbs.SurveyItem;
import com.gewara.model.bbs.SurveyOption;
import com.gewara.model.bbs.SurveyResult;

public interface SurveyService {
	List<SurveyResult> getSurveyResultListBySurveyid(Long surveyid, int from, int maxnum);
	Long getSurveyResultCountBySurveyid(Long surveyid, Integer optionid);
	List<SurveyOption> getSurveyOptionByOptionid(Long surveyid, Integer itemid, Integer optionid);
	void updateSurveyResult(Long memberid, Long surveyId, String optionType, String mark, String otherinfo);
	SurveyOption getSurveyOptionByOptionType(Long surveyid, Integer itemid, String optionType);
	SurveyItem getSurveyItemByBody(String surveyBody);
	Long getSurveyResultCountByMember(Long memberid);
	void saveSurveyItem(Survey survey, String itembody, String itemType);
}
