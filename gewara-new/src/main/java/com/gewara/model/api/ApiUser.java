package com.gewara.model.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.model.BaseObject;
import com.gewara.model.pay.GewaOrder;

public class ApiUser extends BaseObject{
	private static final long serialVersionUID = -6800394265547863600L;
	public static final String STATUS_OPEN = "open";	//正常使用
	public static final String STATUS_PAUSE = "pause";	//暂停使用
	public static final String STATUS_STOP = "stop";	//停止使用
	public static final String ROLE_PAYORDER = "payOrderApi";	//在线购票下定支付

	private Long id;
	private String usertype;		//用户类型：gewa,partner
	private String category;		//分类：ticket,sport....
	private String partnername;		//合作伙伴名称
	private String briefname;		//简称
	private String partnerip;		//合作伙伴IP
	private String partnerkey;		//验证身份密码
	private String privatekey;		//数据加密key
	private String content;			//描述
	private Timestamp updatetime;	//更新时间
	private Long clerk;				//操作人
	private String status;			//当前状态：暂停使用、禁用、正常使用
	private String logo;			//公司LOGO
	private String roles;			//分配的角色
	private String partnerpath;		//公司Path
	private String citycode;		//开放的城市
	private String defaultCity;		//默认城市
	private String addOrderUrl;		//传送给商家订单url
	private String pushurl;			//推送订单url
	private String qryurl;			//反查订单url
	private String notifyurl;		//支付链接
	private String secretKey;		//3DES加密key Hex
	private String pushflag;		//订单推送标识
	private String otherinfo;		//json数据，其他信息
	public final String getRolesString(){
		return roles;
	}
	public final boolean isRole(String rolename){
		if(StringUtils.isBlank(roles)) return false;
		return Arrays.asList(roles.split(",")).contains(rolename);
	}

	public ApiUser(){}
	
	public ApiUser(String partnerkey){
		this.partnerkey = partnerkey;
		this.status = STATUS_PAUSE;
	}
	
	public String getModifyLog(ApiUser old){//只比较重要信息
		String diff = "";
		if(StringUtils.equals(partnerip, old.getPartnerip())) diff += ",partnerip";
		if(StringUtils.equals(partnerkey, old.getPartnerkey())) diff += ",partnerkey";
		if(StringUtils.equals(privatekey, old.getPrivatekey())) diff += ",privatekey";
		if(StringUtils.equals(status, old.getStatus())) diff += ",status";
		return diff;
	}
	public String getPrivatekey() {
		return privatekey;
	}

	public void setPrivatekey(String privatekey) {
		this.privatekey = privatekey;
	}

	public String getPartnerkey() {
		return partnerkey;
	}

	public void setPartnerkey(String partnerkey) {
		this.partnerkey = partnerkey;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
	public String getPartnername() {
		return partnername;
	}

	public void setPartnername(String partnername) {
		this.partnername = partnername;
	}

	public String getPartnerip() {
		return partnerip;
	}

	public void setPartnerip(String partnerip) {
		this.partnerip = partnerip;
	}

	public Long getClerk() {
		return clerk;
	}

	public void setClerk(Long clerk) {
		this.clerk = clerk;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public void copyFrom(ApiUser another) {
		partnername = another.partnername;
		partnerkey = another.partnerkey;
		privatekey = another.privatekey;
		partnerip = another.partnerip;
		status = another.status;
		briefname = another.briefname;
		logo = another.logo;
		content = another.content;
		roles = another.roles;
		secretKey = another.secretKey;
		qryurl = another.qryurl;
		pushflag = another.pushflag;
		pushurl = another.pushurl;
		addOrderUrl = another.addOrderUrl;
		notifyurl = another.notifyurl;
		citycode = another.citycode;
		defaultCity = another.defaultCity;
	}

	public boolean isEnabled() {
		return STATUS_OPEN.equals(status);
	}

	public boolean isValidIp(String remoteIp) {
		return StringUtils.isBlank(partnerip) || partnerip.contains(remoteIp);
	}

	public String getBriefname() {
		return briefname;
	}

	public void setBriefname(String briefname) {
		this.briefname = briefname;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public String getRoles() {
		return roles;
	}
	public String getPartnerpath() {
		return partnerpath;
	}
	public void setPartnerpath(String partnerpath) {
		this.partnerpath = partnerpath;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getPushurl() {
		return pushurl;
	}
	public void setPushurl(String pushurl) {
		this.pushurl = pushurl;
	}
	public String getQryurl() {
		return qryurl;
	}
	public void setQryurl(String qryurl) {
		this.qryurl = qryurl;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getPushflag() {
		return pushflag;
	}
	public void setPushflag(String pushflag) {
		this.pushflag = pushflag;
	}
	public String getRealPayurl(GewaOrder order){
		if(StringUtils.isBlank(notifyurl)) return null;
		String result = StringUtils.replace(notifyurl, "${orderid}", ""+order.getId());
		result = StringUtils.replace(result, "${tradeNo}", order.getTradeNo());
		return result;
	}
	public String getAddOrderUrl() {
		return addOrderUrl;
	}
	public void setAddOrderUrl(String addOrderUrl) {
		this.addOrderUrl = addOrderUrl;
	}
	public String getNotifyurl() {
		return notifyurl;
	}
	public void setNotifyurl(String notifyurl) {
		this.notifyurl = notifyurl;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public boolean supportsCity(String scitycode) {
		return StringUtils.contains(this.citycode, scitycode) || StringUtils.equals(this.citycode, AdminCityContant.CITYCODE_ALL);
	}
	public String getDefaultCity() {
		return defaultCity;
	}
	public void setDefaultCity(String defaultCity) {
		this.defaultCity = defaultCity;
	}
	public String getUsertype() {
		return usertype;
	}
	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
}
