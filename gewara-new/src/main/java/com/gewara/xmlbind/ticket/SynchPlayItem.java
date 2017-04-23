package com.gewara.xmlbind.ticket;

import java.sql.Timestamp;
import java.util.Date;

import com.gewara.util.DateUtil;

public class SynchPlayItem {
	public static final String STATUS_DEL = "N";
	public static final String STATUS_NEW = "Y";
	private String mpiseq;			//排片序号：partner+pseqno
	private Long movieid;			//影院ID
	private Long cinemaid;			//影院名称
	private String roomnum;			//影厅序号
	private String roomname;		//影厅名称
	private Timestamp playtime;		//放映时间			
	private Integer price;			//影院价格			
	private Integer lowest;			//最低票价
	private String language;		//语言
	private String edition;			//版本
	
	private String partner;			//第三方
	private String pseqno;			//第三方ID
	
	private String otherinfo;		//其他信息
	private String status;			//状态：可用、删除
	private Timestamp createtime;	//创建时间
	private Timestamp updatetime;	//更新时间
	public SynchPlayItem(){}

	public Integer getPrice() {
		return this.price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Long getMovieid() {
		return movieid;
	}

	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}

	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}

	public Integer getLowest() {
		return lowest;
	}
	public void setLowest(Integer lowest) {
		this.lowest = lowest;
	}
	public Timestamp getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}
	public String getRoomname() {
		return roomname;
	}
	public void setRoomname(String roomname) {
		this.roomname = roomname;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public String getPseqno() {
		return pseqno;
	}
	public void setPseqno(String pseqno) {
		this.pseqno = pseqno;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getRoomnum() {
		return roomnum;
	}

	public void setRoomnum(String roomnum) {
		this.roomnum = roomnum;
	}
	public Timestamp getPlaytime() {
		return playtime;
	}
	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}
	public String getMpiseq() {
		return mpiseq;
	}
	public void setMpiseq(String mpiseq) {
		this.mpiseq = mpiseq;
	}

	public Date gainPlaydate() {
		return DateUtil.getBeginningTimeOfDay(new Date(playtime.getTime()));
	}

	public String gainPlaytime() {
		return DateUtil.format(playtime, "HH:mm");
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
}
