package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class MovieVideo extends BaseObject {
	private static final long serialVersionUID = -6344392323406408488L;
	private Long id;
	private Long movieid;
	private String videoid;
	private String img;
	private Timestamp addtime;
	
	public MovieVideo() {}
	
	public MovieVideo(Long movieid, String videoid){
		this.movieid = movieid;
		this.videoid = videoid;
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

	public Long getMovieid() {
		return movieid;
	}

	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}

	public String getVideoid() {
		return videoid;
	}

	public void setVideoid(String videoid) {
		this.videoid = videoid;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}
}
