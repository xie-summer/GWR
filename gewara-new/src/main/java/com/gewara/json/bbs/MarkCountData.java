package com.gewara.json.bbs;

import com.gewara.model.bbs.MarkCount;

public class MarkCountData extends MarkCount{

	private static final long serialVersionUID = 7486444487060223602L;

	public MarkCountData(MarkCount markCount, Double scale) {
		this.mkey = markCount.getMkey();
		this.tag = markCount.getTag();
		this.relatedid = markCount.getRelatedid();
		this.bookingtimes = markCount.getBookingtimes();
		this.unbookingtimes = markCount.getUnbookingtimes();
		if(scale!=null){
			if(markCount.getUnbookingmarks() > 0){
				Double unbook = Math.min(8.0, markCount.getUnbookingmarks() * scale/(1.0 * unbookingtimes)) *  unbookingtimes;
				this.unbookingmarks = unbook.intValue();
			}else{
				this.unbookingmarks = markCount.getUnbookingmarks();
			}
			if(markCount.getBookingmarks() > 0){
				Double book = Math.min(8.0, markCount.getBookingmarks() * scale/(1.0 * bookingtimes)) *  bookingtimes;
				this.bookingmarks = book.intValue();
			}else{
				this.bookingmarks = markCount.getBookingmarks();
			}
			
		}else{
			this.bookingmarks = markCount.getBookingmarks();
			this.unbookingmarks = markCount.getUnbookingmarks();
		}
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

}
