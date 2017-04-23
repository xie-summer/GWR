package com.gewara.model.drama;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class TheatreRoomSeat  extends BaseObject implements Comparable<TheatreRoomSeat>{
	private static final long serialVersionUID = 2610922805158148972L;
	public static final String INITSTATUS_CLOSE = "C";
	public static final String INITSTATUS_OPEN = "O";
	private Long id;
	private Long roomid;			//影厅ID
	private Integer lineno;		//前起第几排,系统生成
	private Integer rankno;		//左起第几列,系统生产
	private String seatline;	//座位行号
	private String seatrank;	//座位列号
	private String loveInd;		//情侣座 0：普通座位 1：情侣座首座位标记 2：情侣座第二座位标记
	private String initstatus;	//每次开放初始状态
	
	public TheatreRoomSeat() {
	}
	public TheatreRoomSeat(Long roomid, int lineno, int rankno){
		this.roomid = roomid;
		this.lineno = lineno;
		this.rankno = rankno;
		this.loveInd = "0";
		this.initstatus = INITSTATUS_OPEN;
	}
	public TheatreRoomSeat(Long roomid, int lineno, int rankno, String seatline, String seatrank){
		this.roomid = roomid;
		this.lineno = lineno;
		this.rankno = rankno;
		this.seatline = seatline;
		this.seatrank = seatrank;
		this.loveInd = "0";
		this.initstatus = INITSTATUS_OPEN;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getRoomid() {
		return roomid;
	}

	public void setRoomid(Long roomid) {
		this.roomid = roomid;
	}

	public Integer getLineno() {
		return lineno;
	}

	public void setLineno(Integer lineno) {
		this.lineno = lineno;
	}

	public Integer getRankno() {
		return rankno;
	}

	public void setRankno(Integer rankno) {
		this.rankno = rankno;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public String getSeatline() {
		return seatline;
	}

	public void setSeatline(String seatline) {
		this.seatline = seatline;
	}

	public String getSeatrank() {
		return seatrank;
	}

	public void setSeatrank(String seatrank) {
		this.seatrank = seatrank;
	}
	public String getSeatLabel(){
		return seatline+"排"+seatrank+"座";
	}
	@Override
	public int compareTo(TheatreRoomSeat o) {
		if(this.equals(o)) return 0;
		if(!roomid.equals(o.roomid)) return roomid.compareTo(o.roomid);
		return o.lineno*100 + o.rankno - lineno*100 - rankno;
	}
	public String getLoveInd() {
		return loveInd;
	}
	public void setLoveInd(String loveInd) {
		this.loveInd = loveInd;
	}
	public String getInitstatus() {
		return initstatus;
	}
	public void setInitstatus(String initstatus) {
		this.initstatus = initstatus;
	}
	
	public static boolean isValidStatus(String initstatus) {
		return StringUtils.contains("CO", initstatus);
	}
	public String getPosition(){
		return this.lineno+":" + this.rankno; 
	}
	
}
