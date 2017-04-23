package com.gewara.model.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.gewara.model.acl.GewaraUser;

public class CooperUser extends GewaraUser{
	private static final long serialVersionUID = -3385352619283074454L;
	public static final String ROLR_APIUSER = "apiuser";
	public static final String ROLR_LOCTICKET = "locTicket";
	private Long id;
	private Long partnerid;
	private String loginname;
	private String loginpass;
	private String name;
	private String partnerids;
	private String appsource;
	private String status;
	private String roles;
	private String usertype;
	private String origin;
	
	private String realName;
	private String mobile;
	private String tag;
	private String relatedids;
	private String category;
	private String categoryids;
	
	private List<GrantedAuthority> tmpAuth;
	public final List<GrantedAuthority> getAuthorities() {
		if(tmpAuth!=null) return tmpAuth;
		tmpAuth = new ArrayList<GrantedAuthority>();
		if(StringUtils.isBlank(roles)) return tmpAuth;
		tmpAuth.addAll(AuthorityUtils.createAuthorityList(roles.split(",")));
		return tmpAuth;
	}
	public final String getRolesString(){
		return roles;
	}
	public final boolean isRole(String rolename){
		if(StringUtils.isBlank(roles)) return false;
		return Arrays.asList(roles.split(",")).contains(rolename);
	}
	public CooperUser(){
		
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPartnerids() {
		return partnerids;
	}
	public void setPartnerids(String partnerids) {
		this.partnerids = partnerids;
	}
	public String getAppsource() {
		return appsource;
	}
	public void setAppsource(String appsource) {
		this.appsource = appsource;
	}
	@Override
	public Serializable realId() {
		return id;
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
		return ApiUser.STATUS_OPEN.equals(status);
	}
	@Override
	public String getRealname() {
		return name;
	}
	@Override
	public String getUsertype() {
		return usertype;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public String getPartnername(){
		return name;
	}
	public List<String> getAsList(){
		List<String> strList = new ArrayList();
		if(StringUtils.isNotBlank(appsource)) {
			return Arrays.asList(appsource.split(","));
		}
		return strList;
	}
	public List<String> getOriList(){
		List<String> strList = new ArrayList();
		if(StringUtils.isNotBlank(origin)) {
			return Arrays.asList(origin.split(","));
		}
		return strList;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getRelatedids() {
		return relatedids;
	}
	public void setRelatedids(String relatedids) {
		this.relatedids = relatedids;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCategoryids() {
		return categoryids;
	}
	public void setCategoryids(String categoryids) {
		this.categoryids = categoryids;
	}
	public List<GrantedAuthority> getTmpAuth() {
		return tmpAuth;
	}
	public void setTmpAuth(List<GrantedAuthority> tmpAuth) {
		this.tmpAuth = tmpAuth;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
}
