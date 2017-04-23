package com.gewara.model.bbs.qa;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
public class GewaQaPoint extends BaseObject {
	private static final long serialVersionUID = 8512385224617282034L;
	public static String TAG_SENDQUESTION = "sendquestion";
	public static String TAG_BESTANSWER = "bestanswer";
	public static String TAG_REPLYQUESTION = "replyquestion";
	private Long id;
	private Long questionid;
	private Long answerid;
	private Long memberid;
	private Integer point;
	private Timestamp addtime;
	private String tag; 
	
	public GewaQaPoint(){
		
	}
	public GewaQaPoint(Long questionid, Long answerid, Long memberid, Integer point,String tag) {
		this.questionid = questionid;
		this.answerid = answerid;
		this.memberid = memberid;
		this.point = point;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.tag = tag;
	}
	public GewaQaPoint(Long questionid, Long memberid, Integer point,String tag) {
		this.questionid = questionid;
		this.memberid = memberid;
		this.point = point;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.tag = tag;
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

	public Long getAnswerid() {
		return answerid;
	}

	public void setAnswerid(Long answerid) {
		this.answerid = answerid;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Integer getPoint() {
		return point;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Long getQuestionid() {
		return questionid;
	}

	public void setQuestionid(Long questionid) {
		this.questionid = questionid;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}
