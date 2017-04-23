package com.gewara.web.action.admin.acl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.AdminCityContant;
import com.gewara.model.acl.Module2Role;
import com.gewara.model.acl.Role;
import com.gewara.model.acl.User;
import com.gewara.model.acl.User2Role;
import com.gewara.model.acl.WebModule;
import com.gewara.model.common.GewaCity;
import com.gewara.service.AclManager;
import com.gewara.service.AuthService;
import com.gewara.service.GewaCityService;
import com.gewara.util.ChangeEntry;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.support.DBFilterInvocationSecurityMetadataSource;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since Mar 31, 2008 AT 3:20:44 PM
 */
@Controller
public class AclAdminController extends BaseAdminController {

	@Autowired@Qualifier("securityMetadataSource")
	private SecurityMetadataSource securityMetadataSource;
	public void setSecurityMetadataSource(SecurityMetadataSource securityMetadataSource) {
		this.securityMetadataSource = securityMetadataSource;
	}
	@Autowired@Qualifier("aclManager")
	private AclManager aclManager = null;
	public void setAclManager(AclManager aclManager) {
		this.aclManager = aclManager;
	}
	@Autowired@Qualifier("authService")
	private AuthService authService;
	
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	@RequestMapping("/refreshAcl.xhtml")
	@ResponseBody
	public String refreshAcl(){
		List<Long> useridList = hibernateTemplate.find("select id from User order by id");
		for(Long id:useridList){
			List<String> roles = authService.getUserRoles(id);
			User user = daoService.getObject(User.class, id);
			user.setRolenames(StringUtils.join(roles, ","));
			daoService.saveObject(user);
		}
		List<Long> moduleidList = hibernateTemplate.find("select id from WebModule order by id");
		for(Long id: moduleidList){
			List<String> roles = authService.getModuleRoles(id);
			WebModule module = daoService.getObject(WebModule.class, id);
			module.setRolenames(StringUtils.join(roles, ","));
			daoService.saveObject(module);
		}
		return "success:" + new Date();
	}
	
	@RequestMapping("/admin/acl/userList.xhtml")
	public String userList(ModelMap model){
		List<User> userList = daoService.getAllObjects(User.class);
		model.put("userList", userList);
		Map<Long, Boolean> abnormalIPMap = new HashMap<Long, Boolean>();
		model.put("abnormalIPMap", abnormalIPMap);
		return "admin/acl/userList.vm";
	}
	@RequestMapping("/admin/acl/userCityList.xhtml")
	public String userCityList(ModelMap model){
		model.put("userList", daoService.getAllObjects(User.class));
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/acl/userCityList.vm";
	}
	@RequestMapping("/admin/acl/editUser.xhtml")
	public String editUser(Long userId, ModelMap model){
		List<Role> roleList = aclManager.getRoleListByTag("G");
		List<Role> userRoles = new ArrayList<Role>();
		if(userId==null) return showError(model, "不能新增用户！！");
		User user = daoService.getObject(User.class, userId);
		List<User2Role> u2rList = daoService.getObjectListByField(User2Role.class, "userid", userId);
		for(User2Role u2r: u2rList){
			if(u2r.getRole()!=null){
				roleList.remove(u2r.getRole());
				userRoles.add(u2r.getRole());
			}
		}
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		model.put("userRoles", userRoles);
		model.put("user", user);
		model.put("availableRoles", roleList);
		model.put("proMap", proMap);
		return "admin/acl/userForm.vm";
	}
	@RequestMapping("/admin/acl/saveUser.xhtml")
	public String saveUser(Long userId, String userCitycode, String accountEnabled, Long[] roleid, ModelMap model) {
		if(StringUtils.isBlank(userCitycode)) return showJsonError(model, "管理城市不能为空!");
		if(userId==null) return showError(model, "不能新增用户！！");
		User user = daoService.getObject(User.class, userId);
		ChangeEntry changeEntry = new ChangeEntry(user);
		user.setCitycode(userCitycode);
		user.setAccountEnabled(accountEnabled);
		Set<String> rolenames = new TreeSet<String>();
		List<User2Role> newList = new ArrayList<User2Role>();
		
		if (roleid != null) {
			List<User2Role> u2rList = daoService.getObjectListByField(User2Role.class, "userid", userId);
			List<Long> newRoleidList = new ArrayList<Long>(Arrays.asList(roleid));
			List<User2Role> removeList = new ArrayList<User2Role>();
			for(User2Role u2r: u2rList){
				if(ArrayUtils.contains(roleid, u2r.getRole().getId())) {
					newRoleidList.remove(u2r.getRole().getId());
					rolenames.add(u2r.getRole().getName());
				}else{
					removeList.add(u2r);
				}
			}
			for(Long rid: newRoleidList){
				Role role = daoService.getObject(Role.class, rid);
				newList.add(new User2Role(user.getId(), role));
				rolenames.add(role.getName());
			}
			daoService.removeObjectList(removeList);
		}
		daoService.saveObjectList(newList);
		user.setRolenames(StringUtils.join(rolenames, ","));
		daoService.saveObject(user);
		monitorService.saveChangeLog(getLogonUser().getId(), User.class, user.getId(),changeEntry.getChangeMap(user));
		return showMessage(model, "成功保存用户！") ;
	}
	@RequestMapping("/admin/acl/roleList.xhtml")
	public String roleList(ModelMap model, String tag){
		List<Role> roleList = aclManager.getRoleListByTag(tag);
		model.put("roleList", roleList);
		return "admin/acl/roleList.vm";
	}
	
