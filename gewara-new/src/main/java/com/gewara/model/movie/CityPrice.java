package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class CityPrice extends BaseObject{
	
	private static final long serialVersionUID = -9085841938136368895L;
	private Long id ;
	private String tag;//类型标签
	private String citycode; // 城市代码
	private Long relatedid; //movie等id
	private Timestamp addtime;
	private Timestamp updatetime;
	private Integer avgprice;
	private Integer minprice;
	private Integer maxprice;
	private Integer quantity;
	private Integer cquantity;
	
	public CityPrice(){}
	
	public CityPrice(String citycode, String tag, Long relatedid){
		this.citycode = citycode;
		this.tag = tag;
		this.relatedid = relatedid;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.quantity = 0;
		this.cquantity = 0;
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

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
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

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getCquantity() {
		return cquantity;
	}

	public void setCquantity(Integer cquantity) {
		this.cquantity = cquantity;
	}

	@Override
	public Serializable realId() {
		return id;
	}
}
