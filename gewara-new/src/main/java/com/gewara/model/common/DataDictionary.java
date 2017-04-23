package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class DataDictionary extends BaseObject{
	
	private static final long serialVersionUID = -3494033847886017738L;
	private Long id;
	private String objectName;
	private String propertyName;
	private String propertyRealName;
	private String dataType;
	private Integer propertyLength;
	private String required;
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public DataDictionary(){}
	
	public DataDictionary(String objectName, String propertyName, String dataType){
		this.objectName = objectName;
		this.propertyName = propertyName;
		this.dataType = dataType;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = DateUtil.getCurFullTimestamp();
		this.required = Status.N;
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

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyRealName() {
		return propertyRealName;
	}

	public void setPropertyRealName(String propertyRealName) {
		this.propertyRealName = propertyRealName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getPropertyLength() {
		return propertyLength;
	}

	public void setPropertyLength(Integer propertyLength) {
		this.propertyLength = propertyLength;
	}


	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	public Long getId(){
		return this.id;
	}
	
	public void setId(Long id){
		this.id = id;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
