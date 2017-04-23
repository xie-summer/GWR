package com.gewara.model.goods;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class GoodsDisQuantity extends BaseObject {

	private static final long serialVersionUID = 7413926656886081456L;
	private Long id;
	private Long goodsid;
	private Long gspid;
	private Integer quantity;
	private Integer allownum;
	private Integer sellordernum;
	
	private Integer price;
	private Integer oriprice;
	private Integer costprice;
	private Integer version;
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public GoodsDisQuantity(){}
	
	public GoodsDisQuantity(Long gspid, Integer quantity, Integer price, Integer costprice, Integer oriprice){
		this.gspid = gspid;
		this.quantity = quantity;
		this.price = price;
		this.costprice = costprice;
		this.oriprice = oriprice;
		this.sellordernum = 0;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGspid() {
		return gspid;
	}

	public void setGspid(Long gspid) {
		this.gspid = gspid;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getOriprice() {
		return oriprice;
	}

	public void setOriprice(Integer oriprice) {
		this.oriprice = oriprice;
	}

	public Integer getCostprice() {
		return costprice;
	}

	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Long getGoodsid() {
		return goodsid;
	}

	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
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

	@Override
	public Serializable realId() {
		return id;
	}

}
