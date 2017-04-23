package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class SurveyResult extends BaseObject {
	private static final long serialVersionUID = -4102497864896980635L;
	private Long id;
	private Long surveyid;		//问卷id，为survey表中的recordid
	private Integer itemid;		//问题id，为survey_item表中的itemid
	private Integer optionid;	//选项id，为survey_option表中的optionid
	private Long memberid;		//参与问卷调查的用户id
	private Timestamp addtime;	//创建时间
	private String flag;			//选项选定状态，'Y'为选定，'N'为未选定
	private String mark;			//选项追加文本
	private String otherinfo;	//其他信息
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
}
