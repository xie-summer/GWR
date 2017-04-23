/**
 * 
 */
package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
public class MemberPicture extends BaseObject{
	private static final long serialVersionUID = -8293511979417836214L;

	private Long id;
	private String tag;
	private Long relatedid;
	private String picturename;
	private String description;
	private String name;
	private Timestamp addtime;
	private String status;
	private Long memberid;
	private String membername;
	private String flag;
	private BaseObject relate;
	public MemberPicture(){
		
	}
	public MemberPicture(String tag, Long relatedid, Long memberid, String membername, String picturename, String flag){
		this.tag = tag;
		this.status = "N";
		this.relatedid = relatedid;
		this.memberid = memberid;
		this.membername = membername;
		this.picturename = picturename;
		this.flag = flag;
		this.addtime=new Timestamp(System.currentTimeMillis());
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
	public String getPicturename() {
		return picturename;
	}
	public void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public BaseObject getRelate() {
		return relate;
	}
	public void setRelate(BaseObject relate) {
		this.relate = relate;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public String getLimg(){
		if(StringUtils.isBlank(picturename)) return "img/default_head.png";
		return picturename;
	}
}
