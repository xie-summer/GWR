package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class LinkShare extends BaseObject {
	private static final long serialVersionUID = 2412910059855030192L;
	private Long id;
	private String url;
	private String tag;
	private Long tagid;
	private String type;
	private Long clicktimes;
	private Long memberid;
	private Timestamp addtime;
	private String category;
	private String status;
	private String content;
	private String picUrl;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public LinkShare() {
	}
	public LinkShare(Long memberid, String url,String tag,Long tagid,String type){
		this.tag=tag;
		this.tagid=tagid;
		this.type=type;
		this.addtime=new Timestamp(System.currentTimeMillis());
		this.clicktimes=0l;
		this.memberid = memberid;
		this.url = url;
		this.status = "Y";
	}
	public LinkShare(Long memberid,String tag,Long tagid,String type,String category){
		this.memberid = memberid;
		this.tag = tag;
		this.tagid = tagid;
		this.type = type;
		this.category = category;
		this.status = "N";
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Long getClicktimes() {
		return clicktimes;
	}
	public void setClicktimes(Long clicktimes) {
		this.clicktimes = clicktimes;
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
	@Override
	public Serializable realId() {
		return id;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getTagid() {
		return tagid;
	}
	public void setTagid(Long tagid) {
		this.tagid = tagid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
