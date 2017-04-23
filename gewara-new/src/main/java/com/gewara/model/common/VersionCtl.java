package com.gewara.model.common;

import java.io.Serializable;

import com.gewara.model.BaseObject;
import com.gewara.util.StringUtil;

public class VersionCtl extends BaseObject{
	private static final long serialVersionUID = 38475683L;
	private String id;
	private Integer version;
	private String ctldata;
	public VersionCtl(){}
	public VersionCtl(String id) {
		this.id = id;
		this.ctldata = StringUtil.getRandomString(20);
		this.version = 0;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getCtldata() {
		return ctldata;
	}

	public void setCtldata(String ctldata) {
		this.ctldata = ctldata;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
