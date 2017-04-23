package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class Agenda extends BaseObject {
	
	private static final long serialVersionUID = 0L;
	private Long id;
	private String title;
	private Long memberid;
	private String membername;
	private Date startdate;
	private String starttime;
	private String content;
	private String tag;
	private Long relatedid;
	private String category;
	private Long categoryid;
	private Timestamp addtime;
	private String action; //生活类型
	private Long actionid; //活动id
	
	private Date enddate;
	private String endtime;
	private String address;
	private String otherinfo;
	
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

	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Long getCategoryid() {
		return categoryid;
	}
	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
	}
	public Date getStartdate() {
		return startdate;
	}
	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getDtag(){
		if(this.categoryid != null) return category;
		return tag;
	}
	public Long getDrelatedid(){
		if(this.categoryid != null) return categoryid;
		return relatedid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAgendTime() {
		String result = DateUtil.formatDate(startdate);
		if(StringUtils.isNotBlank(starttime)) result += " " + starttime;
		return result;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public Date getEnddate() {
		return enddate;
	}
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Long getActionid() {
		return actionid;
	}
	public void setActionid(Long actionid) {
		this.actionid = actionid;
	}
	public Timestamp agendaTime(){
		String agendatime1 = DateUtil.formatDate(startdate);
		String agendatime2 = ""; 
		if(StringUtils.isNotBlank(starttime)){
			agendatime2 = starttime +":00"; 
		}else {
			agendatime2 = "00:00:00"; 
		}
		Timestamp agendatime = DateUtil.parseTimestamp(agendatime1 +" "+agendatime2);
		return agendatime;
	}
}
