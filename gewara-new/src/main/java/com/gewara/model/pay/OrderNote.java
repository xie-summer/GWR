package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OrderNoteConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class OrderNote extends BaseObject {

	private static final long serialVersionUID = -135038508746485007L;
	private Long id;
	private Long orderid;				//订单ID
	private String tradeno;				//订单号
	private String ordertype;			//订单类型
	private String mobile;				//手机号
	private String placename;			//场馆名称
	private String placetype;			//场馆类型
	private Long placeid;				//场馆ID
	private String itemname;			//项目名称
	private String itemtype;			//项目类型
	private Long itemid;				//项目ID
	private String checkpass;			//取票密码
	private Integer ticketnum;			//票数量
	private String smallitemtype;		//关联类型
	private Long smallitemid;			//关联对象(场次或物品)
	private Timestamp addtime;			//添加时间
	private Timestamp updatetime;		//更新时间
	private Timestamp validtime;		//有效时间
	private String message;				//短信内容
	
	private Timestamp playtime;			//放映时间
	private Timestamp taketime;			//取票时间
	private String status;
	private String serialno;			//流水号
	private Timestamp modifytime;		//一体机同步
	private String result;				//同步状态
	private String description;			//说明
	
	private String fromup;
	private String express;
	private String otherinfo;
	public OrderNote(){}
	public OrderNote(GewaOrder order){
		this.orderid = order.getId();
		this.tradeno = order.getTradeNo();
		this.ordertype = order.getOrdertype();
		this.mobile = order.getMobile();
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.modifytime = this.addtime;
		this.playtime = order.getPlaytime();
		this.status = OrderNoteConstant.STATUS_P;
		this.express = order.getExpress();
		this.description = "{}";
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

	public Long getOrderid() {
		return orderid;
	}
	
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	
	public String getTradeno() {
		return tradeno;
	}
	
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
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
	public String getPlacename() {
		return placename;
	}

	public void setPlacename(String placename) {
		this.placename = placename;
	}

	public String getPlacetype() {
		return placetype;
	}

	public void setPlacetype(String placetype) {
		this.placetype = placetype;
	}

	public Long getPlaceid() {
		return placeid;
	}

	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}

	public String getItemname() {
		return itemname;
	}

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}

	public String getItemtype() {
		return itemtype;
	}

	public void setItemtype(String itemtype) {
		this.itemtype = itemtype;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}

	public String getCheckpass() {
		return checkpass;
	}

	public void setCheckpass(String checkpass) {
		this.checkpass = checkpass;
	}

	public Integer getTicketnum() {
		return ticketnum;
	}

	public void setTicketnum(Integer ticketnum) {
		this.ticketnum = ticketnum;
	}

	public String getSmallitemtype() {
		return smallitemtype;
	}

	public void setSmallitemtype(String smallitemtype) {
		this.smallitemtype = smallitemtype;
	}

	public Long getSmallitemid() {
		return smallitemid;
	}

	public void setSmallitemid(Long smallitemid) {
		this.smallitemid = smallitemid;
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

	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String gainSynchtype(){
		if(StringUtils.equals(result, "U")) return "1";
		if(StringUtils.equals(result, "D")) return "2";
		return "0";
	}
	
	public Timestamp getModifytime() {
		return modifytime;
	}
	public void setModifytime(Timestamp modifytime) {
		this.modifytime = modifytime;
	}
	public String getSerialno() {
		return serialno;
	}
	public void setSerialno(String serialno) {
		this.serialno = serialno;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String gainSmsKey(){
		return tradeno + smallitemtype + smallitemid;
	}
	public Timestamp getTaketime() {
		return taketime;
	}
	public void setTaketime(Timestamp taketime) {
		this.taketime = taketime;
	}
	public String getFromup() {
		return fromup;
	}
	public void setFromup(String fromup) {
		this.fromup = fromup;
	}
	public Timestamp getPlaytime() {
		return playtime;
	}
	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
	}
	public String getExpress() {
		return express;
	}
	public void setExpress(String express) {
		this.express = express;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
}
