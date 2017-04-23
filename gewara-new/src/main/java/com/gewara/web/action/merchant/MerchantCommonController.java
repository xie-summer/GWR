package com.gewara.web.action.merchant;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.model.partner.Merchant;
import com.gewara.web.menu.GBMenuDataBuilder;
import com.gewara.web.menu.MenuRepository;
import com.gewara.web.support.AclService;

@Controller
public class MerchantCommonController extends BaseMerchantController {
	@Autowired
	private AclService aclService;
	
	@RequestMapping("/merchant/console.xhtml")
	public String console(String reload, ModelMap model){
		Merchant merchant = getLogonMerchant();
		MenuRepository repository = (MenuRepository)applicationContext.getServletContext().getAttribute(Merchant.ACL_MENU_REPOSITORY_KEY);
		if(repository==null || "true".equals(reload)){
			repository = new MenuRepository(aclService.getMenuList(Merchant.ACL_TAG_MERCHANT));
			applicationContext.getServletContext().setAttribute(Merchant.ACL_MENU_REPOSITORY_KEY, repository);
		}
		List<GrantedAuthority> granted = merchant.getAuthorities();
		String[] roles = new String[granted.size()];
		for (int i = 0; i < granted.size(); i++) {
			roles[i]=granted.get(i).getAuthority();
		}
		GBMenuDataBuilder mdb = new GBMenuDataBuilder(config.getBasePath(), roles, repository);
		Map<Map, List<Map>> treeMap = mdb.getMenuTree();
		String menuData = mdb.getMenuData().toString();
		model.put("menuData", menuData);
		model.put("user", merchant);
		model.put("treeMap", treeMap);
		return "merchant/console.vm";
	}
	@RequestMapping(value="/merLogin.xhtml",method=RequestMethod.GET)
	public String partnerLogin(String TARGETURL, ModelMap model){
		if(StringUtils.isBlank(TARGETURL)){
			TARGETURL = "/merchant/console.xhtml";
		}
		model.put("TARGETURL", TARGETURL);
		return "merchant/login.vm";
	}

}
