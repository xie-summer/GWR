package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

/**
 * 运动终端机升级配置
 * 
 * @author liushusong
 * 
 */

public class SportUpGrade implements Serializable {
	private static final long serialVersionUID = 5334779739394551134L;
	/**智能终端系统**/
	public static final String SPORT_APP_ITS = "SPORT_ITS";
	/**羽毛球在线预订系统**/
	public static final String SPORT_APP_BOOKING = "SPORT_BOOKING";
	/**影院一体机**/
	public static final String CINEMA_SMARTGETTICKETS = "CINEMA_SMARTGETTICKETS";
	
	private String id;
	private String versionCode;
	private String upgradeUrl;
	private Date addTime;
	private String remark;
	private String apptype;//应用类型 
	private String nickName;

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public SportUpGrade() {
	}

	public SportUpGrade(String versionCode, String upgradeUrl) {
		this.versionCode = versionCode;
		this.upgradeUrl = upgradeUrl;
		this.addTime = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getUpgradeUrl() {
		return upgradeUrl;
	}

	public void setUpgradeUrl(String upgradeUrl) {
		this.upgradeUrl = upgradeUrl;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getApptype() {
		return apptype;
	}

	public void setApptype(String apptype) {
		this.apptype = apptype;
	}
	
	
	
}
