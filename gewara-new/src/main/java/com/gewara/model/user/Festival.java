package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.BaseObject;

public class Festival extends BaseObject {
	private static final long serialVersionUID = -7594434508072085867L;
	private Long id;
	private String festname;
	private Date festdate;
	private Long drawid;
	private Timestamp addtime;
	private String summary;
	private String logo;
	private String link;
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Festival(){}
	
	public Festival(String festName) {
		this.festname = festName;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	
	public Festival(String festName,Date festDate,Long drawid,String summary){
		this(festName);
		this.festdate = festDate;
		this.drawid = drawid;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.summary = summary;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFestname() {
		return festname;
	}

	public void setFestname(String festname) {
		this.festname = festname;
	}

	public Date getFestdate() {
		return festdate;
	}

	public void setFestdate(Date festdate) {
		this.festdate = festdate;
	}

	public Long getDrawid() {
		return drawid;
	}

	public void setDrawid(Long drawid) {
		this.drawid = drawid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	@Override
	public Serializable realId() {
		return id;
	}
}
