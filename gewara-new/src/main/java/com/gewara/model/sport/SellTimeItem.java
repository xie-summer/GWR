/**
 * 
 */
package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class SellTimeItem extends BaseObject{
	private static final long serialVersionUID = 2962568400914014901L;
	public static final String STATUS_NEW = "A";		//新座位
	public static final String STATUS_SELLING = "W";//售出未付款
	public static final String STATUS_SOLD = "S_GW";	//售出
	private Long id;
	private Integer version;		//版本
	private Long ottid;				//关联场次
	private Long orderid;			//订单号
	private String hour;				//小时
	private Long fieldid;			//场地ID
	private Timestamp validtime;	//有效时间
	private Integer price;			//价格
	private Integer costprice;		//成本价
	private String status;			//状态
	private String remark;			//记录
	public SellTimeItem(){}
	public SellTimeItem(OpenTimeItem oti, Timestamp validtime){
		this.version = 0;
		this.status = STATUS_NEW;
		this.validtime = validtime;
		copyFrom(oti);
	}
	public void copyFrom(OpenTimeItem oti){
		this.id = oti.getId();
		this.ottid = oti.getOttid();
		this.fieldid = oti.getFieldid();
		this.hour = oti.getHour();
		this.price = oti.getPrice();

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
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
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
	public Long getFieldid() {
		return fieldid;
	}
	public void setFieldid(Long fieldid) {
		this.fieldid = fieldid;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
}