	@RequestMapping("/admin/acl/modifyRole.xhtml")
	public String editRole(@RequestParam(value="roleId", required=true)Long roleId, ModelMap model){
		Role role = daoService.getObject(Role.class, roleId);
		model.put("role", role);
		if(StringUtils.equals(WebModule.TAG_GEWA, role.getTag())){
			if(roleId!=null){
				String query = "from User where id not in (select userid from User2Role where role.id = ?)";
				List<User> unrelatedUsers = hibernateTemplate.find(query, roleId);
				model.put("unrelatedUsers", unrelatedUsers);
				String query2 = "from User where id in (select userid from User2Role where role.id = ?)";
				List<User> roleUsers = hibernateTemplate.find(query2, roleId);
				model.put("roleUsers", roleUsers);
			}else{
				model.put("unrelatedUsers", daoService.getAllObjects(User.class));
				model.put("roleUsers", new ArrayList<User>());
			}
			
		}
		if(roleId!=null){
			String roleModuleQuery = "select module.id from Module2Role where role.id = ? ";
			List<Long> roleModules = hibernateTemplate.find(roleModuleQuery, roleId);
			model.put("roleModules", roleModules);
		}else{
			model.put("roleModules", new ArrayList<Long>());
		}
		DetachedCriteria query = DetachedCriteria.forClass(WebModule.class);
		query.add(Restrictions.isNull("menucode"));
		query.add(Restrictions.eq("tag", role.getTag()));
		query.addOrder(Order.asc("matchorder"));
		List<WebModule> moduleList = hibernateTemplate.findByCriteria(query);
		List<WebModule> mainMenuList = aclManager.getMainMenuList(role.getTag(), true);
		Map<String, List<WebModule>> subMenuMap = new HashMap<String, List<WebModule>>();
		List<WebModule> subMenuList = null;
		for(WebModule wm : mainMenuList){
			subMenuList = aclManager.getSubMenuList(role.getTag(), wm.getMenucode(), true);
			subMenuMap.put(wm.getMenucode(), subMenuList);
		}
		model.put("moduleList", moduleList);
		model.put("mainMenuList", mainMenuList);
		model.put("subMenuMap", subMenuMap);
		return "admin/acl/roleForm.vm";
	}
	@RequestMapping("/admin/acl/saveRole.xhtml")
	public String saveRole(@RequestParam(value="roleId", required=true)Long roleId, String name, String description,
			Long[] webmodule, Long[] user, ModelMap model){
		Role role = daoService.getObject(Role.class, roleId);
		ChangeEntry changeEntry = new ChangeEntry(role);
		role.setName(name);
		role.setDescription(description);
		if(webmodule==null) webmodule=new Long[0];
		if (webmodule != null) {
			String roleModuleQuery = "from Module2Role where role.id = ? and module.tag = ?";
			List<Module2Role> m2rList = hibernateTemplate.find(roleModuleQuery, roleId, role.getTag());
			List<Module2Role> removeList = new ArrayList<Module2Role>();
			List<Long> newmoduleidList = new ArrayList<Long>(Arrays.asList(webmodule));
			List<WebModule> refreshModuleList = new ArrayList<WebModule>();
			Map<Long, Set<String>/*moduleRoles*/> moduleMap = new HashMap<Long, Set<String>>();
			for(Module2Role m2r:m2rList){
				Set<String> wm = moduleMap.get(m2r.getModule().getId());
				if(wm==null){
					WebModule m = m2r.getModule();
					wm = new TreeSet<String>(Arrays.asList(StringUtils.split(m.getRolenames(), ",")));
					moduleMap.put(m2r.getModule().getId(), wm);
				}
				if(ArrayUtils.contains(webmodule, m2r.getModule().getId())){//已经存在
					newmoduleidList.remove(m2r.getModule().getId());
					wm.add(role.getName());
				}else{//原来的不存在，删除
					wm.remove(role.getName());
					removeList.add(m2r);
					refreshModuleList.add(m2r.getModule());
				}
			}
			List<Module2Role> newList = new ArrayList<Module2Role>();
			for(Long mid: newmoduleidList){
				Set<String> wm = moduleMap.get(mid);
				WebModule m = daoService.getObject(WebModule.class, mid);
				if(wm==null){
					wm = new TreeSet<String>();
					if(StringUtils.isNotBlank(m.getRolenames())){
						wm.addAll(Arrays.asList(StringUtils.split(m.getRolenames(), ",")));
					}
					wm.add(role.getName());
					moduleMap.put(mid, wm);
					refreshModuleList.add(m);
				}
				Module2Role mr = new Module2Role(m, role);
				newList.add(mr);
			}
			for(WebModule wm: refreshModuleList){
				wm.setRolenames(StringUtils.join(moduleMap.get(wm.getId()), ","));
			}
			daoService.removeObjectList(removeList);
			daoService.saveObjectList(newList);
			daoService.saveObjectList(refreshModuleList);
		}
		//角色
		if(user==null) user = new Long[]{};
		String roleModuleQuery = "from User2Role where role.id = ? ";
		List<User2Role> u2rList = hibernateTemplate.find(roleModuleQuery, roleId);
		List<User2Role> removeList = new ArrayList<User2Role>();
		List<Long> newuseridList = new ArrayList<Long>(Arrays.asList(user));
		List<User> refreshUserList = new ArrayList<User>();
		Map<Long, Set<String>/*userRoles*/> userRolesMap = new HashMap<Long, Set<String>>();
		for(User2Role u2r:u2rList){
			Set<String> uroles = userRolesMap.get(u2r.getUserid());
			if(uroles==null){
				User u = daoService.getObject(User.class, u2r.getUserid());
				uroles = new TreeSet<String>(Arrays.asList(StringUtils.split(u.getRolenames(), ",")));
				userRolesMap.put(u2r.getUserid(), uroles);
			}
			if(ArrayUtils.contains(user, u2r.getUserid())){
				uroles.add(u2r.getRole().getName());
				newuseridList.remove(u2r.getUserid());
			}else{
				uroles.remove(role.getName());
				removeList.add(u2r);
				User u = daoService.getObject(User.class, u2r.getUserid());
				refreshUserList.add(u);
			}
		}
		List<User2Role> newList = new ArrayList<User2Role>();
		for(Long uid: newuseridList){
			User2Role mr = new User2Role(uid, role);
			newList.add(mr);
			Set<String> uroles = userRolesMap.get(uid);
			if(uroles==null){
				User u = daoService.getObject(User.class, uid);
				uroles = new TreeSet<String>();
				if(StringUtils.isNotBlank(u.getRolenames())){
					uroles.addAll(Arrays.asList(StringUtils.split(u.getRolenames(), ",")));
				}
				userRolesMap.put(uid, uroles);
				refreshUserList.add(u);
			}
			uroles.add(role.getName());
		}
		for(User u: refreshUserList){
			u.setRolenames(StringUtils.join(userRolesMap.get(u.getId()), ","));
		}
		daoService.removeObjectList(removeList);
		daoService.saveObjectList(newList);
		daoService.saveObjectList(refreshUserList);

		daoService.saveObject(role);
		monitorService.saveChangeLog(getLogonUser().getId(), Role.class, role.getId(),changeEntry.getChangeMap( role));
		return showMessage(model, "成功保存角色信息");
	}

