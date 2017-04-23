package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

public class GrabTicketSubject extends BaseObject {
	private static final long serialVersionUID = 7365148972996181946L;
	public static final String STATUS_OPEN = "open";
	public static final String STATUS_STOP = "stop";
	public static final String TAG_PRICE5 = "price5";			//5元抢票
	public static final String TAG_PROMOTION = "promotion";	//促销
	public static final String TAG_XPRICE = "xprice";			//其他价格抢票
	private Long id;
	private String title;
	private Long price;
	private String picheight;
	private String smalllogo;
	private String logo;
	private String status;
	private String description;
	private String content;
	private Timestamp starttime;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String tag;
	private Long movieid;
	private String citycode;
	private String seokeywords;
	private String seodescription;
	private Long relatedid;
	private String flag;
	private String marker;
	@Override
	public Serializable realId() {
		return id;
	}
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getSeokeywords() {
		return seokeywords;
	}

	public void setSeokeywords(String seokeywords) {
		this.seokeywords = seokeywords;
	}

	public String getSeodescription() {
		return seodescription;
	}

	public void setSeodescription(String seodescription) {
		this.seodescription = seodescription;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public GrabTicketSubject() {}
	
	public GrabTicketSubject(String title) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.status = STATUS_OPEN;
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicheight() {
		return picheight;
	}

	public void setPicheight(String picheight) {
		this.picheight = picheight;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Timestamp getStarttime() {
		return starttime;
	}

	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}

	public String getLogo() {
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getSmalllogo() {
		return smalllogo;
	}

	public void setSmalllogo(String smalllogo) {
		this.smalllogo = smalllogo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getPrice() {
		return price;
	}

	public void setPrice(Long price) {
		this.price = price;
	}

	public Long getMovieid() {
		return movieid;
	}

	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getMarker() {
		return marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}
	
}
