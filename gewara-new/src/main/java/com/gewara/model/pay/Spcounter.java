package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author gebiao(ge.biao@gewara.com)
 * @since May 29, 2012 3:23:18 PM
 */
public class Spcounter extends BaseObject{
	private static final long serialVersionUID = 3758832149177783157L;
	//特价活动计数控制
	public static final String CTLTYPE_ORDER = "order";			//根据订单控制
	public static final String CTLTYPE_QUANTITY = "quantity";	//根据数量控制
	private Integer version;
	private Long id;
	private String ctlmember;			//控制一组活动的用户唯一键规则：Y，N：各活动自己控制
	private String ctltype;				//控制类型：根据订单数 或 票数
	private Integer limitmaxnum;		//最大名额数量控制
	private Integer basenum;			//每期下单基控制
	private Integer allowaddnum;		//最大下单总数量控制

	private Timestamp periodtime;		//周期时间
	private Integer periodMinute;		//周期分钟
	
	private Integer sellquantity;		//当期卖出数量
	private Integer sellordernum;		//当期卖出订单笔数
	private Integer allquantity;		//总卖出数量		
	private Integer allordernum;		//总卖出订单数
	
	public Spcounter(){}
	
	public Spcounter(String ctltype){
		this.version = 0;
		this.ctltype = ctltype;
		this.periodtime = DateUtil.getCurTruncTimestamp();
		this.sellordernum = 0;
		this.sellquantity = 0;
		this.allquantity = 0;
		this.allordernum = 0;
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
	public String getCtltype() {
		return ctltype;
	}
	public void setCtltype(String ctltype) {
		this.ctltype = ctltype;
	}
	public Integer getLimitmaxnum() {
		return limitmaxnum;
	}
	public void setLimitmaxnum(Integer limitmaxnum) {
		this.limitmaxnum = limitmaxnum;
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
	public String getCtlmember() {
		return ctlmember;
	}
	public void setCtlmember(String ctlmember) {
		this.ctlmember = ctlmember;
	}
	public Integer getAllowaddnum() {
		return allowaddnum;
	}
	public void setAllowaddnum(Integer allowaddnum) {
		this.allowaddnum = allowaddnum;
	}
	public Timestamp getPeriodtime() {
		return periodtime;
	}
	public void setPeriodtime(Timestamp periodtime) {
		this.periodtime = periodtime;
	}
	public Integer getPeriodMinute() {
		return periodMinute;
	}
	public void setPeriodMinute(Integer periodMinute) {
		this.periodMinute = periodMinute;
	}
	public Integer getAllquantity() {
		return allquantity;
	}
	public void setAllquantity(Integer allquantity) {
		this.allquantity = allquantity;
	}
	public Integer getAllordernum() {
		return allordernum;
	}
	public void setAllordernum(Integer allordernum) {
		this.allordernum = allordernum;
	}
	public Integer getBasenum() {
		return basenum;
	}
	public void setBasenum(Integer basenum) {
		this.basenum = basenum;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
}
