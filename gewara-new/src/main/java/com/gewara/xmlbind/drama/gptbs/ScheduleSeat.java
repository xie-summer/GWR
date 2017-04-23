package com.gewara.xmlbind.drama.gptbs;

import java.io.Serializable;

import com.gewara.model.BaseObject;
//场次座位
public class ScheduleSeat extends BaseObject{
	private static final long serialVersionUID = -1102028537356157035L;
	private Long id;				//ID
	private String lineno;			//排
	private String rankno;			//座(列)
	private Integer x;				//物理坐标X
	private Integer y;				//物理坐标Y
	private Long ticketPriceId;		//价格ID
	private Long venueAreaId;		//场次区域ID
	private Long ticketPoolId;		//票池ID
	private Long scheduleId;		//场次ID
	private Integer status;			//状态
	private Long programId;			//项目ID
	private Integer serialNum;		//序号
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getLineno() {
		return lineno;
	}
	public void setLineno(String lineno) {
		this.lineno = lineno;
	}
	public String getRankno() {
		return rankno;
	}
	public void setRankno(String rankno) {
		this.rankno = rankno;
	}
	public Integer getX() {
		return x;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}
	public Long getTicketPriceId() {
		return ticketPriceId;
	}
	public void setTicketPriceId(Long ticketPriceId) {
		this.ticketPriceId = ticketPriceId;
	}
	public Long getVenueAreaId() {
		return venueAreaId;
	}
	public void setVenueAreaId(Long venueAreaId) {
		this.venueAreaId = venueAreaId;
	}
	public Long getTicketPoolId() {
		return ticketPoolId;
	}
	public void setTicketPoolId(Long ticketPoolId) {
		this.ticketPoolId = ticketPoolId;
	}
	public Long getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(Long scheduleId) {
		this.scheduleId = scheduleId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getProgramId() {
		return programId;
	}
	public void setProgramId(Long programId) {
		this.programId = programId;
	}
	public Integer getSerialNum() {
		return serialNum;
	}
	public void setSerialNum(Integer serialNum) {
		this.serialNum = serialNum;
	}
}
