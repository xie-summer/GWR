package com.gewara.model.goods;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class GoodsPrice extends BaseObject{
	private static final long serialVersionUID = 4488601662449254057L;
	public static String[] disallowBindField = new String[]{//不允许绑定的字段
		"quantity", "sellquantity", "sellordernum", "allowaddnum"
	};
	private Long id;
	private Long goodsid;
	private String pricelevel;
	private Integer price;
	private Integer costprice;
	private Integer oriprice;
	private String section;				//区域
	private String status;
	private String remark;
	private Integer quantity;			//拿票数量
	private Integer allowaddnum;		//最大订单名额
	private Integer sellquantity;		//卖出数量
	private Integer sellordernum;		//卖出订单笔数
	private Timestamp updatetime;		//更新时间
	private Integer version;
	
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
	public GoodsPrice(){
		
	}
	public GoodsPrice(Long goodsid){
		this.goodsid = goodsid;
		this.updatetime = new Timestamp(System.currentTimeMillis());
		this.sellquantity = 0;
		this.sellordernum = 0;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}
	
	public String getPricelevel() {
		return pricelevel;
	}
	public void setPricelevel(String pricelevel) {
		this.pricelevel = pricelevel;
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
		return this.oriprice;
	}
	public Timestamp getUpdatetime(){
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime){
		this.updatetime = updatetime;
	}
	public static List<String> getSeatTypeList(){
		List<String> list = new ArrayList<String>();
		list.add("A"); list.add("B"); list.add("C"); list.add("D"); 
		list.add("E"); list.add("F"); list.add("G"); list.add("H"); 
		list.add("I"); list.add("J"); list.add("K"); list.add("L"); 
		list.add("M"); 
		return list;
	}
	
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	public Integer getAllowaddnum() {
		return allowaddnum;
	}
	public void setAllowaddnum(Integer allowaddnum) {
		this.allowaddnum = allowaddnum;
	}
	public Integer getSellquantity() {
		return sellquantity;
	}
	public void setSellquantity(Integer sellquantity) {
		this.sellquantity = sellquantity;
	}
	public Integer getSellordernum() {
		return sellordernum;
	}
	public void setSellordernum(Integer sellordernum) {
		this.sellordernum = sellordernum;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public boolean hasBooking(){
		return StringUtils.equals(this.status, Status.Y)
			&& quantity > sellquantity && allowaddnum > 0;
	}
	
}
