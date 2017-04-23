package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 *    关联表 - 任意关联多条记录
 * 	@author bob.hu
 *		@date	2011-02-24 17:43:04
 */
public class Relationship extends BaseObject {
	
	public static final String RELATIONSHIP_COMMUKING = "commuking";

	private static final long serialVersionUID = 1L;

	private Long id;
	private String category;
	private String tag;
	private Long relatedid1;
	private Long relatedid2;
	private Timestamp addtime;
	private Timestamp validtime;
	@Override
	public Serializable realId() {
		return id;
	}
	
	public Relationship() {
	}

	public Relationship(String category, String tag, Long relatedid1, Long relatedid2) {
		this.category = category;
		this.tag = tag;
		this.relatedid1 = relatedid1;
		this.relatedid2 = relatedid2;
		this.addtime = DateUtil.getCurFullTimestamp();
	}
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid1() {
		return relatedid1;
	}

	public void setRelatedid1(Long relatedid1) {
		this.relatedid1 = relatedid1;
	}

	public Long getRelatedid2() {
		return relatedid2;
	}

	public void setRelatedid2(Long relatedid2) {
		this.relatedid2 = relatedid2;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getValidtime() {
		return validtime;
	}

	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}

}
