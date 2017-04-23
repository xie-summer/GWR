package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-10-9上午08:57:47
 */
public class Picture extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	private Long id;
	private String tag;//类型标签
	private Long relatedid;//相关Id
	private String picturename;//图片名称
	private String description;//描述
	private String category;		
	private Long categoryid;	
	
	private String name;
	private Timestamp posttime;//发布时间
	private Long memberid;//专区用户名
	private String memberType;
	
	private Integer clickedtimes;
	
	@Override
	public Serializable realId() {
		return id;
	}
	public Integer getClickedtimes() {
		return clickedtimes;
	}
	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}
	public Long getMemberid() {
	return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public Picture() {}
	
	public Picture(String tag) {
		this.tag = tag;
		this.posttime=new Timestamp(System.currentTimeMillis());
		this.clickedtimes = 0;
	}
	public Picture(String tag, Long relatedid){
		this(tag);
		this.relatedid = relatedid;
	}
	public Picture(String tag, Long relatedid, String picturename, String description){
		this(tag, relatedid);
		this.picturename = picturename;
		this.description = description;
		if(StringUtils.isNotBlank(description)){
			this.name = description.substring(0,description.length()>50?50:description.length());
		}
	}
	public Picture(String tag, Long relatedid, Long memberid, String picturename){
		this(tag, relatedid);
		this.tag = tag;
		this.relatedid = relatedid;
		this.memberid = memberid;
		this.picturename = picturename;
		this.posttime=new Timestamp(System.currentTimeMillis());
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getPicturename() {
		return picturename;
	}

	public void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public String getLogo(){
		return this.picturename;
	}
	public String getLimg() {
		return picturename;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Timestamp getPosttime() {
		return posttime;
	}
	public void setPosttime(Timestamp posttime) {
		this.posttime = posttime;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Long getCategoryid() {
		return categoryid;
	}
	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
	}
	public String getMemberType() {
		return memberType;
	}
	public void setMemberType(String memberType) {
		this.memberType = memberType;
	}
	
	public boolean hasMemberType(String type){
		return StringUtils.equals(this.memberType, type);
	}
}
