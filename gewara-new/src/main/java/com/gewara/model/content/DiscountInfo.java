package com.gewara.model.content;

import java.io.Serializable;
import java.util.Date;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Administrator
 *
 */
public class DiscountInfo extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	private Long id;
	private String content;
	private Date posttime;
	private Date validtime;
	private Long relatedid;
	private String title;
	private String tag;//¿‡–Õ±Í«©
	
	public DiscountInfo(){}
	
	public DiscountInfo(String content){
		this.content = content;
		this.posttime = new Date();
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public Date getPosttime() {
		return posttime;
	}

	public void setPosttime(Date posttime) {
		this.posttime = posttime;
	}

	public Date getValidtime() {
		return validtime;
	}
	public String getVaildtimeStr(){
		if(validtime==null) return "";
		return DateUtil.formatDate(validtime);
	}
	public void setValidtime(Date validtime) {
		this.validtime = validtime;
	}

	@Override
	public Serializable realId() {
		return id;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
