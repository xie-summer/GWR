package com.gewara.model.drama;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class TheatreProfile extends BaseObject{
	private static final long serialVersionUID = 8293331615143909101L;
	public static final String TAKEMETHOD_A = "A";		//电子票
	public static final String TAKEMETHOD_E = "E";		//快递
	private Long id;
	private Long topicid;			//取票帖子
	private String notifymsg1;		//取票短信
	private String notifymsg2;		//提前3小时提醒短信
	private String notifymsg3;		//提前一天提醒短信
	private String notifyRemark;	//取票短信(快递)
	private String takemethod;		//取票方式
	private String takemsg;			//取票描述
	private String opentype;		//场馆开放类型：GPTBS, GEWA
	private String status;			//开放状态
	private Integer eticketHour;		//（A,E 默认电子票时间(小时))
	private Integer eticketWeekHour;	//（A,E 默认电子票时间周末(小时))
	
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public TheatreProfile(){}
	
	public TheatreProfile(Long id){
		this.id = id;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
	}
	
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTopicid() {
		return topicid;
	}
	public void setTopicid(Long topicid) {
		this.topicid = topicid;
	}
	public String getTakemethod() {
		return takemethod;
	}
	public void setTakemethod(String takemethod) {
		this.takemethod = takemethod;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNotifymsg1() {
		return notifymsg1;
	}
	public void setNotifymsg1(String notifymsg1) {
		this.notifymsg1 = notifymsg1;
	}
	public String getNotifymsg2() {
		return notifymsg2;
	}
	public void setNotifymsg2(String notifymsg2) {
		this.notifymsg2 = notifymsg2;
	}
	public String getNotifymsg3() {
		return notifymsg3;
	}
	public void setNotifymsg3(String notifymsg3) {
		this.notifymsg3 = notifymsg3;
	}

	public String getNotifyRemark() {
		return notifyRemark;
	}
	public void setNotifyRemark(String notifyRemark) {
		this.notifyRemark = notifyRemark;
	}
	public String getTakemsg() {
		return takemsg;
	}
	public String getOpentype() {
		return opentype;
	}
	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}
	public void setTakemsg(String takemsg) {
		this.takemsg = takemsg;
	}

	public Integer getEticketHour() {
		return eticketHour;
	}
	public void setEticketHour(Integer eticketHour) {
		this.eticketHour = eticketHour;
	}
	public Integer getEticketWeekHour() {
		return eticketWeekHour;
	}
	public void setEticketWeekHour(Integer eticketWeekHour) {
		this.eticketWeekHour = eticketWeekHour;
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
	public boolean hasOpentype(String htype){
		if(StringUtils.isBlank(htype)) return false;
		return StringUtils.equals(opentype, htype);
	}
}
