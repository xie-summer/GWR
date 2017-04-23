package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class SurveyOption extends BaseObject {
	private static final long serialVersionUID = -3781931902226905354L;
	private Long id;
	private Long surveyid;
	private Integer itemid;
	private Integer optionid;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String body;
	private String optiontype;
	private String status;

	@Override
	public Serializable realId() {
		return id;
	}
	
	public SurveyOption() {}
	
	public SurveyOption(Long surveyid, Integer itemid, String body,String optionType) {
		this.surveyid = surveyid;
		this.itemid = itemid;
		this.optionid = 1;
		if(optionType.equals(Status.N)) this.optionid = 2;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = DateUtil.getCurFullTimestamp();
		this.body = body;
		this.optiontype = optionType;
		this.status = Status.Y;
	}
	
	public Long getSurveyid() {
		return surveyid;
	}

	public void setSurveyid(Long surveyid) {
		this.surveyid = surveyid;
	}

	public Integer getItemid() {
		return itemid;
	}

	public void setItemid(Integer itemid) {
		this.itemid = itemid;
	}

	public Integer getOptionid() {
		return optionid;
	}

	public void setOptionid(Integer optionid) {
		this.optionid = optionid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getOptiontype() {
		return optiontype;
	}

	public void setOptiontype(String optiontype) {
		this.optiontype = optiontype;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
