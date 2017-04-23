package com.gewara.model.ticket;
import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class OpenPlayItemExt extends BaseObject {
	private static final long serialVersionUID = -4016785855588367848L;
	private Long mpid;
	private Long createuser;		//设置人
	private Timestamp createtime;	//设置时间
	private Integer actualprice;	//真实结算价：系统录入成本价与实际结算价有差异
	private Integer totalcost;		//虚拟场包场总成本，0表示不设置。
	private Integer seatnum;		//虚拟场包场座位数，0表示不设置。
	private Integer delayMin;		//延迟时间：minutes
	private Timestamp opentime;		//对外开放时间
	private Long openuser;			//开放人
	private String remark;			//备注
	private String settle;			//是否结算Y,N
	private String imprest;			//是否预付款Y,N
	public Timestamp getOpentime() {
		return opentime;
	}
	public void setOpentime(Timestamp opentime) {
		this.opentime = opentime;
	}
	public Timestamp getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}
	public Long getOpenuser() {
		return openuser;
	}
	public void setOpenuser(Long openuser) {
		this.openuser = openuser;
	}
	public Integer getActualprice() {
		return actualprice;
	}
	public void setActualprice(Integer actualprice) {
		this.actualprice = actualprice;
	}
	public OpenPlayItemExt(){}
	public OpenPlayItemExt(Long userid, OpenPlayItem opi){
		this.createuser = userid;
		this.mpid = opi.getMpid();
		this.totalcost = 0;
		this.actualprice = 0;
		this.openuser = 0L;
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.opentime = this.createtime;
		this.seatnum = 0;
		this.delayMin = 0;
		this.settle = Status.Y;
		this.imprest = Status.N;
	}
	@Override
	public Serializable realId() {
		return mpid;
	}
	public Long getMpid() {
		return mpid;
	}
	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}
	public Integer getTotalcost() {
		return totalcost;
	}
	public void setTotalcost(Integer totalcost) {
		this.totalcost = totalcost;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getSeatnum() {
		return seatnum;
	}
	public void setSeatnum(Integer seatnum) {
		this.seatnum = seatnum;
	}
	public Integer getDelayMin() {
		return delayMin;
	}
	public void setDelayMin(Integer delayMin) {
		this.delayMin = delayMin;
	}
	public Long getCreateuser() {
		return createuser;
	}
	public void setCreateuser(Long createuser) {
		this.createuser = createuser;
	}
	public String getSettle() {
		return settle;
	}
	public void setSettle(String settle) {
		this.settle = settle;
	}
	public String getImprest() {
		return imprest;
	}
	public void setImprest(String imprest) {
		this.imprest = imprest;
	}
	
	
}
