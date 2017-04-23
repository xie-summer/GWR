package com.gewara.web.action.drama;

public class SearchTheatreCommand {
	public String theatrename;
	public String countycode;
	public String indexareacode;
	public Long lineid;
	public String park;
	public String order;
	public String playground;
	public String visacard;
	public String booking;
	public String chooseSeat;
	public String takeMethod;
	public int pageNo=0;
	public int rowsPerpage=10;
	public Long stationid;
	public String myRange;
	public String county;
	public String getMyRange() {
		return myRange;
	}
	public void setMyRange(String myRange) {
		this.myRange = myRange;
	}
	public Long getStationid() {
		return stationid;
	}
	public void setStationid(Long stationid) {
		this.stationid = stationid;
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
 
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getPlayground() {
		return playground;
	}
	public void setPlayground(String playground) {
		this.playground = playground;
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
	public String getTheatrename() {
		return theatrename;
	}
	public void setTheatrename(String theatrename) {
		this.theatrename = theatrename;
	}
	public String getVisacard() {
		return visacard;
	}
	public void setVisacard(String visacard) {
		this.visacard = visacard;
	}
	public String getBooking() {
		return booking;
	}
	public void setBooking(String booking) {
		this.booking = booking;
	}
	public String getChooseSeat() {
		return chooseSeat;
	}
	public void setChooseSeat(String chooseSeat) {
		this.chooseSeat = chooseSeat;
	}
	public String getTakeMethod() {
		return takeMethod;
	}
	public void setTakeMethod(String takeMethod) {
		this.takeMethod = takeMethod;
	}
	
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public boolean hasUnCounty(){
		return Boolean.parseBoolean(county);
	}
}
