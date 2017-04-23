package com.gewara.model.bbs;

import java.io.Serializable;

import com.gewara.model.BaseObject;

public class MarkCount extends BaseObject {
	private static final long serialVersionUID = 7402495088924178076L;
	protected String mkey;
	protected String tag;
	protected Long relatedid;
	protected Integer bookingmarks;
	protected Integer bookingtimes;
	protected Integer unbookingmarks;
	protected Integer unbookingtimes;
	
	public MarkCount(){
	}
	public MarkCount(String tag, Long relatedid){
		this.mkey = tag + relatedid;
		this.bookingmarks = 0;
		this.bookingtimes = 0;
		this.unbookingmarks = 0;
		this.unbookingtimes = 0;
		this.tag = tag;
		this.relatedid = relatedid;
	}
	@Override
	public Serializable realId() {
		return mkey;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public Integer getBookingmarks() {
		return bookingmarks;
	}
	public void setBookingmarks(Integer bookingmarks) {
		this.bookingmarks = bookingmarks;
	}
	public Integer getBookingtimes() {
		return bookingtimes;
	}
	public void setBookingtimes(Integer bookingtimes) {
		this.bookingtimes = bookingtimes;
	}
	public Integer getAvgbookingmarks() {
		return bookingmarks * 10 / bookingtimes;
	}
	public Integer getUnbookingmarks() {
		return unbookingmarks;
	}
	public void setUnbookingmarks(Integer unbookingmarks) {
		this.unbookingmarks = unbookingmarks;
	}
	public Integer getUnbookingtimes() {
		return unbookingtimes;
	}
	public void setUnbookingtimes(Integer unbookingtimes) {
		this.unbookingtimes = unbookingtimes;
	}
	public Integer getUnavgbookingmarks() {
		return unbookingmarks * 10 / unbookingtimes;
	}
	public String getMkey() {
		return mkey;
	}
	public void setMkey(String mkey) {
		this.mkey = mkey;
	}
}
