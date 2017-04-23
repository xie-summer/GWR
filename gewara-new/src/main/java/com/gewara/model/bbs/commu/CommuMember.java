package com.gewara.model.bbs.commu;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class CommuMember extends BaseObject {
	// 圈子成员身份状态 : 创建者 / 管理员 / 普通成员 / 黑名单 
	public static final String FLAG_CREATOR = "y_creator";
	public static final String FLAG_ADMIN = "y_admin";	
	public static final String FLAG_NORMAL = "y_normal";
	public static final String FLAG_BLACK = "n_black";	
	
	private static final long serialVersionUID = -2870507813965913805L;
	private Long id;
	private Long memberid;
	private Long commuid;
	private Timestamp addtime;
	private String bbsmail;
	private String adminmail;
	private String flag;
	
	public CommuMember(){}
	
	public CommuMember(Long memberid) {
		this.bbsmail = "Y";
		this.adminmail = "Y";
		this.flag = CommuMember.FLAG_CREATOR;	// 默认为创建者
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
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

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Long getCommuid() {
		return commuid;
	}

	public void setCommuid(Long commuid) {
		this.commuid = commuid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getBbsmail() {
		return bbsmail;
	}

	public void setBbsmail(String bbsmail) {
		this.bbsmail = bbsmail;
	}

	public String getAdminmail() {
		return adminmail;
	}

	public void setAdminmail(String adminmail) {
		this.adminmail = adminmail;
	}
	public CommuMember(Long id, Long memberid, Long commuid,
			String bbsmail, String adminmail) {
		super();
		this.id = id;
		this.memberid = memberid;
		this.commuid = commuid;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.bbsmail = bbsmail;
		this.adminmail = adminmail;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}

}
