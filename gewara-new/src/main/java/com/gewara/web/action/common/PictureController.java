package com.gewara.web.action.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Member;
import com.gewara.service.content.PictureService;
import com.gewara.web.action.AnnotationController;

@Controller
public class PictureController extends AnnotationController{

	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	
	//”∞‘∫Õº∆¨œÍœ∏
	@RequestMapping("/picture/ajax/pictureDetail.xhtml")
	public String cinemaPictureDetial(ModelMap model, String tag, Long relatedid, String pvtype, Long pid) {
		if(StringUtils.isBlank(pvtype))pvtype = "apic";
		Object object = relateService.getRelatedObject(tag, relatedid);
		
		if(object == null) return showJsonError_NOT_FOUND(model);
		Long pictureid = null;
		Picture picture = null;
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, relatedid, 0, 100);
		if(pictureList.isEmpty()) return showJsonError(model, "≤ª¥Ê‘⁄Õº∆¨£°");
		if(pid != null){
			picture = daoService.getObject(Picture.class, pid);
			if(picture == null) return showJsonError_NOT_FOUND(model);
			pictureList.remove(picture);
			pictureList.add(picture);
			pictureid = picture.getId();
		}else{
			picture = pictureList.get(0);
			pictureid = picture.getId();
		}
		
		List<Map<String,Object>> pictureMapList = new LinkedList<Map<String,Object>>();
		for(Picture pic : pictureList){
			Map<String,Object> vm = new HashMap<String,Object>();
			vm.put("picturename",pic.getPicturename());
			vm.put("minpic","cw96h72/"+pic.getPicturename());
			vm.put("description", pic.getDescription());
			if(pic.hasMemberType(GewaraUser.USER_TYPE_MEMBER)){
				vm.put("membername", daoService.getObject(Member.class, pic.getMemberid()).getNickname());
			}
			vm.put("posttime", pic.getPosttime());
			vm.put("id", pic.getId());
			pictureMapList.add(vm);
		}
		Map jsonMap = new HashMap();
		jsonMap.put("relatedid", relatedid);
		jsonMap.put("tag", tag);
		jsonMap.put("vid", pid);
		jsonMap.put("pvtype", pvtype);
		jsonMap.put("pictureid", pictureid);
		jsonMap.put("pictureList", pictureMapList);
		return showJsonSuccess(model, jsonMap);
	}
}
