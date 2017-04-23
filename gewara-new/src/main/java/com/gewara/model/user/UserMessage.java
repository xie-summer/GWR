package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class UserMessage extends BaseObject {
	private static final long serialVersionUID = 5538904907942762002L;
	
	private Long id;
	private String subject;
	private String content;
	private Timestamp addtime;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public UserMessage() {}
	
	public UserMessage(String subject){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.subject = subject;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getRcontent(String basePath){
		if(StringUtils.isNotBlank(this.content)) {
			return this.content.replaceAll("\\[([^\\]]*)\\]", "<img src=" + basePath + "img/minFace/$1\\.gif />");
		}
		return "";
	}
	public String getCutContent(){
		if(StringUtils.isNotBlank(this.content)) {
			return StringUtils.substring(this.content.replaceAll("\\[([^\\]]*)\\]", ""), 0, 200);
		}
		return "";
	}
}
