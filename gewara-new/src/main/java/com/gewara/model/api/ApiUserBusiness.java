package com.gewara.model.api;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class ApiUserBusiness extends BaseObject{
	private static final long serialVersionUID = 1570825734279704022L;
	private Long id;
	private String showModel;		//展现方式 android ios wap pc 终端机、电视机
	private String coopModel;		//合作模式
	private String createOrder;		//是否产生订单
	private String moneyto;			//收款方
	private String gewaBusUser;		//格瓦拉商务负责人
	private String gewaTecUser;		//格瓦拉技术负责人
	private String partnerBusUser;	//合作商商务联系人
	private String partnerTecUser;	//合作商技术联系人
	private Timestamp onTime;		//上线日期
	private Timestamp offTime;		//下线日期
	private String webSite;			//线上地址
	private String remark;
	//合作模式:	1.纯API调用 支付方式是合作商 如IPTV、终端机、QQ
	//		   	2.选择影院和影片场次是API，选择座位是iframe嵌入 支付方式是合作商 如taobao
	//   		3.选择影院和影片场次是API，选择座位是iframe嵌入 支付方式是格拉瓦 srcbshop
	//			4.纯iframe嵌入 支付方式是合作商
	//			5.纯iframe嵌入 支付方式是格瓦拉
	// 			6.选择影院和影片是API，选择购票调转到格瓦拉
	public ApiUserBusiness(){
		
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

	public String getShowModel() {
		return showModel;
	}

	public void setShowModel(String showModel) {
		this.showModel = showModel;
	}

	public String getCoopModel() {
		return coopModel;
	}

	public void setCoopModel(String coopModel) {
		this.coopModel = coopModel;
	}

	public String getCreateOrder() {
		return createOrder;
	}

	public void setCreateOrder(String createOrder) {
		this.createOrder = createOrder;
	}

	public String getMoneyto() {
		return moneyto;
	}

	public void setMoneyto(String moneyto) {
		this.moneyto = moneyto;
	}

	public String getGewaBusUser() {
		return gewaBusUser;
	}

	public void setGewaBusUser(String gewaBusUser) {
		this.gewaBusUser = gewaBusUser;
	}

	public String getGewaTecUser() {
		return gewaTecUser;
	}

	public void setGewaTecUser(String gewaTecUser) {
		this.gewaTecUser = gewaTecUser;
	}

	public String getPartnerBusUser() {
		return partnerBusUser;
	}

	public void setPartnerBusUser(String partnerBusUser) {
		this.partnerBusUser = partnerBusUser;
	}

	public String getPartnerTecUser() {
		return partnerTecUser;
	}

	public void setPartnerTecUser(String partnerTecUser) {
		this.partnerTecUser = partnerTecUser;
	}

	public Timestamp getOnTime() {
		return onTime;
	}

	public void setOnTime(Timestamp onTime) {
		this.onTime = onTime;
	}

	public Timestamp getOffTime() {
		return offTime;
	}

	public void setOffTime(Timestamp offTime) {
		this.offTime = offTime;
	}

	public String getWebSite() {
		return webSite;
	}

	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
}
