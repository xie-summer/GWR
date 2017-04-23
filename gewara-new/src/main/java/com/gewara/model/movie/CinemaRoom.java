package com.gewara.model.movie;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.ticket.PlayRoom;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class CinemaRoom extends PlayRoom implements Comparable<CinemaRoom>{
	private static final long serialVersionUID = -1894201195221566397L;
	private Long cinemaid;
	private Integer screenheight; 		//银幕高
	private Integer screenwidth; 		//银幕宽
	private Integer allowsellnum;		//允许卖出数
	private String vipflag;				//是否为VIP
	private String seatmap;				//座位图（位图）
	private String loveflag;			//是否有情侣座
	private Date effectivedate;			//座位生效日期
	private String playtype;			//播放类型：放映3D、2D、IMAX
	private String roomDoor;         //影厅的门
	private String otherinfo;
	private String characteristic;	//特色厅类型
	private String defaultEdition; //默认版本，多个英文逗号,分割开 

	public CinemaRoom(){}
	
	public CinemaRoom(Long cinemaId, String roomtype){
		this.cinemaid = cinemaId;
		this.roomtype = roomtype;
		this.num = "0";
		this.linenum = 0;
		this.ranknum = 0;
		this.seatnum = 0;
		this.vipflag = "N";
		this.loveflag = "Y";
		this.allowsellnum = 9999;
	}
	
	public String getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(String characteristic) {
		this.characteristic = characteristic;
	}
	
	public Integer getScreenheight() {
		return screenheight;
	}

	public void setScreenheight(Integer screenheight) {
		this.screenheight = screenheight;
	}

	public Integer getScreenwidth() {
		return screenwidth;
	}

	public void setScreenwidth(Integer screenwidth) {
		this.screenwidth = screenwidth;
	}

	public Long getCinemaid() {
		return cinemaid;
	}
	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
	public String getVipflag() {
		return vipflag;
	}
	public void setVipflag(String vipflag) {
		this.vipflag = vipflag;
	}
	public Date getEffectivedate() {
		return effectivedate;
	}
	public void setEffectivedate(Date effectivedate) {
		this.effectivedate = effectivedate;
	}
	@Override
	public int compareTo(CinemaRoom o) {
		return StringUtils.leftPad(""+num, 3, '0').compareTo(StringUtils.leftPad(""+o.num, 3, '0'));
	}
	public String getSeatmap() {
		return seatmap;
	}
	public void setSeatmap(String seatmap) {
		this.seatmap = seatmap;
	}
	public String getLoveflag() {
		return loveflag;
	}
	public void setLoveflag(String loveflag) {
		this.loveflag = loveflag;
	}

	public Integer getAllowsellnum() {
		return allowsellnum;
	}

	public void setAllowsellnum(Integer allowsellnum) {
		this.allowsellnum = allowsellnum;
	}

	public String getPlaytype() {
		return playtype;
	}

	public void setPlaytype(String playtype) {
		this.playtype = playtype;
	}
	
	public boolean hasGewaRoom(){
		return StringUtils.equals(roomtype, OpiConstant.OPEN_GEWARA);
	}
	
	public boolean hasRemoteRoom(){
		return !hasGewaRoom();
	}

	public String getRoomDoor() {
		return roomDoor;
	}

	public void setRoomDoor(String roomDoor) {
		this.roomDoor = roomDoor;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public String getDefaultEdition() {
		return defaultEdition;
	}

	public void setDefaultEdition(String defaultEdition) {
		this.defaultEdition = defaultEdition;
	}

}
