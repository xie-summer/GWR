package com.gewara.web.action.admin.sys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.support.ServiceHelper;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
@Controller
public class MongoAdminController extends BaseAdminController{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@RequestMapping("/admin/sysmgr/execMongo.xhtml")
	public String execMongo(){
		return "admin/sysmgr/execMongo.vm";
	}
	@RequestMapping("/admin/sysmgr/getMongoTables.xhtml")
	public String execMongoTables(ModelMap model){
		List<Map<String, String>> resultList = mongoService.getAllTables();
		model.put("resultList", resultList);
		return "admin/sysmgr/execMongo.vm";
	}
	@RequestMapping("/admin/sysmgr/getNamespaceFields.xhtml")
	public String getNamespaceFields(String namespace, ModelMap model){
		List<Map> filedstipsResultList = mongoService.find(namespace, new HashMap<String, Object>(), "_id", false, 0, 1);
		if(filedstipsResultList.size() > 0) {
			model.put("filedstipsResult", filedstipsResultList.get(0));
		}
		return "admin/sysmgr/execMongo.vm";
	}
	
	@RequestMapping("/admin/sysmgr/execMongoQuery.xhtml")
	public String execMongoQuery(HttpServletRequest request, Integer pageNo, Integer rows, String isXls, HttpServletResponse response, ModelMap model){
		Map paramMap = request.getParameterMap();
		DBObject queryCondition = new BasicDBObject();
		String queryType = ServiceHelper.get(paramMap, "queryType");
		
		if(pageNo == null) pageNo = 0;
		if(rows == null) rows = 1000;
		int firstPerPage = pageNo*rows;
		
		String namespace = ServiceHelper.get(paramMap, "namespace");// 表名
		String orderby = ServiceHelper.get(paramMap, "orderby");		// 排序字段
		if(StringUtils.isBlank(orderby)) orderby = MongoData.SYSTEM_ID;
		String isasc = ServiceHelper.get(paramMap, "isasc");		// 升/降序
		boolean asc = Boolean.parseBoolean(isasc);
		
		String fields = ServiceHelper.get(paramMap, "fields");		// 需要查询的字段
		DBObject fieldsCondition = new BasicDBObject();
		fields += "|";
		if(StringUtils.startsWith(fields, "|") && fields.length() > 1) fields = " "+fields;
		String[] fieldsss = StringUtils.split(fields, "|");
		// |之前为true, 之后为false; 超过1个|则不计算
		if(fieldsss.length == 1){
			String fieldtrue = fieldsss[0];
			for(String tmp : StringUtils.split(fieldtrue, ",")){
				fieldsCondition.put(tmp, true);
			}
		}
		if(fieldsss.length == 2){
			String fieldtrue = StringUtils.deleteWhitespace(fieldsss[0]);
			String fieldfalse = fieldsss[1];
			for(String tmp : StringUtils.split(fieldtrue, ",")){
				fieldsCondition.put(tmp, true);
			}
			for(String tmp : StringUtils.split(fieldfalse, ",")){
				fieldsCondition.put(tmp, false);
			}
		}
		
		if(StringUtils.equals(queryType, "simpleQuery")){ // 简单查询
			List<DBObject> dbObjectList = new ArrayList<DBObject>();
			// 条件1
			String key1 = ServiceHelper.get(paramMap, "key1");
			if(StringUtils.isNotBlank(key1)){
				DBObject dbObject = new BasicDBObject();
				String opera = ServiceHelper.get(paramMap, "opera1");
				String value = ServiceHelper.get(paramMap, "value1");
				String opera_1 = ServiceHelper.get(paramMap, "opera1_1");
				String value_1 = ServiceHelper.get(paramMap, "value1_1");
				if(StringUtils.isNotBlank(value) && StringUtils.isNotBlank(value_1)){
					dbObject = mongoService.queryAdvancedDBObject(key1, new String[]{opera, opera_1}, new String[]{value, value_1});
				}else if (StringUtils.isNotBlank(value)) {
					dbObject = mongoService.queryBasicDBObject(key1, opera, value);
				}
				dbObjectList.add(dbObject);
			}
			
			// 条件2
			String key2 = ServiceHelper.get(paramMap, "key2");
			if(StringUtils.isNotBlank(key2)){
				DBObject dbObject = new BasicDBObject();
				String opera = ServiceHelper.get(paramMap, "opera2");
				String value = ServiceHelper.get(paramMap, "value2");
				String opera_1 = ServiceHelper.get(paramMap, "opera2_1");
				String value_1 = ServiceHelper.get(paramMap, "value2_1");
				if(StringUtils.isNotBlank(value) && StringUtils.isNotBlank(value_1)){
					dbObject = mongoService.queryAdvancedDBObject(key2, new String[]{opera, opera_1}, new String[]{value, value_1});
				}else if (StringUtils.isNotBlank(value)) {
					dbObject = mongoService.queryBasicDBObject(key2, opera, value);
				}
				dbObjectList.add(dbObject);
			}
			
			// 条件3
			String key3 = ServiceHelper.get(paramMap, "key3");
			if(StringUtils.isNotBlank(key3)){
				DBObject dbObject = new BasicDBObject();
				String opera = ServiceHelper.get(paramMap, "opera3");
				String value = ServiceHelper.get(paramMap, "value3");
				if (StringUtils.isNotBlank(opera)) {
					if(StringUtils.equals(opera, "exists")){
						boolean booleanValue = Boolean.parseBoolean(value);
						dbObject = mongoService.queryBasicDBObject(key3, opera, booleanValue);
					}else if(StringUtils.equals(opera, "typeOf")){
						Integer intValue = Integer.parseInt(value);
						dbObject = mongoService.queryBasicDBObject(key3, opera, intValue);
					}else if(StringUtils.equals(opera, "=")){
						Long longValue = Long.parseLong(value);
						dbObject = mongoService.queryBasicDBObject(key3, opera, longValue);
					}
				}
				dbObjectList.add(dbObject);
			}
			
			for(DBObject subObject : dbObjectList){
				queryCondition.putAll(subObject);
			}
		}else{  // 高级查询
			String advSql = ServiceHelper.get(paramMap, "advSql");
			queryCondition = (DBObject)JSON.parse(advSql);
		}
		dbLogger.warn(queryType + ": 【" + namespace + "】" + queryCondition + fieldsCondition);
		List<Map> resultList = mongoService.find(namespace, queryCondition, fieldsCondition, orderby, asc, firstPerPage, rows);
		int count = mongoService.getCount(namespace, queryCondition);
		PageUtil pageUtil = new PageUtil(count, rows, pageNo, "/admin/sysmgr/execMongoQuery.xhtml");
		pageUtil.initPageInfo(paramMap);
		model.put("pageUtil", pageUtil);
		
		model.put("resultList", resultList);
		if(StringUtils.isNotBlank(isXls)){
			this.download("xls", response);
			return "admin/sysmgr/execQueryResultExcel.vm";
		}
		return "admin/sysmgr/execQueryResult.vm";
	}

