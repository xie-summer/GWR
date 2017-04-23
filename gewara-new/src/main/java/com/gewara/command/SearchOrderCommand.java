package com.gewara.command;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OrderConstant;
import com.gewara.util.DateUtil;

/**
 * 搜索订单Bean
 * @author acerge(acerge@163.com)
 * @since 12:13:41 PM Oct 17, 2009
 */
public class SearchOrderCommand {
	private String ordertype;	//订单类型
	private String pricategory;
	private String category;
	private String placetype;
	private Long placeid;
	private String itemtype;
	private String mptype;
	private Long cid;				//CinemaID
	private Long movieid;		//MOVIEID
	private Long orderid;		//orderID
	private Long mpid;			//场次ID
	private Long memberid;		//用户ID
	private Integer minute;		//查询分钟
	private String mobile;		//手机号
	private String tradeNo;		//交易号
	private String status = OrderConstant.STATUS_PAID;
	private Timestamp timeFrom;//下单时间范围
	private Timestamp timeTo;
	private Long goodsid;
	private Long ottid;
	private Long sportid;
	private Long itemid;
	private Long dramaid;
	private Long theatrid;
	private String sno;
	private String takemethod;
	private String card;
	private String citycode;
	private Long gymid;			//健身场馆id 2012-04-24添加
	private String expressid;
	private Long areaid;
	private Long mctid;
	public Long getGymid() {
		return gymid;
	}
	public void setGymid(Long gymid) {
		this.gymid = gymid;
	}
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}
	public String getTakemethod() {
		return takemethod;
	}
	public void setTakemethod(String takemethod) {
		this.takemethod = takemethod;
	}
	public String getSno() {
		return sno;
	}
	public void setSno(String sno) {
		this.sno = sno;
	}
	public Long getDramaid() {
		return dramaid;
	}
	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}
	public Long getTheatrid() {
		return theatrid;
	}
	public void setTheatrid(Long theatrid) {
		this.theatrid = theatrid;
	}
	public Long getSportid() {
		return sportid;
	}
	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}
	
	public Long getOttid() {
		return ottid;
	}
	public void setOttid(Long ottid) {
		this.ottid = ottid;
	}
	public Long getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}
	public Timestamp getTimeFrom() {
		return timeFrom;
	}
	public void setTimeFrom(Timestamp timeFrom) {
		this.timeFrom = timeFrom;
	}
	public Timestamp getTimeTo() {
		return timeTo;
	}
	public void setTimeTo(Timestamp timeTo) {
		this.timeTo = timeTo;
	}
	public String getOrdertype() {
		return ordertype;
	}
	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public Long getMpid() {
		return mpid;
	}
	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public Integer getMinute() {
		return minute;
	}
	public void setMinute(Integer minute) {
		if(minute!=null && minute>14400) this.minute=14400;
		else this.minute = minute;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public boolean isBlankCond(){
		return (minute == null || minute > 1440 && !StringUtils.startsWith(ordertype, OrderConstant.STATUS_PAID_FAILURE))
				&&(timeFrom == null || timeTo == null || DateUtil.addDay(timeFrom, 30).before(timeTo))
				&& mpid == null && orderid == null && StringUtils.length(tradeNo) != 16 && StringUtils.length(mobile) != 11;
	}
	public boolean hasBlankCond(){
		return isBlankCond() && placeid == null;
	}
	public Long getCid() {
		return cid;
	}
	public void setCid(Long cid) {
		this.cid = cid;
	}
	public String getPricategory() {
		return pricategory;
	}
	public void setPricategory(String pricategory) {
		this.pricategory = pricategory;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getMovieid() {
		return movieid;
	}
	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}
	public Long getItemid() {
		if(itemid == null) itemid = movieid;
		if(itemid == null) itemid = dramaid;
		return itemid;
	}
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	public String getCitycode(){
		return citycode;
	}
	public void setCitycode(String citycode){
		this.citycode = citycode;
	}
	public String getPlacetype() {
		return placetype;
	}
	public void setPlacetype(String placetype) {
		this.placetype = placetype;
	}
	public String getItemtype() {
		return itemtype;
	}
	public void setItemtype(String itemtype) {
		this.itemtype = itemtype;
	}
	public String getMptype() {
		return mptype;
	}
	public void setMptype(String mptype) {
		this.mptype = mptype;
	}
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}
	public String getExpressid() {
		return expressid;
	}
	public void setExpressid(String expressid) {
		this.expressid = expressid;
	}
	public Long getAreaid() {
		return areaid;
	}
	public void setAreaid(Long areaid) {
		this.areaid = areaid;
	}
	public Long getMctid() {
		return mctid;
	}
	public void setMctid(Long mctid) {
		this.mctid = mctid;
	}
	
}
