package com.gewara.model.common;
import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)
 * @since 2007-12-24 ÏÂÎç08:49:56
 */
public class Subwayline extends BaseObject implements Serializable {
	private static final long serialVersionUID = -1449397931879372657L;
	private Long id;
	private String citycode;
	private String linename;
	private String remark;
	@Override
	public Serializable realId() {
		return id;
	}

	public Subwayline(String linename, String remark) {
		this.linename = linename;
		this.remark = remark;
	}

	public Subwayline() {
	}

	public String getLinename() {
		return linename;
	}

	public void setLinename(String linename) {
		this.linename = linename;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
}
