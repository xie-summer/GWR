package com.gewara.json.order;

import java.io.Serializable;

import com.gewara.util.DateUtil;

public class QryResponse implements Serializable{
	private static final long serialVersionUID = 6218002154055918900L;
	private String resid;
	private Long updatetime;
	private String response;//座位，直接是1,2@1,3@
	
	public QryResponse(){
	}
	public QryResponse(Long mpid){
		this.updatetime = System.currentTimeMillis() - DateUtil.m_hour;
		this.resid = "qryMpi" + mpid;
	}
	public String getResid() {
		return resid;
	}
	public void setResid(String resid) {
		this.resid = resid;
	}

	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public boolean isExpired(int seconds){
		Long cur = System.currentTimeMillis();
		return cur > updatetime + seconds*1000;
	}
	
	public Long getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Long updatetime) {
		this.updatetime = updatetime;
	}
}
