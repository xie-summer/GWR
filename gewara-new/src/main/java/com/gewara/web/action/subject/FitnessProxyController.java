package com.gewara.web.action.subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.content.Picture;
import com.gewara.service.content.PictureService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.gewara.xmlbind.gym.RemoteCoach;

/**
 * @desc 原力健 专题 代理
 */
@Controller
public class FitnessProxyController  extends AnnotationController {
	
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	//根据 gymid、tag 查询场馆相册
	@RequestMapping("/subject/proxy/getGymPhotoList.xhtml")
	public String getGymPhotoList(Long gymid,String tag,Integer from,Integer max,ModelMap model){
		if(from == null) from = 0;
		if(max == null) max = 5;
		List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
		List<Picture> list = pictureService.getPictureListByRelatedid(tag, gymid, from, max);
		for (Picture picture : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("picturename", picture.getPicturename()); // 照片url
			map.put("tag", picture.getTag());
			map.put("posttime", picture.getPosttime());
			results.add(map);
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(results));
	}
	
	//根据 gymid、tag 查询教练风采
	@RequestMapping("/subject/proxy/getCoachPhotoList.xhtml")
	public String getCoachPhotoList(Long gymid,String order,Integer from,Integer max,ModelMap model){
		if(from == null) from = 0;
		if(max == null) max = 5;
		List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
		ErrorCode<List<RemoteCoach>> code = synchGymService.getCoachListByGymId(gymid, order, false, from, max);
		if(code.isSuccess()){
			List<RemoteCoach> list = code.getRetval();
			for (RemoteCoach gymCoach : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("logo", gymCoach.getLogo());  // 照片url
				map.put("coachtype", gymCoach.getCoachtype());
				map.put("coachid", gymCoach.getId()); //教练id
				map.put("coachname", gymCoach.getCoachname());
				results.add(map);
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(results));
	}
	
	@RequestMapping("/subject/proxy/fitness/isJionActivity.xhtml")
	public String isJionActivity(Long memberid, Long relatedid, ModelMap model){
		if(memberid == null) return showJsonError(model, "请先登录！");
		if(relatedid == null) return showJsonError(model, "参数错误！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
		if(!code.isSuccess()) return showJsonError(model, "不存在此活动！");
		ErrorCode<RemoteApplyjoin> code2 = synchActivityService.getApplyJoin(memberid, relatedid);
		String result="no";
		if(code2.isSuccess() && code2.getRetval() != null) result="jion";
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/newsubject/fitness.xhtml")
	public String fitness(){
		return "admin/newsubject/after/fitness.vm";
	}
	
}