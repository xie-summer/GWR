package com.gewara.web.action.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.content.Video;
import com.gewara.service.content.VideoService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.AnnotationController;

@Controller
public class VideoController extends AnnotationController {

	@Autowired
	private VideoService videoService;
	
	@RequestMapping("/video/ajax/videoList.xhtml")
	public String videoList(String tag, Long relatedid, Long vid, ModelMap model){
		Object object = relateService.getRelatedObject(tag, relatedid);
		if(object == null) return showJsonError_NOT_FOUND(model);
		// 视频列表
		List<Video> videoList = videoService.getVideoListByTag(tag, relatedid, 0, 1000);
		if(videoList.isEmpty()) return showJsonError(model, "不存在视频！");
		Video video = null;
		if(vid != null){
			video = daoService.getObject(Video.class, vid);
			if(video == null) return showJsonError_NOT_FOUND(model);
			videoList.remove(video);
			videoList.add(0, video);
		}else{
			video = videoList.get(0);
			vid = video.getId();
		}

		List<Map<String,String>> videos = new LinkedList<Map<String,String>>();
		for(Video v : videoList){
			Map<String,String> vm = new HashMap<String,String>();
			vm.put("picturename",v.getLimg());
			vm.put("minpic",(StringUtils.indexOf(v.getLimg(), "http") == -1 ? "cw96h72/" : "" )+v.getLimg());
			vm.put("description", v.getContent());
			vm.put("url", v.getUrl());
			vm.put("id", v.getId() + "");
			vm.put("titile",v.getVideotitle());
			vm.put("addTime",DateUtil.format(v.getAddtime(), "yyyy-MM-dd"));
			videos.add(vm);
		}

		Map jsonMap = new HashMap();
		jsonMap.put("tag", tag);
		jsonMap.put("relatedid", relatedid);
		jsonMap.put("vid", vid);
		jsonMap.put("videoList", videos);
		return showJsonSuccess(model, jsonMap);
	}
}
