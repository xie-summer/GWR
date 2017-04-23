package com.gewara.xmlbind.drama;

import java.sql.Timestamp;
import java.util.Date;

import com.gewara.xmlbind.BaseInnerResponse;


public class BusDrama extends BaseInnerResponse{
	
	private Long busid;
	private Long gewaid;
	private String language;
	private String dramaname;
	private String englishname;
	private String	directorid;
	private String director;
	private String directortext;
	private String	actorsid;
	private String actors;
	private String actorstext;
	private String content;
	private String logo;
	private String status;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String playwright;
	private Date releasedate;
	private Date enddate;
	private String troupecompany;
	private String citycode;
	private String type;//剧目类型
	private String dramatype;//话剧类型
	
	public Long getBusid() {
		return busid;
	}
	public void setBusid(Long busid) {
		this.busid = busid;
	}
	public Long getGewaid() {
		return gewaid;
	}
	public void setGewaid(Long gewaid) {
		this.gewaid = gewaid;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDramaname() {
		return dramaname;
	}
	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}
	public String getEnglishname() {
		return englishname;
	}
	public void setEnglishname(String englishname) {
		this.englishname = englishname;
	}
	public String getDirectorid() {
		return directorid;
	}
	public void setDirectorid(String directorid) {
		this.directorid = directorid;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getDirectortext() {
		return directortext;
	}
	public void setDirectortext(String directortext) {
		this.directortext = directortext;
	}
	public String getActorsid() {
		return actorsid;
	}
	public void setActorsid(String actorsid) {
		this.actorsid = actorsid;
	}
	public String getActors() {
		return actors;
	}
	public void setActors(String actors) {
		this.actors = actors;
	}
	public String getActorstext() {
		return actorstext;
	}
	public void setActorstext(String actorstext) {
		this.actorstext = actorstext;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public String getPlaywright() {
		return playwright;
	}
	public void setPlaywright(String playwright) {
		this.playwright = playwright;
	}
	public Date getReleasedate() {
		return releasedate;
	}
	public void setReleasedate(Date releasedate) {
		this.releasedate = releasedate;
	}
	public Date getEnddate() {
		return enddate;
	}
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	public String getTroupecompany() {
		return troupecompany;
	}
	public void setTroupecompany(String troupecompany) {
		this.troupecompany = troupecompany;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDramatype() {
		return dramatype;
	}
	public void setDramatype(String dramatype) {
		this.dramatype = dramatype;
	}
}
