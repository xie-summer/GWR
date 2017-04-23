package com.gewara.web.action.subject.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.mobile.FordTestDrive;
import com.gewara.mongo.MongoService;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class FordAdminController extends BaseAdminController {
	
	private static final int ROWS_PER_PAGE = 500;
	
	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	@RequestMapping("/admin/subject/ford/index.xhtml")
	public String index() {
		return "admin/subject/ford/index.vm";
	}

	@RequestMapping("/admin/subject/ford/queryTestDrive.xhtml")
	public String queryTestDrive(FordTestDrive fordTestDrive,Integer pageNo, ModelMap model) {
		DBObject queryCondition = new BasicDBObject();
		Map params = new HashMap();
		if (StringUtils.isNotEmpty(fordTestDrive.getSource())) {
			params.put("source", fordTestDrive.getSource());
			queryCondition.put("source", fordTestDrive.getSource());
		}
		if (StringUtils.isNotBlank(fordTestDrive.getDriveName())) {
			params.put("driveName", fordTestDrive.getDriveName());
			queryCondition.put("driveName", fordTestDrive.getDriveName());
		}
		if (StringUtils.isNotBlank(fordTestDrive.getMobileNo())) {
			params.put("mobileNo", fordTestDrive.getMobileNo());
			queryCondition.put("mobileNo", fordTestDrive.getMobileNo());
		}
		if (StringUtils.isNotBlank(fordTestDrive.getCityname())) {
			params.put("cityname", fordTestDrive.getCityname());
			Pattern cityname = Pattern.compile("^.*" + fordTestDrive.getCityname() + ".*$");
			queryCondition.put("cityname", cityname);
		}
		int allCount = mongoService.getObjectCount(FordTestDrive.class, queryCondition);
		
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = ROWS_PER_PAGE;
		int firstPerPage = pageNo * rowsPerPage;
		
		List<FordTestDrive> fordTestDriveList = mongoService.getObjectList(FordTestDrive.class, queryCondition, "addTime", true, firstPerPage, rowsPerPage);
		model.put("fordTestDriveList", fordTestDriveList);
		
		PageUtil pageUtil = new PageUtil(allCount, rowsPerPage, pageNo, "admin/subject/ford/queryTestDrive.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		
		model.put("requestParam", fordTestDrive);
		return "admin/subject/ford/index.vm";
	}
}
