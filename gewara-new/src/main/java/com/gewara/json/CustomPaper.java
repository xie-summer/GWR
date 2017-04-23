package com.gewara.json;

import java.io.Serializable;
import java.util.Date;

import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;

public class CustomPaper implements Serializable{
	
	//自定义票纸内容 后台需统计
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8652184285519078973L;
	private String id;
	private String tradeno;
	private String tag;
	private Long memberid;
	private Date addtime;
	private String selfdomcontent;	//票纸内容
	
	public CustomPaper(){}
	public CustomPaper(String tradeno, String tag, Long memberid, String selfdomcontent){
		this.id = ObjectId.uuid();
		this.tradeno = tradeno;
		this.tag = tag;
		this.memberid = memberid;
		this.addtime = DateUtil.currentTime();
		this.selfdomcontent = selfdomcontent;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}
	public String getSelfdomcontent() {
		return selfdomcontent;
	}
	public void setSelfdomcontent(String selfdomcontent) {
		this.selfdomcontent = selfdomcontent;
	}
}
