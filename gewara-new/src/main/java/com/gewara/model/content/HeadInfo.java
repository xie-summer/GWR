package com.gewara.model.content;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class HeadInfo extends BaseObject {
	private static final long serialVersionUID = -3955645337525890794L;
	public static final String TAG = "headinfo";
	private Long id;
	private String logobig;
	private String logosmall;
	private String title;
	private String css;
	private String link;
	private Long ordernum;
	private String isslide;
	private Timestamp addtime;
	private String board;	// °æ¿é eg. movie/suject...etc.
	private String citycode;
	private String track;		//¸ú×ÙÍ¼Æ¬
	
	
	public HeadInfo() {}
	
	public HeadInfo(String title) {
		this.ordernum=0l;
		this.addtime=new Timestamp(System.currentTimeMillis());
		this.isslide="Y";
		this.title = title;
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

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public Long getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Long ordernum) {
		this.ordernum = ordernum;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLogobig() {
		return logobig;
	}

	public void setLogobig(String logobig) {
		this.logobig = logobig;
	}

	public String getLogosmall() {
		return logosmall;
	}

	public void setLogosmall(String logosmall) {
		this.logosmall = logosmall;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIsslide() {
		return isslide;
	}

	public void setIsslide(String isslide) {
		this.isslide = isslide;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}
}
