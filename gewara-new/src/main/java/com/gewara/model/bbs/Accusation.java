package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * @author acerge(acerge@163.com)
 * @since 11:38:26 AM Jan 19, 2010
 */
public class Accusation extends BaseObject {
	private static final long serialVersionUID = 4476980910614491968L;
	public static String STATUS_NEW = "new";
	public static String STATUS_PROCESSED_Y= "processed_y";
	public static String STATUS_PROCESSED_N = "processed_n";
	public static String TAG_DIARY = "diary";
	public static String TAG_DIARYCOMMENT = "diarycomment";
	public static String TAG_GEWAQUESTION = "gewaquestion";
	public static String TAG_GEWAANSWER = "gewaanswer";
	public static String TAG_ACTIVITY = "activity";
	public static String TAG_ACTIVITYCOMMENT = "activitycomment";
	public static String TAG_COMMENT = "comment";
	public static String TAG_USERMESSAGE="userMessage";
	private Long id;
	private Long memberid; // 举报人
	private String email;
	private String tag; // 类别：帖子、问答、活动、点评
	private String tag2;
	private Long relatedid; // 关联ID
	private Long relatedid2; 
	private String referer; // 链接地址
	private String body; // 内容
	private String message;
	private Long clerk; // 处理人
	private String status; // 状态：new processed
	private Timestamp addtime;
	private Timestamp updatetime; // 处理时间

	public Accusation(){}
	
	public Accusation(String tag) {
		this.tag = tag;
		this.status = STATUS_NEW;
		this.addtime = new Timestamp(System.currentTimeMillis());
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
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

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getClerk() {
		return clerk;
	}

	public void setClerk(Long clerk) {
		this.clerk = clerk;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTag2() {
		return tag2;
	}

	public void setTag2(String tag2) {
		this.tag2 = tag2;
	}

	public Long getRelatedid2() {
		return relatedid2;
	}

	public void setRelatedid2(Long relatedid2) {
		this.relatedid2 = relatedid2;
	}
}
