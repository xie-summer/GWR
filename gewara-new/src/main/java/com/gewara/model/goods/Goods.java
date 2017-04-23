package com.gewara.model.goods;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
/**
 * @author acerge(acerge@163.com)
 * @since 5:48:03 PM Nov 3, 2009
 * –Èƒ‚…Ã∆∑
 */
public class Goods extends BaseGoods{
	private static final long serialVersionUID = 3430744654621798259L;
	public Goods(){}
	public Goods(String goodsname, Integer unitprice, Long clerkid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.goodsname = goodsname;
		this.unitprice = unitprice;
		this.limitnum = 0;
		this.quantity = 0;
		this.allowaddnum = 0;
		this.maxbuy = 5;
		this.maxpoint = 0;
		this.minpoint = 0;
		this.goodssort = 0;
		this.clerkid = clerkid;
		this.deliver = "N";
		this.costprice = 0;
		this.status = "Y";
		this.sales = 0;
		this.period = GoodsConstant.PERIOD_N;
		this.msgMinute = OdiConstant.SEND_MSG_3H;
		this.clickedtimes = 1;
	}
	
	@Override
	public String getGoodstype() {
		return GoodsConstant.GOODS_TYPE_GOODS;
	}
	@Override
	public boolean hasBooking() {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.after(fromtime) && cur.before(totime)
				&& StringUtils.equals(this.status, Status.Y);
	}
}
