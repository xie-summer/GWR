package com.gewara.json.mobile;

import java.io.Serializable;

public class SpShare implements Serializable{
	private static final long serialVersionUID = 6928455050291123282L;
	private String id;
	private Long spid;
	private String starttime;
	private String endtime;
	private String title;
	private String content;
	private String logo;
	private String link;
	private String shortlink;
	private String msgtype;
	private String addtime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getSpid() {
		return spid;
	}
	public void setSpid(Long spid) {
		this.spid = spid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getAddtime() {
		return addtime;
	}
	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getShortlink() {
		return shortlink;
	}
	public void setShortlink(String shortlink) {
		this.shortlink = shortlink;
	}
	public String getMsgtype() {
		return msgtype;
	}
	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
