package com.gewara.web.action.subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;
@Controller
public class OscarController extends AnnotationController {
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;

	@RequestMapping("/subject/proxy/oscar/getOscar.xhtml")
	public String getOscar(String type, String id, ModelMap model){
		if(id == null) return showJsonSuccess(model, "参数错误。");
		Map paraMap = new HashMap();
		paraMap.put(MongoData.ACTION_TYPE, type);
		paraMap.put(MongoData.ACTION_RELATEDID, id);
		Map oscarMap = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, paraMap);
		if(oscarMap != null)return showJsonSuccess(model,JsonUtils.writeObjectToJson(oscarMap));
		return showJsonSuccess(model);
	}
	@RequestMapping("/subject/proxy/oscar/getOscarMember.xhtml")
	public String getOscarMember(String type, Integer fromnum, Integer maxnum, ModelMap model){
		if(fromnum == null || maxnum == null || StringUtils.isBlank(type)) return showJsonError(model, "参数错误！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		List<Map> oscarMemberList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER, params, MongoData.ACTION_ADDTIME, false, fromnum, maxnum);
		if(oscarMemberList != null) return showJsonSuccess(model, JsonUtils.writeObjectToJson(oscarMemberList));
		return showJsonSuccess(model);
		
	}
}
