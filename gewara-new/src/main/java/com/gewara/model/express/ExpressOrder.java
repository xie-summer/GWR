package com.gewara.model.express;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.ticket.OrderExtraConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;
//配送的订单
public class ExpressOrder extends BaseObject {

	private static final long serialVersionUID = -8124166932412338199L;
	private String id;				
	private String expressnote;				//快递单号
	private String expressType;				//快递类型
	private Timestamp addtime;				//系统添加时间
	private Timestamp updatetime;			//更新时间
	private String status;					//快递单状态
	private String otherinfo;				//其它信息
	
	public ExpressOrder(){}
	
	public ExpressOrder(String expressnote, String expressType){
		this.id = expressType + "_" + expressnote;
		this.expressnote = expressnote;
		this.expressType = expressType;
		this.status = OrderExtraConstant.EXPRESS_STATUS_NEW;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.otherinfo = "{}";
	}
	
	@Override
	public Serializable realId() {
		return id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExpressnote() {
		return expressnote;
	}

	public void setExpressnote(String expressnote) {
		this.expressnote = expressnote;
	}

	public String getExpressType() {
		return expressType;
	}

	public void setExpressType(String expressType) {
		this.expressType = expressType;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public boolean hasStatus(String stats){
		return StringUtils.equals(this.status, stats);
	}
	
	public boolean hasExpressType(String type){
		return StringUtils.equals(this.expressType, type);
	}
}
