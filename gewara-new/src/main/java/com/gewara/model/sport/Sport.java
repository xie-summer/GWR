package com.gewara.model.sport;
import com.gewara.model.common.BaseInfo;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class Sport extends BaseInfo{
	private static final long serialVersionUID = -5384514237227604638L;
	public static final String BOOKING_OPEN = "open";
	public static final String BOOKING_CLOSE = "close";
	private String services;
	private String openinfo;
	private String flag;
	public static final String FLAG_RECOMMEND = "recommend";// 推荐分类
	public static final String FLAG_HOT = "hot";// 推荐热门
	private Integer avgfield;
	private String booking;
	private String floorplan;
	private String machinepic;
	public Sport(){}
	public Sport(String name) {
		super(name);
		this.booking = BOOKING_CLOSE;
	}


	public String getServices() {
		return services;
	}

	public void setServices(String services) {
		this.services = services;
	}
	public String getOpeninfo() {
		return openinfo;
	}
	public void setOpeninfo(String openinfo) {
		this.openinfo = openinfo;
	}
	public Integer getAvgfield() {
		return avgfield;
	}
	public void setAvgfield(Integer avgfield) {
		this.avgfield = avgfield;
	}
	public String getUrl(){
		return "sport/" + this.getId();
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getBooking() {
		return booking;
	}
	public void setBooking(String booking) {
		this.booking = booking;
	}
	public boolean isEnableBook(){
		return BOOKING_OPEN.equals(this.booking);
	}
	public String getFloorplan() {
		return floorplan;
	}
	public void setFloorplan(String floorplan) {
		this.floorplan = floorplan;
	}
	public String getMachinepic() {
		return machinepic;
	}
	public void setMachinepic(String machinepic) {
		this.machinepic = machinepic;
	}
}
