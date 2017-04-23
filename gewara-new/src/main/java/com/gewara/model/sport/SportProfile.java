package com.gewara.model.sport;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class SportProfile extends BaseObject{
	private static final long serialVersionUID = 2725805400089821424L;
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_CLOSE = "close";
	public static final String EXITSRETURN_YES = "Y";	//支持退票
	public static final String EXITSRETURN_NO = "N";	//不支持退票
	public static final String RETURNMONEYTYPE_B= "B";	//按照订单折扣，   ‘A’是无效值
	public static final String RETURNMONEYTYPE_C= "C";	//按照固定值，   ‘A’是无效值
	public static final String RETURNMONEYTYPE_D= "D";	//按照百分比，   ‘A’是无效值
	
	public static final String EXITSCHANGE_YES = "Y";	//支持换票
	public static final String EXITSCHANGE_NO = "N";	//不支持换票
	public static final String CHANGEMONEYTYPE_D= "D";	//按照百分比，   ‘A’是无效值
	public static final String CHANGEMONEYTYPE_C= "C";	//按照固定值，   ‘A’是无效值
	
	public static final String TICKETTYPE_B = "B";	//POS机取票①，场馆方取票     ‘A’是无效值
	public static final String TICKETTYPE_C = "C";	//POS机取票②，自行取票     ‘A’是无效值
	public static final String TICKETTYPE_D = "D";	//凭手机人工识别，场馆工作人员核对信息     ‘A’是无效值
	public static final String TICKETTYPE_E = "E";	//现场派票，格瓦拉工作人员派票     ‘A’是无效值
	
	public static final String PRETYPE_ENTRUST = "E"; //委托代售
	public static final String PRETYPE_MANAGE = "M";	//自主经营
	
	private Long id;	
	private String encryptCode;
	private String opentime;
	private String closetime;		
	private String booking;			//是否开放购票
	private Integer sortnum;		//排序
	private String company;			//公司名称
	private String pretype;			//预售类型
	private String premessage;		// 代售说明
	
	private String citycode;
	
	public Integer getSortnum() {
		return sortnum;
	}
	public void setSortnum(Integer sortnum) {
		this.sortnum = sortnum;
	}
	public SportProfile(){
		
	}
	public SportProfile(Long sportid){
		this.id = sportid;
		this.sortnum = 0;
	}
	public String getBooking() {
		return booking;
	}
	public void setBooking(String booking) {
		this.booking = booking;
	}
	public String getEncryptCode() {
		return encryptCode;
	}
	public void setEncryptCode(String encryptCode) {
		this.encryptCode = encryptCode;
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
	public String getOpentime() {
		return opentime;
	}
	public void setOpentime(String opentime) {
		this.opentime = opentime;
	}
	public String getClosetime() {
		return closetime;
	}
	public void setClosetime(String closetime) {
		this.closetime = closetime;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getPretype() {
		return pretype;
	}
	public void setPretype(String pretype) {
		this.pretype = pretype;
	}
	
	public String getPremessage() {
		return premessage;
	}
	public void setPremessage(String premessage) {
		this.premessage = premessage;
	}
	public boolean hasPretype(String type){
		return StringUtils.equals(this.pretype, type);
	}
}
