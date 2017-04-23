package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class QryItemResponse extends BaseObject{
	private static final long serialVersionUID = 6218002154055918900L;
	private String resid;
	private Timestamp updatetime;
	private String response;//座位，直接是1,2@1,3@
	
	public QryItemResponse(){
	}
	public QryItemResponse(String resid){
		this.updatetime = new Timestamp(System.currentTimeMillis() - DateUtil.m_hour);
		this.resid = resid;
	}
	@Override
	public final Serializable realId() {
		return resid;
	}
	public String getResid() {
		return resid;
	}
	public void setResid(String resid) {
		this.resid = resid;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public boolean isExpired(int seconds){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.after(DateUtil.addSecond(updatetime, seconds));
	}
}
