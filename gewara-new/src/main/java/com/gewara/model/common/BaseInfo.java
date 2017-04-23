package com.gewara.model.common;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.util.MarkHelper;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public abstract class BaseInfo extends BaseEntity{
	private static final long serialVersionUID = 6889418516482337249L;
	protected String brandname;
	protected String citycode;
	protected String countycode;
	protected String countyname;
	protected String indexareacode;
	protected String indexareaname;
	protected String address;
	protected String postalcode;
	protected String contactphone;
	protected String fax;
	protected String website;
	protected String email;
	protected String transport;
	protected Long stationid;
	protected String stationname;
	protected String exitnumber;//出口
	protected String googlemap;
	protected String opentime;//营业时间
	protected String feature;
	protected String remark;
	protected String discount; //优惠信息
	protected String coupon; //优惠券
	protected String pointx;
	protected String pointy;
	protected String bpointx;
	protected String bpointy;
	protected String briefaddress;//名称简称
	protected String lineidlist;
	protected String otherinfo;
	protected BaseInfo(){}
	protected BaseInfo(String name){
		this.name = name;
		this.generalmark = 7;
		this.generalmarkedtimes = 1;
		this.quguo = 1;
		this.xiangqu = 1;
		this.clickedtimes = 1;
		this.collectedtimes = 0;
		this.coupon = "N";
	}
	public String getLineidlist() {
		return lineidlist;
	}
	public void setLineidlist(String lineidlist) {
		this.lineidlist = lineidlist;
	}
	public String getBriefaddress() {
		return briefaddress;
	}
	public void setBriefaddress(String briefaddress) {
		this.briefaddress = briefaddress;
	}
	public String getPointx() {
		return pointx;
	}

	public void setPointx(String pointx) {
		this.pointx = pointx;
	}

	public String getPointy() {
		return pointy;
	}

	public void setPointy(String pointy) {
		this.pointy = pointy;
	}

	public String getBpointx() {
		return bpointx;
	}
	public void setBpointx(String bpointx) {
		this.bpointx = bpointx;
	}
	public String getBpointy() {
		return bpointy;
	}
	public void setBpointy(String bpointy) {
		this.bpointy = bpointy;
	}

	public Long getStationid() {
		return stationid;
	}
	public void setStationid(Long stationid) {
		this.stationid = stationid;
	}
	public String getStationname() {
		return stationname;
	}
	public void setStationname(String stationname) {
		this.stationname = stationname;
	}
	public String getBrandname() {
		return this.brandname;
	}

	public void setBrandname(String brandname) {
		this.brandname = brandname;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalcode() {
		return this.postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public String getContactphone() {
		return this.contactphone;
	}

	public void setContactphone(String contactphone) {
		this.contactphone = contactphone;
	}

	public String getFax() {
		return this.fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getWebsite() {
		return this.website;
	}

	public void setWebsite(String website) {
		if(StringUtils.isNotBlank(website) && !StringUtils.startsWith(website, "http://")) website = "http://" + website;
		this.website = website;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTransport() {
		return this.transport;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	public String getRTransport() {
		return this.transport == null ? null : transport.replace("@", ";");
	}
	public String getOpentime() {
		return this.opentime;
	}

	public void setOpentime(String opentime) {
		this.opentime = opentime;
	}
	public String getContent() {
		return this.content;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getExitnumber() {
		return exitnumber;
	}

	public void setExitnumber(String exitnumber) {
		this.exitnumber = exitnumber;
	}
	public String getDividePhone(){
		if(StringUtils.isBlank(contactphone) || contactphone.length()!=8) return contactphone;
		return contactphone.substring(0,4) + " " + contactphone.substring(4);
	}
	public String getGooglemap() {
		return googlemap;
	}
	public void setGooglemap(String googlemap) {
		this.googlemap = googlemap;
	}
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}

	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}
	public String getCoupon() {
		return coupon;
	}

	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}
	public boolean havaCoupon(){
		return StringUtils.isNotBlank(coupon) && "Y".equals(coupon);
	}

	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
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
	public String getCountyname() {
		return countyname;
	}
	public void setCountyname(String countyname) {
		this.countyname = countyname;
	}
	public String getIndexareaname() {
		return indexareaname;
	}
	public void setIndexareaname(String indexareaname) {
		this.indexareaname = indexareaname;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getLineName(Map<String, String> lineMap){
		String result = "";
		if(lineMap==null) return result; 
		if(StringUtils.isNotBlank(this.lineidlist)){
			for(String lineid : lineidlist.split(",")){
				result = result + "," +lineMap.get(lineid);
			}
		}
		if(StringUtils.isNotBlank(result)) return result.substring(1);
		return result;
	}
	public String getCityname(){
		return AdminCityContant.allcityMap.get(citycode);
	}

	public String getGeneral(){
		Integer gmark = MarkHelper.getSingleMarkStar(this, "general");
		if (gmark == null) return "0.0";
		return  gmark/10 + "." + gmark%10;
	}
}
