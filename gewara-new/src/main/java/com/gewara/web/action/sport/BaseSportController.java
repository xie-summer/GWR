package com.gewara.web.action.sport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;

import com.gewara.service.PlaceService;
import com.gewara.service.sport.AgencyService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.web.action.AnnotationController;

public class BaseSportController extends AnnotationController {
	@Autowired@Qualifier("placeService")
	protected PlaceService placeService;
	@Autowired@Qualifier("sportService")
	protected SportService sportService;
	@Autowired@Qualifier("agencyService")
	protected AgencyService agencyService;
	@Autowired@Qualifier("synchGymService")
	protected SynchGymService synchGymService;
	
	protected void setheadData(String citycode, ModelMap model){
		//总场馆数量
		int leftSportCount = sportService.getSportCountByCode(citycode, null, null);
		model.put("leftSportCount", leftSportCount);
		//总项目
		int leftSportItemCount = sportService.getSportItemCount(null, 0L, null);
		model.put("leftSportItemCount", leftSportItemCount);
		//总培训课程
		int leftCurriculumCount = agencyService.getTrainingGoodsCount(citycode, null, null, null, null, null, null, null, null);
		model.put("leftCurriculumCount", leftCurriculumCount);
		ErrorCode<Integer> gymCountCode = synchGymService.getGymCount(citycode, null, null);
		int gymCount = 0;
		if(gymCountCode.isSuccess()) gymCount = gymCountCode.getRetval();
		model.put("leftGymCount", gymCount);
	}
}
