package com.gewara.model.partner;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.gewara.model.acl.GewaraUser;
import com.gewara.util.DateUtil;

/**
 * 上游合作商家（影院、运动场馆）
 * @author gebiao(ge.biao@gewara.com)
 * @since Mar 9, 2013 4:58:33 PM
 */
public class Merchant extends GewaraUser{
	public static final String ACL_TAG_MERCHANT = "GM";		//WebModule,Role tag
	public static final String ACL_USER_TYPE_MERCHANT = "merchant";
	public static final String ACL_MENU_REPOSITORY_KEY = "_merchant_menu_key";
	private static final long serialVersionUID = -6800394265547863600L;
	private Long id;
	private String loginname;		//登录名
	private String loginpass;		//登录密码
	private String mername;			//合作伙伴名称
	private String status;			//当前状态：暂停使用、禁用、正常使用
	private String roles;			//分配的角色
	private String company;			//公司名称
	private String opentype;		//类型：HFH,MTX,DX,GEWA
	private String relatelist;		//关联影院
	private String contact;			//联系电话
	private Timestamp addtime;
	
	private List<GrantedAuthority> tmpAuth;
	public Merchant(){
	}
	public Merchant(String loginname){
		this.loginname = loginname;
		this.addtime = DateUtil.getCurFullTimestamp();
	}
	@Override
	public final List<GrantedAuthority> getAuthorities() {
		if(tmpAuth!=null) return tmpAuth;
		tmpAuth = new ArrayList<GrantedAuthority>();
		if(StringUtils.isBlank(roles)) return tmpAuth;
		tmpAuth.addAll(AuthorityUtils.createAuthorityList(roles.split(",")));
		return tmpAuth;
	}
	@Override
	public final String getRolesString(){
		return roles;
	}
	@Override
	public final boolean isRole(String rolename){
		if(StringUtils.isBlank(roles)) return false;
		return Arrays.asList(roles.split(",")).contains(rolename);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Serializable realId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getPassword() {
		return loginpass;
	}

	@Override
	public String getUsername() {
		return loginname;
	}
	@Override
	public boolean isEnabled() {
		return "Y".equals(status);
	}

	@Override
	public String getRealname() {
		return mername;
	}

	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getLoginpass() {
		return loginpass;
	}

	public void setLoginpass(String loginpass) {
		this.loginpass = loginpass;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public String getRoles() {
		return roles;
	}
	public String getUsertype() {
		return ACL_USER_TYPE_MERCHANT;
	}
	public String getMername() {
		return mername;
	}
	public void setMername(String mername) {
		this.mername = mername;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public List<GrantedAuthority> getTmpAuth() {
		return tmpAuth;
	}
	public void setTmpAuth(List<GrantedAuthority> tmpAuth) {
		this.tmpAuth = tmpAuth;
	}
	public String getRelatelist() {
		return relatelist;
	}
	public void setRelatelist(String relatelist) {
		this.relatelist = relatelist;
	}
	public String getOpentype() {
		return opentype;
	}
	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
}
