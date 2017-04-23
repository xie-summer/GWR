package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * 与外部商家合作登录账号
 * uk(loginname,source),uk(memberid)
 * @author acerge(acerge@163.com)
 * @since 2:24:19 PM Dec 12, 2010
 */
public class OpenMember extends BaseObject{
	private static final long serialVersionUID = 349587236L;

	private Long id;
	private Long memberid;			//绑定的用户
	private String loginname;		//登录名：Email、mobile等
	private String source;			//来源
	private String category;		//小类 比如支付宝：分为快捷登陆和钱包
	private String nickname;		//第三方用户的昵称
	private String otherinfo;		//其他数据
	private Long relateid;			//当前同步用户ID
	private Timestamp validtime;	//有效时间
	public OpenMember(){}
	public OpenMember(Long memberid, String source, String loginname,Long relateid) {
		this.memberid = memberid;
		this.source = source;
		this.loginname = loginname;
		this.relateid = relateid;
	}
	public OpenMember(String source,String loginname,Long relateid){
		this.source = source;
		this.loginname = loginname;
		this.relateid = relateid;
	}
	public Long getRelateid() {
		return relateid;
	}
	public void setRelateid(Long relateid) {
		this.relateid = relateid;
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
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
}
