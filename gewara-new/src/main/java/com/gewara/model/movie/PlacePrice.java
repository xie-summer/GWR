package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class PlacePrice extends BaseObject {
	
	private static final long serialVersionUID = -5004972318109932820L;
	private Long id;
	private String tag;
	private Long relatedid;
	private String category;
	private Long categoryid;
	private Timestamp addtime;
	private Timestamp updatetime;
	private Integer avgprice;
	private Integer minprice;
	private Integer maxprice;
	
	public PlacePrice() {}
	
	public PlacePrice(String tag, Long relatedid, String category, Long categoryid, Integer avgprice, Integer minprice, Integer maxprice){
		this.tag = tag;
		this.relatedid = relatedid;
		this.category = category;
		this.categoryid = categoryid;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.avgprice = avgprice;
		this.minprice = minprice;
		this.maxprice = maxprice;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
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

	public Integer getAvgprice() {
		return avgprice;
	}

	public void setAvgprice(Integer avgprice) {
		this.avgprice = avgprice;
	}

	public Integer getMinprice() {
		return minprice;
	}

	public void setMinprice(Integer minprice) {
		this.minprice = minprice;
	}

	public Integer getMaxprice() {
		return maxprice;
	}

	public void setMaxprice(Integer maxprice) {
		this.maxprice = maxprice;
	}
	@Override
	public Serializable realId() {
		return id;
	}
}
