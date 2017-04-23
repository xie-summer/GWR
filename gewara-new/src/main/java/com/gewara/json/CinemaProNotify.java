package com.gewara.json;

import java.io.Serializable;

/**
 * 商家系统公告
 * @author gang.liu
 *
 */
public class CinemaProNotify implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -753235878383454430L;
	
	public static final String STATUS_NEW = "new";//处理标记   new 新 
	public static final String STATUS_PROCESS_Y = "process_y";//处理标记   处理成功 
	public static final String STATUS_PROCESS_N = "process_n";//处理标记   处理失败 

	private String _id;
	
	private String num; //编号
	
	private Long cinemaId;
	
	private String cinemaName;
	
	private String addTime;
	
	private String title;//标题
	
	private String content;//内容
	
	private String publishUser;//发布用户
	
	private Long checkUserId;//处理用户
	
	private String checkUserName;//处理用户
	
	private String checkTime;//处理时间
	
	private String status;//处理标记   new 新  process_y 处理成功  process_n 处理失败
	
	private String remark;//处理记录

	public CinemaProNotify(){}
	
	public CinemaProNotify(String _id,String num,Long cinemaId,String cinemaName,String addTime,
			String title,String content,String publishUser){
		this._id = _id;
		this.num = num;
		this.cinemaId = cinemaId;
		this.cinemaName = cinemaName;
		this.addTime = addTime;
		this.title = title;
		this.content = content;
		this.publishUser = publishUser;
		this.status = CinemaProNotify.STATUS_NEW;
		
	}
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public Long getCinemaId() {
		return cinemaId;
	}

	public void setCinemaId(Long cinemaId) {
		this.cinemaId = cinemaId;
	}

	public String getCinemaName() {
		return cinemaName;
	}

	public void setCinemaName(String cinemaName) {
		this.cinemaName = cinemaName;
	}

	public String getAddTime() {
		return addTime;
	}

	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPublishUser() {
		return publishUser;
	}

	public void setPublishUser(String publishUser) {
		this.publishUser = publishUser;
	}

	public Long getCheckUserId() {
		return checkUserId;
	}

	public void setCheckUserId(Long checkUserId) {
		this.checkUserId = checkUserId;
	}

	public String getCheckUserName() {
		return checkUserName;
	}

	public void setCheckUserName(String checkUserName) {
		this.checkUserName = checkUserName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
