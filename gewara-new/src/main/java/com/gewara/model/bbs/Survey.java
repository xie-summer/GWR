package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class Survey extends BaseObject {
	private static final long serialVersionUID = -5417707904306940796L;
	private Long id;
	private String title;			//标题
	private Long relatedid;			//相关对象id
	private String tag;				//与relatedid同，确定类别
	private String category;		//一级分类
	private String subcategory;		//二级分类
	private Timestamp addtime;		//创建时间
	private Timestamp updatetime;	//更新时间
	private Timestamp validtime;	//有效时间
	private Long viewcount;			//访问次数
	private String showUrl;			//相关页面
	private String citycode;		//城市
	private String body;				//备注，或者说明
	public Survey(){}
	public Survey(String title) {
		this.title = title;
		this.tag = "other";
		this.category = title;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = DateUtil.getCurFullTimestamp();
		this.validtime = DateUtil.addDay(updatetime, 365);
		this.citycode = "310000";
		this.viewcount = 0L;
		this.body = title;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void setSubcategory(String subcategory) {
		this.subcategory = subcategory;
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

	public Timestamp getValidtime() {
		return validtime;
	}

	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}

	public Long getViewcount() {
		return viewcount;
	}

	public void setViewcount(Long viewcount) {
		this.viewcount = viewcount;
	}

	public String getShowUrl() {
		return showUrl;
	}

	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
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
