package com.gewara.json.cooperate;

import java.io.Serializable;
import java.util.List;

public class UnionPayFastCardbin implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4632967364277257197L;

	
	private String id;
	
	// 卡bin唯一标识符
	private String cardbinUkey;

	// 需求人员
	private String requirements;
	// 备注
	private String remark;
	// 卡bin
	private List<String> cardbinList;

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getCardbinUkey() {
		return cardbinUkey;
	}

	public void setCardbinUkey(String cardbinUkey) {
		this.cardbinUkey = cardbinUkey;
	}

	public String getRequirements() {
		return requirements;
	}

	public void setRequirements(String requirements) {
		this.requirements = requirements;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public List<String> getCardbinList() {
		return cardbinList;
	}

	public void setCardbinList(List<String> cardbinList) {
		this.cardbinList = cardbinList;
	}

}
