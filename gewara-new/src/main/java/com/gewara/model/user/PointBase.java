package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 *    @function Point表基类
 * 	@author bob.hu
 *		@date	2011-11-03 17:49:42
 */
public class PointBase extends BaseObject {
	private static final long serialVersionUID = 6556756647820158115L;
	protected Long id;
	protected Long memberid;
	protected String tag;
	protected Long tagid;
	protected Integer point;
	protected String reason;
	protected Long adminid;
	protected Timestamp addtime;
	protected String uniquetag;	// 唯一标识
	protected String statflag;	//
	public PointBase(){}
	
	public PointBase(Long memberid){
		this.memberid = memberid;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public PointBase(Long memberid, String tag){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
		this.tag = tag;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getTagid() {
		return tagid;
	}
	public void setTagid(Long tagid) {
		this.tagid = tagid;
	}
	public Integer getPoint() {
		return point;
	}
	public void setPoint(Integer point) {
		this.point = point;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Long getAdminid() {
		return adminid;
	}
	public void setAdminid(Long adminid) {
		this.adminid = adminid;
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
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getUniquetag() {
		return uniquetag;
	}

	public void setUniquetag(String uniquetag) {
		this.uniquetag = uniquetag;
	}

	public String getStatflag() {
		return statflag;
	}

	public void setStatflag(String statflag) {
		this.statflag = statflag;
	}
}