	@RequestMapping("/admin/acl/moduleList.xhtml")
	public String moduleList(String tag, ModelMap model){
		List<WebModule> mainMenuList = aclManager.getMainMenuList(tag, true);
		Map<String, List<WebModule>> subMenuMap = new HashMap<String, List<WebModule>>();
		List<WebModule> subMenuList = null;
		for(WebModule wm : mainMenuList){
			subMenuList = aclManager.getSubMenuList(tag, wm.getMenucode(), true);
			subMenuMap.put(wm.getMenucode(), subMenuList);
		}
		model.put("mainMenuList", mainMenuList);
		model.put("subMenuMap", subMenuMap);
		return "admin/acl/moduleList.vm";
	}
	@RequestMapping("/admin/acl/modifyWebModule.xhtml")
	public String editWebModule(Long moduleId, String tag, ModelMap model){
		WebModule module = new WebModule();
		List<Role> moduleRoles = new ArrayList<Role>();
		if(moduleId==null && StringUtils.isBlank(tag)){
			return showError(model, "未选择Tag！");
		}
		if(moduleId != null) {
			module = daoService.getObject(WebModule.class, moduleId);
			tag = module.getTag();
			moduleRoles = hibernateTemplate.find("select role from Module2Role m where m.module.id=?", moduleId);
		}	
		model.put("moduleRoles", moduleRoles);
		List<Role> allRoles = aclManager.getRoleListByTag(tag);
		allRoles.removeAll(moduleRoles);
		model.put("availableRoles", allRoles);
		List<WebModule> mainMenuList = aclManager.getMainMenuList(tag, true);
		model.put("module", module);
		model.put("mainMenuList", mainMenuList);
		model.put("tag", tag);
		return "admin/acl/webModuleForm.vm";
	}
	@RequestMapping("/admin/acl/saveWebModule.xhtml")
	public String saveWebModule(ModelMap model, 
			@RequestParam String moduleurl,
			@RequestParam String menutitle,
			@RequestParam Integer matchorder,
			@RequestParam String display, 
			@RequestParam String tag,
			Long moduleId,
			Long[] roleid, 
			String mainmenucode,
			String menucode,
			String target){
		WebModule module = new WebModule();
		if(moduleId != null) {
			module = daoService.getObject(WebModule.class, moduleId);
		} else {	//新增加
			if(StringUtils.isBlank(mainmenucode) && "Y".equals(display)) {//添加主菜单的情况，防止重复
				if(StringUtils.isBlank(menucode) || menucode.length()!=2)
					return forwardMessage(model, "主菜单的菜单编码必须是两位数字" );
				DetachedCriteria query = DetachedCriteria.forClass(WebModule.class);
				query.add(Restrictions.eq("menucode", menucode));
				List<WebModule> wmList = hibernateTemplate.findByCriteria(query);
				if(!wmList.isEmpty()) {
					return forwardMessage(model, "你输入的菜单编码：" + menucode + ", 已经被[" + wmList.get(0).getMenutitle() +"]占有" );
				}
			}
		}
		ChangeEntry changeEntry = new ChangeEntry(module);
		module.setModuleurl(moduleurl);
		module.setMatchorder(matchorder);
		module.setTag(tag);
		module.setMenutitle(menutitle);
		module.setTarget(target);//是否显示
		module.setDisplay(display);//是否显示
		module.setMenucode(menucode);//是否显示
		List<Module2Role> newList = new ArrayList<Module2Role>();
		Set<String> rolenames = new TreeSet<String>();
		if (roleid != null) {
			if(moduleId!=null){//修改
				List<Module2Role> m2rList = hibernateTemplate.find("from Module2Role where module.id=? ", moduleId);
				List<Module2Role> removeList = new ArrayList<Module2Role>();
				List<Long> newRoleidList = new ArrayList<Long>(Arrays.asList(roleid));
				for(Module2Role m2r: m2rList){
					if(ArrayUtils.contains(roleid, m2r.getRole().getId())) {
						newRoleidList.remove(m2r.getRole().getId());
						rolenames.add(m2r.getRole().getName());
					}else{
						removeList.add(m2r);
					}
				}
				for(Long rid: newRoleidList){
					Role role = daoService.getObject(Role.class, rid);
					newList.add(new Module2Role(module, role));
					rolenames.add(role.getName());
				}
				daoService.removeObjectList(removeList);
				
			}else{
				daoService.saveObject(module);
				for(Long rid: roleid){
					Role role = daoService.getObject(Role.class, rid);
					newList.add(new Module2Role(module, role));
					rolenames.add(role.getName());
				}
			}
		}
		module.setRolenames(StringUtils.join(rolenames, ","));
		daoService.saveObjectList(newList);
		daoService.saveObject(module);
		monitorService.saveChangeLog(getLogonUser().getId(), WebModule.class, module.getId(),changeEntry.getChangeMap( module));
		return showMessage(model, "成功保存模块信息");
	}
	@RequestMapping("/admin/acl/reloadAcl.xhtml")
	public String reload(ModelMap model){
		((DBFilterInvocationSecurityMetadataSource)securityMetadataSource).init();
		return forwardMessage(model, "重新载入权限成功！");
	}
	@RequestMapping("/admin/acl/ajax/addRole.xhtml")
	public String addRole(String rolename, String description, String tag, ModelMap model) {
		Role role = aclManager.addRole(rolename, description, tag);
		return showJsonSuccess(model, role.getId()+"");
	}
	@RequestMapping("/admin/acl/ajax/getUserWebModule.xhtml")
	public String getUserWebModule(Long userid, ModelMap model){
		List<Role> userRoles = hibernateTemplate.find("select m.role from User2Role m where m.userid=?", userid);
		Set<Long> moduleidList = new HashSet<Long>();
		for(Role role: userRoles){
			List<Long> roleModules = hibernateTemplate.find("select module.id from Module2Role where role.id = ?", role.getId());
			moduleidList.addAll(roleModules);
		}
		
		Map<String, List<WebModule>> wMap = new HashMap<String, List<WebModule>>();
		List<WebModule> mainList = new ArrayList<WebModule>();
		List<WebModule> wList = daoService.getObjectList(WebModule.class, moduleidList);
		for(WebModule w : wList){
			if(w.getMenucode().length()==2) {
				mainList.add(w);
			}else{
				String key = w.getMenucode().substring(0, 2);
				List<WebModule> moduleList = wMap.get(key);
				if(moduleList==null){
					moduleList = new ArrayList<WebModule>();
					wMap.put(key, moduleList);
				}
				moduleList.add(w);
			}
		}
		model.put("mainList", mainList);
		model.put("wMap", wMap);
		return "admin/acl/aclPreview.vm";
	}
}
