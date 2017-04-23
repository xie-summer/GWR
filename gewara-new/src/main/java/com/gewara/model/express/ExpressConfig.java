package com.gewara.model.express;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;
//配送方式
public class ExpressConfig extends BaseObject {

	private static final long serialVersionUID = -1612023130138521871L;
	private String id;
	private String name;			//配送方式名称
	private String expresstype;		//配送物流		
	private Timestamp addtime;		//添加时间
	private Timestamp updatetime;	//更新时间
	private String remark;			//描述
	
	public ExpressConfig(){}
	
	public ExpressConfig(String id, String name, String expresstype){
		this.id = id;
		this.name = name;
		this.expresstype = expresstype;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpresstype() {
		return expresstype;
	}

	public void setExpresstype(String expresstype) {
		this.expresstype = expresstype;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public Serializable realId() {
		return id;
	}

}
