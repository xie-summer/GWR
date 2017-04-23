package com.gewara.command;

public class SearchCinemaCommand {
	public String countycode;
	public String indexareacode;
	public String order;
	public Long lineId;
	public Long cinemaid;
	private String cinemaname;
	private String park;
	private String playground;
	private String visacard;
	private String pairseat;		//情侣座
	private String coupon;			//优惠券
	private String booking;
	public Long stationid;
	public String popcorn;			//爆米花
	public int pageNo=0;
	public int rowsPerpage=10;
	private String imax;
	private String child;
	private String cinemaids;		//我常去IDs
	private String acthas;			//是否有活动
	private String refund;			//是否可退票
	private String characteristic;//特效厅
	private String hotcinema;		//热门影院
	private String ctype;			//特效厅类型
	private String lineall;			//所有地铁线路
	
	public String getImax() {
		return imax;
	}
	public void setImax(String imax) {
		this.imax = imax;
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
	public Long getCinemaid() {
		return cinemaid;
	}
	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getRowsPerpage() {
		return rowsPerpage;
	}
	public void setRowsPerpage(int rowsPerpage) {
		this.rowsPerpage = rowsPerpage;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getCinemaname() {
		return cinemaname;
	}
	public void setCinemaname(String cinemaname) {
		if(cinemaname!=null && cinemaname.contains("输入")) this.cinemaname=null;
		else this.cinemaname = cinemaname;
	}
	public Long getLineId() {
		return lineId;
	}
	public void setLineId(Long lineId) {
		this.lineId = lineId;
	}
	public String getPark() {
		return park;
	}
	public void setPark(String park) {
		this.park = park;
	}
	public String getPlayground() {
		return playground;
	}
	public void setPlayground(String playground) {
		this.playground = playground;
	}
	public String getVisacard() {
		return visacard;
	}
	public void setVisacard(String visacard) {
		this.visacard = visacard;
	}
	public String getPairseat() {
		return pairseat;
	}
	public void setPairseat(String pairseat) {
		this.pairseat = pairseat;
	}
	public String getCoupon() {
		return coupon;
	}
	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}
	public Long getStationid() {
		return stationid;
	}
	public void setStationid(Long stationid) {
		this.stationid = stationid;
	}
	public String getBooking() {
		return booking;
	}
	public void setBooking(String booking) {
		this.booking = booking;
	}
	public String getPopcorn() {
		return popcorn;
	}
	public void setPopcorn(String popcorn) {
		this.popcorn = popcorn;
	}
	public String getChild() {
		return child;
	}
	public void setChild(String child) {
		this.child = child;
	}
	public String getCinemaids() {
		return cinemaids;
	}
	public void setCinemaids(String cinemaids) {
		this.cinemaids = cinemaids;
	}
	public String getActhas() {
		return acthas;
	}
	public void setActhas(String acthas) {
		this.acthas = acthas;
	}
	public String getRefund() {
		return refund;
	}
	public void setRefund(String refund) {
		this.refund = refund;
	}
	public String getCharacteristic() {
		return characteristic;
	}
	public void setCharacteristic(String characteristic) {
		this.characteristic = characteristic;
	}
	public String getHotcinema() {
		return hotcinema;
	}
	public void setHotcinema(String hotcinema) {
		this.hotcinema = hotcinema;
	}
	public String getCtype() {
		return ctype;
	}
	public void setCtype(String ctype) {
		this.ctype = ctype;
	}
	public String getLineall() {
		return lineall;
	}
	public void setLineall(String lineall) {
		this.lineall = lineall;
	}

}
