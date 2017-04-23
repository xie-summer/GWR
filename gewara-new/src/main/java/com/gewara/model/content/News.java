package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class News extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	private Long id;
	private String tag;//类型标签
	private String title;
	private String secondtitle;
	private String summary; //摘要
	private String logo;
	private String smallLogo;
	private String tplLogo;
	private String relatedlink;
	private String content;
	private String newstype;
	private String flag;
	private Timestamp releasetime;
	private Timestamp updatetime;
	private Timestamp addtime;
	private Long relatedid;//关联对象
	private String category;
	private String linksource; //来源地
	private String countycode;
	private String newslabel;	//手动设置标签
	private String author;
	private Long categoryid;
	private String citycode;
	private Integer pagesize; 
	private Integer clickedtimes = 0;
	private String otherinfo;
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getNewslabel() {
		return newslabel;
	}

	public void setNewslabel(String newslabel) {
		this.newslabel = newslabel;
	}

	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
	
	public News() {}
	
	public News(String tag){
		this.pagesize = 1;
		this.updatetime = new Timestamp(System.currentTimeMillis());
		this.addtime = updatetime;
		this.tag = tag;
	}

	public Integer getPagesize() {
		return pagesize;
	}

	public void setPagesize(Integer pagesize) {
		this.pagesize = pagesize;
	}

	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRelatedlink() {
		return relatedlink;
	}

	public void setRelatedlink(String relatedlink) {
		this.relatedlink = relatedlink;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getSecondtitle() {
		return secondtitle;
	}

	public void setSecondtitle(String secondtitle) {
		this.secondtitle = secondtitle;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLogo() {
		if(StringUtils.isBlank(logo)) return "img/default_logo.png";
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getSmallLogo() {
		return smallLogo;
	}
	
	public String getSimg(){
		if(StringUtils.isBlank(smallLogo)) return "img/default_logo.png";
		return smallLogo;
	}

	public void setSmallLogo(String smallLogo) {
		this.smallLogo = smallLogo;
	}

	public String getNewstype() {
		return newstype;
	}

	public void setNewstype(String newstype) {
		this.newstype = newstype;
	}

	public Timestamp getReleasetime() {
		return releasetime;
	}

	public void setReleasetime(Timestamp releasetime) {
		this.releasetime = releasetime;
	}
	public String getLink(String basePath){
		if(StringUtils.isNotBlank(this.relatedlink)) return this.relatedlink;
		else return basePath + "news/" + this.id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getLinksource() {
		return linksource;
	}

	public void setLinksource(String linksource) {
		this.linksource = linksource;
	}
	public String getCname() {
		return this.title;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getTplLogo() {
		return tplLogo;
	}

	public void setTplLogo(String tplLogo) {
		this.tplLogo = tplLogo;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
}
