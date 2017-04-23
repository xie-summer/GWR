package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class RelateToCity extends BaseObject {
	private static final long serialVersionUID = -5698319064398199925L;
	private Long id;
	private Long relatedid;
	private String citycode;
	private String tag;
	private String	flag;
	private Timestamp addtime;
	
	public RelateToCity() {	}

	public RelateToCity(Long relatedid, String citycode, String tag) {
		this.relatedid = relatedid;
		this.citycode = citycode;
		this.tag = tag;
		this.addtime = DateUtil.getCurFullTimestamp();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
