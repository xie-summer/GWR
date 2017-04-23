package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 * 记录用户的评分
 * 
 * @author acerge(acerge@163.com)
 * @since Jun 4, 2009 4:23:55 PM
 */
public class MemberMark extends BaseObject {
	private static final long serialVersionUID = 5366927542839966178L;
	private Long id;
	private String tag;
	private Long relatedid;
	private String markname;
	private Integer markvalue;
	private Timestamp addtime;
	private Long memberid;
	private String nickname;
	private String flag = Status.N;
	public static Map<String, String[]> markMap = new HashMap<String, String[]>();

	public MemberMark() {
	}

	public MemberMark(String tag) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.tag = tag;
	}
	public MemberMark(String tag, Long relatedid, String markname, Integer markvalue,Long memberid, String nickname){
		this(tag);
		this.relatedid = relatedid;
		this.markname = markname;
		this.markvalue = markvalue;
		this.memberid = memberid;
		this.nickname = nickname;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getMarkname() {
		return markname;
	}

	public void setMarkname(String markname) {
		this.markname = markname;
	}

	public Integer getMarkvalue() {
		return markvalue;
	}

	public void setMarkvalue(Integer markvalue) {
		this.markvalue = markvalue;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
}
