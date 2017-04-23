package com.gewara.model.movie;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class MultiPlay extends BaseObject implements Comparable<MultiPlay> {
	private static final long serialVersionUID = -4016785855588367848L;
	private Long id;
	private Long cinemaid;
	private String playroom;
	private Date playdate;
	private String playtime;
	private Integer price;
	private String pricemark;
	private String remark;
	public MultiPlay() {
	}
	public MultiPlay(Long id, Long cinemaid) {
		this.id = id;
		this.cinemaid = cinemaid;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getPlaydate() {
		return playdate;
	}

	public void setPlaydate(Date playdate) {
		this.playdate = playdate;
	}

	public Integer getPrice() {
		return this.price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getPlayroom() {
		return playroom;
	}

	public void setPlayroom(String playroom) {
		this.playroom = playroom;
	}
	public int compareTo(MultiPlay another) {
		if(this == another) return 0;
		if(another==null) return 1;
		if(this.playdate.after(another.playdate)) return 1;
		if(this.playdate.before(another.playdate)) return -1;
		return StringUtils.isBlank(this.playroom) ? (StringUtils.isBlank(another.playroom) ? 0 : -1) : (StringUtils.isBlank(another.playroom) ? 1 : this.playroom.compareTo(another.playroom));
	}

	public String getPricemark() {
		return pricemark;
	}
	public void setPricemark(String pricemark) {
		this.pricemark = pricemark;
	}
	public String getPlaytime() {
		return playtime;
	}
	public void setPlaytime(String playtime) {
		this.playtime = playtime;
	}
	public Long getCinemaid() {
		return cinemaid;
	}
	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
}
