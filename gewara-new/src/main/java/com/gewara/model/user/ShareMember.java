package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * 
 * uk(loginname,source),uk(memberid,source)
 * @author weikai
 * @since 11:50:19 PM July 9, 2012
 */
public class ShareMember extends BaseObject{
	private static final long serialVersionUID = 2767376045284485907L;
	private Long id;
	private Long memberid;			//绑定的用户
	private String loginname;		//登录名：Email、mobile等
	private String source;			//来源
	private String otherinfo;		//其他数据
	private Timestamp addtime;		//绑定时间

	public ShareMember(){}
	public ShareMember(Long memberid, String source, String loginname) {
		this.memberid = memberid;
		this.source = source;
		this.loginname = loginname;
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
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public String getLoginname() {
		return loginname;
	}
	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
}
