package com.gewara.model.agency;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.common.BaseInfo;
import com.gewara.util.DateUtil;

/**
 * @author wkxyl9
 * @since 2013-03-14 16:20:00
 */
public class Agency extends BaseInfo{
	
	private static final long serialVersionUID = 4919260911564534787L;
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_CLOSE = "close";
	
	private String type;
	private String booking;
	private String status;
	
	public Agency(){}
	public Agency(String name, String citycode) {
		super(name);
		this.status = Status.Y;
		this.citycode = citycode;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.booking = STATUS_CLOSE;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBooking() {
		return booking;
	}
	public void setBooking(String booking) {
		this.booking = booking;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public boolean openBooking(){
		return StringUtils.equals(this.booking, STATUS_OPEN);
	}
}
