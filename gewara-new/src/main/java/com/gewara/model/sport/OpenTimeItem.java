package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

//类似影院座位
public class OpenTimeItem extends BaseObject {
	private static final long serialVersionUID = 4809769791023525466L;

	private Long id;
	private Long sportid;			//场馆ID
	private Long itemid;			//项目ID
	private Long ottid;				//场次ID
	private Long fieldid;			//场地ID
	private String fieldname;		//场地名
	private String hour;			//小时（11：00）
	private String endhour;			//结束时间
	private Long memberid;			//被谁占用
	private Integer price;			//价格
	private Integer costprice;		//成本价
	private Integer norprice;		//场馆价
	private String status;			//状态
	private Integer version;		//版本
	private Timestamp validtime;	//有效时间
	private Long rottid;				//远程场次ID
	private Long rfieldid;			//远程场地ID
	private Long rotiid;				//远程记录ID
	private String ikey;				//记录标识 sportid-itemid-playdate-fieldordernum-hour
	
	private String openType;				//	预订模式
	private Integer unitMinute;				//	单位时长,分钟
	private String unitType;				//	计价方式
	
	private Integer minpoint;				//	使用积分下限
	private Integer maxpoint;				//	使用积分上限
	
	private String spflag;					//	特殊活动标识
	private String elecard;					//	优惠券
	private String remark;					//	描述
	private String otherinfo;				//	其他信息
	
	private Integer quantity;
	private Integer sales;
	private String citycode;
	
	private String itemtype;				//场次类型：会员：1,竞拍：2
	private Integer auctionprice;
	private String bindInd;					//单卖：null;
	private String saleInd;					//竞价组合
	private Long otsid;
	
	private Long settleid;					//结算比率关联id
	private Integer upsetprice;			//保底价格
	
	public OpenTimeItem(){}

	public OpenTimeItem(Long ottid, ProgramItemTime pit){
		this.sportid = pit.getSportid();
		this.itemid = pit.getItemid();
		this.ottid = ottid;
		this.fieldid = pit.getFieldid();
		this.hour = pit.getStarttime();
		this.endhour = pit.getEndtime();
		this.price = pit.getPrice();
		this.auctionprice = pit.getPrice();
		this.costprice = pit.getCostprice();
		this.norprice = pit.getSportprice();
		this.unitMinute = pit.getUnitMinute();
		this.unitType = pit.getUnitType();
		this.openType = pit.getOpenType();
		this.citycode = pit.getCitycode();
		this.status = OpenTimeItemConstant.STATUS_NEW;
		this.validtime = new Timestamp(System.currentTimeMillis());
		
		this.minpoint = 0;
		this.maxpoint = 0;
		this.otherinfo = "{}";
		this.quantity = pit.getQuantity();
		this.sales = 0;
		
		this.upsetprice = 0;
	}
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
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
	public boolean hasAvailable() {//有效的场地
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return status.equals(OpenTimeItemConstant.STATUS_NEW) && validtime.before(cur);
	}
	
	public boolean hasStatusNew(){
		return hasStatus(OpenTimeItemConstant.STATUS_NEW);
	}
	public boolean hasLock(){
		return OpenTimeItemConstant.LOCKEDLIST.contains(status);
	}
	
