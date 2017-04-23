package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.TagConstant;
import com.gewara.model.BaseObject;

public class UserMessageAction extends BaseObject {
	private static final long serialVersionUID = 2743852504940627385L;
	
	private Long id;
	private Long usermessageid;
	private Long frommemberid;
	private Long tomemberid;
	private Timestamp addtime;
	private Integer isread;
	private Long groupid;
	private String status;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public UserMessageAction() {}
	
	public UserMessageAction(Long tomemberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.isread = TagConstant.READ_NO;
		this.status = "0";
		this.tomemberid = tomemberid;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Integer getIsread() {
		return isread;
	}

	public void setIsread(Integer isread) {
		this.isread = isread;
	}

	public Long getGroupid() {
		return groupid;
	}

	public void setGroupid(Long groupid) {
		this.groupid = groupid;
	}
	public Long getFrommemberid() {
		return frommemberid;
	}
	public void setFrommemberid(Long frommemberid) {
		this.frommemberid = frommemberid;
	}
	public Long getTomemberid() {
		return tomemberid;
	}
	public void setTomemberid(Long tomemberid) {
		this.tomemberid = tomemberid;
	}
	public Long getUsermessageid() {
		return usermessageid;
	}
	public void setUsermessageid(Long usermessageid) {
		this.usermessageid = usermessageid;
	}

}
