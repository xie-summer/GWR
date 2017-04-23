package com.gewara.xmlbind.ticket;

public class TicketRoomSeat {
	private Long id;
	private Long roomid;		//影厅ID
	private Integer lineno;		//前起第几排,系统生成
	private Integer rankno;		//左起第几列,系统生产
	private String seatline;	//座位行号
	private String seatrank;	//座位列号
	private String loveInd;		//情侣座 0：普通座位 1：情侣座首座位标记 2：情侣座第二座位标记
	private String seatno;		//座位编号
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getLoveInd() {
		return loveInd;
	}
	public void setLoveInd(String loveInd) {
		this.loveInd = loveInd;
	}
	public String getSeatno() {
		return seatno;
	}
	public void setSeatno(String seatno) {
		this.seatno = seatno;
	}
	
}
