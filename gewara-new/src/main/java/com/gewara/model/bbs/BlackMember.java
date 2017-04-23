package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
public class BlackMember extends BaseObject{
	private static final long serialVersionUID = -3188650815451237075L;
	private Long id;
	private Long memberId;//关联的用户
	private Long operatorId;
	private String description;
	private Timestamp addtime;
	
	public BlackMember(){}
	
	public BlackMember(Long memberId){
		this.memberId = memberId;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public BlackMember(Long memberId, String description, Long operatorId){
		this(memberId);
		this.description = description;
		this.operatorId = operatorId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public Long getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(Long operatorId) {
		this.operatorId = operatorId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
}
