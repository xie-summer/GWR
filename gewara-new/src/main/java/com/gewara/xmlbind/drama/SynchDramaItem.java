package com.gewara.xmlbind.drama;

import java.sql.Timestamp;

public class SynchDramaItem {

	private Long gewaid;
	private Long busid;
	private Long gewatheatreid;
	private String theatrename;
	private Long gewadramaid;
	private String dramaname;
	private Long gewaroomid;
	private String roomname;
	private Timestamp playtime;
	private String language;
	private Integer price;
	private Integer costprice;
	private Integer gewaprice;
	private String status;
	private Timestamp opentime;
	private Timestamp closetime;
	private Timestamp updatetime;
	private String opentype;
	private Long topicid;			//取票帖子
	private String takemethod;		//取票方式
	private String takemsg;			//取票描述
	private String buylimit;		//购买张数限制，1,2,3,4,5
	private String remark;			//描述
	private String elecard;
	private Long dpid;
	private String seatlink;		//座位图链接
	private String spflag;
	private String otherinfo;
	private Integer minpoint;				//使用积分下限
	private Integer maxpoint;				//使用积分上限
	private String partner;			//合作伙伴开放状态：Y对外开放,N不对外开放
	private String citycode;
	private Long dramastarid; 		//剧团ID
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
	public Long getGewatheatreid() {
		return gewatheatreid;
	}
	public void setGewatheatreid(Long gewatheatreid) {
		this.gewatheatreid = gewatheatreid;
	}
	public String getTheatrename(){
		return theatrename;
	}
	public void setTheatrename(String theatrename){
		this.theatrename = theatrename;
	}
	public Long getGewadramaid() {
		return gewadramaid;
	}
	public void setGewadramaid(Long gewadramaid) {
		this.gewadramaid = gewadramaid;
	}
	public String getDramaname() {
		return dramaname;
	}
	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}
	public Long getGewaroomid() {
		return gewaroomid;
	}
	public void setGewaroomid(Long gewaroomid) {
		this.gewaroomid = gewaroomid;
	}
	public String getRoomname() {
		return roomname;
	}
	public void setRoomname(String roomname) {
		this.roomname = roomname;
	}
	public Timestamp getPlaytime() {
		return playtime;
	}
	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Integer getGewaprice() {
		return gewaprice;
	}
	public void setGewaprice(Integer gewaprice) {
		this.gewaprice = gewaprice;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getOpentime() {
		return opentime;
	}
	public void setOpentime(Timestamp opentime) {
		this.opentime = opentime;
	}
	public Timestamp getClosetime() {
		return closetime;
	}
	public void setClosetime(Timestamp closetime) {
		this.closetime = closetime;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getOpentype() {
		return opentype;
	}
	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}
	public Long getTopicid() {
		return topicid;
	}
	public void setTopicid(Long topicid) {
		this.topicid = topicid;
	}
	public String getTakemethod() {
		return takemethod;
	}
	public void setTakemethod(String takemethod) {
		this.takemethod = takemethod;
	}
	public String getTakemsg() {
		return takemsg;
	}
	public void setTakemsg(String takemsg) {
		this.takemsg = takemsg;
	}
	public String getBuylimit() {
		return buylimit;
	}
	public void setBuylimit(String buylimit) {
		this.buylimit = buylimit;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getElecard() {
		return elecard;
	}
	public void setElecard(String elecard) {
		this.elecard = elecard;
	}
	public Long getDpid() {
		return dpid;
	}
	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}
	public String getSeatlink() {
		return seatlink;
	}
	public void setSeatlink(String seatlink) {
		this.seatlink = seatlink;
	}
	public String getSpflag() {
		return spflag;
	}
	public void setSpflag(String spflag) {
		this.spflag = spflag;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public Integer getMinpoint() {
		return minpoint;
	}
	public void setMinpoint(Integer minpoint) {
		this.minpoint = minpoint;
	}
	public Integer getMaxpoint() {
		return maxpoint;
	}
	public void setMaxpoint(Integer maxpoint) {
		this.maxpoint = maxpoint;
	}
	public String getPartner() {
		return partner;
	}
	public void setPartner(String partner) {
		this.partner = partner;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public Long getDramastarid(){
		return dramastarid;
	}
	public void setDramastarid(Long dramastarid){
		this.dramastarid = dramastarid;
	}
}
