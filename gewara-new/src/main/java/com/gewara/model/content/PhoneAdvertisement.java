package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class PhoneAdvertisement extends BaseObject{
	private static final long serialVersionUID = 4961436964379898448L;
	public static final String APP_TYPE_CINAME="ciname";
	public static final String APP_TYPE_SPORT="sport";
	public static final String APP_TYPE_BAR="bar";
	
	public static final String OS_TYPE_ANDROID="ANDROID";
	public static final String OS_TYPE_IPHONE="IPHONE";
	public static final String OS_TYPE_ALL="ALL";
	
	public static final String IS_SHOW_Y="Y";
	public static final String IS_SHOW_N="N";
	
	public static final String STATUS_DELETE="D";
	public static final String STATUS_NEW="N";
	
	public static final String ADVERTTYPE_NORMAL="normal";//普通广告
	public static final String ADVERTTYPE_GRABTICKET="grabTicket";//抢票
	
	
	private Long id;
	private String advlink;
	private String link;
	private String title;
	private String apptype;//应用类型(cinema,bar,sport)
	private String osType;//系统类型(ANDROID,IPHONE)
	private String citycode;
	private Timestamp addtime;
	private String isshow;//是否显示
	private String status;
	private Timestamp starttime;
	private Timestamp endtime;
	
	private Long relatedid;  //关联id
	private Long activityid; //真实活动id
	private String summary;
	
	private Integer rank;//排序
	private String advertType;//活动类型 
	private String sharefriend;
	@Override
	public Serializable realId() {
		return id;
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getApptypeName(String type){
		if(APP_TYPE_BAR.equals(type)) return "酒吧";
		if(APP_TYPE_CINAME.equals(type)) return "影院";
		if(APP_TYPE_SPORT.equals(type)) return "运动";
		return "";
	}
	
	public PhoneAdvertisement() {
		super();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getApptype() {
		return apptype;
	}

	public void setApptype(String apptype) {
		this.apptype = apptype;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}


	public String getIsshow() {
		return isshow;
	}

	public void setIsshow(String isshow) {
		this.isshow = isshow;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getStarttime() {
		return starttime;
	}

	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}

	public Timestamp getEndtime() {
		return endtime;
	}

	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}

	public String getAdvlink() {
		return advlink;
	}

	public void setAdvlink(String advlink) {
		this.advlink = advlink;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getAdvertType() {
		return advertType;
	}

	public void setAdvertType(String advertType) {
		this.advertType = advertType;
	}

	public Long getActivityid() {
		return activityid;
	}

	public void setActivityid(Long activityid) {
		this.activityid = activityid;
	}

	public String getSharefriend() {
		return sharefriend;
	}

	public void setSharefriend(String sharefriend) {
		this.sharefriend = sharefriend;
	}
	

}
