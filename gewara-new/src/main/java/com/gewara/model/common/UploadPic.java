package com.gewara.model.common;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-10-9ÉÏÎç08:57:47
 */
public class UploadPic extends BaseObject{
	public static final String STATUS_UNSAVE = "unsave";
	public static final String STATUS_SAVED = "saved";
	public static final String STATUS_CHECKED = "checked";
	public static final String STATUS_DEL = "del";
	private static final long serialVersionUID = 4914995483381697551L;
	private String picname;
	private Long modifytime;
	private Long memberid;
	private Integer picsize;
	private String status;
	private String tag;
	private Long relatedid;
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
	public UploadPic(){}
	public UploadPic(String picname, Long modifytime, Integer picsize) {
		this.picname = picname;
		this.modifytime = modifytime;
		this.picsize = picsize;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	@Override
	public Serializable realId() {
		return picname;
	}
	public Long getModifytime() {
		return modifytime;
	}
	public void setModifytime(Long modifytime) {
		this.modifytime = modifytime;
	}
	public String getPicname() {
		return picname;
	}
	public void setPicname(String picname) {
		this.picname = picname;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getPicsize() {
		return picsize;
	}
	public void setPicsize(Integer picsize) {
		this.picsize = picsize;
	}
}
