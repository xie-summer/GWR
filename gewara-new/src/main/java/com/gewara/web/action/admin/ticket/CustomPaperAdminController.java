package com.gewara.web.action.admin.ticket;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.json.CustomPaper;
import com.gewara.model.common.GewaConfig;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.util.XSSFilter;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class CustomPaperAdminController extends BaseAdminController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;

	
	//自定义票纸查询
	@RequestMapping("/admin/custom/getPaperList.xhtml")
	public String getPaperList(String startdate, String enddate, Integer pageNo, ModelMap model){
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CUSTOM_PAPER);
		model.put("gewaConfig", gewaConfig);
		Date sd = null;
		Date ed = null;
		if(StringUtils.isBlank(enddate)){
			ed = DateUtil.currentTime();
		}else{
			ed = DateUtil.addDay(DateUtil.parseDate(enddate), 1);
		}
		if(StringUtils.isBlank(startdate)){
			sd = DateUtil.addDay(ed, -7);
		}else{
			sd = DateUtil.parseDate(startdate);
		}
		if(pageNo == null) pageNo = 0;
		int maxnum = 100;
		int from = pageNo * maxnum;
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("tag", "=", TagConstant.TAG_CINEMA);
		DBObject relate2 = mongoService.queryAdvancedDBObject("addtime", new String[]{">=","<="}, new Date[]{sd, ed});
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		int count = mongoService.getObjectCount(CustomPaper.class, queryCondition);
		if(count > 0){
			List<CustomPaper> customPaperList = mongoService.getObjectList(CustomPaper.class, queryCondition, "addtime", false, from, maxnum);
			model.put("customPaperList", customPaperList);
			PageUtil pageUtil = new PageUtil(count, maxnum, pageNo, "admin/custom/getPaperList.xhtml");
			Map params = new HashMap();
			params.put("startdate", startdate);
			params.put("enddate", enddate);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		return "admin/ticket/paper/customList.vm";
	}
	
	@RequestMapping("/admin/custom/saveConfig.xhtml")
	public String saveConfig(String content, ModelMap model){
		if(StringUtils.isNotBlank(content) && content.length() > 15) return showJsonError(model, "内容最多15字！");
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CUSTOM_PAPER);
		if(StringUtils.isNotBlank(content)){
			content = XSSFilter.filterAttr(content);
			content = StringUtils.replace(content, "&", "");
		}
		gewaConfig.setContent(content);
		daoService.saveObject(gewaConfig);
		return showJsonSuccess(model);
	}
}
