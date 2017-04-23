package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;


/**
 * @author acerge(acerge@163.com)
 * @since May 24, 2009 3:51:16 PM
 */
public class DiaryComment extends BaseObject{
	private static final long serialVersionUID = 4476980910614491968L;
	private Long id;
	private Long memberid;
	private Long diaryid;
	private String body;
	private Timestamp addtime;
	private Timestamp updatetime;
	private String status;
	private String citycode;
	private String ip;//ªÿ∏¥»ÀIP
	public DiaryComment(){}
	public DiaryComment(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.status = Status.Y_NEW;
		this.memberid = memberid;
	}
	public DiaryComment(Long diaryid, Long memberid, String body){
		this(memberid);
		this.diaryid = diaryid;
		this.body = body;
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
	
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
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
	
	public Long getDiaryid() {
		return diaryid;
	}
	public void setDiaryid(Long diaryid) {
		this.diaryid = diaryid;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getIp(){
		return ip;
	}
	public void setIp(String ip){
		this.ip = ip;
	}
}
