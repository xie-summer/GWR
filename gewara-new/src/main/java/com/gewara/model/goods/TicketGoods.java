package com.gewara.model.goods;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.util.DateUtil;

public class TicketGoods extends BaseGoods {
	
	private static final long serialVersionUID = -286652420672400690L;
	private Long starid;
	private Long roomid;
	private String roomname;
	private String language;
	
	public TicketGoods(){}
	
	public TicketGoods(String tag, Long relatedid, String itemtype, Long itemid){
		this.tag = tag;
		this.relatedid = relatedid;
		this.itemtype = itemtype;
		this.itemid = itemid;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.limitnum = 0;
		this.allowaddnum = 0;
		this.unitprice = 0;
		this.quantity = 0;
		this.maxbuy = 5;
		this.maxpoint = 0;
		this.minpoint = 0;
		this.goodssort = 0;
		this.deliver = Status.N;
		this.costprice = 0;
		this.status = Status.N;
		this.sales = 0;
		this.msgMinute = OdiConstant.SEND_MSG_3H;
		this.clickedtimes = 1;
	}
	
	public String getCategory() {
		return itemtype;
	}

	public Long getCategoryid() {
		return itemid;
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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public Long getStarid() {
		return starid;
	}

	public void setStarid(Long starid) {
		this.starid = starid;
	}

	public boolean hasBooking(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return fromtime.before(cur) && totime.after(cur) 
			&& StringUtils.equals(this.status, Status.Y) && !hasExpired()
			&& ((this.hasPeriod()&& fromvalidtime.after(cur)) || (!this.hasPeriod()&& tovalidtime.after(cur)));
	}
	@Override
	public String getGoodstype() {
		return GoodsConstant.GOODS_TYPE_TICKET;
	}

}
