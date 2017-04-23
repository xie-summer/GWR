package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class AlbumComment extends BaseObject {
	private static final long serialVersionUID = 3778025254894844805L;
	private Long id;
	private Long albumid;
	private Long imageid;
	private Long memberid;
	private String body;
	private Timestamp addtime;
	
	public AlbumComment(){}
	
	public AlbumComment(Long memberid){
		this.memberid = memberid;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getAlbumid() {
		return albumid;
	}
	public void setAlbumid(Long albumid) {
		this.albumid = albumid;
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
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getImageid() {
		return imageid;
	}
	public void setImageid(Long imageid) {
		this.imageid = imageid;
	}
}
