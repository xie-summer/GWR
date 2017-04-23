package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 * 临时账号
 * @author gebiao(ge.biao@gewara.com)
 * @since Dec 2, 2013 4:22:31 PM
 */
public class TempMember extends BaseObject{
	private static final long serialVersionUID = -7348020080962170246L;
	public static final String MEMBERTYPE_NEW = "new";
	public static final String MEMBERTYPE_BIND = "bind";
	public static final String MEMBERTYPE_UNBIND = "unbind";
	
	private Long id;
	private String mobile;		//
	private String tmppwd;		//
	private Long memberid;		//如果memberid不为空，则是临时绑定手机号
	private String flag;		//标识：如某活动标识
	private String status;
	private String membertype;	//用户类型：new 新用户,bind 老用户已绑定,unbind 老用用户未绑定
	private String otherinfo;	//其他令牌
	private String ip;
	private Timestamp addtime;
	public TempMember(){}
	public TempMember(String mobile, String password, String flag, String ip){
		this.status = Status.N;
		this.mobile = mobile;
		this.tmppwd = password;
		this.flag = flag;
		this.ip = ip;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.otherinfo = "{}";
	}
	
	public TempMember(String mobile, Long memberid, String flag, String ip){
		this.status = Status.N;
		this.memberid = memberid;
		this.mobile = mobile;
		this.flag = flag;
		this.ip = ip;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.otherinfo = "{}";
	}
	
	@Override
	public Serializable realId() {
		return id;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public String getTmppwd() {
		return tmppwd;
	}

	public void setTmppwd(String tmppwd) {
		this.tmppwd = tmppwd;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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
	public String getMembertype() {
		return membertype;
	}
	public void setMembertype(String membertype) {
		this.membertype = membertype;
	}
	
}
