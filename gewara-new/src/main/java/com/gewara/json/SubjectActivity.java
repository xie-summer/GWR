package com.gewara.json;

import java.io.Serializable;

import com.gewara.constant.Status;
import com.gewara.util.DateUtil;

public class SubjectActivity implements Serializable{

	private static final long serialVersionUID = -8054853068756272632L;
	private String id;
	
	private String link;
	private Long relatedid;
	private String addtime;
	private String status;
	private Integer ordernum;
	
	public SubjectActivity(){}
	
	public SubjectActivity(String link, Long relatedid){
		this.link = link;
		this.relatedid = relatedid;
		this.status = Status.Y_NEW;
		this.addtime = DateUtil.getCurTimeStr();
		this.ordernum = 0;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}
}
