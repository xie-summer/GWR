package com.gewara.xmlbind.gym;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class BookingRecordListResponse extends BaseObjectListResponse<BookingRecord> {
	private List<BookingRecord> bookingRecordList = new ArrayList<BookingRecord>();

	public List<BookingRecord> getBookingRecordList() {
		return bookingRecordList;
	}

	public void setBookingRecordList(List<BookingRecord> bookingRecordList) {
		this.bookingRecordList = bookingRecordList;
	}
	
	public void addBookingRecord(BookingRecord bookingRecord){
		this.bookingRecordList.add(bookingRecord);
	}

	@Override
	public List<BookingRecord> getObjectList() {
		return bookingRecordList;
	}
}
