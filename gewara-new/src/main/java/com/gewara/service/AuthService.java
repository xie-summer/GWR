package com.gewara.service;

import java.util.List;

import com.gewara.model.acl.Role;

public interface AuthService {
	List<String> getUserRoles(Long userid);
	List<String> getModuleRoles(Long moduleid);
	List<Role> getRoleListByTag(String tag);
}
