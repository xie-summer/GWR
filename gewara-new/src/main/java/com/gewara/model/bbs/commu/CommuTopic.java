package com.gewara.model.bbs.commu;

import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * È¦×Ó»°Ìâ°å¿é
 * @author Administrator
 *
 */
public class CommuTopic extends BaseObject {
	private static final long serialVersionUID = -8076769136860243920L;
	private Long id;
	private Long commuid;
	private String topicname;
	private Integer ordernum;
	private Integer displaynum;
	@Override
	public Serializable realId() {
		return id;
	}
	
	public CommuTopic(){}
	
	public CommuTopic(Long commuid){
		this.ordernum = 0;
		this.displaynum = 1;
		this.commuid = commuid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCommuid() {
		return commuid;
	}

	public void setCommuid(Long commuid) {
		this.commuid = commuid;
	}

	public String getTopicname() {
		return topicname;
	}

	public void setTopicname(String topicname) {
		this.topicname = topicname;
	}

	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	public Integer getDisplaynum() {
		return displaynum;
	}

	public void setDisplaynum(Integer displaynum) {
		this.displaynum = displaynum;
	}
}
