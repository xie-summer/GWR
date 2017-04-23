package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class GewaConfig extends BaseObject{
	private static final long serialVersionUID = 4476980910614491968L;
	private Long id;
	private String content;			//内容
	private String description;	//描述
	private Timestamp updatetime;	//更新时间
	@Override
	public Serializable realId() {
		return id;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

}
