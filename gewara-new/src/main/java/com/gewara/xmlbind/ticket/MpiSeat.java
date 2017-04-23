package com.gewara.xmlbind.ticket;

public class MpiSeat {
	private String seatline;
	private String seatrank;
	private Integer lineno;
	private Integer rankno;
	
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
	
	public String getSeatLabel(){
		return seatline+"ÅÅ"+seatrank+"×ù";
	}
}
