package com.gewara.json;

import java.io.Serializable;

import com.gewara.constant.Status;
import com.gewara.util.DateUtil;

public class RelateToSettle implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String id;
	
	private Long relatedid;
	
	private String tag;
	
	private String content;
	
	private String addtime;
	
	private Long userid;
	
	private String username;
	
	private String status;
	
	private String updatetime;
	
	public RelateToSettle(){ }
	
	public RelateToSettle(String tag, Long relatedid, Long userid, String content){
		String curtimeStr = DateUtil.formatTimestamp(System.currentTimeMillis());
		this.tag = tag;
		this.relatedid = relatedid;
		this.userid = userid;
		this.content = content;
		this.addtime = curtimeStr;
		this.status = Status.Y_NEW;
		this.updatetime = curtimeStr;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

	public Long getUserid() {
		return userid;
	}
	
	public String getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RelateToSettle))
			return false;
		final RelateToSettle temp = (RelateToSettle) o;
		return !(getId() != null ? !(getId().equals(temp.getId())) : (temp.getId() != null));
	}

	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}
