package com.gewara.service.impl;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.api.CooperUser;
import com.gewara.model.partner.Merchant;
import com.gewara.model.user.Member;
import com.gewara.util.ValidateUtil;

/**
 * @author acerge(acerge@163.com)
 * @since 1:59:19 PM Aug 11, 2009
 */
@Service("aclService")
public class ShAclServiceImpl extends AclServiceImpl {
	private static final String validLogonType = "[member][wap][partner][user][merchant]";
	private static boolean isValidLogonType(String logonType){
		return validLogonType.indexOf("["+logonType+"]")>=0;
	}
	
	@Override
	public GewaraUser loadUserByUsername(String username) throws UsernameNotFoundException {
		GewaraUser user = null;
		String ltype = LOGON_TYPE.get();
		if(!isValidLogonType(ltype)) ltype = "member";
		if(ltype.equals("member") || ltype.equals("wap")){
			user = getMember(username);
		}else if(ltype.equals("user")){
			user = baseDao.getObjectByUkey(User.class, "username", username, true);
		}else if(ltype.equals("partner")){
			user = baseDao.getObjectByUkey(CooperUser.class, "loginname", username, true);
		}else if(ltype.equals("merchant")){
			user = baseDao.getObjectByUkey(Merchant.class, "loginname", username, true);
		}
		LOGON_TYPE.set(null);
		return user;
	}
	
	private Member getMember(String emailOrMobile){
		String query1 = "from Member m where m.email = ? ";
		String query2 = "from Member m where m.mobile = ? ";
		List<Member> members = null;
		if(ValidateUtil.isMobile(emailOrMobile)){
			members = hibernateTemplate.find(query2, emailOrMobile);
		}else{
			members = hibernateTemplate.find(query1, emailOrMobile.toLowerCase());
		}
		if(members.size()> 0) {
			return members.get(0);
		}
		return null;
	}
}