	public boolean hasStatus(String stats){
		if(StringUtils.isBlank(stats)) return false;
		return StringUtils.equals(this.status, stats);
	}
	public boolean hasWait(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return (status.equals(OpenTimeItemConstant.STATUS_NEW) && validtime.after(cur));
	}
	public String getFieldname() {
		return fieldname;
	}
	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Integer getNorprice() {
		return norprice;
	}
	public void setNorprice(Integer norprice) {
		this.norprice = norprice;
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
	public Long getRottid() {
		return rottid;
	}
	public void setRottid(Long rottid) {
		this.rottid = rottid;
	}
	public Long getRfieldid() {
		return rfieldid;
	}
	public void setRfieldid(Long rfieldid) {
		this.rfieldid = rfieldid;
	}
	public Long getRotiid() {
		return rotiid;
	}
	public void setRotiid(Long rotiid) {
		this.rotiid = rotiid;
	}
	
	public String getEndhour() {
		return endhour;
	}
	public void setEndhour(String endhour) {
		this.endhour = endhour;
	}
	public String getSpflag() {
		return spflag;
	}
	public void setSpflag(String spflag) {
		this.spflag = spflag;
	}
	public String getOpenType() {
		return openType;
	}
	public void setOpenType(String openType) {
		this.openType = openType;
	}
	public Integer getUnitMinute() {
		return unitMinute;
	}
	public void setUnitMinute(Integer unitMinute) {
		this.unitMinute = unitMinute;
	}
	public String getUnitType() {
		return unitType;
	}
	public void setUnitType(String unitType) {
		this.unitType = unitType;
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
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getSales() {
		return sales;
	}
	public void setSales(Integer sales) {
		this.sales = sales;
	}
	public String getElecard() {
		return elecard;
	}
	public void setElecard(String elecard) {
		this.elecard = elecard;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String gainZhour(int minutes){
		Date curDate = DateUtil.currentTime();
		curDate = DateUtil.addMinute(curDate, minutes);
		return DateUtil.format(curDate, "HH:mm");
	}
	
	public boolean hasOver(int minutes){
		String date=gainZhour(minutes);
		return hour.compareTo(date)<0;
	}
	public boolean hasZeroPrice(){
		if(price==null || costprice==null || settleid==null || upsetprice==null) return true;
		if(price*costprice<=0) return true;
		return false;
	}
	public void setStatusByV2(String st) {
		if(StringUtils.equals(st, status)) return;
		else if(StringUtils.equals(st, OpenTimeItemConstant.STATUS_SOLD) && StringUtils.equals(status, OpenTimeItemConstant.STATUS_DELETE)) this.status = OpenTimeItemConstant.STATUS_SOLD;
		else if(StringUtils.equals(st, OpenTimeItemConstant.STATUS_LOCKD) || StringUtils.equals(st, OpenTimeItemConstant.STATUS_SOLD)) return;
		else if(StringUtils.equals(st, OpenTimeItemConstant.STATUS_NEW) && (StringUtils.equals(status, OpenTimeItemConstant.STATUS_LOCKL) || StringUtils.equals(status, OpenTimeItemConstant.STATUS_LOCKLF))) return;
		else if(StringUtils.equals(st, OpenTimeItemConstant.STATUS_LOCKR) && StringUtils.equals(status, OpenTimeItemConstant.STATUS_LOCKLF)) return;
		else if(StringUtils.equals(st, OpenTimeItemConstant.STATUS_DELETE) && hasAvailable()) this.status = OpenTimeItemConstant.STATUS_DELETE;
		else this.status = st;
	}
	public String getIkey() {
		return ikey;
	}
	public void setIkey(String ikey) {
		this.ikey = ikey;
	}
	
	public boolean hasOpentype(String type){
		if(StringUtils.isBlank(type)) return false;
		return StringUtils.equals(this.openType, type);
	}
	
	public boolean hasPeriod(){
		return hasOpentype(OpenTimeTableConstant.OPEN_TYPE_PERIOD);
	}
	public boolean hasField(){
		return hasOpentype(OpenTimeTableConstant.OPEN_TYPE_FIELD);
	}
	public boolean hasInning(){
		return hasOpentype(OpenTimeTableConstant.OPEN_TYPE_INNING);
	}
	public boolean hasUnitTime(){
		return hasUnittype(OpenTimeTableConstant.UNIT_TYPE_TIME);
	}
	
	public String getItemtype() {
		return itemtype;
	}
	public void setItemtype(String itemtype) {
		this.itemtype = itemtype;
	}
	
	public boolean hasItemtype(String type){
		if(StringUtils.isBlank(type)){
			return false;
		}
		return StringUtils.equals(this.itemtype, type);
	}
	
	public Integer getAuctionprice() {
		return auctionprice;
	}
	public void setAuctionprice(Integer auctionprice) {
		this.auctionprice = auctionprice;
	}
	public String getBindInd() {
		return bindInd;
	}
	public void setBindInd(String bindInd) {
		this.bindInd = bindInd;
	}
	
	public Long getOtsid() {
		return otsid;
	}
	public void setOtsid(Long otsid) {
		this.otsid = otsid;
	}
	public boolean hasBindInd(String ind){
		if(StringUtils.isBlank(ind)) return false;
		return StringUtils.equals(this.bindInd, ind);
	}
	//判断场地类型
	public boolean hasItemType(String itype){
		return StringUtils.equals(this.itemtype, itype);
	}
	public boolean needMemberCardPay(){
		return StringUtils.equals(this.itemtype, OpenTimeTableConstant.ITEM_TYPE_VIP);
	}
	
	public boolean hasUnittype(String type){
		if(StringUtils.isBlank(type)) return false;
		return StringUtils.equals(this.unitType, type);
	}
	
	public boolean hasUnitWhote(){
		return hasUnittype(OpenTimeTableConstant.UNIT_TYPE_WHOLE);
	}
	public String getSaleInd() {
		return saleInd;
	}
	public void setSaleInd(String saleInd) {
		this.saleInd = saleInd;
	}
	public Long getSettleid() {
		return settleid;
	}
	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}
	public Integer getUpsetprice() {
		return upsetprice;
	}
	public void setUpsetprice(Integer upsetprice) {
		this.upsetprice = upsetprice;
	}
	
	public String getOtiKey(){
		return this.fieldid + this.hour;
	}
}
