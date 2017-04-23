package com.gewara.model.pay;

import java.sql.Timestamp;
import java.util.Map;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.util.DateUtil;
public class GoodsOrder extends GewaOrder{
	private static final long serialVersionUID = -84016418448009250L;
	private Long goodsid;			//关联活动
	private Long placeid;			//关联场馆id
	private Long itemid;			//关联项目id
	private Map otherInfo;
	private Integer costprice;		//成本价
	public GoodsOrder(){
	}
	
	public GoodsOrder(Long memberid, String membername, BaseGoods goods){
		this.itemfee = 0;
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.addtime = createtime;
		this.updatetime = createtime;
		this.modifytime = createtime;
		this.paymethod = PaymethodConstant.PAYMETHOD_PNRPAY;
		this.validtime = DateUtil.addHour(this.addtime, 2);//默认2小时
		this.status = OrderConstant.STATUS_NEW;
		this.alipaid = 0;
		this.gewapaid = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.memberid = memberid;
		this.membername = membername;
		this.goodsid = goods.getId();
		this.unitprice = goods.getUnitprice();
		this.ordertitle = goods.getGoodsname();
		this.settle = OrderConstant.SETTLE_NONE;
		this.category = goods.getGoodstype();
		this.express = Status.N;
		this.otherinfo = "{}";
	}
	public GoodsOrder(TicketOrder order, BuyItem item, Timestamp validtime){
		this.mobile = order.getMobile();
		this.itemfee = order.getItemfee();
		this.totalfee = item.getDue();
		this.discount = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.createtime = order.getCreatetime();
		this.addtime = order.getAddtime();
		this.updatetime = order.getUpdatetime();
		this.modifytime = order.getModifytime();
		this.paidtime = order.getPaidtime();
		this.validtime = validtime;//有效时间+7天
		
		this.paymethod = order.getPaymethod();
		this.status = order.getStatus();
		this.alipaid = order.getAlipaid();
		this.gewapaid = order.getGewapaid();
		this.memberid = order.getMemberid();
		this.membername = order.getMembername();
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_MOVIE;
		
		this.unitprice = item.getUnitprice();
		this.quantity = item.getQuantity();
		this.ordertitle = item.getGoodsname();
		this.settle = OrderConstant.SETTLE_NONE;
		this.placeid = order.getCinemaid();
		this.express = Status.N;
	}
	public GoodsOrder(TicketOrder order, Goods goods, Timestamp validtime){
		this.mobile = order.getMobile();
		this.itemfee = 0;
		this.totalfee = 0;
		this.discount = 0;
		this.otherfee = 0;
		this.wabi = 0;
		this.addtime = order.getAddtime();
		this.updatetime = order.getUpdatetime();
		this.paidtime = order.getPaidtime();
		this.paymethod = order.getPaymethod();
		this.validtime = validtime;//有效时间+7天
		this.status = order.getStatus();
		this.alipaid = order.getAlipaid();
		this.gewapaid = order.getGewapaid();
		this.memberid = order.getMemberid();
		this.membername = order.getMembername();
		
		this.goodsid = goods.getId();
		this.unitprice = goods.getUnitprice();
		this.quantity = order.getQuantity();
		this.ordertitle = goods.getGoodsname();
		this.settle = OrderConstant.SETTLE_NONE;
		this.pricategory = OrderConstant.ORDER_PRICATEGORY_MOVIE;
		this.category = goods.getGoodstype();
		this.placeid = order.getCinemaid();
		this.express = Status.N;
	}
	public Integer getOrderAmount() {//实时获取总价
		return unitprice * quantity;
	}
	public Map getOtherInfo() {
		return otherInfo;
	}
	public void setOtherInfo(Map otherInfo) {
		this.otherInfo = otherInfo;
	}
	public Long getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}
	public String getOrdertype(){
		return "goods";
	}

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Long getPlaceid() {
		return placeid;
	}

	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
}
