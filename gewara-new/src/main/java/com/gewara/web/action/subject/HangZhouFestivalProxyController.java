package com.gewara.web.action.subject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.util.DateUtil;
import com.gewara.web.action.AnnotationController;

// 杭州音乐节专题 代理
@Controller
public class HangZhouFestivalProxyController  extends AnnotationController {

	
	@RequestMapping("/admin/newsubject/hangzhoufestival.xhtml")
	public String hangzhoufestival(){
		return "admin/newsubject/hangzhoufestival.vm";
	}
	@RequestMapping("/subject/proxy/getRemoteServerTime.xhtml")
	public String getRemoteServerTime(ModelMap model){
		String servertime = DateUtil.format(DateUtil.currentTime(), "yyyy-MM-dd HH:mm:ss");
		return showJsonSuccess(model, servertime);
	}
	
	
}