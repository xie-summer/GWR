package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * 影院结账记录
 * @author gebiao(ge.biao@gewara.com)
 * @since Sep 3, 2012 11:09:18 PM
 */
public class CinemaSettle extends BaseObject{
	private static final long serialVersionUID = 8597217472925580238L;
	private Long id;
	private Long cinemaid;
	private Timestamp timefrom;			//下单时间
	private Timestamp timeto;			//下单结束时间
	private Timestamp lasttime;			//上期结账时间  用于退款增补
	private Timestamp curtime;			//本期结账时间
	private Timestamp nexttime;			//预计下期时间，用于未结账显示
	private Integer amount;				//本期结账金额
	private Integer lastOrderRefund;	//上期订单退款差额
	private Integer curOrderRefund;		//本期订单退款差额
	private Integer adjustment;			//手工调整金额：正  增加结算，负：减少结算金额
	private String status;				//状态：Y：已经结算，N：未结算
	private String remark;
	public CinemaSettle() {
		
	}
	public CinemaSettle(Long cinemaid, Timestamp timefrom, Timestamp timeto, Timestamp lasttime, Timestamp curtime) {
		this.cinemaid = cinemaid;
		this.timefrom = timefrom;
		this.timeto = timeto;
		this.lasttime = lasttime;
		this.curtime = curtime;
		this.amount = 0;
		this.lastOrderRefund = 0;
		this.curOrderRefund = 0;
		this.status = "N";
		this.adjustment = 0;
	}
	public CinemaSettle(CinemaSettle last, Timestamp timeto, Timestamp curtime) {
		this(last.getCinemaid(), last.getTimeto(), timeto, last.getCurtime(), curtime);
	}
	public Long getCinemaid() {
		return cinemaid;
	}
	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
	public Timestamp getTimefrom() {
		return timefrom;
	}
	public void setTimefrom(Timestamp timefrom) {
		this.timefrom = timefrom;
	}
	public Timestamp getTimeto() {
		return timeto;
	}
	public void setTimeto(Timestamp timeto) {
		this.timeto = timeto;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public Timestamp getLasttime() {
		return lasttime;
	}
	public void setLasttime(Timestamp lasttime) {
		this.lasttime = lasttime;
	}
	public Timestamp getCurtime() {
		return curtime;
	}
	public void setCurtime(Timestamp curtime) {
		this.curtime = curtime;
	}
	public Integer getLastOrderRefund() {
		return lastOrderRefund;
	}
	public void setLastOrderRefund(Integer lastOrderRefund) {
		this.lastOrderRefund = lastOrderRefund;
	}
	public Integer getCurOrderRefund() {
		return curOrderRefund;
	}
	public void setCurOrderRefund(Integer curOrderRefund) {
		this.curOrderRefund = curOrderRefund;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getAdjustment() {
		return adjustment;
	}
	public void setAdjustment(Integer adjustment) {
		this.adjustment = adjustment;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Timestamp getNexttime() {
		return nexttime;
	}
	public void setNexttime(Timestamp nexttime) {
		this.nexttime = nexttime;
	}
	
}
