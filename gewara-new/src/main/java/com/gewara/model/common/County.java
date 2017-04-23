package com.gewara.model.common;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class County  extends BaseObject{
	//1=洲，2=国家，3=省，7=市，8=区县，10=商圈
	private static final long serialVersionUID = -886944461240705718L;
	private String countycode;
	private String briefname;
	private String citycode;
	private String countyname;
	
	public County() {
	}

	public String getCountycode() {
		return this.countycode;
	}

	public void setCountycode(String citycode) {
		this.countycode = citycode;
	}

	public String getCountyname() {
		return this.countyname;
	}

	public void setCountyname(String county) {
		this.countyname = county;
	}
	/**
	 * for manage perpose
	 * @return
	 */
	public String getId() {
		return countycode;
	}
	@Override
	public Serializable realId() {
		return countycode;
	}
	public String getCode(){
		return countycode;
	}
	
	public String getName(){
		return countyname;
	}

	public String getBriefname() {
		return briefname;
	}

	public void setBriefname(String briefname) {
		this.briefname = briefname;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
}
