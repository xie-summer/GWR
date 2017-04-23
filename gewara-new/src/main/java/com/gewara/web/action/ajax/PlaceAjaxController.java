package com.gewara.web.action.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.common.BaseInfo;
import com.gewara.service.PlaceService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.AnnotationController;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since Apr 14, 2008 AT 6:32:00 PM
 */
@Controller
public class PlaceAjaxController extends AnnotationController {
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@RequestMapping("/ajax/getPlace.xhtml")
	public String getPlaceListByTag(String tag, String countycode, 
			String indexareacode, String props, ModelMap model) {
		String[] properties = props.split(",");
		List<BaseInfo> placeList = placeService.getPlaceListByTag(tag, countycode, indexareacode);
		List<Map> result = BeanUtil.getBeanMapList(placeList, properties);
		Map jsonMap = new HashMap();
		jsonMap.put("placeList", result);
		return showJsonSuccess(model, jsonMap);
	}
}
