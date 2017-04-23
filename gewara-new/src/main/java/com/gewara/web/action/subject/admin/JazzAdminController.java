package com.gewara.web.action.subject.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.web.action.AnnotationController;

// 爵士音乐专题 代理
@Controller
public class JazzAdminController  extends AnnotationController {

	
	@RequestMapping("/admin/newsubject/jazz.xhtml")
	public String jazz(){
		return "admin/newsubject/jazz.vm";
	}
	
}