package com.gewara.json;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.gewara.constant.Status;
import com.gewara.constant.order.AddressConstant;
import com.gewara.util.DateUtil;

public class PlayItemMessage implements Serializable{
	private static final long serialVersionUID = -6910024422360011237L;
	public static final List TAG_LIST =  Arrays.asList("cinema");
	public static final String TYPE_WEB_FILMFEST = "webFilmFest";
	
	private String id;
	private String tag;
	private Long relatedid; 		//电影院ID
	private Long categoryid; 		//电影ID
	private Long memberid;
	private Date playdate;
	private Date updatetime;

	private String mobile;
	private String status; 			//N 未发送，Y 已发送
	private String flag; 			//N 没有排片，Y 有排片
	private String adddate;
	private String msg;
	/**
	 * @see AddressConstant.ADDRESS_
	 */
	private String type;			//网站web，网站电影节webFilmFest
	
	//以下字段手机电影节场次日程所用字段
	private Long mpid;				//场次ID
	private Integer wantBuyNumber;//期望购买张数
	

	public Integer getWantBuyNumber() {
		return wantBuyNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setWantBuyNumber(Integer wantBuyNumber) {
		this.wantBuyNumber = wantBuyNumber;
	}

	public Long getMpid() {
		return mpid;
	}

	public void setMpid(Long mpid) {
		this.mpid = mpid;
	}

	public PlayItemMessage() {	}

	public PlayItemMessage(String tag, Long relatedid, Long categoryid, Date playdate, String mobile, String type) {
		this.tag = tag;
		this.relatedid = relatedid;
		this.categoryid = categoryid;
		this.playdate = DateUtil.getBeginningTimeOfDay(playdate);
		this.mobile = mobile;
		this.status = Status.N;
		this.flag = Status.N;
		this.adddate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		this.updatetime = DateUtil.currentTime();
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public Long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Date getPlaydate() {
		return playdate;
	}

	public void setPlaydate(Date playdate) {
		this.playdate = playdate;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getAdddate() {
		return adddate;
	}

	public void setAdddate(String adddate) {
		this.adddate = adddate;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
}
