package com.gewara.web.action.admin.content;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class ExplainController extends BaseAdminController{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@RequestMapping("/admin/explain/explainList.xhtml")
	public String getExplainList(ModelMap model){
		Map paraMap = new HashMap();
		List<Map> explainList= mongoService.find(MongoData.NS_EXPLAIN, paraMap);
		model.put("explainList", explainList);
		return "admin/explain/explainList.vm";
	}
	@RequestMapping("/admin/explain/ajax/saveOrUpdateExplain.xhtml")
	public String saveExplain(ModelMap model,String id,String itemname,String content){
		Map paraMap = new HashMap();
		paraMap.put(MongoData.SYSTEM_ID, id);
		Map map = mongoService.findOne(MongoData.NS_EXPLAIN, paraMap);
		if(map == null){
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, com.gewara.util.ObjectId.uuid());
			map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			map.put("itemname", itemname);
			map.put("content", content);
			mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_EXPLAIN);
		}else{
			map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			map.put("itemname", itemname);
			map.put("content", content);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_EXPLAIN);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/explain/updateExplain.xhtml")
	public String updateExplain(ModelMap model,String id){
		Map paraMap = new HashMap();
		paraMap.put(MongoData.SYSTEM_ID, id);
		Map map = mongoService.findOne(MongoData.NS_EXPLAIN, paraMap);
		model.put("map", map);
		return "admin/explain/explain.vm";
	}
	@RequestMapping("/admin/explain/ajax/deleteExplain.xhtml")
	public String deleteExplain(ModelMap model,String id){
		mongoService.removeObjectById(MongoData.NS_EXPLAIN, "_id", id);
		return showJsonSuccess(model);
	}
}
