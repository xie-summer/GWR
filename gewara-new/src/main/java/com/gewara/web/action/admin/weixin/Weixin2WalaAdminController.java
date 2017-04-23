package com.gewara.web.action.admin.weixin;

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
import com.gewara.json.Weixin2Wala;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class Weixin2WalaAdminController extends BaseAdminController {

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	@RequestMapping("/admin/weixin/walaList.xhtml")
	public String walaList(Integer pageNo, ModelMap model) {
		Map params = new HashMap();
		DBObject queryCondition = new BasicDBObject();
		queryCondition.put("docType", "new");
		params.put("docType", "new");
		int allCount = mongoService.getObjectCount(Weixin2Wala.class, queryCondition);
		
		int ROWS_PER_PAGE = 5;
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = ROWS_PER_PAGE;
		int firstPerPage = pageNo * rowsPerPage;
		
		
		List<Weixin2Wala> walaList = mongoService.getObjectList(Weixin2Wala.class, queryCondition, "addTime", false, firstPerPage, rowsPerPage);
		model.put("walaList", walaList);
		
		PageUtil pageUtil = new PageUtil(allCount, rowsPerPage, pageNo, "admin/weixin/walaList.xhtml", true, true);
		
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		return "admin/weixin/walaList.vm";
	}

	@RequestMapping("/admin/weixin/getWala.xhtml")
	public String toWalaModify(String id, String optionType, ModelMap model) {
		Weixin2Wala wala = new Weixin2Wala();
		if (StringUtils.isNotBlank(id)) {
			wala = mongoService.getObject(Weixin2Wala.class, "id", id);
		}
		model.put("wala", wala);
		if (StringUtils.equals("detail", optionType)) {
			return "admin/weixin/walaDetail.vm";
		}
		return "admin/weixin/walaModify.vm";
	}

	@RequestMapping("/admin/weixin/saveWala.xhtml")
	public String saveWala(Weixin2Wala wala, String operaType, ModelMap model) {
		if (StringUtils.isBlank(wala.getId())) {
			wala.setId(MongoData.buildId(10));
			wala.setAddTime(DateUtil.getCurFullTimestampStr());
		}
		wala.setDocType(operaType);
		mongoService.saveOrUpdateObject(wala, "id");
		return showJsonSuccess(model);
	}
}
