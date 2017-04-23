package com.gewara.model.bbs.qa;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
public class GewaAnswer extends BaseObject {
	private static final long serialVersionUID = 5578727148166770087L;
	public static final Integer SEND_QUESTION_POINT = 10; //发布问题增加经验值
	public static final String AS_STATUS_N = "N";
	public static final String AS_STATUS_Y = "Y";
	private Long id;
	private Long questionid; //对应的问题
	private Long memberid;
	private String content; // 内容
	private Integer hotvalue;
	private String answerstatus; // 是否是问题的答案 不是:N 是：Y
	private String status;// 状态
	private Timestamp addtime;
	private String ip; //用户IP
	
	public GewaAnswer(){}
	
	public GewaAnswer(String content){
		this.content = content;
		this.hotvalue = 0;
		this.answerstatus = AS_STATUS_N;
		this.status = Status.Y_NEW;
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

	public Long getQuestionid() {
		return questionid;
	}

	public void setQuestionid(Long questionid) {
		this.questionid = questionid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getHotvalue() {
		return hotvalue;
	}

	public void setHotvalue(Integer hotvalue) {
		this.hotvalue = hotvalue;
	}

	public String getAnswerstatus() {
		return answerstatus;
	}

	public void setAnswerstatus(String answerstatus) {
		this.answerstatus = answerstatus;
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
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getIp(){
		return ip;
	}
	public void setIp(String ip){
		this.ip = ip;
	}
}
