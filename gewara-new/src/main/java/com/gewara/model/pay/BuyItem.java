package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.order.BuyItemConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.goods.BaseGoods;

public class BuyItem extends BaseObject {
	private static final long serialVersionUID = 1769434405086656774L;
	private Long id;
	private Long orderid;
	private String checkpass;
	private Long memberid;
	private String tag;					//商品或场次类型
	private Long relatedid;				//商品或场次ID
	private String goodsname;
	private Integer quantity;
	private Integer unitprice;			//单价
	private String summary;
	private Timestamp validtime;
	private Timestamp addtime;
	
	private String bundle;				//是否赠品
	private Timestamp playtime;			//消费时间
	private Integer costprice;			//成本价
	private Integer oriprice;			//市场价、场馆价、原价
	private Integer totalcost;			//总成本价
	private Integer totalfee;			//商品总金额
	private Integer discount;			//商品优惠
	private String disreason;			//优惠理由
	private String remark;				//特别说明
	private String placetype;			//场馆类型
	private Long placeid;				//场馆ID
	private String itemtype;			//项目类型
	private Long itemid;				//项目ID
	private String otherinfo;			//其他信息
	private String citycode;			//城市代码
	private String description;			//商品描述
	private String smallitemtype;		//商品卖出方式(价格)
	private Long smallitemid;			//关联对象ID(如价格)
	private Long disid;					//优惠套票ID
	private Integer disfee;				//优惠套票
	private String express;				//是否快递
	
	private Long settleid;				//结算折扣
	
	private String fromup;
	
	public BuyItem(){}
	
	public BuyItem(Integer quantity){
		this.quantity = quantity;
		this.discount = 0;
		this.disfee = 0;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.otherinfo = "{}";
	}
	
	public BuyItem(Integer quantity, BaseGoods goods) {
		this(quantity);
		this.tag = BuyItemConstant.TAG_GOODS;
		this.relatedid = goods.getId();
		this.goodsname = goods.getGoodsname();
		this.unitprice = goods.getUnitprice();
		this.costprice = goods.getCostprice();
		this.oriprice = goods.getOriprice() == null? 0 : goods.getOriprice();
		this.summary = goods.getSummary();
		this.citycode = goods.getCitycode();
		this.express = StringUtils.isBlank(goods.getExpressid()) ? BuyItemConstant.EXPRESS_N : BuyItemConstant.EXPRESS_Y;
		
	}
	public BuyItem(BuyItem item) {
		this.memberid = item.getMemberid();
		this.tag = item.getTag();
		this.relatedid = item.getRelatedid();
		this.goodsname = item.getGoodsname();
		this.quantity = item.getQuantity();
		this.unitprice = item.getUnitprice();
		this.summary = item.getSummary();
		this.validtime = item.getValidtime();
		this.addtime = item.getAddtime();
		this.playtime = item.getPlaytime();
		this.costprice = item.getCostprice();
		this.oriprice = item.getOriprice();
		this.totalcost = item.getTotalcost();
		this.totalfee = item.getTotalfee();
		this.discount = item.getDiscount();
		this.disreason = item.getDisreason();
		this.remark = item.getRemark();
		this.placetype = item.getPlacetype();
		this.placeid = item.getPlaceid();
		this.itemtype = item.getItemtype();
		this.itemid = item.getItemid();
		this.otherinfo = item.getOtherinfo();
		this.citycode = item.getCitycode();
		this.description = item.getDescription();
		this.smallitemid = item.getSmallitemid();
		this.smallitemtype = item.getSmallitemtype();
		this.disid = item.getDisid();
		this.disfee = item.getDisfee();
		this.express = item.getExpress();
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
	public String getGoodsname() {
		return goodsname;
	}
	public void setGoodsname(String goodsname) {
		this.goodsname = goodsname;
	}
	
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getUnitprice() {
		return unitprice;
	}
	public void setUnitprice(Integer unitprice) {
		this.unitprice = unitprice;
	}

	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getCheckpass() {
		return checkpass;
	}
	public void setCheckpass(String checkpass) {
		this.checkpass = checkpass;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getGoodsid(){
		return getRelatedid();
	}
	
	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Timestamp getPlaytime() {
		return playtime;
	}

	public void setPlaytime(Timestamp playtime) {
		this.playtime = playtime;
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

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Integer getTotalcost() {
		return totalcost;
	}

	public void setTotalcost(Integer totalcost) {
		this.totalcost = totalcost;
	}

	public Integer getDiscount() {
		return discount;
	}

	public void setDiscount(Integer discount) {
		this.discount = discount;
	}

	public String getDisreason() {
		return disreason;
	}

	public void setDisreason(String disreason) {
		this.disreason = disreason;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getTotalfee() {
		return totalfee;
	}
	
	public void setTotalfee(Integer totalfee) {
		this.totalfee = totalfee;
	}

	public Integer getDue(){
		int due = totalfee - disfee - discount;
		return due < 0 ? 0 : due;
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

	public Long getDisid() {
		return disid;
	}

	public void setDisid(Long disid) {
		this.disid = disid;
	}

	public Integer getDisfee() {
		return disfee;
	}

	public void setDisfee(Integer disfee) {
		this.disfee = disfee;
	}

	public String getExpress() {
		return express;
	}

	public Integer getOriprice() {
		return oriprice;
	}

	public void setOriprice(Integer oriprice) {
		this.oriprice = oriprice;
	}

	public void setExpress(String express) {
		this.express = express;
	}

	public Long getSettleid() {
		return settleid;
	}

	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}

	public String getFromup() {
		return fromup;
	}

	public void setFromup(String fromup) {
		this.fromup = fromup;
	}
	
}
