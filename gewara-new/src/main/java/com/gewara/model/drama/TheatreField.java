package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class TheatreField extends BaseObject {

	private static final long serialVersionUID = 196262388196761756L;
	private Long id;
	private Long theatreid;
	private String name;				//场地名称
	private String fieldnum;			//场地编号
	private String fieldtype;			//场地类型: GEWA、GPTBS
	private String logo;				//图片
	private String mobilelogo;			//图片
	private String description;			//描述
	private String status;				//
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public TheatreField(){}
	
	public TheatreField(Long theatreid, String fieldtype){
		this.theatreid = theatreid;
		this.fieldtype = fieldtype;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.status = Status.Y;
		this.updatetime = this.addtime;
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

	public Long getTheatreid() {
		return theatreid;
	}

	public void setTheatreid(Long theatreid) {
		this.theatreid = theatreid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFieldnum() {
		return fieldnum;
	}

	public void setFieldnum(String fieldnum) {
		this.fieldnum = fieldnum;
	}

	public String getFieldtype() {
		return fieldtype;
	}

	public void setFieldtype(String fieldtype) {
		this.fieldtype = fieldtype;
	}

	public String getLimg(){
		return logo;
	}
	
	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public boolean hasFieldtype(String type){
		if(StringUtils.isBlank(type)) return false;
		return StringUtils.equals(this.fieldtype, type);
	}

	public String getMobilelogo() {
		return mobilelogo;
	}

	public void setMobilelogo(String mobilelogo) {
		this.mobilelogo = mobilelogo;
	}
}
