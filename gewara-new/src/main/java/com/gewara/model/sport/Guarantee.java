package com.gewara.model.sport;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class Guarantee extends BaseObject {
	private static final long serialVersionUID = -6472564510693501627L;
	public static String[] disallowBindField = new String[]{"citycode", "citycode", "addtime", "updatetime"};
	private Long id;						
	private String name;					//名称
	private Integer price;					//价格
	private Integer costprice;				//结算价格 
	private String otherinfo;				//其它信息
	private String status;					//状态
	private String citycode;				//城市编码
	private String remark;
	private String description;				//说明
	
	private Long createuser;				//增加人
	private Timestamp addtime;				//增加时间
	private Timestamp updatetime;			//更新时间
	private String ordermsg;				//订单短信
	
	@Override
	public Serializable realId() {
		return id;
	}

	public Guarantee(){}
	
	public Guarantee(Integer price){
		this.price = price;
		this.costprice = 0;
		this.status = Status.Y;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getCreateuser() {
		return createuser;
	}

	public void setCreateuser(Long createuser) {
		this.createuser = createuser;
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

	public String getOrdermsg() {
		return ordermsg;
	}

	public void setOrdermsg(String ordermsg) {
		this.ordermsg = ordermsg;
	}
	
}
