package com.gewara.model.bbs.commu;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class Commu extends BaseObject {
	
	private static final long serialVersionUID = 6556756647820158115L;
	public static final long HOTVALUE_GENERAL = 0; //一般
	public static final long HOTVALUE_HOT = 30000; //热门
	public static final long HOTVALUE_RECOMMEND = 50000; //推荐
	public static final String COMMU_VISITPERMISSION_PUBLIC = "public";//非登录成员
	public static final String COMMU_VISITPERMISSION_COMMUMEMBER = "commumember";//圈子成员
	public static final String COMMU_VISITPERMISSION_COMMUADMIN = "commuadmin";//圈子管理员
	
	private Long id;
	private String name;
	private Long adminid;
	private Long subadminid;
	private String authority;//public会员发帖，admin_only圈子管理员可以发帖
	private String info;
	private Long relatedid;
	private String tag;
	private Long smallcategoryid;
	private String smallcategory;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String logo;
	private String publicflag;//public谁都可以加入，论坛对全体公开。auth_sns必须得到管理人的承认，论坛对全体公开 。auth_commu_member必须得到管理人的承认，论坛只对参加者公开。
	private String joinemail; //加入接收邮件Y接收邮件N不接收邮件
	private String registjoin;//拒绝用户加入Y拒绝N不拒绝
	private Long hotvalue;
	private String citycode;
	private Long commumembercount;
	private Object relate;
	private Object relate2;
	private String status;
	private Integer clickedtimes;
	private String countycode;
	private String interesttag;
	private String indexareacode;
	private String commubg;
	//20101118
	private String visitpermission; //访问权限
	
	private String checkstatus;	// 圈子审核状态
	private String ip; //创建者IP
	@Override
	public Serializable realId() {
		return id;
	}
	
	public String getVisitpermission() {
		return visitpermission;
	}

	public void setVisitpermission(String visitpermission) {
		this.visitpermission = visitpermission;
	}

	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCommumembercount() {
		return commumembercount;
	}

	public void setCommumembercount(Long commumembercount) {
		this.commumembercount = commumembercount;
	}

	public Commu(){}
	
	public Commu(String name){
		this.authority="public";
		this.subadminid=0l;
		this.addtime=new Timestamp(System.currentTimeMillis());
		this.updatetime=new Timestamp(System.currentTimeMillis());
		this.joinemail="Y";
		this.registjoin="N";
		this.hotvalue=0l;
		this.commumembercount=1l;
		this.status=Status.N_NIGHT;	//应产品需求：新建圈子全部变为夜间状态
		this.clickedtimes = 0;
		this.checkstatus = "N";	// CommuManage.STATUSXXX
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getSubadminid() {
		return subadminid;
	}
	public void setSubadminid(Long subadminid) {
		this.subadminid = subadminid;
	}
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public Long getSmallcategoryid() {
		return smallcategoryid;
	}
	public void setSmallcategoryid(Long smallcategoryid) {
		this.smallcategoryid = smallcategoryid;
	}
	public String getSmallcategory() {
		return smallcategory;
	}
	public void setSmallcategory(String smallcategory) {
		this.smallcategory = smallcategory;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getLogo() {
		return logo;
	}
	public String getRlogo() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getPublicflag() {
		return publicflag;
	}
	public void setPublicflag(String publicflag) {
		this.publicflag = publicflag;
	}
	public String getJoinemail() {
		return joinemail;
	}
	public void setJoinemail(String joinemail) {
		this.joinemail = joinemail;
	}
	public String getRegistjoin() {
		return registjoin;
	}
	public void setRegistjoin(String registjoin) {
		this.registjoin = registjoin;
	}
	public Long getHotvalue() {
		return hotvalue;
	}
	public void setHotvalue(Long hotvalue) {
		this.hotvalue = hotvalue;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getAdminid() {
		return adminid;
	}
	public void setAdminid(Long adminid) {
		this.adminid = adminid;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getHeadpicUrl(){
		if(StringUtils.isNotBlank(logo)) return logo;
			return "img/default_head.png";
	}
	public void addCommumembercount(){
		commumembercount = commumembercount + 1; 
	}

	public Object getRelate() {
		return relate;
	}

	public void setRelate(Object relate) {
		this.relate = relate;
	}

	public Object getRelate2() {
		return relate2;
	}

	public void setRelate2(Object relate2) {
		this.relate2 = relate2;
	}

	public String getInteresttag() {
		return interesttag;
	}

	public void setInteresttag(String interesttag) {
		this.interesttag = interesttag;
	}
	
	public String getInterest(){
		if(StringUtils.isNotBlank(this.interesttag)){
			return this.interesttag.replace("|",",");
		}
		return "";
	}

	public String getIndexareacode() {
		return indexareacode;
	}

	public void setIndexareacode(String indexareacode) {
		this.indexareacode = indexareacode;
	}

	public String getCheckstatus() {
		return checkstatus;
	}

	public void setCheckstatus(String checkstatus) {
		this.checkstatus = checkstatus;
	}

	public String getCommubg() {
		return commubg;
	}

	public void setCommubg(String commubg) {
		this.commubg = commubg;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	
	public String getIp(){
		return ip;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public boolean hasStatus(String stats){
		return StringUtils.equals(this.status, stats);
	}
}
