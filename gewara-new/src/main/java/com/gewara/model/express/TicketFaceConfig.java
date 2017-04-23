package com.gewara.model.express;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class TicketFaceConfig extends BaseObject {

	private static final long serialVersionUID = 8795436977539457377L;
	private String id;
	private String remark;
	private String facecontent;
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public TicketFaceConfig(){}
	
	public TicketFaceConfig(String id){
		this.id = id;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFacecontent() {
		return facecontent;
	}

	public void setFacecontent(String facecontent) {
		this.facecontent = facecontent;
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

}
