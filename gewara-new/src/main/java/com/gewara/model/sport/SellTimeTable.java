/**
 * 
 */
package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class SellTimeTable extends BaseObject{
	private static final long serialVersionUID = 2962568400914014901L;
	public static final String STATUS_NEW = "A";		//新座位
	public static final String STATUS_SELLING = "W";//售出未付款
	public static final String STATUS_SOLD = "S_GW";	//售出
	private Long id;
	private Integer version;		//版本
	private Long ottid;				//关联场次
	private Long otiid;	
	private String starttime;		//开始时间
	private String endtime;			//结束时间
	private Timestamp validtime;	//有效时间
	private Integer unitMinute;		//单位时段
	private Integer sumMinute;			//总时段
	private Integer price;			//价格
	private Integer costprice;		//成本价
	private Integer sportprice;		//场馆价
	private Integer quantity;		//人数
	private String status;			//状态
	private String remark;			//记录
	private Long fieldid;
	public SellTimeTable(){}
	public SellTimeTable(Long orderid, OpenTimeItem oti, Timestamp validtime){
		this.id = orderid;
		this.version = 0;
		this.status = STATUS_NEW;
		this.validtime = validtime;
		copyFrom(oti);
	}
	public void copyFrom(OpenTimeItem oti){
		this.ottid = oti.getOttid();
		this.price = oti.getPrice();
		this.costprice = oti.getCostprice();
		this.sportprice = oti.getNorprice();
		this.unitMinute = oti.getUnitMinute();
		this.fieldid = oti.getFieldid();
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getOttid() {
		return ottid;
	}
	public void setOttid(Long ottid) {
		this.ottid = ottid;
	}

	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public Integer getSportprice() {
		return sportprice;
	}
	public void setSportprice(Integer sportprice) {
		this.sportprice = sportprice;
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
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getUnitMinute() {
		return unitMinute;
	}
	public void setUnitMinute(Integer unitMinute) {
		this.unitMinute = unitMinute;
	}
	public Integer getSumMinute() {
		return sumMinute;
	}
	public void setSumMinute(Integer sumMinute) {
		this.sumMinute = sumMinute;
	}
	public boolean isAvailable() {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return status.equals(STATUS_NEW) && validtime.before(cur);
	}
	public boolean isSold(){
		return STATUS_SOLD.equals(status);
	}
	public boolean isWait(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return (status.equals(STATUS_NEW) && validtime.after(cur));
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}

	public Long getFieldid() {
		return fieldid;
	}
	public void setFieldid(Long fieldid) {
		this.fieldid = fieldid;
	}
	public Long getOtiid() {
		return otiid;
	}
	public void setOtiid(Long otiid) {
		this.otiid = otiid;
	}
}
