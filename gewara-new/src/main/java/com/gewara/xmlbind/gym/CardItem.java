package com.gewara.xmlbind.gym;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.xmlbind.BaseInnerResponse;

public class CardItem extends BaseInnerResponse {
	private Long id;
	private String cardCode;
	private String cardname;
	private String itemType;
	private String itemTypeText;
	private Integer courseOptions;
	private String logo;
	private String content;
	private Integer allTimes;
	private Integer validDay;
	private Integer enableDay;
	private Integer quantity;
	private Integer sales;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String specialcourses;
	private Long gymid;
	private Integer gymprice;
	private Integer price;
	private Integer costprice;
	private String refund;
	private String onlineStatus;
	private Boolean reserve;
	private String elecard;
	private Integer minpoint;
	private Integer maxpoint;
	private String spflag;
	private String otherinfo;
	private RemoteGym gym;
	private List<RemoteSpecialCourse> specialCourseList = new ArrayList<RemoteSpecialCourse>();
	
	public String getCardname() {
		return cardname;
	}

	public void setCardname(String cardname) {
		this.cardname = cardname;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getItemTypeText() {
		return itemTypeText;
	}

	public void setItemTypeText(String itemTypeText) {
		this.itemTypeText = itemTypeText;
	}

	public Integer getValidDay() {
		return validDay;
	}

	public void setValidDay(Integer validDay) {
		this.validDay = validDay;
	}

	public Integer getEnableDay() {
		return enableDay;
	}

	public void setEnableDay(Integer enableDay) {
		this.enableDay = enableDay;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public String getSpecialcourses() {
		return specialcourses;
	}

	public void setSpecialcourses(String specialcourses) {
		this.specialcourses = specialcourses;
	}

	public Long getGymid() {
		return gymid;
	}

	public void setGymid(Long gymid) {
		this.gymid = gymid;
	}

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public void setGymprice(Integer gymprice) {
		this.gymprice = gymprice;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public void setReserve(Boolean reserve) {
		this.reserve = reserve;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCardCode() {
		return cardCode;
	}

	public void setCardCode(String cardCode) {
		this.cardCode = cardCode;
	}

	public String getName() {
		return getCardname();
	}

	public Integer getCourseOptions() {
		return courseOptions;
	}

	public void setCourseOptions(Integer courseOptions) {
		this.courseOptions = courseOptions;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getAllTimes() {
		return allTimes;
	}

	public void setAllTimes(Integer allTimes) {
		this.allTimes = allTimes;
	}

	
	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getElecard() {
		return elecard;
	}

	public void setElecard(String elecard) {
		this.elecard = elecard;
	}

	public String getSpflag() {
		return spflag;
	}

	public void setSpflag(String spflag) {
		this.spflag = spflag;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public Integer getSales() {
		return sales;
	}

	public void setSales(Integer sales) {
		this.sales = sales;
	}

	public Integer getMinpoint() {
		return minpoint;
	}

	public void setMinpoint(Integer minpoint) {
		this.minpoint = minpoint;
	}

	public Integer getMaxpoint() {
		return maxpoint;
	}

	public void setMaxpoint(Integer maxpoint) {
		this.maxpoint = maxpoint;
	}

	public Integer getGymprice() {
		return gymprice;
	}

	public Integer getPrice() {
		return price;
	}

	public String getRefund() {
		return refund;
	}
	
	public void setRefund(String refund) {
		this.refund = refund;
	}
	
	public Boolean getReserve() {
		return reserve;
	}

	public RemoteGym getGym() {
		return gym;
	}

	public void setGym(RemoteGym gym) {
		this.gym = gym;
	}

	public List<RemoteSpecialCourse> getSpecialCourseList() {
		return specialCourseList;
	}

	public void setSpecialCourseList(List<RemoteSpecialCourse> specialCourseList) {
		this.specialCourseList = specialCourseList;
	}
	
	public void addSpecialCourse(RemoteSpecialCourse course){
		this.specialCourseList.add(course);
	}

	public boolean isOpenPointPay(){
		return maxpoint !=null && maxpoint > 0;
	}
	
	public boolean isOpenCardPay(){
		return StringUtils.containsAny(this.elecard, "ABC");
	}
	
	public boolean isDisCountPay(){
		return StringUtils.contains(this.elecard, "M");
	}
	
	public boolean hasBooking(){
		return StringUtils.equals(onlineStatus, Status.Y);
	}
	
	public String getCardTypeText(){
		return getItemTypeText();
	}
	
	public String getLimg(){
		return logo;
	}
}
