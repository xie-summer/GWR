package com.gewara.web.action.admin.blog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.bbs.Survey;
import com.gewara.model.bbs.SurveyItem;
import com.gewara.model.bbs.SurveyOption;
import com.gewara.model.bbs.SurveyResult;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.SurveyService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;

@Controller
public class SurveyAdminController extends AnnotationController {
	@Autowired@Qualifier("surveyService")
	private SurveyService surveyService;
	public void setSurveyService(SurveyService surveyService) {
		this.surveyService = surveyService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@RequestMapping("/admin/survey/showSurveyResult.xhtml")
	public String showSurveyResult(Integer pageNo, Long surveyid, ModelMap model){
		if(null == pageNo) pageNo=0;
		int rowsPerPage = 20;
		int forms = pageNo * rowsPerPage;
		List<SurveyResult> surveyResultList = surveyService.getSurveyResultListBySurveyid(surveyid, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(Integer.valueOf(surveyService.getSurveyResultCountBySurveyid(surveyid, null)+""), rowsPerPage, pageNo, "/admin/survey/showSurveyResult.xhtml");
		Map param = new HashMap();
		param.put("surveyid", surveyid);
		pageUtil.initPageInfo(param);
		model.put("pageUtil", pageUtil);
		Map<Long, String> optionMap = new HashMap<Long, String>();
		for(SurveyResult surveyResult : surveyResultList){
			List<SurveyOption> optionList = surveyService.getSurveyOptionByOptionid(surveyid, surveyResult.getItemid(), surveyResult.getOptionid());
			if(optionList.size() > 0)optionMap.put(surveyResult.getId(), optionList.get(0).getOptiontype());
		}
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(surveyResultList);
	   addCacheMember(model, memberidList);
		model.put("surveyResultList", surveyResultList);
		model.put("optionMap", optionMap);
		Long resultcount1 = surveyService.getSurveyResultCountBySurveyid(surveyid, 1);
		Long resultcount2 = surveyService.getSurveyResultCountBySurveyid(surveyid, 2);
		model.put("resultcount1", resultcount1);
		model.put("resultcount2", resultcount2);
		model.put("survey", daoService.getObject(Survey.class, surveyid));
		return "admin/survey/surveyResultList.vm";
	}
	
	@RequestMapping("/admin/survey/showSurvey.xhtml")
	public String showSurvey(ModelMap model){
		List<Survey> surveyList = daoService.getAllObjects(Survey.class);
		model.put("surveyList", surveyList);
		return "admin/survey/surveyList.vm";
	}
	@RequestMapping("/admin/survey/mobileSurvey.xhtml")
	public String mobileSurvey(ModelMap model){
		List<Map> qryMapList = mongoService.find("mobile.survey", new HashMap(), "addtime", false);
		model.put("qryMapList", qryMapList);
		return "admin/survey/surveyMobileList.vm";
	}
	@RequestMapping("/admin/survey/editSurvey.xhtml")
	public String editSurvey(String surveyid, ModelMap model) {
		if(StringUtils.isNotBlank(surveyid)) {
			model.put("survey", daoService.getObject(Survey.class, new Long(surveyid)));
			model.put("surveyItem", daoService.getObjectByUkey(SurveyItem.class, "surveyid", new Long(surveyid), true));
		}
		return "admin/survey/editSurvey.vm";
	}
	
	@RequestMapping("/admin/survey/saveSurvey.xhtml")
	public String saveSurvey(String surveyid,String title, String itembody,String itemType, HttpServletRequest request, ModelMap model) {
		Survey survey = null;
		if(StringUtils.isNotBlank(surveyid)) {
			survey = daoService.getObject(Survey.class, new Long(surveyid));
		}else {
			survey = new Survey(title);
		}
		BindUtils.bindData(survey, request.getParameterMap());
		surveyService.saveSurveyItem(survey, itembody, itemType);
		return showJsonSuccess(model, BeanUtil.getBeanMap(survey));
	}
}
