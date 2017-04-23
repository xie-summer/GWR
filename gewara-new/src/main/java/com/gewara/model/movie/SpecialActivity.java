package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class SpecialActivity extends BaseObject{
	private static final long serialVersionUID = -1894201195221566397L;
	private Long id;
	private String activityname;
	private String theme;
	private String content;
	private Date startdate;
	private Date enddate;
	private String cinemas;
	private String movies;
	private String tag;
	private String website;
	private String flag;
	private String relatedid;
	
	/**
	 *  <!-- 20100915 hubo add -->
	 * */
	private Timestamp addtime;
	private String seokeywords;
	private String seodescription;
	private Long headpic;
	private String walatitle;
	private String acttitle;
	private String surveytitle;
	private String answertitle;
	private String blogtitle;
	private Long blogpic;
	private String teampictitle;
	private Long logo;
	private String status;

	public SpecialActivity() {}
	
	public SpecialActivity(String tag) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.tag = tag;
		this.status = Status.N;
	}

	@Override
	public Serializable realId() {
		return id;
	}
	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getTheme() {
		return theme;
	}

	public String getContent() {
		return content;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getSeokeywords() {
		return seokeywords;
	}

	public void setSeokeywords(String seokeywords) {
		this.seokeywords = seokeywords;
	}

	public String getSeodescription() {
		return seodescription;
	}

	public void setSeodescription(String seodescription) {
		this.seodescription = seodescription;
	}

	public Long getHeadpic() {
		return headpic;
	}

	public void setHeadpic(Long headpic) {
		this.headpic = headpic;
	}

	public String getWalatitle() {
		return walatitle;
	}

	public void setWalatitle(String walatitle) {
		this.walatitle = walatitle;
	}

	public String getActtitle() {
		return acttitle;
	}

	public void setActtitle(String acttitle) {
		this.acttitle = acttitle;
	}

	public String getSurveytitle() {
		return surveytitle;
	}

	public void setSurveytitle(String surveytitle) {
		this.surveytitle = surveytitle;
	}

	public String getAnswertitle() {
		return answertitle;
	}

	public void setAnswertitle(String answertitle) {
		this.answertitle = answertitle;
	}

	public String getBlogtitle() {
		return blogtitle;
	}

	public void setBlogtitle(String blogtitle) {
		this.blogtitle = blogtitle;
	}

	public Long getBlogpic() {
		return blogpic;
	}

	public void setBlogpic(Long blogpic) {
		this.blogpic = blogpic;
	}

	public String getTeampictitle() {
		return teampictitle;
	}

	public void setTeampictitle(String teampictitle) {
		this.teampictitle = teampictitle;
	}

	public Date getStartdate() {
		return startdate;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCinemas() {
		return cinemas;
	}

	public void setCinemas(String cinemas) {
		this.cinemas = cinemas;
	}

	public String getActivityname() {
		return activityname;
	}

	public void setActivityname(String activityname) {
		this.activityname = activityname;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getMovies() {
		return movies;
	}

	public void setMovies(String movies) {
		this.movies = movies;
	}

	public Long getLogo() {
		return logo;
	}
	public void setLogo(Long logo) {
		this.logo = logo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(String relatedid) {
		this.relatedid = relatedid;
	}
}
