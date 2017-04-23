package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class OpenDramaItem extends BaseObject {
	private static final long serialVersionUID = 1060512664419218341L;
	
	private Long id;
	private Long dpid;
	private Long dramaid;
	private String dramaname;
	private String name;			//场次名称
	private Long theatreid;
	private String theatrename;
	private Long roomid;
	private String roomname;
	private Timestamp playtime;		//演出时间
	private Timestamp endtime;		//演出结束时间
	private String language;
	private String status;
	private String partner;			//合作伙伴开放状态：Y对外开放,N不对外开放
	private Timestamp opentime;
	private Timestamp closetime;
	private Timestamp updatetime;	//更新时间
	private String opentype;		//开放类型：选座，价格，
	private String elecard;
	
	private Integer minpoint;		//使用积分下限
	private Integer maxpoint;		//使用积分上限
	private Integer maxbuy;			//购票限制
	private Integer msgMinute;		//短信提前发送时间(分钟)
	
	private Long topicid;			//取票帖子
	private String takemethod;		//取票方式
	private String greetings;		//是否支持文字票面功能(Y,N)
	private String takemsg;			//取票描述
	private String buylimit;		//购买张数限制，1,2,3,4,5
	private String notifymsg1;		//取票短信
	private String notifymsg2;		//提前3小时提醒短信
	private String notifymsg3;		//提前一天提醒短信
	private String notifyRemark;	//取票短信(快递)
	private String remark;			//描述
	private String seatlink;		//座位图链接
	private String otherinfo;
	private String spflag;
	private String citycode;
	private String expressid;		//配送方式id
	private String ticketfaceid;	//票面信息
	private String barcode;			
	private String period;			//是否固定时间
	private String seller;			//第三方类型：GEWA,GPTBS
	private String sellerseq;		//第三方场次编号
	private String print;
	private Integer sortnum;		//排序字段
	private String saleCycle;		//预售周期
	
	private Integer eticketHour;		//（A,E 默认电子票时间(小时))
	private Integer eticketWeekHour;	//（A,E 默认电子票时间周末(小时))
	
	private Integer seatnum;		//座位数量
	private Integer asellnum;		//allow 允许卖出数
	private Integer gsellnum;		//Gewa卖出数
	private Integer csellnum;		//影院卖出
	private Integer locknum;		//Gewa锁定数
	
	public OpenDramaItem() {}
	public OpenDramaItem(Theatre theatre, Drama drama, DramaPlayItem item, TheatreProfile profile) {
		this.status = OdiConstant.STATUS_NOBOOK;
		this.name = item.getName();
		this.partner = Status.Y;
		this.print = Status.Y;
		this.greetings = Status.N;
		this.dpid = item.getId();
		this.dramaid = item.getDramaid();
		this.dramaname = drama.getRealBriefname();
		this.theatreid = item.getTheatreid();
		this.theatrename = theatre.getRealBriefname();
		this.citycode = theatre.getCitycode();
		this.roomid = item.getRoomid();
		this.roomname = item.getRoomname();
		this.playtime = item.getPlaytime();
		this.endtime = item.getEndtime();
		this.language = item.getLanguage();
		this.period = item.getPeriod();
		this.opentype = item.getOpentype();
		this.seller = item.getSeller();
		this.sellerseq = item.getSellerseq();
		this.msgMinute = OdiConstant.SEND_MSG_3H;
		if(item.isOpenseat()){
			this.maxbuy = OdiConstant.MAX_BUY;
		}else{
			this.maxbuy = OdiConstant.ODI_MAX_BUY;
		}
		this.opentime = DateUtil.addMinute(new Timestamp(System.currentTimeMillis()), 30);
		this.closetime = DateUtil.addMinute(item.getPlaytime(), -OdiConstant.CLOSE_MIN);
		
		this.takemethod = profile.getTakemethod();
		this.takemsg = profile.getTakemsg();
		this.notifymsg1 = profile.getNotifymsg1();
		this.notifymsg2 = profile.getNotifymsg2();
		this.notifymsg3 = profile.getNotifymsg3();
		this.topicid = profile.getTopicid();
		this.notifyRemark = profile.getNotifyRemark();
		this.eticketHour = profile.getEticketHour();
		this.eticketWeekHour = profile.getEticketWeekHour();
		this.saleCycle = drama.getSaleCycle();
		this.minpoint = 500;
		this.maxpoint = 10000;
		this.sortnum = 1;
		
		this.seatnum = 0;
		this.asellnum = 0;
		this.gsellnum = 0;
		this.csellnum = 0;
		this.locknum = 0;
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

	public Long getDramaid() {
		return dramaid;
	}

	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}

	public String getDramaname() {
		return dramaname;
	}

	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}

	public Long getTheatreid() {
		return theatreid;
	}

	public void setTheatreid(Long theatreid) {
		this.theatreid = theatreid;
	}

	public String getTheatrename() {
		return theatrename;
	}

	public void setTheatrename(String theatrename) {
		this.theatrename = theatrename;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Timestamp getEndtime() {
		return endtime;
	}
	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}
	public String getSeller() {
		return seller;
	}
	public void setSeller(String seller) {
		this.seller = seller;
	}
	public String getSellerseq() {
		return sellerseq;
	}
	public void setSellerseq(String sellerseq) {
		this.sellerseq = sellerseq;
	}
	public Long getRoomid() {
		return roomid;
	}

	public void setRoomid(Long roomid) {
		this.roomid = roomid;
	}

	public String getRoomname() {
		return roomname;
	}

	public void setRoomname(String roomname) {
		this.roomname = roomname;
	}

	public Timestamp getPlaytime() {
		return playtime;
	}

	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Integer getMaxbuy() {
		return maxbuy;
	}
	public void setMaxbuy(Integer maxbuy) {
		this.maxbuy = maxbuy;
	}

	public String getExpressid() {
		return expressid;
	}
	public void setExpressid(String expressid) {
		this.expressid = expressid;
	}
	public String getTicketfaceid() {
		return ticketfaceid;
	}
	public void setTicketfaceid(String ticketfaceid) {
		this.ticketfaceid = ticketfaceid;
	}
	public Integer getGsellnum() {
		return gsellnum;
	}
	public void setGsellnum(Integer gsellnum) {
		this.gsellnum = gsellnum;
	}
	
	public Integer getSeatnum() {
		return seatnum;
	}
	public void setSeatnum(Integer seatnum) {
		this.seatnum = seatnum;
	}
	public Integer getAsellnum() {
		return asellnum;
	}
	public void setAsellnum(Integer asellnum) {
		this.asellnum = asellnum;
	}
	public Integer getCsellnum() {
		return csellnum;
	}
	public void setCsellnum(Integer csellnum) {
		this.csellnum = csellnum;
	}
	public Integer getLocknum() {
		return locknum;
	}
	public void setLocknum(Integer locknum) {
		this.locknum = locknum;
	}
	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public String getSpflag() {
		return spflag;
	}

	public void setSpflag(String spflag) {
		this.spflag = spflag;
	}

	public String getSeatlink() {
		return seatlink;
	}

	public void setSeatlink(String seatlink) {
		this.seatlink = seatlink;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getTopicid() {
		return topicid;
	}

	public void setTopicid(Long topicid) {
		this.topicid = topicid;
	}

	public String getTakemethod() {
		return takemethod;
	}

	public void setTakemethod(String takemethod) {
		this.takemethod = takemethod;
	}

	public String getTakemsg() {
		return takemsg;
	}

	public void setTakemsg(String takemsg) {
		this.takemsg = takemsg;
	}

	public Integer getMsgMinute() {
		return msgMinute;
	}
	
	public void setMsgMinute(Integer msgMinute) {
		this.msgMinute = msgMinute;
	}
	public String getBuylimit() {
		return buylimit;
	}

	public void setBuylimit(String buylimit) {
		this.buylimit = buylimit;
	}

	public String getNotifymsg1() {
		return notifymsg1;
	}

	public void setNotifymsg1(String notifymsg1) {
		this.notifymsg1 = notifymsg1;
	}

	public String getNotifymsg2() {
		return notifymsg2;
	}

	public void setNotifymsg2(String notifymsg2) {
		this.notifymsg2 = notifymsg2;
	}

	public String getNotifymsg3() {
		return notifymsg3;
	}

	public void setNotifymsg3(String notifymsg3) {
		this.notifymsg3 = notifymsg3;
	}

	public String getOpentype() {
		return opentype;
	}

	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public boolean isBooking() {
		return status.equals(OdiConstant.STATUS_BOOK) && !isClosed() && isOpen() && !isExpired();
	}
	public boolean isPartnerBooking() {
		return StringUtils.contains(status, OdiConstant.STATUS_BOOK) && !isClosed() && isOpen() && !isExpired() && isOpenPartner();
	}
	
	public boolean isExpired() {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return playtime.before(cur) && StringUtils.equals(this.period, Status.Y) 
			|| endtime.before(cur) && StringUtils.equals(this.period, Status.N) 
			|| StringUtils.equals(this.status, Status.DEL);
	}
	
	public boolean isSorted(){
		return isExpired() || isClosed();
	}
	
	public boolean isOpen(){
		if(opentime==null) return false;
		return opentime.before(new Timestamp(System.currentTimeMillis()));
	}
	public boolean isClosed() {
		if(closetime==null) return false;
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.after(closetime);
	}
	public boolean isOpenseat(){
		return OdiConstant.OPEN_TYPE_SEAT.equals(this.opentype);
	}
	public boolean isOpenprice(){
		return OdiConstant.OPEN_TYPE_PRICE.equals(this.opentype);
	}

	public String getElecard() {
		return elecard;
	}

	public void setElecard(String elecard) {
		this.elecard = elecard;
	}

	public String getGreetings() {
		return greetings;
	}
	public void setGreetings(String greetings) {
		this.greetings = greetings;
	}
	
	public boolean isOpenPointPay(){
		return maxpoint !=null && maxpoint > 0 ;
	}
	public boolean isOpenCardPay(){
		return StringUtils.containsAny(this.elecard, "ABD");
	}
	
	public boolean isDisCountPay(){
		return StringUtils.contains(this.elecard, "M");
	}
	
	public boolean hasDiscount(){
		return isOpenPointPay() || isDisCountPay() || isOpenCardPay();
	}
	
	public Long getDpid() {
		return dpid;
	}

	public void setDpid(Long dpid) {
		this.dpid = dpid;
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

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}
	public boolean isOpenPartner(){
		return StringUtils.equals(partner, Status.Y);
	}
	
	public Integer getEticketHour() {
		return eticketHour;
	}
	public void setEticketHour(Integer eticketHour) {
		this.eticketHour = eticketHour;
	}
	public Integer getEticketWeekHour() {
		return eticketWeekHour;
	}
	public void setEticketWeekHour(Integer eticketWeekHour) {
		this.eticketWeekHour = eticketWeekHour;
	}
	public String getCitycode(){
		return citycode;
	}
	public void setCitycode(String citycode){
		this.citycode = citycode;
	}
	public String getNotifyRemark() {
		return notifyRemark;
	}
	public void setNotifyRemark(String notifyRemark) {
		this.notifyRemark = notifyRemark;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public boolean isOpenBarcode(){
		return StringUtils.equals(barcode, Status.Y);
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	
	public Integer getSortnum() {
		return sortnum;
	}
	public void setSortnum(Integer sortnum) {
		this.sortnum = sortnum;
	}
	public boolean hasGewara(){
		return hasSeller(OdiConstant.PARTNER_GEWA);
	}
	public boolean hasSeller(String sell){
		if(StringUtils.isBlank(sell)) return false;
		return StringUtils.equals(this.seller, sell);
	}
	public String getPrint() {
		return print;
	}
	public void setPrint(String print) {
		this.print = print;
	}
	
	public String getSaleCycle() {
		return saleCycle;
	}
	public void setSaleCycle(String saleCycle) {
		this.saleCycle = saleCycle;
	}
	public boolean hasPeriod(String perod){
		if(StringUtils.isBlank(perod))return false;
		return StringUtils.equals(this.period, perod);
	}
	
	public boolean hasUnOpenToGewa(){
		return StringUtils.contains(otherinfo, OpiConstant.UNOPENGEWA);
	}
	public boolean hasUnShowToGewa(){
		return hasOnlyUnShowToGewa() || hasUnOpenToGewa();
	}
	
	public boolean hasOnlyUnShowToGewa(){
		return StringUtils.contains(otherinfo, OpiConstant.UNSHOWGEWA);
	}
	
	public String gainItemName(){
		if(hasPeriod(Status.Y)){
			return DateUtil.format(this.playtime, "M月d日 HH:mm");
		}
		return this.name;
	}
	public Integer getRemainnum(){
		return this.seatnum - gsellnum - csellnum - locknum;
	}
}
