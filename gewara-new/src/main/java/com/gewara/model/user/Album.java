package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.TagConstant;
import com.gewara.model.BaseObject;

public class Album extends BaseObject {
	private static final long serialVersionUID = -1457310202049618996L;
	private Long id;
	private Long memberid;
	private String subject;
	private String description;
	private String logo;
	private Long commuid;
	private String tag;
	private Long relatedid;
	private String smallcategory;
	private Long smallcategoryid;
	private Timestamp addtime;
	private Timestamp updatetime;
	private Long visitnum;
	private String rights;
	@Override
	public Serializable realId() {
		return id;
	}
	
	public Album(){
	}
	
	public Album(Long memberid){
		this.memberid = memberid;
		this.visitnum = 0L;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.rights = TagConstant.ALBUM_PUBLIC;
		this.commuid = 0l;
	}
	public String getAlbumLogoUrl() {
		if(StringUtils.isNotBlank(logo)) return logo;
		return "img/default_head.png";
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLogo() {
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Long getCommuid() {
		return commuid;
	}
	public void setCommuid(Long commuid) {
		this.commuid = commuid;
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
	public String getSmallcategory() {
		return smallcategory;
	}
	public void setSmallcategory(String smallcategory) {
		this.smallcategory = smallcategory;
	}
	public Long getSmallcategoryid() {
		return smallcategoryid;
	}
	public void setSmallcategoryid(Long smallcategoryid) {
		this.smallcategoryid = smallcategoryid;
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
	public Long getVisitnum() {
		return visitnum;
	}
	public void setVisitnum(Long visitnum) {
		this.visitnum = visitnum;
	}
	public String getRights() {
		return rights;
	}
	public void setRights(String rights) {
		this.rights = rights;
	}
}
