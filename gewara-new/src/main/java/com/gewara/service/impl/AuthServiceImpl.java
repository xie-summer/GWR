package com.gewara.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gewara.model.acl.Role;
import com.gewara.service.AuthService;

@Service("authService")
public class AuthServiceImpl extends BaseServiceImpl implements AuthService{

	@Override
	public List<String> getUserRoles(Long userid) {
		List<String> result = jdbcTemplate.queryForList("select name from WEBDATA.role where id in (select role_id from WEBDATA.USER_ROLE where USER_ID=?)", String.class, userid);
		return result;
	}

	@Override
	public List<String> getModuleRoles(Long moduleid) {
		List<String> result = jdbcTemplate.queryForList("select name from WEBDATA.role where id in (select role_id from WEBDATA.WEBMODULE_ROLE where MODULE_ID=?)", String.class, moduleid);
		return result;
	}

	@Override
	public List<Role> getRoleListByTag(String tag) {
		String query = "from Role where tag =?";
		List<Role> result = hibernateTemplate.find(query, tag);
		return result;
	}
}
