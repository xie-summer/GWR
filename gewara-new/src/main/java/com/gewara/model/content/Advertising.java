package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Advertising extends BaseObject {
	private static final long serialVersionUID = -6121847644718307715L;
	public static final String STATUS_UP = "up"; // 投放
	public static final String STATUS_DOWN = "down"; // 下线
	public static final String STATUS_DELETED = "deleted"; // 删除
	private Long id;
	private Long adpositionid;
	private String link;
	private String title;
	private String ad;
	private String adtype;
	private String description;
	private Integer rang1;
	private Integer rang2;
	private Integer viewtimes;
	private Integer remaintimes;
	private Timestamp starttime;
	private Timestamp endtime;
	private Timestamp addtime;
	private String status;
	private String logicaldir;
	private boolean reassign;
	private String citycode;
	private String tag;
	private Long relatedid;
	private String track;
	
	private Integer ordernum;
	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public boolean isReassign() {
		return reassign;
	}

	public void setReassign(boolean reassign) {
		this.reassign = reassign;
	}

	public Advertising(){}
	
	public Advertising(String title) {
		this.title = title;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.status = STATUS_DOWN;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	public String getAdtype() {
		return adtype;
	}

	public void setAdtype(String adtype) {
		this.adtype = adtype;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getViewtimes() {
		return viewtimes;
	}

	public void setViewtimes(Integer viewtimes) {
		this.viewtimes = viewtimes;
	}

	public Integer getRemaintimes() {
		return remaintimes;
	}

	public void setRemaintimes(Integer remaintimes) {
		this.remaintimes = remaintimes;
	}

	public Integer getRang2() {
		return rang2;
	}

	public void setRang2(Integer rang2) {
		this.rang2 = rang2;
	}

	public Integer getRang1() {
		return rang1;
	}

	public void setRang1(Integer rang1) {
		this.rang1 = rang1;
	}

	public Timestamp getStarttime() {
		return starttime;
	}

	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}

	public Timestamp getEndtime() {
		return endtime;
	}

	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}

	public String getLogicaldir() {
		return logicaldir;
	}

	public void setLogicaldir(String logicaldir) {
		this.logicaldir = logicaldir;
	}

	public Long getAdpositionid() {
		return adpositionid;
	}

	public void setAdpositionid(Long adpositionid) {
		this.adpositionid = adpositionid;
	}
	
	public boolean getBooking(){
		if(starttime == null || endtime== null) return false;
		if(this.starttime.after(new Timestamp(System.currentTimeMillis())))return false;
		if(this.endtime.before(new Timestamp(System.currentTimeMillis())))return false;
		return true;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}
	
}
