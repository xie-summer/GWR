package com.gewara.json.mobile;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * WAP页福特试驾，参照：http://rd.gewara.com/attachments/download/3253/ford.jpg
 */
public class FordTestDrive implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6143982298828240530L;

	private String id;
	private String source;
	// 姓名
	private String driveName;
	// 手机
	private String mobileNo;
	// 所在城市
	private String cityname;
	// 邮编
	private String zipAddress;
	// 是否想获取更多福特嘉年华ST的信息
	private String isAllow;
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	private Long addTime;

	public Long getAddTime() {
		return addTime;
	}

	public void setAddTime(Long addTime) {
		this.addTime = addTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDriveName() {
		return driveName;
	}

	public void setDriveName(String driveName) {
		this.driveName = driveName;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getZipAddress() {
		return zipAddress;
	}

	public void setZipAddress(String zipAddress) {
		this.zipAddress = zipAddress;
	}

	public String getIsAllow() {
		return isAllow;
	}

	public void setIsAllow(String isAllow) {
		this.isAllow = isAllow;
	}

	public boolean checkNotBlankValue() {
		return StringUtils.isBlank(cityname) || StringUtils.isBlank(mobileNo) || StringUtils.isBlank(driveName) || StringUtils.isBlank(source);
	}

}
