package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class HisData extends BaseObject{
	private static final long serialVersionUID = -3781233801545624754L;
	public static final String KEY_PRE_MPI = "mpi";
	private String key;
	private String jsonData;
	private Timestamp validtime;
	public HisData(){}
	public HisData(String key, String jsonData, Timestamp validtime){
		this.key = key;
		this.jsonData = jsonData;
		this.validtime = validtime;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	@Override
	public Serializable realId() {
		return key;
	}
	public String getJsonData() {
		return jsonData;
	}
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
}
