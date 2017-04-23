package com.gewara.xmlbind.ticket;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.xmlbind.BaseInnerResponse;

public class TicketRemoteOrder extends BaseInnerResponse {
	private Long orderid;
	private String seqno;
	private String mobile;
	private String bookingId;
	private String confirmationId;
	private String tickets;
	private String status;
	private String seatText;
	private String seatno;
	private String checkmark;
	private String message;
	private String orderType;
	private String otherinfo;
	private Timestamp updatetime;
	
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getSeqno() {
		return seqno;
	}
	public void setSeqno(String seqno) {
		this.seqno = seqno;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}
	public String getConfirmationId() {
		return confirmationId;
	}
	public void setConfirmationId(String confirmationId) {
		this.confirmationId = confirmationId;
	}
	public String getTickets() {
		return tickets;
	}
	public void setTickets(String tickets) {
		this.tickets = tickets;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSeatText() {
		return seatText;
	}
	public void setSeatText(String seatText) {
		this.seatText = seatText;
	}
	public String getCheckmark() {
		return checkmark;
	}
	public void setCheckmark(String checkmark) {
		this.checkmark = checkmark;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public boolean hasFixed(){
		return OrderConstant.REMOTE_STATUS_FIXED.equals(status) && StringUtils.isNotBlank(confirmationId) && 
				StringUtils.isNotBlank(bookingId) && !StringUtils.equals("00000000", "confirmationId");
	}
	
	public boolean hasStatus(String fixseat){
		if(StringUtils.isBlank(fixseat)) return false;
		return StringUtils.equals(this.status, fixseat);
	}
	
	public boolean hasOrderType(String ordertype){
		if(StringUtils.isBlank(ordertype)) return false;
		return StringUtils.equals(this.orderType, ordertype);
	}
	public String getSeatno() {
		return seatno;
	}
	public void setSeatno(String seatno) {
		this.seatno = seatno;
	}
}
