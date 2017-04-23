package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class OpenTimeSale extends BaseObject {
	private static final long serialVersionUID = -582343026116173553L;
	private Long id;
	private Long sportid;
	private Long itemid;
	private Long fieldid;
	private Long ottid;
	private String bindInd;			// opentimeitem --> saleInd
	private Integer lowerprice; 	// 低价 单位元
	private Integer curprice; 		// 当前竞拍价 单位元
	private Integer dupprice; 		
	private Integer auctionprice;
	private String otiids;
	private Date playdate;
	private String starttime;
	private String endtime;
	private Integer version;
	private String nickname;
	private Long memberid;
	private Timestamp opentime; 			// 开始时间
	private Timestamp closetime; 			// 结束时间
	private Timestamp validtime;			// 场次有效时间
	private Timestamp paidvalidtime;		// 竞价支付有效时间
	private String status; 					// 状态 N, Y, N_DELETE
	private String citycode;
	private Timestamp addtime;				
	private Timestamp jointime;				//最后一次竞价时间
	private Integer joinnum;				//竞价次数
	private String lockStatus;
	private Long guaranteeid;
	private String otherinfo;
	private Long orderid;					//订单ID
	private String mobile;					//
	private String message;
	
	public OpenTimeSale(){}
	public OpenTimeSale(OpenTimeTable ott, Integer lowerprice, Integer dupprice, Integer auctionprice){
		this.sportid = ott.getSportid();
		this.itemid = ott.getItemid();
		this.playdate = ott.getPlaydate();
		this.citycode = ott.getCitycode();
		this.lowerprice = lowerprice;
		this.curprice = this.lowerprice;
		this.dupprice = dupprice;
		this.auctionprice = auctionprice;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.paidvalidtime = this.addtime;
		this.jointime = this.addtime;
		this.status = Status.Y;
		this.version = 0;
		this.joinnum = 0;
		this.opentime = this.addtime;
		this.lockStatus = OpenTimeTableConstant.SALE_STATUS_UNLOCK;
		this.otherinfo = "{}";
	}
	
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSportid() {
		return sportid;
	}

	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}

	public Long getFieldid() {
		return fieldid;
	}
	public void setFieldid(Long fieldid) {
		this.fieldid = fieldid;
	}
	public Long getOttid() {
		return ottid;
	}

	public void setOttid(Long ottid) {
		this.ottid = ottid;
	}

	public Integer getLowerprice() {
		return lowerprice;
	}

	public void setLowerprice(Integer lowerprice) {
		this.lowerprice = lowerprice;
	}

	public Integer getCurprice() {
		return curprice;
	}

	public void setCurprice(Integer curprice) {
		this.curprice = curprice;
	}

	public Integer getDupprice() {
		return dupprice;
	}

	public void setDupprice(Integer dupprice) {
		this.dupprice = dupprice;
	}

	public Integer getAuctionprice() {
		return auctionprice;
	}
	public void setAuctionprice(Integer auctionprice) {
		this.auctionprice = auctionprice;
	}
	public String getOtiids() {
		return otiids;
	}
	public void setOtiids(String otiids) {
		this.otiids = otiids;
	}
	public Date getPlaydate() {
		return playdate;
	}
	public void setPlaydate(Date playdate) {
		this.playdate = playdate;
	}
	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Timestamp getOpentime() {
		return opentime;
	}

	public void setOpentime(Timestamp opentime) {
		this.opentime = opentime;
	}

	public Timestamp getClosetime() {
		return closetime;
	}

	public void setClosetime(Timestamp closetime) {
		this.closetime = closetime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(String lockStatus) {
		this.lockStatus = lockStatus;
	}
	public String getBindInd() {
		return bindInd;
	}
	public void setBindInd(String bindInd) {
		this.bindInd = bindInd;
	}
	public Long getGuaranteeid() {
		return guaranteeid;
	}
	public void setGuaranteeid(Long guaranteeid) {
		this.guaranteeid = guaranteeid;
	}

	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public Timestamp getPaidvalidtime() {
		return paidvalidtime;
	}
	public void setPaidvalidtime(Timestamp paidvalidtime) {
		this.paidvalidtime = paidvalidtime;
	}
	public Timestamp getJointime() {
		return jointime;
	}
	public void setJointime(Timestamp jointime) {
		this.jointime = jointime;
	}
	public Integer getJoinnum() {
		return joinnum;
	}
	public void setJoinnum(Integer joinnum) {
		this.joinnum = joinnum;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	
	public boolean hasSoon(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return opentime.after(cur);
	}
	
	public boolean hasBooking(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return cur.after(this.opentime) && cur.before(this.closetime) 
				&& StringUtils.equals(this.status, Status.Y)
				&& !StringUtils.startsWith(this.lockStatus, OpenTimeTableConstant.SALE_STATUS_SUCCESS);
	}
	
	public boolean hasSuccess(){
		return StringUtils.startsWith(this.lockStatus, OpenTimeTableConstant.SALE_STATUS_SUCCESS);
	}
	
	public boolean hasSuccessPaid(){
		return StringUtils.equals(this.lockStatus, OpenTimeTableConstant.SALE_STATUS_SUCCESS_PAID);
	}
	
	public boolean hasClose(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return cur.after(this.closetime);
	}
	
	public boolean hasEnd(){
		return hasClose() || hasSuccess();
	}
	
	public boolean hasEnd2(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp endt = DateUtil.addMinute(this.closetime, 5);
		return endt.after(cur);
	}
	
	public boolean hasEnd3(Timestamp cur){
		if(cur == null){
			cur = DateUtil.getCurFullTimestamp();
		}
		Timestamp t = DateUtil.addSecond(cur, -2);
		return t.after(this.closetime);
	}
	
	public boolean hasLockStatus(String lock){
		if(StringUtils.isBlank(lock)) return false;
		return StringUtils.equals(this.lockStatus, lock);
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
}
