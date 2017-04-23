package com.gewara.model.draw;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

/**
 * 获奖用户信息
 * @author Administrator
 */
public class WinnerInfo extends BaseObject {
	private static final long serialVersionUID = -7691160993786955260L;
	public static final String TAG_SYSTEM = "system";
	public static final String TAG_USER = "user";
	public final static String SUM_PRIZECOUNT = "sumprizecount";//奖品总数
	public final static String PROBABILTY_STATUS = "probabiltystatus";//判断几率是是否是第一次生成
	private Long id;
	private Long activityid;
	private Long memberid;
	private String nickname;
	private Long prizeid;
	private String mobile;
	private Timestamp addtime;
	private String status;//奖品是否已经送出Y:已送出，N：未送出
	private String tag;//默认system,管理员后台添加user
	private Long relatedid; 	//奖品关联ID，如卡ID
	private String remark;		//奖品说明，如卡号等
	private String ip;
	public WinnerInfo(){
	}
	public WinnerInfo(Long activityid, Timestamp addtime){
		this.addtime = addtime;
		this.status = Status.N;
		this.tag = TAG_SYSTEM;
		this.activityid = activityid;
	}
	public WinnerInfo(Long activityid, Long prizeid, String mobile, Timestamp addtime, String tag){
		this(activityid, addtime);
		this.prizeid = prizeid;
		this.mobile = mobile;
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getActivityid() {
		return activityid;
	}

	public void setActivityid(Long activityid) {
		this.activityid = activityid;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Long getPrizeid() {
		return prizeid;
	}

	public void setPrizeid(Long prizeid) {
		this.prizeid = prizeid;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Serializable realId() {
		return id;
	}

	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public String getEnmobile(){
		String result = mobile;
		if(StringUtils.length(result)<=4) return result;
		return "*******" + result.substring(result.length()-4);
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
}
