package com.gewara.web.action.admin.partner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.acl.Role;
import com.gewara.model.movie.Cinema;
import com.gewara.model.partner.Merchant;
import com.gewara.service.AuthService;
import com.gewara.service.partner.MerchantService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class MerchantAdminController extends BaseAdminController{
	@Autowired@Qualifier("authService")
	private AuthService authService;
	@Autowired@Qualifier("merchantService")
	private MerchantService merchantService;
	
	@RequestMapping("/admin/merchant/merchantList.xhtml")
	public String merchantList(String company, ModelMap model){
		List<Merchant> merchantList = merchantService.getMerchantList(company);
		Map<Long, String> nameMap = new HashMap<Long, String>();
		Map<Long, String> brandMap = new HashMap<Long, String>();
		for(Merchant merchant: merchantList){
			List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
			nameMap.put(merchant.getId(), StringUtils.join(BeanUtil.getBeanPropertyList(cinemaList, "name", true), ", "));
			brandMap.put(merchant.getId(), StringUtils.join(BeanUtil.getBeanPropertyList(cinemaList, "brandname", true), ", "));
		}
		model.put("nameMap", nameMap);
		model.put("brandMap", brandMap);
		model.put("merchantList", merchantList);
		
		return "admin/merchant/merchantList.vm";
	}
	@RequestMapping("/admin/merchant/modifyMerchant.xhtml")
	public String modifyMerchant(Long mid, ModelMap model){
		Merchant merchant = null;
		List<String> merRoles = new ArrayList<String>();
		if(mid!=null){
			merchant = daoService.getObject(Merchant.class, mid);
			model.put("merchant", merchant);
			if(StringUtils.isNotBlank(merchant.getRoles())){
				merRoles.addAll(Arrays.asList(StringUtils.split(merchant.getRoles(), ",")));
			}
		}
		model.put("merRoles", merRoles);
		List<Role> roleList = authService.getRoleListByTag(Merchant.ACL_TAG_MERCHANT);
		model.put("roleList", roleList);
		return "admin/merchant/modifyMerchant.vm";
	}
	@RequestMapping("/admin/merchant/saveMerchant.xhtml")
	public String saveMerchant(Long mid, String loginname, String mername, String roles,
			String newPassword, String company, String status, String opentype, String contact,
			String relatelist, ModelMap model){
		Merchant merchant = null;
		if(mid!=null){
			merchant = daoService.getObject(Merchant.class, mid);
		}else{
			merchant = new Merchant(loginname);
		}
		ChangeEntry entry = new ChangeEntry(merchant);
		Set<String> rolenames = new TreeSet<String>();
		if(StringUtils.isNotBlank(roles)){
			List<Role> roleList = authService.getRoleListByTag(Merchant.ACL_TAG_MERCHANT);
			Map<Long, Role> roleMap = BeanUtil.beanListToMap(roleList, "id");
			for(Long rid: BeanUtil.getIdList(roles, ",")){
				if(roleMap.containsKey(rid)){
					rolenames.add(roleMap.get(rid).getName());
				}
			}
		}
		merchant.setRoles(StringUtils.join(rolenames, ","));
		merchant.setCompany(company);
		merchant.setMername(mername);
		merchant.setOpentype(opentype);
		merchant.setStatus(status);
		merchant.setRelatelist(relatelist);
		merchant.setContact(contact);
		merchantService.saveMerchant(merchant, newPassword);
		monitorService.saveChangeLog(getLogonUser().getId(), Merchant.class, merchant.getId(), entry.getChangeMap(merchant));
		return showJsonSuccess(model, ""+merchant.getId());
	}
}
