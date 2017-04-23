package com.gewara.model.agency;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.model.goods.BaseGoods;
import com.gewara.util.DateUtil;

public class TrainingGoods extends BaseGoods {
	
	private static final long serialVersionUID = 2965295573468497745L;
	private String placename;				//运动场地名称
	private Long placeid;						//运动场地ID
	private String fitcrowd;				//适宜人群
	private String timetype;				//时间类型 1.周末班 2.夜间班 3.全日班 4.寒暑班
	private Integer minquantity;			//最小报名人数
	
	public TrainingGoods(){}
	public TrainingGoods(String tag, Long relatedid, String itemtype, Long itemid){
		this.tag = tag;
		this.relatedid = relatedid;
		this.itemtype = itemtype;
		this.itemid = itemid;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.limitnum = 0;
		this.unitprice = 0;
		this.maxbuy = 5;
		this.maxpoint = 0;
		this.minpoint = 0;
		this.goodssort = 1;
		this.costprice = 0;
		this.status = Status.N;
		this.sales = 0;
		this.clickedtimes = 1;
		this.period = GoodsConstant.PERIOD_N;
		this.msgMinute = OdiConstant.SEND_MSG_3H;
	}
	@Override
	public String getGoodstype() {
		return GoodsConstant.GOODS_TYPE_TRAINING;
	}

	public String getFitcrowd() {
		return fitcrowd;
	}

	public void setFitcrowd(String fitcrowd) {
		this.fitcrowd = fitcrowd;
	}

	public String getTimetype() {
		return timetype;
	}

	public void setTimetype(String timetype) {
		this.timetype = timetype;
	}

	public Integer getMinquantity() {
		return minquantity;
	}
	public void setMinquantity(Integer minquantity) {
		this.minquantity = minquantity;
	}
	@Override
	public boolean hasBooking() {
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return fromtime.before(cur) && totime.after(cur) && StringUtils.equals(this.status, Status.Y);
	}
	public String getPlacename() {
		return placename;
	}
	public void setPlacename(String placename) {
		this.placename = placename;
	}
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}

}
