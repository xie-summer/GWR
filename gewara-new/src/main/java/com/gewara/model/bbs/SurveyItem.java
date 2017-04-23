package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class SurveyItem extends BaseObject {
	private static final long serialVersionUID = 7949823691095330472L;
	
	public static final String SINGLETYPE="single"; 
	public static final String MULTIPLETYPE="multiple"; 
	public static final String TEXTTYPE="text"; 
	private Long id;
	private Long surveyid;			//所属问卷，为survey表中的recordid
	private Integer itemid;			//问卷题目序号，可作为排序标准
	private String status;			//题目状态，是否被屏蔽，可选'N','Y'
	private String itemtype;		//题目类型，'single'为单选,'multiple'为多选,'text'为文本，单选和多选亦可追加文本
	private Timestamp addtime;		//创建时间
	private Timestamp updatetime;	//修改时间
	private String body;				//题目内容

	public SurveyItem() {}
	
	public SurveyItem(Long surveyid, String body) {
		this.surveyid = surveyid;
		this.itemid = 1;
		this.itemtype = "text";
		this.status = "Y";
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = DateUtil.getCurFullTimestamp();
		this.body = body;
	}
	public SurveyItem(Long surveyid, String body, String itemtype) {
		this.surveyid = surveyid;
		this.itemid = 1;
		this.itemtype = itemtype;
		this.status = "Y";
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = DateUtil.getCurFullTimestamp();
		this.body = body;
	}
	@Override
	public Serializable realId() {
		return id;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getItemtype() {
		return itemtype;
	}

	public void setItemtype(String itemtype) {
		this.itemtype = itemtype;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