	@RequestMapping("/admin/sysmgr/updateSysMongo.xhtml")
	public String updateSysMongo(ModelMap model){
		String keywords = (String)mongoService.getPrimitiveObject(ConfigConstant.KEY_MANUKEYWORDS);
		model.put("keywords", keywords);
		return "admin/sysmgr/updateSysMongo.vm";
	}
	@RequestMapping("/admin/sysmgr/updateSysMongoFilterKey.xhtml")
	public String updateSysMongoFilterKey(String filterkey, ModelMap model){
		if(StringUtils.isNotBlank(filterkey)) {
			mongoService.savePrimitiveObject(ConfigConstant.KEY_MANUKEYWORDS, filterkey);
		} 
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/updateMongo.xhtml")
	public String updateMongo(String oldname, String newname, ModelMap model){
		if(StringUtils.isBlank(oldname) || StringUtils.isBlank(newname)){
			return forwardMessage(model, "error:" + oldname);
		}
		if(StringUtils.equals(oldname, newname)){
			return forwardMessage(model, "error:" + oldname);
		}
		int count = mongoService.copyCollection(oldname, newname);
		dbLogger.warn("update oldname:" + oldname + ", newname:" + newname + ":" + count);
		return forwardMessage(model, oldname + ":" + count);
	}
	
	@RequestMapping("/admin/sysmgr/queryTableIndex.xhtml")
	public String queryTableIndex(String namespace, ModelMap model){
		if(StringUtils.isBlank(namespace)) return forwardMessage(model, "namespace is required!");
		List<DBObject> indexes = mongoService.getIndexesByNamespace(namespace);
		int c = indexes.isEmpty() ? 0 : indexes.size();
		String indexTipsResult = "共有 " + c + " 个索引: ";
		for(DBObject object: indexes){
			indexTipsResult += object.get("name") + ", ";
		}
		model.put("indexTipsResult", indexTipsResult);
		return "admin/sysmgr/execMongo.vm";
	}
	
	@RequestMapping("/admin/sysmgr/createTableIndex.xhtml")
	public String createTableIndex(String namespace, String indexes, ModelMap model){
		if(StringUtils.isBlank(namespace)) return showJsonError(model, "namespace is required!");
		if(StringUtils.isNotBlank(indexes)){
			mongoService.createIndexes(namespace, indexes);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "indexes is required!");
	}
	
	@RequestMapping("/admin/sysmgr/dropTableIndex.xhtml")
	public String dropTableIndex(String namespace, String indexname, ModelMap model){
		if(StringUtils.isBlank(namespace)) return showJsonError(model, "namespace is required!");
		if(StringUtils.isNotBlank(indexname)){
			mongoService.dropIndex(namespace, indexname);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "indexes is required!");
	}
}
