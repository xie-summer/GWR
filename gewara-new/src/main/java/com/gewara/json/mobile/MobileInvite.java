package com.gewara.json.mobile;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class MobileInvite extends BaseObject {

	private static final long serialVersionUID = -1122172971222775219L;
	public static final String UNKNOW_ACT="unknow_act";
	private String id;
	private Long memberid;// 邀请人id
	private String mobile;// 邀请手机号
	private String addtime;// 邀请时间
	private Long registerid;// 成功注册会员id
	private String relatedid;// 邀请活动id
	private String apptype;// 邀请人产品类型
	private String ostype;// 邀请人系统类型

	public MobileInvite() {
	}

	public MobileInvite(Long memberid) {
		this.memberid = memberid;
	}

	public MobileInvite(Long memberid, String mobile) {
		this(memberid);
		this.mobile = mobile;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

	@Override
	public Serializable realId() {
		return id;
	}

	public String getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(String relatedid) {
		this.relatedid = relatedid;
	}

	public String getApptype() {
		return apptype;
	}

	public void setApptype(String apptype) {
		this.apptype = apptype;
	}

	public String getOstype() {
		return ostype;
	}

	public void setOstype(String ostype) {
		this.ostype = ostype;
	}

	public Long getRegisterid() {
		return registerid;
	}

	public void setRegisterid(Long registerid) {
		this.registerid = registerid;
	}

}
