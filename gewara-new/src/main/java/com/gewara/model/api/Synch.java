package com.gewara.model.api;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;


public class Synch extends BaseObject {
	public static final String TGA_DRAMA = "drama";
	public static final String TGA_CINEMA = "cinema";
	public static final String TAG_SPORT = "sport";
	public static final String TAG_GYM = "gym";
	
	private static final long serialVersionUID = -6800394265547863600L;
	private Long cinemaid;
	private String tag;
	private Timestamp successtime;		//成功同步时间
	private Timestamp synchtime;		//同步时间
	private String ticketnum;
	private Timestamp barcodesuctime;	//条形码成功同步时间
	private Timestamp barcodesyntime;	//条形码同步时间
	private Timestamp gsuctime;			//套餐成功同步时间
	private Timestamp gsyntime;			//套餐同步时间
	private String gticketnum;			
	private String ip;
	private String synchkey;
	private String monitor;
	private String newsys;
	public String getTicketnum() {
		return ticketnum;
	}
	public void setTicketnum(String ticketnum) {
		this.ticketnum = ticketnum;
	}
	public Synch(){}
	public Synch(Long cinemaid,String tag){
		this.cinemaid = cinemaid;
		this.tag = tag;
		this.monitor = Status.Y;
		this.newsys = Status.N;
	}
	public Long getCinemaid() {
		return cinemaid;
	}

	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}

	public Timestamp getSuccesstime() {
		return successtime;
	}

	public void setSuccesstime(Timestamp successtime) {
		this.successtime = successtime;
	}

	public Timestamp getSynchtime() {
		return synchtime;
	}

	public void setSynchtime(Timestamp synchtime) {
		this.synchtime = synchtime;
	}
	@Override
	public Serializable realId() {
		return cinemaid;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getSynchkey(){
		return synchkey;
	}
	public void setSynchkey(String synchkey){
		this.synchkey = synchkey;
	}
	public Timestamp getBarcodesuctime() {
		return barcodesuctime;
	}
	public void setBarcodesuctime(Timestamp barcodesuctime) {
		this.barcodesuctime = barcodesuctime;
	}
	public Timestamp getBarcodesyntime() {
		return barcodesyntime;
	}
	public void setBarcodesyntime(Timestamp barcodesyntime) {
		this.barcodesyntime = barcodesyntime;
	}
	public Timestamp getGsuctime() {
		return gsuctime;
	}
	public void setGsuctime(Timestamp gsuctime) {
		this.gsuctime = gsuctime;
	}
	public Timestamp getGsyntime() {
		return gsyntime;
	}
	public void setGsyntime(Timestamp gsyntime) {
		this.gsyntime = gsyntime;
	}
	public String getGticketnum() {
		return gticketnum;
	}
	public void setGticketnum(String gticketnum) {
		this.gticketnum = gticketnum;
	}
	public String getMonitor() {
		return monitor;
	}
	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}
	public String getNewsys() {
		return newsys;
	}
	public void setNewsys(String newsys) {
		this.newsys = newsys;
	}
	

}
