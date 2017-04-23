package com.gewara.model.goods;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;

public class SportGoods extends BaseGoods {

	private static final long serialVersionUID = 1643611271137177353L;
	private Integer upperlimit;
	private Integer lowerlimit;
	private String hours;

	public SportGoods(){}
	
	public SportGoods(String goodsname, Integer unitprice, Long clerkid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.goodsname = goodsname;
		this.unitprice = unitprice;
		this.limitnum = 0;
		this.quantity = 0;
		this.allowaddnum = 0;
		this.maxbuy = 4;
		this.maxpoint = 0;
		this.minpoint = 0;
		this.goodssort = 0;
		this.clerkid = clerkid;
		this.deliver = "N";
		this.costprice = 0;
		this.status = "Y";
		this.period = GoodsConstant.PERIOD_N;
		this.msgMinute = OdiConstant.SEND_MSG_3H;
		this.clickedtimes = 1;
	}

	public Integer getLowerlimit() {
		return lowerlimit;
	}

	public void setLowerlimit(Integer lowerlimit) {
		this.lowerlimit = lowerlimit;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public Integer getUpperlimit() {
		return upperlimit;
	}

	public void setUpperlimit(Integer upperlimit) {
		this.upperlimit = upperlimit;
	}

	@Override
	public String getGoodstype() {
		return GoodsConstant.GOODS_TYPE_SPORT;
	}

	@Override
	public boolean hasBooking() {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.after(fromtime) && cur.before(totime)
				&& StringUtils.equals(this.status, Status.Y);
	}
}
