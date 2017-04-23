package com.gewara.xmlbind.gym;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;


public class RemoteGym implements Serializable{
	private static final long serialVersionUID = 2825420561955347484L;
	private Long id;
	private String name;
	private String briefname;
	private String contactPhone;
	private String citycode;
	private String cityname;
	private String countycode;
	private String countyname;
	private String indexareacode;
	private String indexareaname;
	private String address;
	private String bpointx;
	private String bpointy;
	private String pointx;
	private String pointy;
	private Long subway;
	private Long stationid;
	private String subwayInfo;
	private String bus;
	private String parkingSpaces;
	private String parkingInfo;
	private String bathing;
	private String logo;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String searchKey;
	private Integer clickedtimes;
	private String booking;
	private Integer generalmark;
	private Integer generalmarkedtimes;
	private Double avggeneral;
	private Integer servicemark;
	private Integer servicemarkedtimes;
	private Double avgservice;
	private Integer environmentmark;
	private Integer environmentmarkedtimes;
	private Double avgenvironment;
	private Integer spacemark;
	private Integer spacemarkedtimes;
	private Double avgspace;
	private Boolean cooperate;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBriefname() {
		return briefname;
	}

	public void setBriefname(String briefname) {
		this.briefname = briefname;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public Long getSubway() {
		return subway;
	}

	public void setSubway(Long subway) {
		this.subway = subway;
	}

	public Long getStationid() {
		return stationid;
	}

	public void setStationid(Long stationid) {
		this.stationid = stationid;
	}

	public String getSubwayInfo() {
		return subwayInfo;
	}

	public void setSubwayInfo(String subwayInfo) {
		this.subwayInfo = subwayInfo;
	}

	public String getBus() {
		return bus;
	}

	public void setBus(String bus) {
		this.bus = bus;
	}

	public String getParkingSpaces() {
		return parkingSpaces;
	}

	public void setParkingSpaces(String parkingSpaces) {
		this.parkingSpaces = parkingSpaces;
	}

	public String getParkingInfo() {
		return parkingInfo;
	}

	public void setParkingInfo(String parkingInfo) {
		this.parkingInfo = parkingInfo;
	}

	public String getBathing() {
		return bathing;
	}

	public void setBathing(String bathing) {
		this.bathing = bathing;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public String getBooking() {
		return booking;
	}

	public void setBooking(String booking) {
		this.booking = booking;
	}

	public Integer getGeneralmark() {
		return generalmark == null ? 0 : generalmark;
	}

	public void setGeneralmark(Integer generalmark) {
		this.generalmark = generalmark;
	}

	public Integer getGeneralmarkedtimes() {
		return generalmarkedtimes == null ? 0 : generalmarkedtimes;
	}

	public void setGeneralmarkedtimes(Integer generalmarkedtimes) {
		this.generalmarkedtimes = generalmarkedtimes;
	}

	public Double getAvggeneral() {
		return avggeneral;
	}

	public void setAvggeneral(Double avggeneral) {
		this.avggeneral = avggeneral;
	}

	public Integer getServicemark() {
		return servicemark == null ? 0 : servicemark;
	}

	public void setServicemark(Integer servicemark) {
		this.servicemark = servicemark;
	}

	public Integer getServicemarkedtimes() {
		return servicemarkedtimes == null ? 0 : servicemarkedtimes;
	}

	public void setServicemarkedtimes(Integer servicemarkedtimes) {
		this.servicemarkedtimes = servicemarkedtimes;
	}

	public Double getAvgservice() {
		return avgservice;
	}

	public void setAvgservice(Double avgservice) {
		this.avgservice = avgservice;
	}

	public Integer getEnvironmentmark() {
		return environmentmark == null ? 0 : environmentmark;
	}

	public void setEnvironmentmark(Integer environmentmark) {
		this.environmentmark = environmentmark;
	}

	public Integer getEnvironmentmarkedtimes() {
		return environmentmarkedtimes == null ? 0 : environmentmarkedtimes;
	}

	public void setEnvironmentmarkedtimes(Integer environmentmarkedtimes) {
		this.environmentmarkedtimes = environmentmarkedtimes;
	}

	public Double getAvgenvironment() {
		return avgenvironment;
	}

	public void setAvgenvironment(Double avgenvironment) {
		this.avgenvironment = avgenvironment;
	}

	public Integer getSpacemark() {
		return spacemark;
	}

	public void setSpacemark(Integer spacemark) {
		this.spacemark = spacemark;
	}

	public Integer getSpacemarkedtimes() {
		return spacemarkedtimes;
	}

	public void setSpacemarkedtimes(Integer spacemarkedtimes) {
		this.spacemarkedtimes = spacemarkedtimes;
	}

	public Double getAvgspace() {
		return avgspace;
	}

	public void setAvgspace(Double avgspace) {
		this.avgspace = avgspace;
	}

	public Boolean getCooperate() {
		return cooperate;
	}

	public void setCooperate(Boolean cooperate) {
		this.cooperate = cooperate;
	}

	public String getRealBriefname(){
		return StringUtils.isNotBlank(this.briefname)? briefname: getName();
	}
	
	public String getSkey(){
		return getName();
	}
	
	public String getUrl(){
		return "gym/" + id;
	}
	
	public String getLimg(){
		return getLogo();
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
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
}

