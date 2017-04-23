package com.gewara.model.common;
import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class Subwaystation  extends BaseObject {
	private static final long serialVersionUID = -1449397931879372657L;
	private Long id;
	private String stationname;
	private String citycode;
	public Subwaystation(String stationname) {
		this.stationname = stationname;
	}

	public Subwaystation() {
	}
	@Override
	public Serializable realId() {
		return id;
	}

	/**
	 * for manage perpose
	 * @return
	 */
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStationname() {
		return stationname;
	}

	public void setStationname(String stationname) {
		this.stationname = stationname;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
}
