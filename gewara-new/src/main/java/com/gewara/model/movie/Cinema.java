package com.gewara.model.movie;



import com.gewara.model.common.BaseInfo;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class Cinema extends BaseInfo {
	private static final long serialVersionUID = 5226491557222831911L;
	public static final String BOOKING_OPEN = "open";
	public static final String BOOKING_CLOSE = "close";
	private String flag;
	private String booking;
	private String popcorn;
	private String contactTelephone;//格式json [{"areaCode":"021","phone":"1234567","phoneRemark":"院长办公室"}{"areaCode":"021","phone":"1234567","phoneRemark":"院长办公室"}]
	private String mobilePhone;
	/**
	 * 地铁交通描述{地铁站id:[{"详细描述":"右转50米","出口":"2号口","线路","1,2,8号线"}]}
	 * 例如： {"170170":[{"detail":"s","exitnumber":"5","lines":"9"}],"7702811":[{"detail":"123","exitnumber":"2","lines":"1"},{"detail":"43","exitnumber":"3","lines":"2"}]}
	 */
	private String subwayTransport; 
	
	public Cinema(){}
	
	public Cinema(String name) {
		super(name);
		this.booking = BOOKING_CLOSE;
	}

	public String getUrl(){
		return "cinema/" + this.id;
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
	
	public String getPopcorn() {
		return popcorn;
	}

	public void setPopcorn(String popcorn) {
		this.popcorn = popcorn;
	}
	public String getContactTelephone() {
		return contactTelephone;
	}
	public void setContactTelephone(String contactTelephone) {
		this.contactTelephone = contactTelephone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getSubwayTransport() {
		return subwayTransport;
	}
	
	public void setSubwayTransport(String subwayTransport) {
		this.subwayTransport = subwayTransport;
	}
}
