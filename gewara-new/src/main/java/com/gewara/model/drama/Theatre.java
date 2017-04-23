package com.gewara.model.drama;

import com.gewara.model.common.BaseInfo;

public class Theatre extends BaseInfo {
	private static final long serialVersionUID = -3092289501427783500L;
	public static final String TAG_THEATRE = "theatre";
	public static final String BOOKING_OPEN = "open";
	public static final String BOOKING_CLOSE = "close";
	private String booking;
	private Integer boughtcount;	// π∫∆±»À¥Œ
	public Theatre() {}
	public Theatre(String name) {
		super(name);
		this.boughtcount = 0;
	}
	public String getBooking() {
		return booking;
	}

	public void setBooking(String booking) {
		this.booking = booking;
	}

	public Integer getBoughtcount() {
		return boughtcount;
	}
	public void setBoughtcount(Integer boughtcount) {
		this.boughtcount = boughtcount;
	}
	public void addBoughtcount(int num){
		this.boughtcount += num;
	}
	public String getUrl(){
		return "theatre/"+this.id;
	}
}
