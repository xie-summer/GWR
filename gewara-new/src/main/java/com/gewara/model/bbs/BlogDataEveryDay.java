package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class BlogDataEveryDay extends BaseObject {
	private static final long serialVersionUID = 294544339329169064L;
	
	private Long id;
	private String tag;
	private Long relatedid;
	private String blogtype;
	private Date blogdate;
	private Integer blogcount;
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public BlogDataEveryDay(){}
	
	public BlogDataEveryDay(String tag, Long relatedid, String blogtype, Date blogdate){
		this.tag = tag;
		this.relatedid = relatedid;
		this.blogtype = blogtype;
		this.blogdate = DateUtil.getBeginningTimeOfDay(blogdate);
		this.blogcount = 0;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
	}
	
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

	public String getBlogtype() {
		return blogtype;
	}

	public void setBlogtype(String blogtype) {
		this.blogtype = blogtype;
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

	public Date getBlogdate() {
		return blogdate;
	}

	public void setBlogdate(Date blogdate) {
		this.blogdate = blogdate;
	}

	public Integer getBlogcount() {
		return blogcount;
	}

	public void setBlogcount(Integer blogcount) {
		this.blogcount = blogcount;
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
	
}
