package com.gewara.json;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.pay.GewaOrder;

public class AppSourceCount implements Serializable{
	private static final long serialVersionUID = 4008994603934771097L;
	public static final String TYPE_IO 		= "io";		//安装和打开
	public static final String TYPE_REG 	= "reg";		//注册
	public static final String TYPE_LOGIN 	= "login";	//登录
	public static final String TYPE_ORDER 	= "order";	//下订单
	public static final String FLAG_INSTALL = "install";	//用户安装应用
	public static final String FLAG_OPEN = "open";			//用户打开应用
	private String appSource;		//应用来源 例如：91 mark
	private String osType;			//OS 类型 例如：iphone
	private String deviceid;		//设备号
	private String flag;			//open:打开(1)，install：安装(0)
	private String type;			//类型
	private Long orderid;			//订单id
	private String tradeno;			//订单号
	private Long partnerid;			//合作商id
	private Long memberid;			//用户ID
	private Timestamp addtime;		//时间
	private String citycode;		//城市编码
	private String osVersion;		//系统版本
	private String mobileType;		//手机型号
	private String apptype;			//应用类型
	private String appVersion;		//手机客户端版本
	private String orderOrigin;
	
	private String newdeviceid;//新设备号，保证手机的唯一性
	
	public AppSourceCount(String appSource){
		this.appSource = appSource;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public AppSourceCount(GewaOrder order){
		this.addtime = order.getAddtime();
		this.tradeno = order.getTradeNo();
		this.orderid = order.getId();
		this.memberid = order.getMemberid();
		this.partnerid = order.getPartnerid();
		this.citycode = order.getCitycode();
		this.type = AppSourceCount.TYPE_ORDER;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getApptype() {
		return apptype;
	}
	public void setApptype(String apptype) {
		this.apptype = apptype;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = StringUtils.lowerCase(osVersion);
	}
	public String getMobileType() {
		return mobileType;
	}
	public void setMobileType(String mobileType) {
		this.mobileType = mobileType;
	}
	public AppSourceCount(){
		
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		if("0".equals(flag)){
			this.flag = FLAG_INSTALL;
		}else if("1".equals(flag)){
			this.flag = FLAG_OPEN;
		}else{
			this.flag = flag;
		}
	}
	public String getAppSource() {
		return appSource;
	}
	public void setAppSource(String appSource) {
		this.appSource = appSource;
	}
	public String getOsType() {
		return osType;
	}
	public void setOsType(String osType) {
		this.osType = osType;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getDeviceid() {
		return deviceid;
	}
	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getNewdeviceid() {
		return newdeviceid;
	}
	public void setNewdeviceid(String newdeviceid) {
		this.newdeviceid = newdeviceid;
	}
	public String getOrderOrigin() {
		return orderOrigin;
	}
	public void setOrderOrigin(String orderOrigin) {
		this.orderOrigin = orderOrigin;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	
	
	
}
