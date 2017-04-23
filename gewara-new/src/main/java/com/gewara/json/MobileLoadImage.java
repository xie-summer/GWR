package com.gewara.json;

import java.io.Serializable;
import java.util.Date;
/**
 * 手机客户端升级
 * @author liushusong
 *
 */

public class MobileLoadImage implements Serializable{
	private static final long serialVersionUID = 5334779739394551134L;
	public static final int STATUS_Y = 1;
	public static final int STATUS_N = 0;
	private String id;
	private String imagesrc;
	private Integer status;	//1，代表显示，0，不显示
	private Date starttime;
	private Date endtime;
	private Date addTime;
	private String apptype;
	public String getApptype() {
		return apptype;
	}
	public void setApptype(String apptype) {
		this.apptype = apptype;
	}
	public MobileLoadImage(){}
	public MobileLoadImage(String imagesrc,Integer status,String apptype){
		this.addTime = new Date();
		this.imagesrc = imagesrc;
		this.status = status;
		this.apptype = apptype;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getImagesrc() {
		return imagesrc;
	}
	public void setImagesrc(String imagesrc) {
		this.imagesrc = imagesrc;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public boolean hasProgress(){
		if(starttime==null || endtime==null) return false;
		Date curtime = new Date();
		return (starttime.before(curtime) && endtime.after(curtime));
	}
	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
}
