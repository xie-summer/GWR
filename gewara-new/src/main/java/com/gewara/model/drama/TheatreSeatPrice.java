package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class TheatreSeatPrice extends BaseObject{
	private static final long serialVersionUID = 4488601662449254057L;
	private Long id;
	private Long dramaid;
	private Long dpid;
	private Long areaid;				
	private Integer version;
	private String seattype;			
	private Integer price;				
	private Integer costprice;			
	private Integer theatreprice;		
	private String status;				
	private String remark;				
	private Integer maxbuy;				//单次购票数量
	private Integer quantity;			//剧院拿票数量
	private Integer allowaddnum;		//最大订单名额
	private Integer sales;				//卖出票数量
	private Integer sellordernum;		//卖出订单数量
	private Timestamp updatetime;		//更新时间
	private String seller;				//
	private String sispseq;				//第三方价格编号(ShowPrice-->sispseq)
	private String retail;				//是否可以零售
	private String showprice;			//是否展示在前台
	
	private Timestamp addtime;
	private Long settleid;				
	private Integer csellnum;			//场馆卖出数
	
	
	public TheatreSeatPrice(){}
	public TheatreSeatPrice(Long itemid, Long areaid, String seattype, Integer price, String seller){
		this.dpid = itemid;
		this.areaid = areaid;
		this.seattype = seattype;
		this.price = price;
		this.costprice = 0;
		this.theatreprice = 0;
		this.status = Status.Y;
		this.retail = Status.Y;
		this.showprice = Status.Y;
		this.sales = 0;
		this.maxbuy = OdiConstant.MAX_BUY;
		this.sellordernum = 0;
		this.csellnum = 0;
		this.seller = seller;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.version = 0;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getTheatreprice() {
		return theatreprice;
	}
	public void setTheatreprice(Integer theatreprice) {
		this.theatreprice = theatreprice;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Integer getAllowaddnum() {
		return allowaddnum;
	}
	public void setAllowaddnum(Integer allowaddnum) {
		this.allowaddnum = allowaddnum;
	}
	public Integer getSellordernum() {
		return sellordernum;
	}
	public void setSellordernum(Integer sellordernum) {
		this.sellordernum = sellordernum;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSeattype() {
		return seattype;
	}
	public Long getDramaid() {
		return dramaid;
	}
	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}
	public void setSeattype(String seattype) {
		this.seattype = seattype;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Integer getShowprice(boolean isBooking){
		if(isBooking) return this.price;
		return this.theatreprice;
	}
	
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getDpid() {
		return dpid;
	}
	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}
	public Timestamp getUpdatetime(){
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime){
		this.updatetime = updatetime;
	}
	
	public Integer getMaxbuy() {
		return maxbuy;
	}
	public void setMaxbuy(Integer maxbuy) {
		this.maxbuy = maxbuy;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getSales() {
		return sales;
	}
	public void setSales(Integer sales) {
		this.sales = sales;
	}
	
	public void addSales(int num){
		this.sales += num;
	}
	public Long getAreaid() {
		return areaid;
	}
	public void setAreaid(Long areaid) {
		this.areaid = areaid;
	}
	public String getSeller() {
		return seller;
	}
	public void setSeller(String seller) {
		this.seller = seller;
	}
	public String getSispseq() {
		return sispseq;
	}
	public void setSispseq(String sispseq) {
		this.sispseq = sispseq;
	}
	
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	
	public Long getSettleid() {
		return settleid;
	}
	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}
	public Integer getCsellnum() {
		return csellnum;
	}
	public void setCsellnum(Integer csellnum) {
		this.csellnum = csellnum;
	}
	public boolean hasBooking(){
		return hasStatus(Status.Y);
	}
	
	public boolean hasAllownum(){
		return quantity > (sales + csellnum) && allowaddnum > 0;
	}
	
	public String getRetail() {
		return retail;
	}
	public void setRetail(String retail) {
		this.retail = retail;
	}
	public String getShowprice() {
		return showprice;
	}
	public void setShowprice(String showprice) {
		this.showprice = showprice;
	}
	public boolean hasAllowBooking(){
		return hasBooking() && hasAllownum();
	}
	
	public boolean hasRetail(){
		return StringUtils.equals(this.retail, Status.Y);
	}
	
	public boolean hasShowPrice(){
		return StringUtils.equals(this.showprice, Status.Y);
	}
	
	public boolean hasStatus(String stats){
		if(StringUtils.isBlank(stats)){
			return false;
		}
		return StringUtils.equals(this.status, stats);
	}
}
