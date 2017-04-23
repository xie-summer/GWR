package com.gewara.model.machine;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.gewara.model.BaseObject;

public class Machine extends BaseObject {
	public static final long serialVersionUID = 30718895092701938L;
	public static final String TYPE_MACHINE_NEW_HOST="newhost";//新主机
	public static final String TYPE_MACHINE_OLD_HOST="oldhost";//旧主机
	public static final String TYPE_MACHINE_LCD="lcd";//显示器
	public static final String TYPE_MACHINE_VPN="vpn";//VPN
	public static final String TYPE_MACHINE_INTEGRATION="integration";//一体机器
	public static final String TYPE_MACHINE_RPT = "rpt";//拖线板
	public static final String TYPE_MACHINE_EXCHANGE = "exchange";//交换机
	public static final String TYPE_MACHINE_ROUTER = "router";//路由器
	public static final String TYPE_MACHINE_NOTEBOOAK = "notebook"; //笔记本
	public static final String TYPE_MACHINE_3GCARD = "3gcard"; //3G上网卡
	public static final String TYPE_MACHINE_PHONE = "phone"; //移动座机
	public static final String TYPE_MACHINE_POS = "pos"; //pos机
	
	private Long id;
	private String machinenumber;
	private String machinename;
	private Long cinemaid;
	private Date hfhopendate;
	private Date leavedate;
	private String linkmethod;
	private String touchtype;
	private Integer ticketcount;
	private String machinetype;
	private String machinestatus;
	private String machinecontent;//主机配置
	private String machineowner;//使用者
	private Date buydate;//购买日期
	private String machineservice;//维修周期
	private String machineusage;//vpn 用途
	private Timestamp addtime;
	private Date usedate;//开始使用时间
	private String remark;//描述，备注
	private String ip; //ip地址
	private String ipremark; //ip备注
	private String operMember; //操作员工信息
	private Timestamp updatetime; //签到更新日期
	private String citycode;
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Machine(){
	}
	public Machine(Long cinemaid){
		this.cinemaid = cinemaid;
		this.addtime= new Timestamp(System.currentTimeMillis());
	}
	@Override
	public final Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMachinenumber() {
		return machinenumber;
	}

	public void setMachinenumber(String machinenumber) {
		this.machinenumber = machinenumber;
	}

	public String getMachinename() {
		return machinename;
	}

	public void setMachinename(String machinename) {
		this.machinename = machinename;
	}

	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}

	public Date getHfhopendate() {
		return hfhopendate;
	}

	public void setHfhopendate(Date hfhopendate) {
		this.hfhopendate = hfhopendate;
	}

	public Date getLeavedate() {
		return leavedate;
	}

	public void setLeavedate(Date leavedate) {
		this.leavedate = leavedate;
	}

	public String getLinkmethod() {
		return linkmethod;
	}

	public void setLinkmethod(String linkmethod) {
		this.linkmethod = linkmethod;
	}

	public String getTouchtype() {
		return touchtype;
	}

	public void setTouchtype(String touchtype) {
		this.touchtype = touchtype;
	}

	public Integer getTicketcount() {
		return ticketcount;
	}

	public void setTicketcount(Integer ticketcount) {
		this.ticketcount = ticketcount;
	}

	public String getMachinetype() {
		return machinetype;
	}

	public void setMachinetype(String machinetype) {
		this.machinetype = machinetype;
	}

	public String getMachinestatus() {
		return machinestatus;
	}

	public void setMachinestatus(String machinestatus) {
		this.machinestatus = machinestatus;
	}

	public String getMachinecontent() {
		return machinecontent;
	}

	public void setMachinecontent(String machinecontent) {
		this.machinecontent = machinecontent;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Date getBuydate() {
		return buydate;
	}

	public void setBuydate(Date buydate) {
		this.buydate = buydate;
	}

	public String getMachineservice() {
		return machineservice;
	}

	public void setMachineservice(String machineservice) {
		this.machineservice = machineservice;
	}

	public String getMachineusage() {
		return machineusage;
	}

	public void setMachineusage(String machineusage) {
		this.machineusage = machineusage;
	}

	public String getMachineowner() {
		return machineowner;
	}

	public void setMachineowner(String machineowner) {
		this.machineowner = machineowner;
	}

	public Date getUsedate() {
		return usedate;
	}

	public void setUsedate(Date usedate) {
		this.usedate = usedate;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIpremark() {
		return ipremark;
	}

	public void setIpremark(String ipremark) {
		this.ipremark = ipremark;
	}

	public String getOperMember() {
		return operMember;
	}

	public void setOperMember(String operMember) {
		this.operMember = operMember;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
}
