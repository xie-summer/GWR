package com.gewara.web.action.admin.report;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.helper.sys.DynReportHelper;
import com.gewara.model.acl.User;
import com.gewara.model.report.Report;
import com.gewara.service.gewapay.DynReportService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class ReportManageController extends BaseAdminController{
	@Autowired@Qualifier("dynReportService")
	private DynReportService dynReportService;
	@RequestMapping("/admin/sysmgr/report/reportList.xhtml")
	public String reportList(ModelMap model){
		List<Report> reportList = dynReportService.getDynReportList();
		Map<String, Report> reportMap = BeanUtil.groupBeanList(reportList, "category");
		model.put("reportMap", reportMap);
		return "admin/sysmgr/report/reportList.vm";
	}
	@RequestMapping("/admin/sysmgr/report/modifyReport.xhtml")
	public String modifyReport(Long rid, ModelMap model){
		Report report = null;
		if(rid!=null){
			report = daoService.getObject(Report.class, rid);
			model.put("report", report);
		}
		return "admin/sysmgr/report/modifyReport.vm";
	}
	@RequestMapping("/admin/sysmgr/report/saveReport.xhtml")
	public String saveReport(Long rid, HttpServletRequest request, ModelMap model){
		Report report = null;
		if(rid!=null){
			report = daoService.getObject(Report.class, rid);
		}else{
			report = new Report();
		}
		BindUtils.bindData(report, request.getParameterMap());
		User user = getLogonUser();
		ErrorCode code = dynReportService.saveReport(report, user);
		if(code.isSuccess()) return showJsonSuccess(model, ""+report.getId());
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/sysmgr/report/getQry.xhtml")
	public String getQry(Long rid, ModelMap model){
		Report report = daoService.getObject(Report.class, rid);
		User user = getLogonUser();
		DynReportHelper helper = new DynReportHelper(report);
		dynReportService.checkRights(report, user);
		model.put("helper", helper);
		model.put("report", report);
		return "admin/sysmgr/report/qryForm.vm";
	}
	@RequestMapping("/admin/sysmgr/report/executeReport.xhtml")
	public String executeReport(Long rid, HttpServletRequest request, ModelMap model){
		Report report = daoService.getObject(Report.class, rid);
		User user = getLogonUser();
		DynReportHelper helper = new DynReportHelper(report, request);
		dynReportService.checkRights(report, user);
		model.put("helper", helper);
		model.put("report", report);
		List<Map<String, Object>> dataList = dynReportService.getReportDataList(report, 0, helper.getParameterList(), user);
		if(!dataList.isEmpty()){
			model.put("fieldList", dataList.get(0).keySet());
		}
		model.put("dataList", dataList);
		return "admin/sysmgr/report/reportResult.vm";
	}
}
