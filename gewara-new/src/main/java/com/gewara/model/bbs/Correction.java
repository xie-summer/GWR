package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Correction extends BaseObject{
	/**
	 * 纠错
	 */
	private static final long serialVersionUID = -4836156521889941630L;
	public static final String CHECKED="1";//审核通过
	public static final String CHECKING="0";//等待审核
	public static final String ALLCHECK="all";//全部
	private Long id;
	private String tag;
	private Long relatedid;
	private Long memberid;
	private String email;
	private String content;
	private String check;
	private String referer;
	private Timestamp addtime;
	private String otherinfo;
	
	public Correction(){}
	
	public Correction(String content){
		this.content = content;
		this.check = "0";
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCheck() {
		return check;
	}
	public void setCheck(String check) {
		this.check = check;
	}
	public String getReferer() {
		return referer;
	}
	public void setReferer(String referer) {
		this.referer = referer;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getOtherinfo(){
		return this.otherinfo;
	}
	public void setOtherinfo(String otherinfo){
		this.otherinfo = otherinfo;
	}
	
}
