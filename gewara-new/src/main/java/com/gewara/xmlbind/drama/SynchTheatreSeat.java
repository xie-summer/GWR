package com.gewara.xmlbind.drama;

import java.sql.Timestamp;

public class SynchTheatreSeat {

	private Long gewaid;
	private Long busid;
	private Integer lineno;			//前起第几排,系统生成
	private Integer rankno;			//左起第几列,系统生产
	private String seatline;		//座位行号
	private String seatrank;		//座位列号
	private String loveInd;			//情侣座
	private String seattype;		//价格类型
	private Long gewaodiid;				//关联场次
	private Integer price;			//价格
	private String status;			//状态
	private String remark;			//备注
	private Integer costprice;		//成本价
	private Integer theatreprice;	//剧院价
	private Timestamp updatetime; //更新时间
	
	public Long getGewaid() {
		return gewaid;
	}
	public void setGewaid(Long gewaid) {
		this.gewaid = gewaid;
	}
	public Long getBusid(){
		return busid;
	}
	public void setBusid(Long busid){
		this.busid = busid;
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
	public String getSeattype() {
		return seattype;
	}
	public void setSeattype(String seattype) {
		this.seattype = seattype;
	}
	public Long getGewaodiid() {
		return gewaodiid;
	}
	public void setGewaodiid(Long gewaodiid) {
		this.gewaodiid = gewaodiid;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Integer getTheatreprice() {
		return theatreprice;
	}
	public void setTheatreprice(Integer theatreprice) {
		this.theatreprice = theatreprice;
	}
	public Timestamp getUpdatetime(){
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime){
		this.updatetime = updatetime;
	}
}
