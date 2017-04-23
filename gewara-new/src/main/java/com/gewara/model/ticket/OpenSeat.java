package com.gewara.model.ticket;

import java.io.Serializable;

import com.gewara.constant.ticket.SeatConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.movie.RoomSeat;

public class OpenSeat extends BaseObject {
	private static final long serialVersionUID = -8401641826228009250L;
	private Long id;					
	private Long mpid;				//关联场次
	private String status;			//锁定状态
	private Integer lineno;			//前起第几排,系统生成
	private Integer rankno;			//左起第几列,系统生产
	private String seatline;		//座位行号
	private String seatrank;		//座位列号
	private String seattype;		//价格类型
	private String loveInd;			//情侣座
	public OpenSeat(){}
	
	public OpenSeat(RoomSeat seat, Long mpid){
		this.lineno = seat.getLineno();
		this.rankno = seat.getRankno();
		this.seatline = seat.getSeatline();
		this.seatrank = seat.getSeatrank();
		this.seattype = SeatConstant.SEAT_TYPE_A;
		this.mpid = mpid;
		this.status = SeatConstant.STATUS_NEW;
		this.loveInd = seat.getLoveInd();
	}
	public String getSeatLabel(){
		return seatline+"排"+seatrank+"座";
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
	public Long getMpid() {
		return mpid;
	}
	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}
	public String getStatus() {
		return status;
	}
	public String getKey(){
		return this.seatline+":"+this.seatrank;
	}
	
	public void setStatus(String status) {
		this.status = status;
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
	public boolean isLocked() {
		return "BCD".contains(status);
	}
	public String getSeattype() {
		return seattype;
	}
	public void setSeattype(String seattype) {
		this.seattype = seattype;
	}
	public String getLoveInd() {
		return loveInd;
	}
	public void setLoveInd(String loveInd) {
		this.loveInd = loveInd;
	}
	public String getPosition(){
		return this.lineno+":" + this.rankno; 
	}
}
