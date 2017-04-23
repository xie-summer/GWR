package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 *  @function 针对用户提问,建议,bug, 进行回复
 * 	@author bob.hu
 *	@date	2011-03-11 12:02:52
 */
public class CustomerAnswer extends BaseObject {
	
	public static final int IS_ADMIN = 1; //是管理员
	public static final int NO_ADMIN = 0; //注册用户
	
	private Long id;
	private Long memberid;
	private String nickname;
	private Long questionid;
	private String body;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String citycode;
	private String status;
	private Integer isAdmin;
	
	private static final long serialVersionUID = 1862248185625951294L;

	public CustomerAnswer(){}
	
	public CustomerAnswer(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.status = Status.Y_NEW;
		this.memberid = memberid;
	}
	public CustomerAnswer(Long questionid, Long memberid, String body){
		this(memberid);
		this.questionid = questionid;
		this.body = body;
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

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Integer getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Integer isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Long getQuestionid() {
		return questionid;
	}

	public void setQuestionid(Long questionid) {
		this.questionid = questionid;
	}
	
}
