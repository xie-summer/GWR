package com.gewara.model.partner;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.model.pay.GewaOrder;

/**
 * 用于回传数据给商家
 * @author acerge(acerge@163.com)
 * @since 5:54:35 PM May 29, 2010
 */
public class CallbackOrder extends BaseObject{
	private static final long serialVersionUID = 8747736594062412630L;
	public static final String STATUS_Y = "Y";	//成功回传
	public static final String STATUS_N = "N";	//没回传
	public static final String STATUS_F = "N_F";	//回传失败
	private Long orderid;
	private Long partnerid;			//商家ID
	private String status;			//回传状态
	private Integer calltimes;		//回传次数
	private Timestamp addtime;		//最后回传时间
	private Timestamp updatetime;	//最后回传时间
	
	public CallbackOrder(){
	}
	public CallbackOrder(GewaOrder order){
		this.orderid = order.getId();
		this.partnerid = order.getPartnerid();
		this.status = STATUS_N;
		this.calltimes = 0;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
	}
	@Override
	public Serializable realId() {
		return orderid;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public Long getPartnerid() {
		return partnerid;
	}
	public void setPartnerid(Long partnerid) {
		this.partnerid = partnerid;
	}
	public Integer getCalltimes() {
		return calltimes;
	}
	public void setCalltimes(Integer calltimes) {
		this.calltimes = calltimes;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public void addCalltimes() {
		this.calltimes ++;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public boolean isSuccess() {
		return STATUS_Y.equals(status);
	}
}
