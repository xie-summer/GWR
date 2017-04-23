package com.gewara.web.action.sport;

public class SearchSportCommand {
	public Long sportid;
	private String sportname;
	public String countycode;
	public String indexareacode;
	public String servicetype;
	public String subservicetype;
	public Long lineid;
	public String park;
	public String coupon;
	public String order;
	public String booking;
	public String indoor;      //ÊÒÄÚ
	public String outdoor;	   //ÊÒÍâ
	public String visacard;    //Ë¢¿¨
	public String cupboard;	   //¹ñ×ÓÆ¾×â
	public String bathe;	   //Ï´Ôè
	public String restregion;  //ÐÝÏ¢Çø
	public String sale;		   //ÂôÆ·
	public String train;	   //ÅàÑµ
	public String meal;		   //Ì×²Í
	public String lease;	   //Æ÷²Ä×â½è	
	public String maintain;    //Æ÷²ÄÎ¬»¤
	public String membercard;	//»áÔ±¿¨
	public String sportids;		//ÎÒ³£È¥³¡¹ÝIDs
	public int pageNo=0;
	public int rowsPerpage=10;
	public Long stationid;
	public Long getStationid() {
		return stationid;
	}
	public void setStationid(Long stationid) {
		this.stationid = stationid;
	}
	public String getServicetype() {
		return servicetype;
	}
	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}
	public int getRowsPerpage() {
		return rowsPerpage;
	}
	public void setRowsPerpage(int rowsPerpage) {
		this.rowsPerpage = rowsPerpage;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public String getCountycode() {
		return countycode;
	}
	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
	public String getIndexareacode() {
		return indexareacode;
	}
	public void setIndexareacode(String indexareacode) {
		this.indexareacode = indexareacode;
	}
	public Long getSportid() {
		return sportid;
	}
	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getSportname() {
		return sportname;
	}
	public void setSportname(String sportname) {
		if(sportname!=null && sportname.contains("ÊäÈë")) this.sportname=null;
		else this.sportname = sportname;
	}
	public String getSubservicetype() {
		return subservicetype;
	}
	public void setSubservicetype(String subservicetype) {
		this.subservicetype = subservicetype;
	}
	public Long getLineid() {
		return lineid;
	}
	public void setLineid(Long lineid) {
		this.lineid = lineid;
	}
	public String getPark() {
		return park;
	}
	public void setPark(String park) {
		this.park = park;
	}
	public String getCoupon() {
		return coupon;
	}
	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}
	public String getBooking() {
		return booking;
	}
	public void setBooking(String booking) {
		this.booking = booking;
	}
	public String getIndoor() {
		return indoor;
	}
	public void setIndoor(String indoor) {
		this.indoor = indoor;
	}
	public String getOutdoor() {
		return outdoor;
	}
	public void setOutdoor(String outdoor) {
		this.outdoor = outdoor;
	}
	public String getVisacard() {
		return visacard;
	}
	public void setVisacard(String visacard) {
		this.visacard = visacard;
	}
	public String getCupboard() {
		return cupboard;
	}
	public void setCupboard(String cupboard) {
		this.cupboard = cupboard;
	}
	public String getBathe() {
		return bathe;
	}
	public void setBathe(String bathe) {
		this.bathe = bathe;
	}
	public String getRestregion() {
		return restregion;
	}
	public void setRestregion(String restregion) {
		this.restregion = restregion;
	}
	public String getSale() {
		return sale;
	}
	public void setSale(String sale) {
		this.sale = sale;
	}
	public String getTrain() {
		return train;
	}
	public void setTrain(String train) {
		this.train = train;
	}
	public String getMeal() {
		return meal;
	}
	public void setMeal(String meal) {
		this.meal = meal;
	}
	public String getLease() {
		return lease;
	}
	public void setLease(String lease) {
		this.lease = lease;
	}
	public String getMaintain() {
		return maintain;
	}
	public void setMaintain(String maintain) {
		this.maintain = maintain;
	}
	public String getMembercard() {
		return membercard;
	}
	public void setMembercard(String membercard) {
		this.membercard = membercard;
	}
	public String getSportids() {
		return sportids;
	}
	public void setSportids(String sportids) {
		this.sportids = sportids;
	}
}
