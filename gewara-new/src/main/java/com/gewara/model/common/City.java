package com.gewara.model.common;
import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class City  extends BaseObject{
	private static final long serialVersionUID = -1449397931879372657L;
	private String citycode;
	private Province province;
	private String cityname;
	private String cityename;
	
	public City() {
	}

	public City(String citycode) {
		this.citycode = citycode;
	}

	public String getCitycode() {
		return this.citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Province getProvince() {
		return this.province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public String getCityname() {
		return this.cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getCityename() {
		return this.cityename;
	}

	public void setCityename(String cityename) {
		this.cityename = cityename;
	}

	@Override
	public Serializable realId() {
		return citycode;
	}

	/**
	 * for manage perpose
	 * @return
	 */
	public String getId() {
		return citycode;
	}
}
