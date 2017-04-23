package com.gewara.model.machine;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

//终端机同步记录，根据场馆id和机器的id来做同步
public class MachineSynch extends BaseObject{
	private static final long serialVersionUID = 1318008006814262985L;
	private Long id;
	private String tag;
	private Long placeid;
	private String macid;
	private Timestamp successtime;		//成功同步时间
	private Timestamp synchtime;		//同步时间
	public MachineSynch(){
		
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
	public Long getPlaceid() {
		return placeid;
	}
	public void setPlaceid(Long placeid) {
		this.placeid = placeid;
	}
	public String getMacid() {
		return macid;
	}
	public void setMacid(String macid) {
		this.macid = macid;
	}
	public Timestamp getSuccesstime() {
		return successtime;
	}
	public void setSuccesstime(Timestamp successtime) {
		this.successtime = successtime;
	}
	public Timestamp getSynchtime() {
		return synchtime;
	}
	public void setSynchtime(Timestamp synchtime) {
		this.synchtime = synchtime;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
}
