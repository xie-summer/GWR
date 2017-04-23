package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.model.BaseObject;

public class SysMessageAction extends BaseObject{
	private static final long serialVersionUID = 4301402240270662228L;
	private Long id;
	private Long frommemberid;
	private Long tomemberid;
	private String body;
	private Long actionid;
	private String action;
	private String status;
	private Long isread;
	private Timestamp addtime;
	private BaseObject relate;
	private Member member;
	
	public SysMessageAction(){}
	
	public SysMessageAction(String action){
		this.isread=0l;
		this.status = SysAction.STATUS_APPLY;
 		this.addtime = new Timestamp(System.currentTimeMillis());
 		this.action = action;
	}
	public SysMessageAction(Long frommemberid, Long tomemberid,	String body, Long actionid, String action) {
		this(action);
		this.frommemberid = frommemberid;
		this.tomemberid = tomemberid;
		this.body = body;
		this.actionid = actionid;
	}
	
	/**
	 *	构造管理员发送生日祝福信息 
	 */
	public SysMessageAction(Long tomemberid) {
		super();
		this.frommemberid = TagConstant.ADMIN_FROMMEMBERID;
		this.tomemberid = tomemberid;
		this.actionid = tomemberid;
		this.action = SysAction.ACTION_FRIEND_BIRTHDAY;
		this.status = SysAction.STATUS_RESULT;
		this.isread = 0l;
 		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public BaseObject getRelate() {
		return relate;
	}
	public void setRelate(BaseObject relate) {
		this.relate = relate;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Long getActionid() {
		return actionid;
	}
	public void setActionid(Long actionid) {
		this.actionid = actionid;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public Long getIsread() {
		return isread;
	}
	public void setIsread(Long isread) {
		this.isread = isread;
	}
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
}
