package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.BaseObject;

public class TempMovie extends BaseObject {
	public static final String STATUS_PASSED = "passed";
	public static final String STATUS_DELETED = "deleted";
	public static final String STATUS_NEW = "new";
	private static final long serialVersionUID = -4377112928212107248L;
	private Long id;
	private String moviename;
	private String state;
	private Timestamp addtime;
	private String content;
	private String type;
	private Long memberid;
	private String status;
	private Integer point;
	private String reason;
	private String actors;
	private String director;
	private String logo;
	private Date releaseDate;
	
	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public TempMovie() {}
	
	public TempMovie(Long memberid){
		this.point = 0;
		this.status = STATUS_NEW;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
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

	public String getMoviename() {
		return moviename;
	}

	public void setMoviename(String moviename) {
		this.moviename = moviename;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public boolean isNew(){
		return STATUS_NEW.equals(this.status);
	}
	public Integer getPoint() {
		return point;
	}
	public void setPoint(Integer point) {
		this.point = point;
	}
	public String getReason(){
		return this.reason;
	}
	public void setReason(String reason){
		this.reason = reason;
	}
}
