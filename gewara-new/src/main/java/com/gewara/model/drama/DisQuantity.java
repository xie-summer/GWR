/**
 * 
 */
package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Administrator
 *
 */
public class DisQuantity extends BaseObject{
	private static final long serialVersionUID = -1529572575971715349L;
	private Long id;
	private Long dpid;
	private Long tspid;
	private Long areaid;
	private Integer quantity;
	private Integer price;
	private Integer costprice;
	private Integer theatreprice;
	private Integer version;
	private Integer maxbuy;				//单次购票数量
	private Integer tickettotal;		//库存票数
	private Integer allownum;			//剩余库存票数
	private Integer sellordernum;		//已卖出的票数
	private Timestamp addtime;
	private Timestamp updatetime;
	private String distype;				//优惠类型 G(格瓦拉的优惠) P(主办方优惠)
	private Long settleid;				//结算比率
	
	private String name;
	private Timestamp starttime;		//套票开始时间
	private Timestamp endtime;			//套票结束时间
	private String retail;				//是否零售
	private String status;				//是否可卖
	private String seller;				//
	private String sispseq;				//
	
	public DisQuantity(){}
	public DisQuantity(TheatreSeatPrice seatPrice, Integer quantity, String distype){
		this.tspid = seatPrice.getId();
		this.dpid = seatPrice.getDpid();
		this.areaid = seatPrice.getAreaid();
		this.quantity = quantity;
		this.price = 0;
		this.costprice = 0;
		this.version = 0;
		this.sellordernum = 0;
		this.maxbuy = OdiConstant.MAX_BUY;
		this.distype = distype;
		this.tickettotal = 0;
		this.allownum = tickettotal;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.starttime = this.addtime;
		this.seller = OdiConstant.PARTNER_GEWA;
		this.status = Status.Y;
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
	public Long getTspid() {
		return tspid;
	}
	public void setTspid(Long tspid) {
		this.tspid = tspid;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public Integer getTheatreprice() {
		return theatreprice;
	}
	public void setTheatreprice(Integer theatreprice) {
		this.theatreprice = theatreprice;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Integer getTickettotal() {
		return tickettotal;
	}
	public void setTickettotal(Integer tickettotal) {
		this.tickettotal = tickettotal;
	}
	public Integer getAllownum() {
		return allownum;
	}
	public void setAllownum(Integer allownum) {
		this.allownum = allownum;
	}
	public Integer getSellordernum() {
		return sellordernum;
	}
	public void setSellordernum(Integer sellordernum) {
		this.sellordernum = sellordernum;
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
	public Long getDpid() {
		return dpid;
	}
	public void setDpid(Long dpid) {
		this.dpid = dpid;
	}
	public Long getAreaid() {
		return areaid;
	}
	public void setAreaid(Long areaid) {
		this.areaid = areaid;
	}
	public String getDistype() {
		return distype;
	}
	public void setDistype(String distype) {
		this.distype = distype;
	}
	
	public Long getSettleid() {
		return settleid;
	}
	public void setSettleid(Long settleid) {
		this.settleid = settleid;
	}
	public Integer getMaxbuy() {
		return maxbuy;
	}
	public void setMaxbuy(Integer maxbuy) {
		this.maxbuy = maxbuy;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Timestamp getStarttime() {
		return starttime;
	}
	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}
	public Timestamp getEndtime() {
		return endtime;
	}
	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}
	public String getRetail() {
		return retail;
	}
	public void setRetail(String retail) {
		this.retail = retail;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	
	public boolean hasBooking(){
		Timestamp cur = DateUtil.getCurFullTimestamp();
		return cur.after(starttime) && cur.before(endtime)
				&& StringUtils.equals(this.status, Status.Y);
	}
	
	public boolean hasStatus(String stats){
		if(StringUtils.isBlank(stats)) return false;
		return StringUtils.equals(this.status, stats);
	}
	
	public boolean hasRetail(){
		return StringUtils.equals(this.getRetail(), Status.Y);
	}
	
	public boolean hasSeller(String sell){
		if(StringUtils.isBlank(sell)) return false;
		return StringUtils.equals(this.seller, sell);
	}
}
