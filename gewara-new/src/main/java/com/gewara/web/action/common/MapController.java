package com.gewara.web.action.common;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.common.BaseInfo;
import com.gewara.web.action.AnnotationController;

@Controller
public class MapController extends AnnotationController {
	@RequestMapping("/common/map.xhtml")
	public String getMap(ModelMap model, String tag, Long relatedid) {
		BaseInfo relate = (BaseInfo) relateService.getRelatedObject(tag, relatedid);
		model.put("googlemap", relate.getGooglemap());
		return "common/new_map.vm";
	}
}
