package com.gewara.web.action.admin.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.acl.User;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class IframeAdminController extends BaseAdminController {

	//头部
	@RequestMapping("/admin/order/header.xhtml")
	public String header(ModelMap model) {
		User user = getLogonUser();
		model.put("user", user);
		return "admin/order/header.vm";
	}
		
	//脚部
	@RequestMapping("/admin/order/bottom.xhtml")
	public String bottom() {
		return "admin/order/bottom.vm";
	}
	
	//子导航_业务监控
	@RequestMapping("/admin/order/subnav.xhtml")
	public String subnav(/*String reload, ModelMap model*/) {
		/*User user = getLogonUser();
		String[] roles = StringUtils.split(user.getRolenames(), ",");
		MenuRepository repository = (MenuRepository)applicationContext.getServletContext().getAttribute(MenuRepository.GEWA_MENU_REPOSITORY_KEY);
		if(repository==null || "true".equals(reload)){
			repository = new MenuRepository(aclService.getMenuList(WebModule.TAG_GEWA));
			applicationContext.getServletContext().setAttribute(MenuRepository.GEWA_MENU_REPOSITORY_KEY, repository);
		}
		GBMenuDataBuilder mdb = new GBMenuDataBuilder(config.getBasePath(), roles, repository);
		String menuData = mdb.getMenuData().toString();
		Map<Map, List<Map>> treeMap = mdb.getMenuTree();
		model.put("menuData", menuData);
		model.put("user", user);
		model.put("treeMap", treeMap);*/
		return "admin/order/subnav.vm";
	}
	
	//子导航_数据管理
	@RequestMapping("/admin/order/subnavManage.xhtml")
	public String subnav_manage() {
		return "admin/rtwarn/subnavManage.vm";
	}
	
	//切换侧导航按钮
	@RequestMapping("/admin/order/tabSubnav.xhtml")
	public String tabSubnav() {
		return "admin/order/tabs.vm";
	}
		
}
