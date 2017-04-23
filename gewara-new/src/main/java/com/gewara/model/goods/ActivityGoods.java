package com.gewara.model.goods;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.util.DateUtil;

public class ActivityGoods extends BaseGoods {
	
	private static final long serialVersionUID = -1642103721400403504L;

	public ActivityGoods(){}
	
	public ActivityGoods(Long relatedid, String goodsname, Integer unitprice, Long clerkid){
		this.relatedid = relatedid;
		this.addtime = DateUtil.getCurFullTimestamp();
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
		this.deliver = Status.N;
		this.costprice = 0;
		this.status = Status.Y;
		this.tag = TagConstant.TAG_ACTIVITY;
		this.sales = 0;
		this.period = GoodsConstant.PERIOD_N;
		this.msgMinute = OdiConstant.SEND_MSG_3H;
		this.clickedtimes = 1;
	}
	
	@Override
	public String getGoodstype() {
		return GoodsConstant.GOODS_TYPE_ACTIVITY;
	}

	@Override
	public boolean hasBooking() {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.after(fromtime) && cur.before(totime)
				&& StringUtils.equals(this.status, Status.Y);
	}

}
