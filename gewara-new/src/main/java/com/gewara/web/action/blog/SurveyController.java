package com.gewara.web.action.blog;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.bbs.SurveyResult;
import com.gewara.service.bbs.SurveyService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;

@Controller
public class SurveyController extends AnnotationController {
	@Autowired@Qualifier("surveyService")
	private SurveyService surveyService;
	public void setSurveyService(SurveyService surveyService) {
		this.surveyService = surveyService;
	}
	@RequestMapping("/survey/ajax/addSurveyResult.xhtml")
	public String addSurveyResult(Long surveyid, Integer itemid, Integer optionid, Long memberid, String mark, ModelMap model){
		Long resultCount = surveyService.getSurveyResultCountByMember(memberid);
		if(resultCount > 4) return showJsonError(model, "今日提交的问卷调查过多");
		if(StringUtils.isNotBlank(mark)){
			if(StringUtil.getByteLength(mark) > 200) return showJsonError(model, "理由控制在100字以内！");
		}
		SurveyResult surveyResult = new SurveyResult();
		if(surveyid == null || itemid == null || optionid == null) return showJsonError(model, "提交问卷错误！");
		surveyResult.setSurveyid(surveyid);
		surveyResult.setItemid(itemid);
		surveyResult.setOptionid(optionid);
		surveyResult.setMemberid(memberid);
		surveyResult.setAddtime(DateUtil.getCurFullTimestamp());
		surveyResult.setFlag("Y");
		if(StringUtils.isNotBlank(mark)) surveyResult.setMark(mark);
		daoService.saveObject(surveyResult);
		return showJsonSuccess(model);
	}
}
