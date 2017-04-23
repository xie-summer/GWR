package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

public class ManageCheck implements Serializable{
	private static final long serialVersionUID = 6364440528724660295L;
	private String id;
	private String tag;
	private Long modifytime;
	private Long userId;
	private Date addtime;
	private Date unmodifytime;
	private String userName;
	
	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Date getUnmodifytime() {
		return unmodifytime;
	}

	public void setUnmodifytime(Date unmodifytime) {
		this.unmodifytime = unmodifytime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public ManageCheck() {}
	
	public ManageCheck(String tag, Long modifytime,Long userId,String userName,Date addtime,Date unmodifytime){
		this.tag = tag;
		this.modifytime = modifytime;
		this.addtime = addtime;
		this.userId = userId;
		this.unmodifytime = unmodifytime;
		this.userName = userName;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getModifytime() {
		return modifytime;
	}

	public void setModifytime(Long modifytime) {
		this.modifytime = modifytime;
	}
}
