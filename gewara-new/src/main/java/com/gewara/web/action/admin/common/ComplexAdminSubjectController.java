package com.gewara.web.action.admin.common;

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
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;


@Controller
public class ComplexAdminSubjectController extends AnnotationController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	//泰坦尼克号专题 start
	//明信片
	@RequestMapping("/admin/newsubject/titanicIndex.xhtml")
	public String titanicIndex(String info, Integer pageNo, String type, String tag, ModelMap model){
		if(StringUtils.isBlank(type)) type = MongoData.TITANIC;
		if(StringUtils.isBlank(tag)) tag = MongoData.TITANIC_POSTCARD;
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		Map postcard = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("postcard", postcard);
		if(StringUtils.isNotBlank(info)){
			if(null == pageNo) pageNo=0;
			int rowsPerPage = 50;
			int forms = pageNo * rowsPerPage;
			List<Map> postcardList = mongoService.find(MongoData.NS_POSTCARD_INFO, params, MongoData.ACTION_ADDTIME, true, forms, rowsPerPage);
			PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_POSTCARD_INFO, params), rowsPerPage, pageNo, "admin/newsubject/titanicIndex.xhtml");
			params.put("info", info);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
			model.put("info", info);
			model.put("postcardList", postcardList);
		}
		Map paramsMap = new HashMap();
		paramsMap.put(MongoData.ACTION_TYPE, type);
		paramsMap.put(MongoData.ACTION_TAG, MongoData.TITANIC_VOTE);
		Map vote = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, paramsMap);
		model.put("vote", vote);
		return "admin/newsubject/titanicIndex.vm";
	}
	//泰坦尼克号专题 end
	
	//超级战舰start
	@RequestMapping("/admin/newsubject/battleship.xhtml")
	public String alienBattlefield(){
		return "admin/newsubject/battleship.vm";
	}
	//复仇者联盟start
	@RequestMapping("/admin/newsubject/avengers.xhtml")
	public String avengers(){
		return "admin/newsubject/after/avengers.vm";
	}
	//复仇者联盟end
}
