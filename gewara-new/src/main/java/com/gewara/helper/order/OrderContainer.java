package com.gewara.helper.order;

import java.util.List;
import java.util.Map;

import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;

public class OrderContainer {
	protected GewaOrder order;
	protected List<Discount> discountList;
	protected List<SMSRecord> smsList;
	protected GoodsGift bindGift;
	protected Goods goods;
	protected SpecialDiscount sd;
	protected Spcounter spcounter;
	protected Discount curUsedDiscount;
	protected String msg;					//处理中返回的信息	
	protected Map<Long/*discountId*/, ElecCard> useCardMap;		//使用的电子券（部分接口使用）
	protected Integer costprice;
	public GewaOrder getOrder() {
		return order;
	}
	public void setOrder(GewaOrder order) {
		this.order = order;
	}
	public List<Discount> getDiscountList() {
		return discountList;
	}
	public void setDiscountList(List<Discount> discountList) {
		this.discountList = discountList;
	}
	public GoodsGift getBindGift() {
		return bindGift;
	}
	public void setBindGift(GoodsGift bindGift) {
		this.bindGift = bindGift;
	}
	public Goods getGoods() {
		return goods;
	}
	public void setGoods(Goods goods) {
		this.goods = goods;
	}
	public SpecialDiscount getSd() {
		return sd;
	}
	public void setSpdiscount(SpecialDiscount sd) {
		this.sd = sd;
	}
	public Discount getCurUsedDiscount() {
		return curUsedDiscount;
	}
	public void setCurUsedDiscount(Discount discount) {
		this.curUsedDiscount = discount;
	}
	public Spcounter getSpcounter() {
		return spcounter;
	}
	public void setSpcounter(Spcounter spcounter) {
		this.spcounter = spcounter;
	}
	public List<SMSRecord> getSmsList() {
		return smsList;
	}
	public void setSmsList(List<SMSRecord> smsList) {
		this.smsList = smsList;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Map<Long, ElecCard> getUseCardMap() {
		return useCardMap;
	}
	public void setUseCardMap(Map<Long, ElecCard> useCardMap) {
		this.useCardMap = useCardMap;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
}
