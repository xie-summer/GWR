package com.gewara.web.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.gewara.util.FileSearchUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;

@Controller
public class NaviVmController extends AnnotationController implements ApplicationContextAware{
	private XmlWebApplicationContext appContext;
	@RequestMapping("/direct.xhtml")
	public String directVm(HttpServletRequest request, String vm, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		if(WebUtils.isLocalIp(ip)){
			return vm + ".vm";			
		}
		return showError(model, "只能用于内部测试！");
	}
	@RequestMapping("/nav.xhtml")
	public String navigator(HttpServletRequest request, ModelMap model) throws Exception{
		String ip = WebUtils.getRemoteIp(request);
		if(!WebUtils.isLocalIp(ip)){
			return showError(model, "只能用于内部测试！");
		}
		String file = appContext.getServletContext().getRealPath("/WEB-INF/pages");
		Map fileTree = FileSearchUtil.getFileTree(file, ".vm");
		model.put("menuData", JsonUtils.writeObjectToJson(fileTree));
		dbLogger.warn(file);
		return "vmTree.vm";
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.appContext = (XmlWebApplicationContext) applicationContext;
	}
}
