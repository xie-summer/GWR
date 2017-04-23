package com.gewara.web.action.admin.content;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class AdminSubjectCompareController extends BaseAdminController {

	/***
	 *  电影对比专题
	 * */
	@RequestMapping("/admin/newsubject/newSubjectList_compare.xhtml")
	public String newSubjectList_compare(){
		return "admin/newsubject/compare_index.vm";
	}
}
